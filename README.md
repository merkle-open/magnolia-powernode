# Magnolia Powernode
The PowerNode module provides a special wrapper for JCR nodes with
extended functionality. The PowerNode acts as dynamic proxy with delegates
all method calls either to the original JCR node implementation or to a 
custom implementation of `NodeUtil` and `PropertyUtil` methods.

Advantages:
* Fully compatible to JCR nodes: PowerNode implements the `javax.jcr.Node` interface
* Implements `NodeUtil` and `PropertyUtil` functionality as methods, no need to use the util classes anymore
* No checked RepositoryExceptions: all checked exceptions either handled or wrapped in runtime exceptions
* Stream compatible: since there are no checked exceptions, PowerNodes can be easily used in streams
* No `null` return values: PowerNode methods return either empty object representation or Java 8 optionals
* Blossom compatible: PowerNodes can be injected into Blossom templates

## Requirements
* Java 11
* Spring >=5
* Magnolia >= 6.0

## Magnolia Version Mapping
* PowerNode 1.0.0-SNAPSHOT → <= Magnolia 6.2.x

## Installation

* Add Maven dependency:
```
<dependency>
    <groupId>com.namics.oss.magnolia</groupId>
    <artifactId>magnolia-powernode</artifactId>
    <version>1.0.4</version>
</dependency>
```
* Import Spring Configuration:
```
[...]
@Configuration
@Import({PowerNodeConfiguration.class})
public class BlossomServletConfiguration {
	[...]
}
```

* If needed, activate the PowerNode argument resolver:
  * in the `BlossomServletConfiguration` add the argument resolver in the `HandlerAdapter` Bean as follows:
  ```java
  @Bean
  public HandlerAdapter handlerAdapter(
      BlossomHandlerMethodArgumentResolver blossomHandlerMethodArgumentResolver,
      PowerNodeArgumentResolver powerNodeArgumentResolver) {
  
      BlossomRequestMappingHandlerAdapter handlerAdapter = new BlossomRequestMappingHandlerAdapter();
      handlerAdapter.setRedirectPatterns("website:*");
  
      List<HandlerMethodArgumentResolver> customResolvers = new ArrayList<>();
      customResolvers.add(powerNodeArgumentResolver);
      customResolvers.add(blossomHandlerMethodArgumentResolver);
      handlerAdapter.setCustomArgumentResolvers(customResolvers);
  
      ConfigurableWebBindingInitializer bindingInitializer = new ConfigurableWebBindingInitializer();
      bindingInitializer.setValidator(validatorFactory());
      handlerAdapter.setWebBindingInitializer(bindingInitializer);

      return handlerAdapter;
  }
  ```
  ## Know Issues
  
  * Time value comparison
  Storing time values in nodes requires the usage of java.util.Calendar objects, which can't handle nano seconds.
  If a value comparison is needed using a more accurate format like java.time.LocalDateTime, make sure to only use time values in milliseconds (e.g. LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)).
  Otherwise, it would case failures e.g. on Windows based environments.