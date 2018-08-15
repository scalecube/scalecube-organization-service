package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.Await;
import io.scalecube.Await.AwaitLatch;
import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.DeleteOrganizationResponse;
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
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.organization.repository.exception.NameAlreadyInUseException;
import io.scalecube.organization.repository.inmem.InMemoryOrganizationRepository;
import io.scalecube.organization.repository.inmem.InMemoryUserOrganizationMembershipRepository;
import io.scalecube.security.Profile;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
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


  private String organisationId;
  private Token token = new Token("google", "user1");
  private Repository<Organization, String> organizationRepository;
  private UserOrganizationMembershipRepository orgMembersRepository;
  private OrganizationMembersRepositoryAdmin admin;

  public OrganizationServiceTest() {
    initInMemory();
//    initCouchbase();
    service = createService(testProfile);
  }

//  private void initCouchbase() {
//    orgMembersRepository = CouchbaseRepositoryFactory.organizationMembers();
//    organizationRepository = CouchbaseRepositoryFactory.organizations();
//    admin = CouchbaseRepositoryFactory.organizationMembersRepositoryAdmin();
//  }

  private void initInMemory() {
    orgMembersRepository = new InMemoryUserOrganizationMembershipRepository();
    organizationRepository = new InMemoryOrganizationRepository();
    admin = new OrganizationMembersRepositoryAdmin() {
      @Override
      public void createRepository(Organization organization) {
        // dummy body
        System.out.println();
      }

      @Override
      public void deleteRepository(Organization organization) {
        // dummy body
        System.out.println();
      }
    };
  }

  @BeforeEach
  public void createOrganizationBeforeTest() {
    AwaitLatch<CreateOrganizationResponse> await = consume(service.createOrganization(
        new CreateOrganizationRequest("myTestOrg5", token, "email")));
    assertThat(await.error(), is(nullValue()));
    organisationId = await.result().id();
  }

  @AfterEach
  public void deleteOrganizationAfterTest() {
    AwaitLatch<DeleteOrganizationResponse> await = consume(service.deleteOrganization(
        new DeleteOrganizationRequest(token, organisationId)));
    assertThat(await.error(), is(nullValue()));
    assertTrue(await.result().deleted(), "failed to delete organization");
  }


  @Test
  public void createOrganizationNameAlreadyInUseShouldFail() {
    Duration duration = expectError(
        service.createOrganization(
            new CreateOrganizationRequest("myTestOrg5", token, "email"))
        , NameAlreadyInUseException.class);
    assertNotNull(duration);
  }

  @Test
  public void createOrganizationTest() {
    String id = createRandomOrganization();
    Duration duration = StepVerifier
        .create(service.getOrganization(new GetOrganizationRequest(token, id)))
        .expectSubscription()
        .assertNext((r) -> assertThat(r.id(), is(id)))
        .verifyComplete();

    deleteOrganization(id);

    assertNotNull(duration);
  }


  @Test
  public void createOrganizationTestShouldFailWithInvalidAuthenticationToken() {
    Duration duration = expectError(
        createService(invalidProfile)
            .createOrganization(
                new CreateOrganizationRequest("myTestOrg5", token, "email"))
        , InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationsOwnerRoleMembership() {
    addMemberToOrganization(organisationId, service, testProfile);
    assertGetOrganizationsMembership(organisationId, testProfile);
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
    Duration duration = StepVerifier.create(service.getOrganization(
        new GetOrganizationRequest(token, organisationId)))
        .expectSubscription()
        .assertNext((r) -> assertThat(r.id(), is(organisationId)))
        .expectComplete()
        .verify();
    assertNotNull(duration);
  }

  private Organization getOrganizationFromRepository(String organisationId) {
    return organizationRepository
        .findById(organisationId)
        .orElseThrow(IllegalStateException::new);
  }

  @Test
  public void getOrganizationShouldFailWithEntityNotFoundException() {
    Duration duration = expectError(service.getOrganization(
        new GetOrganizationRequest(token, "bla")), EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationShouldFailWithInvalidToken() {
    Duration duration = expectError(createService(invalidProfile).deleteOrganization(
        new DeleteOrganizationRequest(token, organisationId)), InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganization() {
    String id = createRandomOrganization();
    Duration duration = StepVerifier
        .create(service.deleteOrganization(new DeleteOrganizationRequest(token, id)))
        .expectSubscription()
        .assertNext((r) -> assertThat(r.deleted(), is(true)))
        .expectComplete()
        .verify();
    deleteOrganization(id);
    assertNotNull(duration);
  }

  private String createRandomOrganization() {
    Random rand = new Random();
    AwaitLatch<CreateOrganizationResponse> await = consume(service.createOrganization(
        new CreateOrganizationRequest("myTestOrg5" + rand.nextInt(50) + 1,
            token, "email")));
    assertThat(await.error(), is(nullValue()));
    assertThat(await.result(), is(notNullValue()));
    assertThat(await.result().id(), is(notNullValue()));
    return await.result().id();
  }

  private void deleteOrganization(String id) {
    consume(service.deleteOrganization(
        new DeleteOrganizationRequest(
            token, id)));
  }

  @Test
  public void updateOrganizationShouldFailWithOrgNotFoundException() {
    Duration duration = expectError(
        service.updateOrganization(new UpdateOrganizationRequest(
            "orgNotExists",
            token,
            "update_name",
            "update@email")), EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationEmptyIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(
        service.updateOrganization(new UpdateOrganizationRequest(
            "",
            token,
            "update_name",
            "update@email")), IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationShouldFailWithInvalidToken() {
    Duration duration = expectError(
        createService(invalidProfile).updateOrganization(new UpdateOrganizationRequest(
            organisationId,
            token,
            "update_name",
            "update@email")), InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganization() {
    Duration duration = StepVerifier
        .create(service.updateOrganization(new UpdateOrganizationRequest(
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
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembers() {
    addMemberToOrganization(organisationId, service, testProfile4);
    addMemberToOrganization(organisationId, service, testProfile5);

    Duration duration = StepVerifier.create(service.getOrganizationMembers(
        new GetOrganizationMembersRequest(organisationId, token)))
        .expectSubscription()
        .assertNext((r) -> {
          Supplier<Stream<OrganizationMember>> members = () -> Arrays.stream(r.members());
          assertThat(r.members().length, is(2));
          long membersCount = members.get()
              .filter((m) -> Objects.equals(m.role(), Role.Member.toString())).count();
          assertThat(membersCount, is(2L));
          List<String> ids = members.get().map(OrganizationMember::id).collect(Collectors.toList());
          assertThat(ids, hasItem(testProfile4.getUserId()));
          assertThat(ids, hasItem(testProfile5.getUserId()));
        }).verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersShouldFailWithInvalidToken() {
    Duration duration = expectError(createService(invalidProfile)
            .getOrganizationMembers(new GetOrganizationMembersRequest(organisationId, token))
        , InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersShouldFailOrgNotFound() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest("bla", token))
        , EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember() {
    addMemberToOrganization(organisationId, service, testProfile2);
    Duration duration = StepVerifier.create(
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
    assertNotNull(duration);
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
  public void inviteMemberEmptyOrgIdShouldWithIllegalArgumentException() {
    expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, "", testProfile5.getUserId())),
        IllegalArgumentException.class);
    assertThat(true, is(true));
  }

  @Test
  public void inviteMemberOrgNOtExistsShouldFailWithEntityNotFoundException() {
    expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, "bla", testProfile5.getUserId())),
        EntityNotFoundException.class);
    assertThat(true, is(true));
  }

  @Test
  public void inviteMemberShouldFailWithInvalidToken() {
    Duration duration = expectError(createService(invalidProfile).inviteMember(
        new InviteOrganizationMemberRequest(token, organisationId, testProfile5.getUserId())),
        InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

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
                  .map(i -> i.id()).collect(Collectors.toList());
              assertThat(members, not(hasItem(testProfile5.getUserId())));
            })
            .verifyComplete()).verifyComplete();
    assertNotNull(duration);
  }


  @Test
  public void kickoutMemberShouldFailWithInvalidUser() {
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
  public void kickoutMemberOrgNotExistsShouldFailWithEntityNotFoundException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest("orgNotExists",
            token, testProfile5.getUserId())),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }

  ///////////////////////////////////////////////////////////
  @Test
  public void leaveOrganization() {
    addMemberToOrganization(organisationId, service, testProfile);
    Duration duration = StepVerifier
        .create(service.leaveOrganization(
            new LeaveOrganizationRequest(token, organisationId)))
        .expectSubscription()
        .assertNext(x -> StepVerifier
            .create(service.getOrganizationMembers(
                new GetOrganizationMembersRequest(organisationId, token)))
            .expectSubscription()
            .assertNext(r -> assertThat(Arrays.asList(r.members()),
                not(hasItem(
                    new OrganizationMember(testProfile.getUserId(), Role.Owner.toString())))))
            .verifyComplete())
        .verifyComplete();
    assertNotNull(duration);
  }

  private void addMemberToOrganization(String organisationId,
      OrganizationService service, Profile profile) {
    consume(service.inviteMember(
        new InviteOrganizationMemberRequest(token, organisationId, profile.getUserId())));
  }


  @Test
  public void leaveOrganizationEmptyOrgIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(token, "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationOrgNotExistsShouldFailWithEntityNotFoundException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(token, "bla")),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }


  @Test
  public void addOrganizationApiKey() {
    Duration duration = StepVerifier
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
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyOrgNotExistShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(token, "", "", new HashMap<>())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyOrgNotExistsShouldFailWithEntityNotFoundException() {
    Duration duration = expectError(service.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(token, "bla", "", new HashMap<>())),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyWithEmptyKeyNameShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(token, organisationId, "", new HashMap<>())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKey() {
    Duration duration = StepVerifier
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
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKeyWithUserNotOwner() {
    Duration duration = expectError(createService(testProfile2).deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, organisationId, "")),
        AccessPermissionException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKeyWithEmptyOrgIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, "", "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }


  @Test
  public void deleteOrganizationApiKeyWithOrgNotExistsShouldFailWithEntityNotFoundException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, "bla", "")),
        EntityNotFoundException.class);
    assertNotNull(duration);
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