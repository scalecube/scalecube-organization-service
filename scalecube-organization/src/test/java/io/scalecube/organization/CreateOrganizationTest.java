package io.scalecube.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.NameAlreadyInUseException;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

class CreateOrganizationTest extends Base {

  @Test
  void createOrganization() {
    String id = createRandomOrganization();
    StepVerifier.create(service.getOrganization(new GetOrganizationRequest(token, id)))
        .expectSubscription()
        .assertNext(response -> assertEquals(response.id(), id))
        .verifyComplete();
    deleteOrganization(id);
  }

  @Test
  void createOrganizationWithEmptyNameShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.createOrganization(new CreateOrganizationRequest("", "email", token)),
        IllegalArgumentException.class);
  }

  @ParameterizedTest
  @MethodSource("invalidOrgNames")
  void createOrganizationWithIlligalNameShouldFailWithIllegalArgumentException(
      String invalidString) {
    StepVerifier.create(
            service.createOrganization(
                new CreateOrganizationRequest(invalidString, "email", token)))
        .expectErrorMessage(
            "Organization name can only contain characters in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent.")
        .verify();
  }

  @ParameterizedTest
  @MethodSource("validOrgNames")
  void createOrganizationWithValidNameShouldNotFailWithIllegalArgumentException(
      String invalidString) {
    StepVerifier.create(
            service.createOrganization(
                new CreateOrganizationRequest(invalidString, "email", token)))
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
  }

  @Test
  void createOrganizationWithNullNameShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.createOrganization(new CreateOrganizationRequest(null, "email", token)),
        NullPointerException.class);
  }

  @Test
  void createOrganizationShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .createOrganization(new CreateOrganizationRequest("myTestOrg5", "email", token)),
        InvalidAuthenticationToken.class);
  }

  @Test
  void createOrganizationWithNameAlreadyInUseShouldFail() {
    assertMonoCompletesWithError(
        service.createOrganization(
            new CreateOrganizationRequest(organisation.name(), "email", token)),
        NameAlreadyInUseException.class);
  }

  @Test
  void createOrganizationNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.createOrganization(new CreateOrganizationRequest("myTestOrg5", "email", null)),
        NullPointerException.class);
  }

  @Test
  void createOrganizationNullInnerTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.createOrganization(
            new CreateOrganizationRequest("myTestOrg5", "email", new Token(null))),
        NullPointerException.class);
  }

  @Test
  void createOrganizationEmptyTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.createOrganization(
            new CreateOrganizationRequest("myTestOrg5", "email", new Token(""))),
        IllegalArgumentException.class);
  }

  private static Stream<Arguments> invalidOrgNames() {
    return IntStream.concat(
            IntStream.concat(IntStream.range('!', '%'), IntStream.range('&', ')')),
            IntStream.range('[', '_'))
        .mapToObj(i -> "org" + (char) i)
        .map(Arguments::of);
  }

  private static Stream<Arguments> validOrgNames() {
    return IntStream.of('_', '%', '.', '-', '%').mapToObj(i -> "org" + (char) i).map(Arguments::of);
  }
}
