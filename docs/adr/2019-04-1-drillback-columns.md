# Drillback Columns
Add columns to a Phoenix schema on import of a pipeline to facilitate drillback from target dataset
to source.


## Status 
Accepted


## Context
A key requirement of the FCR Engine is the ability to be able to trace a piece of data back through a transformation
to provide traceability, audit and confidence in the Regulatory Calculation being performed. This requirement is known as
"Drillback".

In order to do this a "Grammar" has been selected (i.e. MAP, JOIN, AGGREGATION) to limit the transformations done so that 
drillback can be performed.

It is important to note that a transformation takes data from one Phoenix table, transforms it and saves it in another
Phoenix table. So any drillback solution will need to be able to trace data between Phoenix tables. 

## Decision
For each transformation step we add a set of new columns on the "output table" for the transformation that will point back 
to a set of data in the "source table(s)".

The following convention will be applied for a "Drillback Pointer Column"
```sql
FCR_SYS__SOURCE_TABLE_NAME__SOURCE_TABLE_COLUMN
```

These extra columns will be added at Product Config import time.



### Maps
A Map takes one row from a source table and maps it to one row in an ouptut table. 
Therefore the implicit Phoenix Row Key is all we need to be able to drill back.

The output table will have an extra column to point back to the original row key

```sql
INPUT
+------------+--------------+----------------+
| ROW_KEY    | COL1         | COL2           |
+------------+--------------+----------------|
|      1     |      10      |      ...       |
|      2     |      13      |      ...       |
|      3     |      65      |      ...       |
+------------+--------------+----------------+
OUTPUT
+------------+--------------+----------------+----------------------------+
| ROW_KEY    | COL1_TIMES_2 | COL2           | FCR_SYS__INPUT_1__ROW_KEY  |
+------------+--------------+----------------|----------------------------|
|      4     |     20       |      ...       |      1                     |
|      5     |     26       |      ...       |      2                     |
|      6     |     130      |      ...       |      3                     |
+------------+--------------+----------------+----------------------------+
```

### Aggregations
An Aggregation takes many rows from one source table and maps it to one row in a target table.

An aggregation is defined by a grouping column and given the grouping column value we can easily perform a drill back.
Therefore for each grouping column an extra column will be added to the Phoenix table

The FCR System drillback column follows the format defined above and in this case when grouping by "COL1", the extra
column will be `FCR_SYS__INPUT__COL1`. See example below

```sql
INPUT
+------------+--------------+----------------+
| ROW_KEY    | COL1         | COL2           |
+------------+--------------+----------------|
|      1     |      A       |       1        |
|      2     |      A       |       2        |
|      3     |      C       |       3        |
+------------+--------------+----------------+
OUTPUT
+------------+--------------+----------------------+
| ROW_KEY    | SUM(COL2)    | FCR_SYS__INPUT__COL1 |
+------------+--------------+----------------------|
|     201    |     3        |         A            |
|     201    |     3        |         C            |
+------------+--------------+----------------------+
```

### Joins
For a join transformation we take a row from 2 or more tables and combine it into a row in a final output table.

We can also not rely on the join column being unique in any of the source tables, therefore in order to have a unique reliable
drill back we will need to store the Row Key from each source table in the output table.

This will following the same Drillback Column Pointer defined before, consider the join below
```sql
SELECT NAME, DEPARTMENT FROM EMPLOYEE JOIN DEPARTMENT ON EMPLOYEE.E_ID = DEPARTMENT.E_ID;
``` 

Then we will need two new columns on the output table
```sql
FCR_SYS__EMPLOYEE__ROW_KEY
FCR_SYS__DEPARTMENT__ROW_KEY
```

Which will be populated like below,
```sql
EMPLOYEE
+------------+--------------+----------------+
| ROW_KEY    |     E_ID     | NAME           |
+------------+--------------+----------------|
|      1     |     8239     | "Matt"         |
|      2     |     8240     | "Dave"         |
+------------+--------------+----------------+
DEPARTMENT
+------------+--------------+----------------+
| ROW_KEY    |     E_ID     | Department     |
+------------+--------------+----------------|
|     101    |     8239     | "Tech"         |
|     201    |     8240     | "Sales"        |
+------------+--------------+----------------+
RIGHT
+------------+--------------+----------------+----------------------------+------------------------------+
| ROW_KEY    | NAME         | Department     | FCR_SYS__EMPLOYEE__ROW_KEY | FCR_SYS__DEPARTMENT__ROW_KEY |
+------------+--------------+----------------+----------------------------|------------------------------|
|    2001    | "Matt"       | "Tech"         | 1                          | 101                          |
|    2002    | "Dave"       | "Sales"        | 2                          | 102                          |
+------------+--------------+----------------+----------------------------+------------------------------+
```



### Window
A Window function is a combination of an Aggregation and a Map, one row in the input maps to one row in the output but 
the calculation has access to a "Group", this is the same group as a group in an aggregation.

This window function allows us to calculate things like "Rank in a group", "Distance from mean" and other things. 
An aggregation is defined by a grouping column and given the grouping column value we can easily perform a drill back, 
in the same way a Window function defines a "Partition Column" to define a group, so as long as the partition column is 
preserved we can drillback to the group in the input.


The FCR System drillback column follows the format defined and in this case when partition by "COL1", the extra
column will be `FCR_SYS__INPUT__COL1`. See example below

```sql
INPUT
+------------+--------------+----------------+
| ROW_KEY    | COL1         | COL2           |
+------------+--------------+----------------|
|      1     |      A       |       115      |
|      2     |      A       |       220      |
|      3     |      C       |       300      |
+------------+--------------+----------------+
OUTPUT
+------------+--------------+----------------------+
| ROW_KEY    | RANK         | FCR_SYS__INPUT__COL1 |
+------------+--------------+----------------------|
|     201    |     1        |         A            |
|     202    |     2        |         A            |
|     203    |     1        |         C            |
+------------+--------------+----------------------+
```



### Union
A Union is effectively two Map functions, the two Maps take data from different dataset and save the "union" of the datasets
in the output table.

Therefore in order to provide drillback, we need to know which rows came from which input dataset.
The source of the rows is exclusive, one row can only come from one input dataset.
Therefore if we have two inputs 

```sql
INPUT_1
+------------+--------------+----------------+
| ROW_KEY    | COL1         | COL2           |
+------------+--------------+----------------|
|      1     |      10      |      ...       |
|      2     |      13      |      ...       |
|      3     |      65      |      ...       |
+------------+--------------+----------------+
INPUT_2
+------------+--------------+----------------+
| ROW_KEY    | COL1         | COL2           |
+------------+--------------+----------------|
|      4     |      11      |      ...       |
|      5     |      43      |      ...       |
|      6     |      92      |      ...       |
+------------+--------------+----------------+
OUTPUT
+------------+--------------+----------------+----------------------------+----------------------------+ 
| ROW_KEY    | COL1_TIMES_2 | COL2           | FCR_SYS__INPUT_1__ROW_KEY  | FCR_SYS__INPUT_2__ROW_KEY  | 
+------------+--------------+----------------|----------------------------|----------------------------| 
|      7     |     20       |      ...       |      1                     |      NULL                  | 
|      8     |     26       |      ...       |      2                     |      NULL                  | 
|      9     |     130      |      ...       |      3                     |      NULL                  | 
|     10     |      11      |      ...       |      NULL                  |      4                     | 
|     11     |      43      |      ...       |      NULL                  |      5                     | 
|     12     |      92      |      ...       |      NULL                  |      6                     | 
+------------+--------------+----------------+----------------------------+----------------------------+ 
```


## Other Notes
Please note that another solution would be to have inspected the transformation at design time but the decision was made 
to limit a Product Developers knowledge of how drill back works and given them as much freedom as possible when 
creating Schemas and Transformations.

This will often result in some data being duplicated if data in the system columns is already present in the output
schema, however this duplication will be minimal and any solution to minimised this would be too convoluted for our needs.

## Consequences
- Allowing drill back functionality
- Need to maintain a consistent standard for DrillBack Column Pointers
- Some data may be duplicated 
