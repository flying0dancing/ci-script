## Ignis Spark 

This module contains all the code for the [Spark](https://spark.apache.org/) jobs. The jobs themselves are assembled as jars
and submitted to the Yarn cluster by ignis-server.
The runnable jobs are runnable spring boot applications.

This module contains four sub modules
- ignis-spark-api
- ignis-spark-core
- ignis-spark-validation
- ignis-spark-staging

### Ignis Spark Api
This module contains DTO and value objects to be used by all the spark jobs. Ignis server
depends on `ignis-spark-api` and it allows sharing code for submitting without any heavy dependencies.

### Ignis Spark Core
This module contains the core code for both the staging and pipeline jobs to use.
It contains the main class for the Spring Boot Application `com.lombardrisk.ignis.spark.Application` 
which scans for components within ignis-spark-core as well as in the modules that depend on it.

I.e. 
In ignis-spark-validation because this module depends on ignis-spark-core the `@Component` or `@Configuration`
classes will be detected by the spring boot app.

### Ignis Spark Staging
This module is responsible for staging datasets and depends on ignis-spark-core

### Ignis Spark Validation
This module runs validation jobs, these jobs validate data that has already been staged.
