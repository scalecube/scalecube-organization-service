package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.KickoutOrganizationMemberRequest;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class KickoutMemberTest extends Base {

  @Test
  public void kickoutMember() {
    addMemberToOrganization(organisationId, service, testProfile5);
    Duration duration = StepVerifier
        .create(service.kickoutMember(
            new KickoutOrganizationMemberRequest(organisationId, token, testProfile5.getUserId())))
        .expectSubscription()
        .assertNext(x -> StepVerifier
            .create(service.getOrganizationMembers(
                new GetOrganizationMembersRequest(organisationId, token)))
            .expectSubscription()
            .assertNext(r -> {
              List<String> members = Arrays.stream(r.members())
                  .map(OrganizationMember::id).collect(Collectors.toList());
              assertThat(members, not(hasItem(testProfile5.getUserId())));
            })
            .verifyComplete()).verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void kickoutMemberInvalidUserShouldFailWithInvalidAuthenticationToken() {
    Duration duration = expectError(createService(invalidProfile).kickoutMember(
        new KickoutOrganizationMemberRequest(organisationId, token, testProfile5.getUserId())),
        InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMemberEmptyOrgIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest("", token, testProfile5.getUserId())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMemberNullOrgIdShouldFailWithNullPointerException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest(null, token, testProfile5.getUserId())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMemberEmptyTokenShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest(organisationId, new Token(""),
            testProfile5.getUserId())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMemberNullTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest(organisationId, null, testProfile5.getUserId())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMemberNullInnerTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest(organisationId, new Token(null),
            testProfile5.getUserId())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMemberEmptyUserIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest(organisationId, token,
            "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMemberNullUserIdShouldFailWithNullPointerException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest(organisationId, token, null)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMemberOrgNotExistsShouldFailWithEntityNotFoundException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest("orgNotExists",
            token, testProfile5.getUserId())),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMemberNotOrgOwnerShouldFailWithAccessPermissionException() {
    Duration duration = expectError(createService(testProfile5).kickoutMember(
        new KickoutOrganizationMemberRequest(organisationId,
            token, testProfile5.getUserId())),
        AccessPermissionException.class);
    assertNotNull(duration);
  }}
