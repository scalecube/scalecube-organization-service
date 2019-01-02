package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.GetMembershipRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Token;
import io.scalecube.security.Profile;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class UserMembershipTest extends Base {

  @Test
  public void getUserOrganizationsMembership() {
    String orgId1 = createOrganization("testOrg1").id();
    String orgId2 = createOrganization("testOrg2").id();
    addMemberToOrganization(organisationId, service, testProfile);
    addMemberToOrganization(orgId1, service, testProfile);
    addMemberToOrganization(orgId2, service, testProfile);

    assertNotNull(
        StepVerifier.create(service.getUserOrganizationsMembership(new GetMembershipRequest(token)))
            .assertNext(
                r -> {
                  assertThat("expected 3 memberships", r.organizations().length, is(3));
                  Supplier<Stream<String>> ids =
                      () -> Arrays.stream(r.organizations()).map(OrganizationInfo::id);
                  assertThat(
                      orgId1 + " is expected", ids.get().anyMatch(i -> Objects.equals(orgId1, i)));
                  assertThat(
                      orgId2 + " is expected", ids.get().anyMatch(i -> Objects.equals(orgId2, i)));
                  assertThat(
                      organisationId + " is expected",
                      ids.get().anyMatch(i -> Objects.equals(organisationId, i)));
                })
            .verifyComplete());

    deleteOrganization(orgId1);
    deleteOrganization(orgId2);
  }

  @Test
  public void getUserMembershipInvalidUserShouldFailWithInvalidAuthenticationToken() {
    Duration duration =
        assertMonoCompletesWithError(
            createService(invalidProfile)
                .getUserOrganizationsMembership(new GetMembershipRequest(token)),
            InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void getUserMembershipNullTokenShouldFailWithNullPointerException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.getUserOrganizationsMembership(new GetMembershipRequest(null)),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getUserMembershipNullInnerTokenShouldFailWithNullPointerException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.getUserOrganizationsMembership(new GetMembershipRequest(new Token(null))),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getUserMembershipEmptyInnerTokenShouldFailWithIllegalArgumentException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.getUserOrganizationsMembership(new GetMembershipRequest(new Token(""))),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembership() {
    addMemberToOrganization(organisationId, service, testProfile);
    assertGetOrganizationsMembership(organisationId, testProfile);
  }

  private void assertGetOrganizationsMembership(String organisationId, Profile profile) {
    List<String> members =
        orgMembersRepository
            .getMembers(getOrganizationFromRepository(organisationId))
            .stream()
            .map(OrganizationMember::id)
            .collect(Collectors.toList());
    assertThat(members, hasItem(profile.getUserId()));
  }
}
