package io.scalecube.organization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class DeleteOrganizationTest extends Base {

  @Test
  public void deleteOrganizationInvalidTokenShouldFailWithInvalidAuthenticationToken() {

    assertMonoCompletesWithError(
        createService(invalidProfile)
            .deleteOrganization(new DeleteOrganizationRequest(token, organizationId)),
        InvalidAuthenticationToken.class);
  }

  @Test
  public void deleteOrganizationWithIdNotExistsShouldFailWithEntityNotFoundException() {

    assertMonoCompletesWithError(
        createService(testProfile)
            .deleteOrganization(new DeleteOrganizationRequest(token, "orgIdNotExists")),
        EntityNotFoundException.class);
  }

  @Test
  public void deleteOrganizationWithEmptyIdShouldFailWithIllegalArgumentException() {

    assertMonoCompletesWithError(
        createService(testProfile).deleteOrganization(new DeleteOrganizationRequest(token, "")),
        IllegalArgumentException.class);
  }

  @Test
  public void deleteOrganizationWithNullIdShouldFailWithNullPointerException() {

    assertMonoCompletesWithError(
        createService(testProfile).deleteOrganization(new DeleteOrganizationRequest(token, null)),
        NullPointerException.class);
  }

  @Test
  public void deleteOrganizationWithNullTokenShouldFailWithNullPointerException() {

    assertMonoCompletesWithError(
        createService(testProfile)
            .deleteOrganization(new DeleteOrganizationRequest(null, organizationId)),
        NullPointerException.class);
  }

  @Test
  public void deleteOrganizationWithEmptyTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        createService(testProfile)
            .deleteOrganization(new DeleteOrganizationRequest(new Token(""), organizationId)),
        IllegalArgumentException.class);
  }
  
/**
 *   #MPA-7229 (#4)
*  Scenario: Successful delete of specific Organization
*    Given the user "A" have got a valid "token" issued by relevant authority
*    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
*    When the user "A" requested to delete own organization "organizationId"
*   Then user "A" should receive the successful response object: "deleted":true,"organizationId":"org "A" organizationId"
 */
  @Test
  public void deleteOrganization() {
    String id = createRandomOrganization();
    StepVerifier.create(service.deleteOrganization(new DeleteOrganizationRequest(token, id)))
        .expectSubscription()
        .assertNext((r) -> assertThat(r.deleted(), is(true)))
        .expectComplete()
        .verify();
    deleteOrganization(id);
  }
}
