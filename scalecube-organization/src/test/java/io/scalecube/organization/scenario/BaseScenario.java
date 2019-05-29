package io.scalecube.organization.scenario;

import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import reactor.test.StepVerifier;

abstract class BaseScenario {

  static final Duration TIMEOUT = Duration.ofSeconds(5);

  @BeforeAll
  static void beforeAll() {
    StepVerifier.setDefaultTimeout(TIMEOUT);
  }
}
