package io.scalecube.organization.it;

import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import reactor.test.StepVerifier;

abstract class BaseTest {

  static final Duration TIMEOUT = Duration.ofSeconds(1);

  @BeforeAll
  static void beforeAll() {
    StepVerifier.setDefaultTimeout(TIMEOUT);
  }
}
