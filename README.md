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
      <groupId>com.merkle.oss.magnolia</groupId>
      <artifactId>magnolia-powernode</artifactId>
      <version>2.3.1</version>
  </dependency>
  <!-- only if used with spring -->
  <dependency>
      <groupId>com.merkle.oss.magnolia</groupId>
      <artifactId>magnolia-powernode-spring</artifactId>
      <version>2.3.1</version>
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
      <provider>some.package.TimeZoneProvider</provider>
    </component>
  </components>
  ```

* Import Spring Configuration (only if used with spring):
  ```java
  import javax.inject.Inject;
  import com.merkle.oss.magnolia.powernode.DelegatingPowerNodeArgumentResolver;
  import com.merkle.oss.magnolia.powernode.configuration.PowerNodeConfiguration;
  import com.merkle.oss.magnolia.renderer.spring.MagnoliaHandlerMethodArgumentResolver;
      
  @Configuration
  @Import({PowerNodeConfiguration.class})
  public class SpringRendererServletConfiguration {
    private final DelegatingPowerNodeArgumentResolver.Factory delegatingPowerNodeArgumentResolverFactory;
      
    @Inject
    public SpringRendererServletConfiguration(final DelegatingPowerNodeArgumentResolver.Factory delegatingPowerNodeArgumentResolverFactory) {
        this.delegatingPowerNodeArgumentResolverFactory = delegatingPowerNodeArgumentResolverFactory;
    }
    
    // Optional if you want to be able to inject PowerNode in spring controllers
    @Override
    protected void addArgumentResolvers(final List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(delegatingPowerNodeArgumentResolverFactory.create(new MagnoliaHandlerMethodArgumentResolver()));
    }
  }
  ```
