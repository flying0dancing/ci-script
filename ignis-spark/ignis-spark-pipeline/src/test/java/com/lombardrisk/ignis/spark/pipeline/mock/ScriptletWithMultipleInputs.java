package com.lombardrisk.ignis.spark.pipeline.mock;

import com.lombardrisk.ignis.spark.script.api.Scriptlet;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.HashMap;
import java.util.Map;

public class ScriptletWithMultipleInputs implements Scriptlet {

    public static final StructType EMPLOYEES = new StructType(new StructField[]{
            new StructField("ID", DataTypes.IntegerType, false, Metadata.empty()),
            new StructField("Name", DataTypes.StringType, false, Metadata.empty()),
            new StructField("DepartmentId", DataTypes.IntegerType, false, Metadata.empty()),
    });

    public static final StructType DEPARTMENTS = new StructType(new StructField[]{
            new StructField("ID", DataTypes.IntegerType, false, Metadata.empty()),
            new StructField("Department", DataTypes.StringType, false, Metadata.empty()),
    });

    public static final StructType SALARIES = new StructType(new StructField[]{
            new StructField("EmployeeId", DataTypes.IntegerType, false, Metadata.empty()),
            new StructField("Salary", DataTypes.IntegerType, false, Metadata.empty()),
    });

    public static final StructType OUTPUT = new StructType(new StructField[]{
            new StructField("Name", DataTypes.StringType, false, Metadata.empty()),
            new StructField("Department", DataTypes.StringType, false, Metadata.empty()),
            new StructField("Salary", DataTypes.IntegerType, false, Metadata.empty()),
    });

    @Override
    public Dataset<Row> run(final Map<String, Dataset<Row>> inputs) {
        Dataset<Row> employees = inputs.get("Employees");
        Dataset<Row> departments = inputs.get("Departments");
        Dataset<Row> salaries = inputs.get("Salaries");

        return salaries.join(employees, salaries.col("EmployeeId").equalTo(employees.col("ID")), "outer")
                .join(departments, employees.col("DepartmentId").equalTo(departments.col("ID")), "outer")
                .select(employees.col("Name"), departments.col("Department"), salaries.col("Salary"));
    }

    @Override
    public Map<String, StructType> inputTraits() {
        Map<String, StructType> inputs = new HashMap<>();
        inputs.put("Employees", EMPLOYEES);
        inputs.put("Departments", DEPARTMENTS);
        inputs.put("Salaries", SALARIES);

        return inputs;
    }

    @Override
    public StructType outputTrait() {
        return OUTPUT;
    }
}
