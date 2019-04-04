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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class UserMembershipTest extends Base {

  @Test
  void getUserOrganizationsMembership() {
    String orgId1 = createOrganization("testOrg1").id();
    String orgId2 = createOrganization("testOrg2").id();
    addMemberToOrganization(organizationId, testProfile);
    addMemberToOrganization(orgId1, testProfile);
    addMemberToOrganization(orgId2, testProfile);

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
                      organizationId + " is expected",
                      ids.get().anyMatch(i -> Objects.equals(organizationId, i)));
                })
            .verifyComplete());

    deleteOrganization(orgId1);
    deleteOrganization(orgId2);
  }

  @Test
  void getUserMembershipInvalidUserShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .getUserOrganizationsMembership(new GetMembershipRequest(token)),
        InvalidAuthenticationToken.class);
  }

  @Test
  void getUserMembershipNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.getUserOrganizationsMembership(new GetMembershipRequest(null)),
        NullPointerException.class);
  }

  @Test
  void getUserMembershipNullInnerTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.getUserOrganizationsMembership(new GetMembershipRequest(new Token(null))),
        NullPointerException.class);
  }

  @Test
  void getUserMembershipEmptyInnerTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.getUserOrganizationsMembership(new GetMembershipRequest(new Token(""))),
        IllegalArgumentException.class);
  }

  @Test
  void getOrganizationMembership() {
    addMemberToOrganization(organizationId, testProfile);
    assertGetOrganizationsMembership(organizationId, testProfile);
  }

  private void assertGetOrganizationsMembership(String organisationId, Profile profile) {
    List<String> members =
        getOrganizationFromRepository(organisationId).members().stream()
            .map(OrganizationMember::id)
            .collect(Collectors.toList());
    assertThat(members, hasItem(profile.getUserId()));
  }
}
