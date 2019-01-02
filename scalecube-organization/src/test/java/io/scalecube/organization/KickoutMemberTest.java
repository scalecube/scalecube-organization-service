package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.KickoutOrganizationMemberRequest;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class KickoutMemberTest extends Base {

  @Test
  public void kickoutMember() {
    addMemberToOrganization(organisationId, service, testProfile5);
    StepVerifier.create(
            service.kickoutMember(
                new KickoutOrganizationMemberRequest(
                    organisationId, token, testProfile5.getUserId())))
        .expectSubscription()
        .assertNext(
            x ->
                StepVerifier.create(
                        service.getOrganizationMembers(
                            new GetOrganizationMembersRequest(organisationId, token)))
                    .expectSubscription()
                    .assertNext(
                        r -> {
                          List<String> members =
                              Arrays.stream(r.members())
                                  .map(OrganizationMember::id)
                                  .collect(Collectors.toList());
                          assertThat(members, not(hasItem(testProfile5.getUserId())));
                        })
                    .verifyComplete())
        .verifyComplete();
  }

  @Test
  public void kickoutMemberInvalidUserShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .kickoutMember(
                new KickoutOrganizationMemberRequest(
                    organisationId, token, testProfile5.getUserId())),
        InvalidAuthenticationToken.class);
  }

  @Test
  public void kickoutMemberEmptyOrgIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.kickoutMember(
            new KickoutOrganizationMemberRequest("", token, testProfile5.getUserId())),
        IllegalArgumentException.class);
  }

  @Test
  public void kickoutMemberNullOrgIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.kickoutMember(
            new KickoutOrganizationMemberRequest(null, token, testProfile5.getUserId())),
        NullPointerException.class);
  }

  @Test
  public void kickoutMemberEmptyTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.kickoutMember(
            new KickoutOrganizationMemberRequest(
                organisationId, new Token(""), testProfile5.getUserId())),
        IllegalArgumentException.class);
  }

  @Test
  public void kickoutMemberNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.kickoutMember(
            new KickoutOrganizationMemberRequest(organisationId, null, testProfile5.getUserId())),
        NullPointerException.class);
  }

  @Test
  public void kickoutMemberNullInnerTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.kickoutMember(
            new KickoutOrganizationMemberRequest(
                organisationId, new Token(null), testProfile5.getUserId())),
        NullPointerException.class);
  }

  @Test
  public void kickoutMemberEmptyUserIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.kickoutMember(new KickoutOrganizationMemberRequest(organisationId, token, "")),
        IllegalArgumentException.class);
  }

  @Test
  public void kickoutMemberNullUserIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.kickoutMember(new KickoutOrganizationMemberRequest(organisationId, token, null)),
        NullPointerException.class);
  }

  @Test
  public void kickoutMemberOrgNotExistsShouldFailWithEntityNotFoundException() {
    assertMonoCompletesWithError(
        service.kickoutMember(
            new KickoutOrganizationMemberRequest("orgNotExists", token, testProfile5.getUserId())),
        EntityNotFoundException.class);
  }

  @Test
  public void kickoutMemberNotOrgOwnerShouldFailWithAccessPermissionException() {
    assertMonoCompletesWithError(
        createService(testProfile5)
            .kickoutMember(
                new KickoutOrganizationMemberRequest(
                    organisationId, token, testProfile5.getUserId())),
        AccessPermissionException.class);
  }
}
