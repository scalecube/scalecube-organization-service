package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class OrganizationMembersTest extends Base {

  @Test
  public void getOrganizationMembers() {
    addMemberToOrganization(organisationId, service, testProfile4);
    addMemberToOrganization(organisationId, service, testProfile5);

    Duration duration =
        StepVerifier.create(
                service.getOrganizationMembers(
                    new GetOrganizationMembersRequest(organisationId, token)))
            .expectSubscription()
            .assertNext(
                (r) -> {
                  Supplier<Stream<OrganizationMember>> members = () -> Arrays.stream(r.members());
                  assertThat(r.members().length, is(3));
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
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersEmptyOrgIdShouldFailWithIllegalArgumentException() {
    Duration duration =
        expectError(
            service.getOrganizationMembers(new GetOrganizationMembersRequest("", token)),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersNullOrgIdShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            service.getOrganizationMembers(new GetOrganizationMembersRequest(null, token)),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersEmptyTokenShouldFailWithIllegalArgumentException() {
    Duration duration =
        expectError(
            service.getOrganizationMembers(
                new GetOrganizationMembersRequest(organisationId, new Token(""))),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersNullTokenShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            service.getOrganizationMembers(new GetOrganizationMembersRequest(organisationId, null)),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersNullInnerTokenShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            service.getOrganizationMembers(
                new GetOrganizationMembersRequest(organisationId, new Token(null))),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersShouldFailWithInvalidAuthenticationToken() {
    Duration duration =
        expectError(
            createService(invalidProfile)
                .getOrganizationMembers(new GetOrganizationMembersRequest(organisationId, token)),
            InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersShouldFailWithEntityNotFoundException() {
    Duration duration =
        expectError(
            service.getOrganizationMembers(
                new GetOrganizationMembersRequest("orgNotExists", token)),
            EntityNotFoundException.class);
    assertNotNull(duration);
  }
}
