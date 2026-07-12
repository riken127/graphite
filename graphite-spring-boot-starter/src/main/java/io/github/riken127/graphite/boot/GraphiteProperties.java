package io.github.riken127.graphite.boot;

import io.github.riken127.graphite.neo4j.QueryOptions;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** External configuration for the Graphite Neo4j driver and default query options. */
@ConfigurationProperties("graphite")
public final class GraphiteProperties {

  /** Whether Graphite auto-configuration is enabled. */
  private boolean enabled = true;

  /** Neo4j connection URI understood by the Neo4j Java Driver. */
  private String uri = "bolt://localhost:7687";

  /** Neo4j username; leave unset together with the password to use no authentication. */
  private String username;

  /** Neo4j password. */
  private String password;

  /** Default Neo4j database for Graphite sessions. */
  private String database;

  /** Default server-side transaction timeout. */
  private Duration timeout;

  /** Default driver result fetch size. */
  private Long fetchSize;

  /** Whether the application context should fail unless Neo4j is reachable during startup. */
  private boolean verifyConnectivityOnStartup;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public Duration getTimeout() {
    return timeout;
  }

  public void setTimeout(Duration timeout) {
    this.timeout = timeout;
  }

  public Long getFetchSize() {
    return fetchSize;
  }

  public void setFetchSize(Long fetchSize) {
    this.fetchSize = fetchSize;
  }

  public boolean isVerifyConnectivityOnStartup() {
    return verifyConnectivityOnStartup;
  }

  public void setVerifyConnectivityOnStartup(boolean verifyConnectivityOnStartup) {
    this.verifyConnectivityOnStartup = verifyConnectivityOnStartup;
  }

  QueryOptions queryOptions() {
    QueryOptions.Builder builder = QueryOptions.builder();
    if (database != null && !database.isBlank()) {
      builder.database(database);
    }
    if (timeout != null) {
      builder.timeout(timeout);
    }
    if (fetchSize != null) {
      builder.fetchSize(fetchSize);
    }
    return builder.build();
  }
}
