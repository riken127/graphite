package io.github.riken127.graphite.boot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.riken127.graphite.cypher.renderer.CypherRenderer;
import io.github.riken127.graphite.metadata.GraphEntityFactory;
import io.github.riken127.graphite.metadata.GraphValueConverters;
import io.github.riken127.graphite.metadata.NodeMetadataRegistry;
import io.github.riken127.graphite.metadata.RecordEntityMapper;
import io.github.riken127.graphite.neo4j.GraphiteClient;
import io.github.riken127.graphite.neo4j.GraphiteSchemaManager;
import io.github.riken127.graphite.neo4j.QueryObserver;
import io.github.riken127.graphite.neo4j.QueryOptions;
import io.github.riken127.graphite.spring.GraphiteSpringTemplate;
import io.github.riken127.graphite.spring.GraphiteTransactionManager;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class GraphiteAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(GraphiteAutoConfiguration.class));

  @Test
  void configuresExecutionMappingAndTransactionBeans() {
    Driver driver = mock(Driver.class);

    contextRunner
        .withBean(Driver.class, () -> driver)
        .withPropertyValues(
            "graphite.database=analytics", "graphite.timeout=3s", "graphite.fetch-size=250")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).hasSingleBean(GraphiteClient.class);
              assertThat(context).hasSingleBean(CypherRenderer.class);
              assertThat(context).hasSingleBean(GraphiteSpringTemplate.class);
              assertThat(context).hasSingleBean(GraphiteTransactionManager.class);
              assertThat(context).hasSingleBean(NodeMetadataRegistry.class);
              assertThat(context).hasSingleBean(RecordEntityMapper.class);
              assertThat(context).hasSingleBean(GraphEntityFactory.class);
              assertThat(context).hasSingleBean(QueryObserver.class);
              assertThat(context).hasSingleBean(GraphiteSchemaManager.class);
              assertThat(context).hasSingleBean(GraphValueConverters.class);
              assertThat(context).getBean(Driver.class).isSameAs(driver);

              QueryOptions options = context.getBean(QueryOptions.class);
              assertThat(options.database()).contains("analytics");
              assertThat(options.timeout()).contains(Duration.ofSeconds(3));
              assertThat(options.fetchSize()).hasValue(250);
            });
  }

  @Test
  void createsDriverWhenApplicationDoesNotProvideOne() {
    contextRunner
        .withPropertyValues("graphite.uri=bolt://localhost:7687")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).hasSingleBean(Driver.class);
            });
  }

  @Test
  void canDisableGraphiteAutoConfiguration() {
    contextRunner
        .withPropertyValues("graphite.enabled=false")
        .run(context -> assertThat(context).doesNotHaveBean(GraphiteClient.class));
  }
}
