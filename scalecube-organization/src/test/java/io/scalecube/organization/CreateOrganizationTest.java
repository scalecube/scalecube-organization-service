package io.scalecube.organization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.NameAlreadyInUseException;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

public class CreateOrganizationTest extends Base {



  /**
   * #MPA-7229 (#1)
   *
   * <p>Scenario: Successful creation of the Organization
   *
   * <p>Given the user "A" have got a valid "token" issued by relevant authority
   *
   * <p>When user "A" requested to create the organization with specified non-existent "name" and
   * some "email"
   *
   * <p>Then new organization should be created and stored in DB with relevant "organizationId"
   * assigned for potential members
   *
   * <p>And the user "A" should become the "owner" among the potential members of the relevant
   * organization
   */
  @Test
  public void createOrganization() {
    String id = createRandomOrganization();
    StepVerifier.create(service.getOrganization(new GetOrganizationRequest(token, id)))
    .expectSubscription()
    .assertNext((r) -> assertThat(r.id(), is(id)))
    .verifyComplete();

    deleteOrganization(id);

  }

  @Test
  public void createOrganizationWithEmptyNameShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.createOrganization(new CreateOrganizationRequest("", token)),
        IllegalArgumentException.class);
  }

  /**
   * #MPA-7229 (#1.3) - SHOULD WE REMOVE SUCH VALIDATION AND ENABLE TO ADD ANY CHARS? Scenario: Fail
   * to create the Organization with the name which contain else symbols apart of allowed chars
   * Given the user "A" have got a valid "token" issued by relevant authority When the user "A"
   * requested to create the organization with specified "name" which contains "+" and some "email"
   * Then user "A" should get an error message: "name can only contain characters in range A-Z, a-z,
   * 0-9 as well as underscore, period, dash & percent"
   */
  @ParameterizedTest
  @MethodSource("invalidOrgNames")
  public void createOrganizationWithIlligalNameShouldFailWithIllegalArgumentException(
      String invalidString) {
    StepVerifier.create(
            service.createOrganization(new CreateOrganizationRequest(invalidString, token)))
        .expectErrorMessage(
            "Organization name can only contain characters in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent.")
        .verify();
  }

  /**
   * #MPA-7229 (#1.3) - SHOULD WE REMOVE SUCH VALIDATION AND ENABLE TO ADD ANY CHARS? Scenario: Fail
   * to create the Organization with the name which contain else symbols apart of allowed chars
   * Given the user "A" have got a valid "token" issued by relevant authority When the user "A"
   * requested to create the organization with specified "name" which contains "+" and some "email"
   * Then user "A" should get an error message: "name can only contain characters in range A-Z, a-z,
   * 0-9 as well as underscore, period, dash & percent"
   */
  @ParameterizedTest
  @MethodSource("validOrgNames")
  public void createOrganizationWithValidNameShouldNotFailWithIllegalArgumentException(
      String invalidString) {
    StepVerifier.create(
            service.createOrganization(new CreateOrganizationRequest(invalidString, token)))
        .assertNext(Objects::nonNull)
        .verifyComplete();
  }

  @Test
  public void createOrganizationWithNullNameShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.createOrganization(new CreateOrganizationRequest(null, token)),
        NullPointerException.class);
  }

  /**
   * #MPA-7229 (#1.1)
   *
   * <p>Scenario: Fail to create the Organization if the token is invalid (expired)
   *
   * <p>Given a user have got the invalid either expired "token"
   *
   * <p>When this user requested to create the organization with some "name" and "email"
   *
   * <p>Then this user should get an error message: "Token verification failed"
   */
  @Test
  public void createOrganizationShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
        .createOrganization(new CreateOrganizationRequest("myTestOrg5", token)),
        InvalidAuthenticationToken.class);
  }
  
/**
 *   #MPA-7229 (#1.2)
*  Scenario: Fail to create the Organization with the name which already exists (duplicate)
*    Given the user "A" have got a valid "token" issued by relevant authority
*    And the organization "organizationId" with specified "name" and "email" already created and owned by user "B"
*    When the user "A" requested to create the organization with the existent user's "B" organization "name" and some or the same "email"
*    Then user "A" should get an error message: "Organization name: 'org "B" name' already in use"
 */
  @Test
  public void createOrganizationWithNameAlreadyInUseShouldFail() {
    assertMonoCompletesWithError(
        service.createOrganization(new CreateOrganizationRequest(organisation.name(), token)),
        NameAlreadyInUseException.class);
  }
  
  @Test
  public void createOrganizationNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.createOrganization(new CreateOrganizationRequest("myTestOrg5", null)),
        NullPointerException.class);
  }

  @Test
  public void createOrganizationNullInnerTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.createOrganization(new CreateOrganizationRequest("myTestOrg5", new Token(null))),
        NullPointerException.class);
  }

  @Test
  public void createOrganizationEmptyTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.createOrganization(new CreateOrganizationRequest("myTestOrg5", new Token(""))),
        IllegalArgumentException.class);
  }

  public static Stream<Arguments> invalidOrgNames() {
    return IntStream.concat(
            IntStream.concat(IntStream.range('!', '%'), IntStream.range('&', ')')),
            IntStream.range('[', '_'))
        .mapToObj(i -> new StringBuilder("org").append((char) i).toString())
        .map(Arguments::of);
  }

  public static Stream<Arguments> validOrgNames() {
    return IntStream.of('_', '%', '.', '-', '%')
        .mapToObj(i -> new StringBuilder("org").append((char) i).toString())
        .map(Arguments::of);
  }
}
