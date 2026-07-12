package io.github.riken127.graphite.neo4j;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;

class DockerRequirementTest {

  @Test
  void dockerIsAvailableWhenIntegrationExecutionIsRequired() {
    if (Boolean.getBoolean("graphite.requireDocker")) {
      assertTrue(
          DockerClientFactory.instance().isDockerAvailable(),
          "graphite.requireDocker=true but Docker is unavailable");
    }
  }
}
