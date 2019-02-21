package io.scalecube.organization.it;

import static io.scalecube.organization.it.TestProfiles.USER_A;
import static io.scalecube.organization.it.TestProfiles.USER_B;
import static io.scalecube.organization.it.TestProfiles.USER_C;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.KickoutOrganizationMemberRequest;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.OrganizationNotFoundException;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.inmem.InMemoryPublicKeyProvider;
import io.scalecube.tokens.InvalidTokenException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class GetOrganizationMembersTest extends BaseTest {

  @Test
  @DisplayName(
      "#92 Successful get all the members list from relevant Organization by the \"owner\"")
  void getMembersByOwner() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.getUserId(), Role.Member.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_C.getUserId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.getOrganizationMembers(
                new GetOrganizationMembersRequest(organizationA.id(), tokenA)))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertTrue(
                  members.contains(USER_A.getUserId()), "member A is not found in organization");
              assertTrue(
                  members.contains(USER_B.getUserId()), "member B is not found in organization");
              assertTrue(
                  members.contains(USER_C.getUserId()), "member C is not found in organization");
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "#93 Successful get all the members list from relevant Organization by the the \"admin\"")
  void getMembersByAdmin() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.getUserId(), Role.Admin.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.getOrganizationMembers(
                new GetOrganizationMembersRequest(organizationA.id(), tokenB)))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertTrue(
                  members.contains(USER_A.getUserId()), "member A is not found in organization");
              assertTrue(
                  members.contains(USER_B.getUserId()), "member B is not found in organization");
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "#94 Fail to get the list of all the members from the relevant Organization by the existing member with similar role")
  void getMembersByMember() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.getUserId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.getOrganizationMembers(
                new GetOrganizationMembersRequest(organizationA.id(), tokenB)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(AccessPermissionException.class, e.getClass());
              assertEquals(
                  String.format(
                      "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                      USER_B.getUserId(), USER_B.getName(), organizationA.name()),
                  e.getMessage());
            })
        .verify();
  }

  @Test
  @DisplayName(
      "#95 Fail to get the list of all the members from the relevant Organization upon some of the existing (requester) managers was removed from the relevant organization")
  void getMembersByRemovedAdmin() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.getUserId(), Role.Admin.name()))
        .block(TIMEOUT);

    organizationService
        .kickoutMember(
            new KickoutOrganizationMemberRequest(organizationA.id(), tokenA, USER_B.getUserId()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.getOrganizationMembers(
                new GetOrganizationMembersRequest(organizationA.id(), tokenB)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(AccessPermissionException.class, e.getClass());
              assertEquals(
                  String.format(
                      "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                      USER_B.getUserId(), USER_B.getName(), organizationA.name()),
                  e.getMessage());
            })
        .verify();
  }

  @Test
  @DisplayName("#96 Fail to get members from non-existent Organization")
  void getMembersFromNonExistentOrganization() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);

    String organizationId = "ORG-organization-1";

    StepVerifier.create(
            organizationService.getOrganizationMembers(
                new GetOrganizationMembersRequest(organizationId, tokenA)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(OrganizationNotFoundException.class, e.getClass());
              assertEquals(
                  String.format("Organization [id=%s] not found", organizationId), e.getMessage());
            })
        .verify();
  }

  @Test
  @DisplayName(
      "#97 Fail to invite the user to specific Organization if the token is invalid (expired)")
  void getMembersUsingExpiredToken() {
    Token tokenA = InMemoryPublicKeyProvider.expiredToken(USER_A);

    StepVerifier.create(
            organizationService.getOrganizationMembers(
                new GetOrganizationMembersRequest("ORG-organization-1", tokenA)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidTokenException.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }
}
