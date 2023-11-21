# Magnolia Powernode
The PowerNode module provides a special wrapper for JCR nodes with
extended functionality.

Advantages:
* Fully compatible to JCR nodes: PowerNode implements the `javax.jcr.Node` interface
* No checked RepositoryExceptions: all checked exceptions either handled or wrapped in runtime exceptions
* Stream compatible: since there are no checked exceptions, PowerNodes can be easily used in streams
* Blossom compatible: PowerNodes can be injected into Blossom templates

## Installation

* Add Maven dependency:
```xml
<dependency>
    <groupId>com.namics.oss.magnolia</groupId>
    <artifactId>magnolia-powernode</artifactId>
    <version>2.0.4</version>
</dependency>
```

* Import Spring Configuration:
```java
@Configuration
@Import({PowerNodeConfiguration.class})
public class BlossomServletConfiguration {
	//...
}
```

* If needed, add the PowerNodeArgumentResolver in the `BlossomServletConfiguration` as follows:
```java
@Override
protected void addArgumentResolvers(final List<HandlerMethodArgumentResolver> argumentResolvers) {
      argumentResolvers.add(powerNodeArgumentResolver);
      argumentResolvers.add(new BlossomHandlerMethodArgumentResolver());
}
```