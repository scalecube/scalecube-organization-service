package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.KickoutOrganizationMemberRequest;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.OrganizationNotFoundException;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class KickoutMemberTest extends Base {

  @Test
  void kickoutMember() {
    addMemberToOrganization(organizationId, testProfile5);
    StepVerifier.create(
            service.kickoutMember(
                new KickoutOrganizationMemberRequest(
                    organizationId, token, testProfile5.getUserId())))
        .expectSubscription()
        .assertNext(
            x ->
                StepVerifier.create(
                        service.getOrganizationMembers(
                            new GetOrganizationMembersRequest(organizationId, token)))
                    .expectSubscription()
                    .assertNext(
                        response -> {
                          List<String> members =
                              Arrays.stream(response.members())
                                  .map(OrganizationMember::id)
                                  .collect(Collectors.toList());
                          assertThat(members, not(hasItem(testProfile5.getUserId())));
                        })
                    .verifyComplete())
        .verifyComplete();
  }

  @Test
  void kickoutMemberInvalidUserShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .kickoutMember(
                new KickoutOrganizationMemberRequest(
                    organizationId, token, testProfile5.getUserId())),
        InvalidAuthenticationToken.class);
  }

  @Test
  void kickoutMemberEmptyOrgIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.kickoutMember(
            new KickoutOrganizationMemberRequest("", token, testProfile5.getUserId())),
        IllegalArgumentException.class);
  }

  @Test
  void kickoutMemberNullOrgIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.kickoutMember(
            new KickoutOrganizationMemberRequest(null, token, testProfile5.getUserId())),
        NullPointerException.class);
  }

  @Test
  void kickoutMemberEmptyTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.kickoutMember(
            new KickoutOrganizationMemberRequest(
                organizationId, new Token(""), testProfile5.getUserId())),
        IllegalArgumentException.class);
  }

  @Test
  void kickoutMemberNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.kickoutMember(
            new KickoutOrganizationMemberRequest(organizationId, null, testProfile5.getUserId())),
        NullPointerException.class);
  }

  @Test
  void kickoutMemberNullInnerTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.kickoutMember(
            new KickoutOrganizationMemberRequest(
                organizationId, new Token(null), testProfile5.getUserId())),
        NullPointerException.class);
  }

  @Test
  void kickoutMemberEmptyUserIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.kickoutMember(new KickoutOrganizationMemberRequest(organizationId, token, "")),
        IllegalArgumentException.class);
  }

  @Test
  void kickoutMemberNullUserIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.kickoutMember(new KickoutOrganizationMemberRequest(organizationId, token, null)),
        NullPointerException.class);
  }

  @Test
  void kickoutMemberOrgNotExistsShouldFailWithOrganizationNotFoundException() {
    assertMonoCompletesWithError(
        service.kickoutMember(
            new KickoutOrganizationMemberRequest("orgNotExists", token, testProfile5.getUserId())),
        OrganizationNotFoundException.class);
  }

  @Test
  void kickoutMemberNotOrgOwnerShouldFailWithAccessPermissionException() {
    assertMonoCompletesWithError(
        createService(testProfile5)
            .kickoutMember(
                new KickoutOrganizationMemberRequest(
                    organizationId, token, testProfile5.getUserId())),
        AccessPermissionException.class);
  }
}
