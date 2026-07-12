package io.github.riken127.graphite.boot;

import io.github.riken127.graphite.cypher.renderer.CypherRenderer;
import io.github.riken127.graphite.metadata.GraphEntityFactory;
import io.github.riken127.graphite.metadata.GraphValueConverters;
import io.github.riken127.graphite.metadata.NodeMetadataRegistry;
import io.github.riken127.graphite.metadata.RecordEntityMapper;
import io.github.riken127.graphite.metadata.ReflectionNodeMetadataRegistry;
import io.github.riken127.graphite.neo4j.GraphiteClient;
import io.github.riken127.graphite.neo4j.GraphiteSchemaManager;
import io.github.riken127.graphite.neo4j.QueryObserver;
import io.github.riken127.graphite.neo4j.QueryOptions;
import io.github.riken127.graphite.spring.GraphiteSpringTemplate;
import io.github.riken127.graphite.spring.GraphiteTransactionManager;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Spring Boot auto-configuration for the Graphite execution stack. */
@AutoConfiguration
@ConditionalOnClass({Driver.class, GraphiteClient.class, GraphiteSpringTemplate.class})
@ConditionalOnProperty(prefix = "graphite", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(GraphiteProperties.class)
public class GraphiteAutoConfiguration {

  @Bean(destroyMethod = "close")
  @ConditionalOnMissingBean(Driver.class)
  Driver graphiteDriver(GraphiteProperties properties) {
    return GraphDatabase.driver(requireUri(properties), authentication(properties));
  }

  @Bean
  @ConditionalOnMissingBean
  CypherRenderer graphiteCypherRenderer() {
    return new CypherRenderer();
  }

  @Bean
  @ConditionalOnMissingBean
  QueryOptions graphiteQueryOptions(GraphiteProperties properties) {
    return properties.queryOptions();
  }

  @Bean
  @ConditionalOnMissingBean
  GraphiteClient graphiteClient(
      Driver driver,
      CypherRenderer renderer,
      QueryOptions queryOptions,
      QueryObserver queryObserver,
      GraphiteProperties properties) {
    GraphiteClient client = new GraphiteClient(driver, renderer, queryOptions, queryObserver);
    if (properties.isVerifyConnectivityOnStartup()) {
      client.verifyConnectivity();
    }
    return client;
  }

  @Bean
  @ConditionalOnMissingBean(NodeMetadataRegistry.class)
  ReflectionNodeMetadataRegistry graphiteNodeMetadataRegistry() {
    return new ReflectionNodeMetadataRegistry();
  }

  @Bean
  @ConditionalOnMissingBean
  GraphEntityFactory graphiteEntityFactory(NodeMetadataRegistry metadataRegistry) {
    return new GraphEntityFactory(metadataRegistry);
  }

  @Bean
  @ConditionalOnMissingBean
  QueryObserver graphiteQueryObserver() {
    return QueryObserver.noop();
  }

  @Bean
  @ConditionalOnMissingBean
  GraphiteSchemaManager graphiteSchemaManager(GraphiteClient client, QueryOptions queryOptions) {
    return new GraphiteSchemaManager(client, queryOptions);
  }

  @Bean
  @ConditionalOnMissingBean
  RecordEntityMapper graphiteRecordEntityMapper(
      NodeMetadataRegistry metadataRegistry, GraphValueConverters converters) {
    return new RecordEntityMapper(metadataRegistry, converters);
  }

  @Bean
  @ConditionalOnMissingBean
  GraphValueConverters graphiteValueConverters() {
    return GraphValueConverters.empty();
  }

  @Bean
  @ConditionalOnMissingBean
  GraphiteSpringTemplate graphiteTemplate(GraphiteClient client) {
    return new GraphiteSpringTemplate(client);
  }

  @Bean
  @ConditionalOnMissingBean
  GraphiteTransactionManager graphiteTransactionManager(
      GraphiteClient client, QueryOptions queryOptions) {
    return new GraphiteTransactionManager(client, queryOptions);
  }

  private static String requireUri(GraphiteProperties properties) {
    String uri = properties.getUri();
    if (uri == null || uri.isBlank()) {
      throw new IllegalArgumentException("graphite.uri must not be blank");
    }
    return uri.trim();
  }

  private static AuthToken authentication(GraphiteProperties properties) {
    String username = properties.getUsername();
    String password = properties.getPassword();
    if (username == null || username.isBlank()) {
      if (password != null && !password.isBlank()) {
        throw new IllegalArgumentException(
            "graphite.username is required when graphite.password is configured");
      }
      return AuthTokens.none();
    }
    if (password == null) {
      throw new IllegalArgumentException(
          "graphite.password is required when graphite.username is configured");
    }
    return AuthTokens.basic(username.trim(), password);
  }
}
