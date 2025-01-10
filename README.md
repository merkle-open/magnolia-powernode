# Magnolia Powernode
The PowerNode module provides a special wrapper for JCR nodes with
extended functionality.

Advantages:
* Fully compatible to JCR nodes: PowerNode implements the `javax.jcr.Node` interface
* No checked RepositoryExceptions: all checked exceptions either handled or wrapped in runtime exceptions
* Stream compatible: since there are no checked exceptions, PowerNodes can be easily used in streams
* Blossom compatible: PowerNodes can be injected into Blossom templates

## Setup

* Add Maven dependencies:
  ```xml
  <dependency>
      <groupId>com.namics.oss.magnolia</groupId>
      <artifactId>magnolia-powernode</artifactId>
      <version>2.1.3</version>
  </dependency>
  <!-- only if used with blossom -->
  <dependency>
      <groupId>com.namics.oss.magnolia</groupId>
      <artifactId>magnolia-powernode-spring</artifactId>
      <version>2.1.3</version>
  </dependency>
  ```

* Add binding for ZoneId provider
  ```java
  import info.magnolia.context.MgnlContext;
  import info.magnolia.context.WebContext;
  import info.magnolia.ui.framework.util.TimezoneUtil;
  
  import javax.inject.Inject;
  import javax.inject.Provider;
  import java.time.ZoneId;
  import java.util.Optional;
  
  public class TimeZoneProvider implements Provider<ZoneId> {
  
    @Override
    public ZoneId get() {
        return Optional
                .ofNullable(MgnlContext.getWebContextOrNull())
                .map(WebContext::getUser)
                .map(TimezoneUtil::getUserZoneId)
                .orElseGet(ZoneId::systemDefault);
    }
  }
  ```
  ```xml
  <components>
    <id>main</id>
    <component>
      <type>java.time.ZoneId</type>
      <provider>com.namics.engagement.web.configuration.TimeZoneProvider</provider>
    </component>
  </components>
  ```

* Import Spring Configuration (only if used with blossom):
  ```java
  @Configuration
  @Import({PowerNodeConfiguration.class})
  public class BlossomServletConfiguration {
      //...
  }
  ```

  * If needed, add the DelegatingPowerNodeArgumentResolver in the `BlossomServletConfiguration` as follows:
    ```java
    @Override
    protected void addArgumentResolvers(final List<HandlerMethodArgumentResolver> argumentResolvers) {
          argumentResolvers.add(delegatingPowerNodeArgumentResolverFactory.create(new BlossomHandlerMethodArgumentResolver()));
    }
    ```