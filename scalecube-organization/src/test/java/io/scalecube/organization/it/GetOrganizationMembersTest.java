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
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.fixtures.InMemoryOrganizationServiceFixture;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.inmem.InMemoryPublicKeyProvider;
import io.scalecube.organization.tokens.InvalidTokenException;
import io.scalecube.test.fixtures.Fixtures;
import io.scalecube.test.fixtures.WithFixture;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.test.StepVerifier;

@ExtendWith(Fixtures.class)
@WithFixture(value = InMemoryOrganizationServiceFixture.class)
class GetOrganizationMembersTest extends BaseTest {

  @TestTemplate
  @DisplayName(
      "#92 Successful get all the members list from relevant Organization by the \"owner\"")
  void getMembersByOwner(OrganizationService organizationService) {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), USER_A.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.userId(), Role.Member.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_C.userId(), Role.Member.name()))
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
                  members.contains(USER_A.userId()), "member A is not found in organization");
              assertTrue(
                  members.contains(USER_B.userId()), "member B is not found in organization");
              assertTrue(
                  members.contains(USER_C.userId()), "member C is not found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#93 Successful get all the members list from relevant Organization by the the \"admin\"")
  void getMembersByAdmin(OrganizationService organizationService) {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), USER_A.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.userId(), Role.Admin.name()))
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
                  members.contains(USER_A.userId()), "member A is not found in organization");
              assertTrue(
                  members.contains(USER_B.userId()), "member B is not found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#94 Fail to get the list of all the members from the relevant Organization by the existing member with similar role")
  void getMembersByMember(OrganizationService organizationService) {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), USER_A.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.userId(), Role.Member.name()))
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
                      USER_B.userId(), USER_B.name(), organizationA.name()),
                  e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#95 Fail to get the list of all the members from the relevant Organization upon some of the existing (requester) managers was removed from the relevant organization")
  void getMembersByRemovedAdmin(OrganizationService organizationService) {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), USER_A.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    organizationService
        .kickoutMember(
            new KickoutOrganizationMemberRequest(organizationA.id(), tokenA, USER_B.userId()))
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
                      USER_B.userId(), USER_B.name(), organizationA.name()),
                  e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName("#96 Fail to get members from non-existent Organization")
  void getMembersFromNonExistentOrganization(OrganizationService organizationService) {
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

  @TestTemplate
  @DisplayName(
      "#97 Fail to invite the user to specific Organization if the token is invalid (expired)")
  void getMembersUsingExpiredToken(OrganizationService organizationService) {
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
