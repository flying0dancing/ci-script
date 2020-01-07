# Upgrade core dependencies
- Spring boot 1.4 -> 2
- Jackson 2.6.5 -> 2.9.5
- Mockito 1 -> 2
- Spring 4 -> 5

## Status 
Accepted

## Context
Spring boot and Spring is more than 2 years out of date with significant changes to spring data api
and servlet improvements

Old versions of Jackson have security flaws

Mockito upgrade was needed to handle new spring test module

#### Approach
Upgraded dependencies and made the relevant changes to ignis modules for functionality to remain intact



### Decision
No significant issues seen when upgrading, upgrade completed.

### Consequences
The following issues were seen and overcome

##### Version clash javax.validation
Spring boot 2 uses the 2.0.0 version of javax.validation.api. When starting Spring Boot with autoconfiguration
the version of `javax.validation` on the hadoop cluster is one version behind that which spring boot needs. Therefore the 
spark jobs did not start up.

To avoid this the auto configuration for `javax.validation` and `org.togglz` was disabled as it was not needed.

##### Spring batch migration
As the way Spring batch serializes data changed from the previous version any attempt at marshalling context data
from old jobs failed. To solve this a migration was added `V20011__remove_old_step_context.sql` to remove any step 
context information.

###### Risks
The only possible risk of this migration would be upgrading a clients installation.

If the server is brought down before a job is completed there may be issues with spring batch restarting the job.
This risk has minimal impact and should be communicated in release notes.

##### Spring Data JPA Repository API
All finds return optionals in new version. Appropriate code was changed to handle optionals

##### Mockito
Changes in mockito that required code change
- `anyString()` no longer matches null values
- `anyVararg()` is deprecated
- Unnecessary stubbings cause test failures
    - Overcome by either using MockitoJunitRunner.Silent or fixing issue

Note:

Some remaining Mockito warnings remain and will be tackled in tech debt backlog

#### Notes
Future issues may be seen with Hadoop's/Spark's dependency on older version of `javax.validation.api`. If we want to use
auto configuration in spark jobs we may have to upgrade spark or hadoop cluster.