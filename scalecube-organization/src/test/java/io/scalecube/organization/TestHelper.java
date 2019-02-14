package io.scalecube.organization;

import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.Random;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class TestHelper {

  public static final Duration TIMEOUT = Duration.ofSeconds(5);
  public static final KeyPairGenerator KEY_PAIR_GENERATOR;
  public static final Random RANDOM = new Random();

  static {
    try {
      KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance("RSA");
      KEY_PAIR_GENERATOR.initialize(2048);
    } catch (Exception e) {
      throw Exceptions.propagate(e);
    }
  }

  public static String randomString(int targetStringLength) {
    return RANDOM
        .ints(targetStringLength, 'a', 'z')
        .collect(
            StringBuilder::new, (builder, i) -> builder.append((char) i), StringBuilder::append)
        .toString();
  }

  public static <T> void assertMonoCompletesWithError(
      Mono<T> mono, Class<? extends Throwable> exception) {
    StepVerifier.create(mono).expectSubscription().expectError(exception).verify();
  }
}
