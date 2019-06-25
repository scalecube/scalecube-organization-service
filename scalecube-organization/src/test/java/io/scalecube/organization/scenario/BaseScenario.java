package io.scalecube.organization.scenario;

import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import reactor.test.StepVerifier;

public abstract class BaseScenario {

  public static final int API_KEY_TTL_IN_SECONDS = 3;

  static final Duration TIMEOUT = Duration.ofSeconds(10);

  @BeforeAll
  static void beforeAll() {
    StepVerifier.setDefaultTimeout(TIMEOUT);
  }
}
