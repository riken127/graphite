package example;

import io.github.riken127.graphite.boot.GraphiteAutoConfiguration;
import io.github.riken127.graphite.boot.GraphiteProperties;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/** Proves that a consumer receives the complete starter compile classpath. */
@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration(GraphiteAutoConfiguration.class)
public class SpringBootConsumer {

  private final GraphiteProperties properties;

  public SpringBootConsumer(GraphiteProperties properties) {
    this.properties = properties;
  }

  public GraphiteProperties properties() {
    return properties;
  }
}
