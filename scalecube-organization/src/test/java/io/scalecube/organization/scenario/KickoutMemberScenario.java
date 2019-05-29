package io.scalecube.organization.scenario;

import static io.scalecube.organization.scenario.TestProfiles.USER_A;
import static io.scalecube.organization.scenario.TestProfiles.USER_B;
import static io.scalecube.organization.scenario.TestProfiles.USER_C;
import static io.scalecube.organization.scenario.TestProfiles.USER_D;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.KickoutOrganizationMemberRequest;
import io.scalecube.account.api.NotAnOrganizationMemberException;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.fixtures.InMemoryPublicKeyProvider;
import io.scalecube.organization.tokens.InvalidTokenException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import reactor.test.StepVerifier;

public class KickoutMemberScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#59 Successful kick-out (remove) of specific \"member\" from a relevant Organization")
  void kickoutMemberByOwner(OrganizationService organizationService) {
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

    StepVerifier.create(
            organizationService
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenA, USER_B.userId()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(USER_B.userId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#60 Successful kick-out (remove) the \"owner\" and \"member\" from relevant Organization by another owner")
  void kickoutMemberAndOwnerByOwner(OrganizationService organizationService) {
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
                tokenA, organizationA.id(), USER_B.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_C.userId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenB, USER_A.userId()))
                .then(
                    organizationService.kickoutMember(
                        new KickoutOrganizationMemberRequest(
                            organizationA.id(), tokenB, USER_C.userId())))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(USER_A.userId()), "member is found in organization");
              assertFalse(members.contains(USER_C.userId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#61 Successful kick-out (remove) of the \"admin\" and \"member\" from relevant Organization by another \"admin\"")
  void kickoutByAdmin(OrganizationService organizationService) {
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
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_C.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_D.userId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenB, USER_C.userId()))
                .then(
                    organizationService.kickoutMember(
                        new KickoutOrganizationMemberRequest(
                            organizationA.id(), tokenB, USER_D.userId())))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(USER_C.userId()), "member is found in organization");
              assertFalse(members.contains(USER_D.userId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#62 Successful kick-out (remove) one of the \"admin\" from relevant Organization by \"owner\"")
  void kickoutAdminByOwner(OrganizationService organizationService) {
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
                tokenA, organizationA.id(), USER_B.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_C.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenB, USER_C.userId()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(USER_C.userId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#63 Successful kick-out (remove) yourself as the \"owner\" from relevant Organization upon at least one another owner is persisted")
  void kickoutYourselfByOwner(OrganizationService organizationService) {
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
                tokenA, organizationA.id(), USER_B.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenA, USER_A.userId()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(USER_A.userId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#64 Successful kick-out (remove) yourself as the \"admin\" from relevant Organization")
  void kickoutYourselfByAdmin(OrganizationService organizationService) {
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
            organizationService
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenB, USER_B.userId()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(USER_B.userId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#67 Fail to kick-out (remove) yourself as the single \"owner\" from relevant Organization")
  void kickoutSingleOwnerByOwner(OrganizationService organizationService) {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), USER_A.email(), tokenA))
            .block(TIMEOUT);

    StepVerifier.create(
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(
                    organizationA.id(), tokenA, USER_A.userId())))
        .expectErrorSatisfies(
            e -> {
              assertEquals(IllegalStateException.class, e.getClass());
              assertEquals(
                  String.format(
                      "At least one Owner should be persisted in the organization: '%s'",
                      organizationA.id()),
                  e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#68 Fail to kick-out (remove) the single owner from relevant Organization by the \"admin\"")
  void kickoutSingleOwnerByAdmin(OrganizationService organizationService) {
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
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(
                    organizationA.id(), tokenB, USER_A.userId())))
        .expectErrorSatisfies(
            e -> {
              assertEquals(AccessPermissionException.class, e.getClass());
              assertEquals(
                  String.format(
                      "user: '%s', name: '%s', role: 'Admin' cannot kickout user: '%s' in role 'Owner' of organization: '%s'",
                      USER_B.userId(),
                      USER_B.name(),
                      USER_A.userId(),
                      organizationA.name()),
                  e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#69 Fail to kick-out (remove) specific member from relevant Organization upon the existing member (requester) got \"member\" role permission level")
  void kickoutMemberByMember(OrganizationService organizationService) {
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

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_C.userId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(
                    organizationA.id(), tokenB, USER_C.userId())))
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
      "#70 Fail to remove a specific \"member\" from relevant Organization upon some of the existing (requester) managers was removed from the relevant organization")
  void kickoutByMember(OrganizationService organizationService) {
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
                tokenA, organizationA.id(), USER_B.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    organizationService
        .kickoutMember(
            new KickoutOrganizationMemberRequest(organizationA.id(), tokenB, USER_A.userId()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(
                    organizationA.id(), tokenA, USER_B.userId())))
        .expectErrorSatisfies(
            e -> {
              assertEquals(AccessPermissionException.class, e.getClass());
              assertEquals(
                  String.format(
                      "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                      USER_A.userId(), USER_A.name(), organizationA.name()),
                  e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#71 Fail to remove the user from specific Organization if the token is invalid (expired)")
  void kickoutUsingExpiredToken(OrganizationService organizationService) {
    Token tokenA = InMemoryPublicKeyProvider.expiredToken(USER_A);

    StepVerifier.create(
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(
                    "ORG-organization-1", tokenA, USER_B.userId())))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidTokenException.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName("Fail to kick non-existent user member")
  void kickoutNonExistentMember(OrganizationService organizationService) {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), USER_A.email(), tokenA))
            .block(TIMEOUT);

    StepVerifier.create(
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(organizationA.id(), tokenA, USER_B.userId())))
        .expectErrorSatisfies(
            e -> {
              assertEquals(NotAnOrganizationMemberException.class, e.getClass());
              assertEquals(
                  String.format(
                      "user: %s is not a member of organization: %s",
                      USER_B.userId(), organizationA.id()),
                  e.getMessage());
            })
        .verify();
  }
}
