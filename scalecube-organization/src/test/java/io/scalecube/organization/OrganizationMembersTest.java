package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.OrganizationNotFoundException;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class OrganizationMembersTest extends Base {

  @Test
  void getOrganizationMembers() {
    addMemberToOrganization(organizationId, testProfile4);
    addMemberToOrganization(organizationId, testProfile5);

    StepVerifier.create(
            service.getOrganizationMembers(
                new GetOrganizationMembersRequest(organizationId, token)))
        .expectSubscription()
        .assertNext(
            response -> {
              Supplier<Stream<OrganizationMember>> members =
                  () -> Arrays.stream(response.members());
              assertThat(response.members().length, is(3));
              long membersCount =
                  members
                      .get()
                      .filter((m) -> Objects.equals(m.role(), Role.Member.toString()))
                      .count();
              assertThat(membersCount, is(2L));
              List<String> ids =
                  members.get().map(OrganizationMember::id).collect(Collectors.toList());
              assertThat(ids, hasItem(testProfile4.getUserId()));
              assertThat(ids, hasItem(testProfile5.getUserId()));
            })
        .verifyComplete();
  }

  @Test
  void getOrganizationMembersEmptyOrgIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest("", token)),
        IllegalArgumentException.class);
  }

  @Test
  void getOrganizationMembersNullOrgIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(null, token)),
        NullPointerException.class);
  }

  @Test
  void getOrganizationMembersEmptyTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.getOrganizationMembers(
            new GetOrganizationMembersRequest(organizationId, new Token(""))),
        IllegalArgumentException.class);
  }

  @Test
  void getOrganizationMembersNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(organizationId, null)),
        NullPointerException.class);
  }

  @Test
  void getOrganizationMembersNullInnerTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.getOrganizationMembers(
            new GetOrganizationMembersRequest(organizationId, new Token(null))),
        NullPointerException.class);
  }

  @Test
  void getOrganizationMembersShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .getOrganizationMembers(new GetOrganizationMembersRequest(organizationId, token)),
        InvalidAuthenticationToken.class);
  }

  @Test
  void getOrganizationMembersShouldFailWithOrganizationNotFoundException() {
    assertMonoCompletesWithError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest("orgNotExists", token)),
        OrganizationNotFoundException.class);
  }
}
