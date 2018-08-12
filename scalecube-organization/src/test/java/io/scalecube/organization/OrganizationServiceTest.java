package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

import io.scalecube.Await;
import io.scalecube.Await.AwaitLatch;
import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.KickoutOrganizationMemberRequest;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationRequest;
import io.scalecube.organization.repository.OrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.organization.repository.inmem.InMemoryOrganizationRepository;
import io.scalecube.organization.repository.inmem.InMemoryUserOrganizationMembershipRepository;
import io.scalecube.security.Profile;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class OrganizationServiceTest {

  private final OrganizationService service;
  private final Profile testProfile = new Profile(
      "1",
      null,
      "user1@gmail.com",
      true,
      "foo",
      "fname",
      "lname",
      null);
  private final Profile testProfile2 = new Profile(
      "2",
      null,
      "user2@gmail.com",
      true,
      "foo2",
      "fname2",
      "lname2",
      null);
  private final Profile invalidProfile = new Profile(
      "3",
      null,
      "user3@gmail.com",
      true,
      "foo3",
      "fname3",
      "lname3",
      null);
  private final Profile testProfile4 = new Profile(
      "4",
      null,
      "user4@gmail.com",
      true,
      "foo4",
      "fname4",
      "lname4",
      null);
  private final Profile testProfile5 = new Profile(
      "5",
      null,
      "user5@gmail.com",
      true,
      "foo5",
      "fname5",
      "lname5",
      null);


  private final InMemoryOrganizationRepository organizationRepository
      = new InMemoryOrganizationRepository();
  private Token token = new Token("google", "user1");
  private InMemoryUserOrganizationMembershipRepository orgMembersRepository
      = new InMemoryUserOrganizationMembershipRepository();
  private String organisationId;

  private final OrganizationMembersRepositoryAdmin admin
      = new OrganizationMembersRepositoryAdmin() {
    /**
     * Empty implementation.
     * @param organization The organization for which a members repository should be created.
     */
    @Override
    public void createRepository(Organization organization) {
      // dummy body
      System.out.println();
    }

    /**
     * Empty implementation.
     * @param organization The organization for which the members repository should be deleted.
     */
    @Override
    public void deleteRepository(Organization organization) {
      // dummy body
      System.out.println();}
  };

  public OrganizationServiceTest() {

    service = OrganizationServiceImpl
        .builder()
        .organizationRepository(organizationRepository)
        .tokenVerifier((t) -> testProfile)
        .organizationMembershipRepository(orgMembersRepository)
        .organizationMembershipRepositoryAdmin(new OrganizationMembersRepositoryAdmin() {
          @Override
          public void createRepository(Organization organization) {
          }

          @Override
          public void deleteRepository(Organization organization) {
          }
        })
        .build();
  }

  @BeforeEach
  public void createOrganizationBeforeTest() {
    organisationId = consume(service.createOrganization(
        new CreateOrganizationRequest("myTestOrg5", token, "email"))).result().id();
  }

  @AfterEach
  public void deleteOrganizationAfterTest() {
    organizationRepository.deleteById(organisationId);
  }


  @Test
  public void createOrganizationTest() {
    String id = consume(service.createOrganization(
        new CreateOrganizationRequest("myTestOrg5", token, "email"))).result().id();
    StepVerifier
        .create(service.getOrganization(new GetOrganizationRequest(token, id)))
        .expectSubscription()
        .assertNext((r) -> assertThat(r.id(), is(id)))
        .verifyComplete();
    organizationRepository.deleteById(id);
  }

  @Test
  public void createOrganizationTestShouldFailWithInvalidAuthenticationToken() {
    Duration duration = expectError(
        createService(invalidProfile)
            .createOrganization(
                new CreateOrganizationRequest("myTestOrg5", token, "email"))
        , InvalidAuthenticationToken.class);
    assertThat(duration.isZero(), is(false));
  }

  @Test
  public void getOrganizationsOwnerRoleMembership() {
    addMemberToOrganization(organisationId, service, testProfile);
    assertGetOrganizationsMembership(organisationId, testProfile);
  }

  @Test
  public void getOrganizationsMemberRoleMembership() {
    addMemberToOrganization(organisationId, service, testProfile2);
    assertGetOrganizationsMembership(organisationId, testProfile2);
  }

  private void assertGetOrganizationsMembership(String organisationId, Profile profile) {
    List<String> members = orgMembersRepository.getMembers(
        getOrganizationFromRepository(organisationId)).stream().map(OrganizationMember::id)
        .collect(Collectors.toList());
    assertThat(members, hasItem(profile.getUserId()));
  }

  @Test
  public void getOrganizationsMemberMembershipShouldFailWithInvalidAuthenticationToken() {
    expectError(createService(invalidProfile)
            .inviteMember(new InviteOrganizationMemberRequest(token, organisationId,
                invalidProfile.getUserId())),
        InvalidAuthenticationToken.class);
    assertThat(true, is(true));
  }

  @Test
  public void getOrganization() {
    StepVerifier.create(service.getOrganization(
        new GetOrganizationRequest(token, organisationId)))
        .expectSubscription()
        .assertNext((r) -> assertThat(r.id(), is(organisationId)))
        .expectComplete()
        .verify();
  }

  private Organization getOrganizationFromRepository(String organisationId) {
    return organizationRepository
        .findById(organisationId)
        .orElseThrow(IllegalStateException::new);
  }

  @Test
  public void getOrganizationShouldFailWithEntityNotFoundException() {
    expectError(service.getOrganization(
        new GetOrganizationRequest(token, "bla")), EntityNotFoundException.class);
    assertThat(true, is(true));
  }

  @Test
  public void deleteOrganizationShouldFailWithInvalidToken() {
    expectError(createService(invalidProfile).deleteOrganization(
        new DeleteOrganizationRequest(token, organisationId)), InvalidAuthenticationToken.class);
    assertThat(true, is(true));
  }

  @Test
  public void deleteOrganization() {
    StepVerifier
        .create(service.deleteOrganization(new DeleteOrganizationRequest(token, organisationId)))
        .expectSubscription()
        .assertNext((r) -> assertThat(r.deleted(), is(true)))
        .expectComplete()
        .verify();
  }

  @Test
  public void updateOrganizationShouldFailWithOrgNotFoundException() {
    expectError(
        service.updateOrganization(new UpdateOrganizationRequest(
            "",
            token,
            "update_name",
            "update@email")), EntityNotFoundException.class);
    assertThat(true, is(true));
  }

  @Test
  public void updateOrganizationShouldFailWithInvalidToken() {
    expectError(createService(invalidProfile).updateOrganization(new UpdateOrganizationRequest(
        organisationId,
        token,
        "update_name",
        "update@email")), InvalidAuthenticationToken.class);
    assertThat(true, is(true));
  }

  @Test
  public void updateOrganization() {
    StepVerifier.create(service.updateOrganization(new UpdateOrganizationRequest(
        organisationId,
        token,
        "update_name",
        "update@email")))
        .expectSubscription()
        .assertNext((r) -> {
          assertThat(r.name(), is("update_name"));
          assertThat(r.email(), is("update@email"));
        })
        .verifyComplete();
  }

  @Test
  public void getOrganizationMembers() {
    addMemberToOrganization(organisationId, service, testProfile4);
    addMemberToOrganization(organisationId, service, testProfile5);

    StepVerifier.create(service.getOrganizationMembers(
        new GetOrganizationMembersRequest(organisationId, token)))
        .expectSubscription()
        .assertNext((r) -> {
          Supplier<Stream<OrganizationMember>> members = () -> Arrays.stream(r.members());
          assertThat(r.members().length, is(2));
          long membersCount = members.get()
              .filter((m) -> Objects.equals(m.role(), Role.Member.toString())).count();
          assertThat(membersCount, is(2L));
          List<String> ids = members.get().map((i) -> i.id()).collect(Collectors.toList());
          assertThat(ids, hasItem(testProfile4.getUserId()));
          assertThat(ids, hasItem(testProfile5.getUserId()));

        }).verifyComplete();
  }

  @Test
  public void getOrganizationMembersShouldFailWithInvalidToken() {
    expectError(createService(invalidProfile)
            .getOrganizationMembers(new GetOrganizationMembersRequest(organisationId, token))
        , InvalidAuthenticationToken.class);
    assertThat(true, is(true));
  }

  @Test
  public void getOrganizationMembersShouldFailOrgNotFound() {
    expectError(service.getOrganizationMembers(new GetOrganizationMembersRequest("bla", token))
        , EntityNotFoundException.class);
    assertThat(true, is(true));
  }

  @Test
  public void inviteMember() {
    addMemberToOrganization(organisationId, service, testProfile2);
    StepVerifier.create(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(organisationId, token)))
        .expectSubscription()
        .assertNext((r) -> {
          Supplier<Stream<OrganizationMember>> members = () -> Arrays.stream(r.members());

          assertThat(members.get().map(OrganizationMember::id).collect(Collectors.toList()),
              hasItem(testProfile2.getUserId()));
          assertThat(members.get().filter(i -> Objects.equals(i.id(), testProfile2.getUserId()))
              .findFirst().orElseThrow(IllegalStateException::new), is(notNullValue()));

        })
        .verifyComplete();
  }


  /**
   * Subscribe to the mono argument and request unbounded demand
   *
   * @param mono publisher
   * @param <T> type of response
   * @return mono consume or error
   */
  private <T> AwaitLatch<T> consume(Mono<T> mono) {
    AwaitLatch<T> await = Await.one();
    mono.subscribe(await::result, await::error);
    await.timeout(2, TimeUnit.SECONDS);
    return await;
  }

  @Test
  public void inviteMemberShouldFailOrgNotFound() {
    expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, "", testProfile5.getUserId())),
        EntityNotFoundException.class);
    assertThat(true, is(true));
  }

  @Test
  public void inviteMemberShouldFailWithInvalidToken() {
    expectError(createService(invalidProfile).inviteMember(
        new InviteOrganizationMemberRequest(token, organisationId, testProfile5.getUserId())),
        InvalidAuthenticationToken.class);
    assertThat(true, is(true));
  }

  @Test
  public void kickoutMember() {
    addMemberToOrganization(organisationId, service, testProfile5);
    StepVerifier
        .create(service.kickoutMember(
            new KickoutOrganizationMemberRequest(organisationId, token, testProfile5.getUserId())))
        .expectSubscription()
        .assertNext(x -> StepVerifier
            .create(service.getOrganizationMembers(
                new GetOrganizationMembersRequest(organisationId, token)))
            .expectSubscription()
            .assertNext(r -> {
              List<String> members = Arrays.stream(r.members())
                  .map(i -> i.id()).collect(Collectors.toList());
              assertThat(members, not(hasItem(testProfile5.getUserId())));
            })
            .verifyComplete()).verifyComplete();

  }


  @Test
  public void kickoutMemberShouldFailWithInvalidUser() {
    expectError(createService(invalidProfile).kickoutMember(
        new KickoutOrganizationMemberRequest(organisationId, token, testProfile5.getUserId())),
        InvalidAuthenticationToken.class);
    assertThat(true, is(true));
  }

  @Test
  public void kickoutMemberShouldFailWithOrgNotFound() {
    expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest("", token, testProfile5.getUserId())),
        EntityNotFoundException.class);
    assertThat(true, is(true));
  }

  ///////////////////////////////////////////////////////////
  @Test
  public void leaveOrganization() {
    addMemberToOrganization(organisationId, service, testProfile);
    StepVerifier
        .create(service.leaveOrganization(
            new LeaveOrganizationRequest(token, organisationId)))
        .expectSubscription()
        .assertNext(x -> StepVerifier
            .create(service.getOrganizationMembers(
                new GetOrganizationMembersRequest(organisationId, token)))
            .expectSubscription()
            .assertNext(r -> assertThat(Arrays.asList(r.members()),
                not(hasItem(new OrganizationMember(testProfile.getUserId(), Role.Owner.toString())))))
            .verifyComplete())
        .verifyComplete();
  }

  private void addMemberToOrganization(String organisationId,
      OrganizationService service,
      Profile profile) {
    consume(service.inviteMember(
        new InviteOrganizationMemberRequest(token, organisationId, profile.getUserId())));
  }


  @Test
  public void leaveOrganizationShouldFailWithOrgNotFound() {
    expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(token, "bla")),
        EntityNotFoundException.class);
    assertThat(true, is(true));
  }


  @Test
  public void addOrganizationApiKey() {
    StepVerifier
        .create(service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                token,
                organisationId,
                "apiKey",
                Arrays.asList("assertion").stream().collect(Collectors.toMap(x -> x, x -> x)))))
        .expectSubscription()
        .assertNext(x -> {
          Organization org = getOrganizationFromRepository(organisationId);
          assertThat(org.apiKeys()[0].name(), is("apiKey"));
        }).verifyComplete();
  }

  @Test
  public void addOrganizationApiKeyWithOrgNotFound() {
    expectError(service.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(token, "bla", "", new HashMap<>())),
        EntityNotFoundException.class);
    assertThat(true, is(true));
  }

  @Test
  public void deleteOrganizationApiKey() {
    StepVerifier
        .create(service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                token,
                organisationId,
                "apiKey",
                Arrays.asList("assertion").stream().collect(Collectors.toMap(x -> x, x -> x)))))
        .expectSubscription()
        .assertNext(x -> StepVerifier
            .create(service.deleteOrganizationApiKey(
                new DeleteOrganizationApiKeyRequest(
                    token,
                    organisationId,
                    "apiKey")))
            .expectSubscription()
            .assertNext(k -> {
              Organization org = getOrganizationFromRepository(organisationId);
              assertThat(org.apiKeys(), emptyArray());
            }).verifyComplete()).verifyComplete();
  }

  @Test
  public void deleteOrganizationApiKeyWithUserNotOwner() {
    expectError(createService(testProfile2).deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, organisationId, "")),
        AccessPermissionException.class);
    assertThat(true, is(true));
  }

  @Test
  public void deleteOrganizationApiKeyWithOrgNotFound() {
    expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, "bla", "")),
        EntityNotFoundException.class);
    assertThat(true, is(true));
  }


  private OrganizationService createService(Profile profile) {
    return OrganizationServiceImpl
        .builder()
        .organizationRepository(organizationRepository)
        .organizationMembershipRepository(orgMembersRepository)
        .organizationMembershipRepositoryAdmin(admin)
        .tokenVerifier((t) -> Objects.equals(profile, invalidProfile) ? null : profile)
        .build();
  }

  private <T> Duration expectError(Mono<T> mono, Class<? extends Throwable> exception) {
    return StepVerifier
        .create(mono)
        .expectSubscription()
        .expectError(exception)
        .verify();
  }
}