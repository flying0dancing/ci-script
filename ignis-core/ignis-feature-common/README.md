# Ignis Feature Common
This module contains code common to the implementation of ["Feature Flags"](https://martinfowler.com/articles/feature-toggles.html) in the FCR Engine applications.

The library chosen to implement feature flags is [Togglz](https://www.togglz.org), this library has easy integration with Spring Boot
and provides a ui interface to turn of flags.

## Features
The feature flags are defined in the following enum [`IgnisFeature`](src/main/java/com/lombardrisk/ignis/feature/IgnisFeature.java).

The status of this flag is controlled by the `FeatureManager` which is created as a bean by Spring and can be injected or statically looked up.
(Note: For spark jobs the preference is to inject the Feature Manager as the static lookup doesnt work)

### Aspect
This module also contains a [`FeatureAspect`](src/main/java/com/lombardrisk/ignis/feature/FeatureAspect.java) which can be used to 
add feature flags using the decorator patter. I.e.

```java
public class AService {
    
    @FeatureEnabled(feature = IgnisFeature.MY_FEATURE)
    public void logicForNewFeature() {
        System.out.println("Aren't feature flags cool!");
    }
}
```

If this flag is disabled on the feature manager the application will throw a [`FeatureNotActiveException`](src/main/java/com/lombardrisk/ignis/feature/FeatureNotActiveException.java)
when this method is called which can be handled by a global execption handler