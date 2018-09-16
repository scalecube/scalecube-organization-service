package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import io.scalecube.account.api.GetMembershipRequest;
import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.KickoutOrganizationMemberRequest;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.OrganizationNotFound;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationRequest;
import io.scalecube.organization.repository.OrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import io.scalecube.organization.repository.couchbase.CouchbaseRepositoryFactory;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.omg.PortableInterceptor.INACTIVE;
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
    init();
    service = createService(testProfile);
  }

  private void init() {
    orgMembersRepository = new InMemoryUserOrganizationMembershipRepository();
    organizationRepository = new InMemoryOrganizationRepository();
    admin = new OrganizationMembersRepositoryAdmin() {
      @Override
      public void createRepository(Organization organization) {
        // dummy body
        System.out.print(".");
      }

      @Override
      public void deleteRepository(Organization organization) {
        // dummy body
        System.out.print(".");
      }
    };

// init with couchbase
//    orgMembersRepository = CouchbaseRepositoryFactory.organizationMembers();
//    organizationRepository = CouchbaseRepositoryFactory.organizations();
//    admin = CouchbaseRepositoryFactory.organizationMembersRepositoryAdmin();
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
  public void getUserOrganizationsMembership() {
    addMemberToOrganization(organisationId, service, testProfile);
    assertNotNull(
    StepVerifier.create(
        service.getUserOrganizationsMembership(
            new GetMembershipRequest(token)
        )
    ).assertNext(r -> assertEquals(r.organizations()[0].id(), organisationId))
        .verifyComplete());
  }

  @Test
  public void getUserMembership_invalid_user_should_fail_with_InvalidAuthenticationToken() {
    Duration duration = expectError(
        createService(invalidProfile).getUserOrganizationsMembership(
            new GetMembershipRequest(token))
        , InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void getUserMembership_null_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        service.getUserOrganizationsMembership(
            new GetMembershipRequest(null))
        , NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getUserMembership_null_inner_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        service.getUserOrganizationsMembership(
            new GetMembershipRequest(new Token(null, null)))
        , NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getUserMembership_empty_inner_token_should_fail_with_NIllegalArgumentException() {
    Duration duration = expectError(
        service.getUserOrganizationsMembership(
            new GetMembershipRequest(new Token(null, "")))
        , IllegalArgumentException.class);
    assertNotNull(duration);
  }


  @Test
  public void createOrganization_with_name_already_in_use_should_Fail() {
    Duration duration = expectError(
        service.createOrganization(
            new CreateOrganizationRequest("myTestOrg5", token, "email"))
        , NameAlreadyInUseException.class);
    assertNotNull(duration);
  }

  @Test
  public void createOrganization() {
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
  public void createOrganization_with_empty_name_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(
        service.createOrganization(
            new CreateOrganizationRequest("",
                token, "email")), IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void createOrganization_with_null_name_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        service.createOrganization(
            new CreateOrganizationRequest(null,
                token, "email")), NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void createOrganization_with_empty_email_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(
        service.createOrganization(
            new CreateOrganizationRequest("foo",
                token, "")), IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void createOrganization_with_null_email_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        service.createOrganization(
            new CreateOrganizationRequest("foo",
                token, null)), NullPointerException.class);
    assertNotNull(duration);
  }


  @Test
  public void createOrganization_should_fail_with_InvalidAuthenticationToken() {
    Duration duration = expectError(
        createService(invalidProfile).createOrganization(
                new CreateOrganizationRequest("myTestOrg5", token, "email"))
        , InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void createOrganization_null_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        service.createOrganization(
                new CreateOrganizationRequest("myTestOrg5", null, "email"))
        , NullPointerException.class);
    assertNotNull(duration);
  }


  @Test
  public void createOrganization_null_inner_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        service.createOrganization(
                new CreateOrganizationRequest("myTestOrg5", new Token(null, null), "email"))
        , NullPointerException.class);
    assertNotNull(duration);
  }


  @Test
  public void createOrganization_empty_token_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(
        service.createOrganization(
            new CreateOrganizationRequest("myTestOrg5", new Token(null, ""), "email"))
        , IllegalArgumentException.class);
    assertNotNull(duration);
  }


  @Test
  public void getOrganizationMembership() {
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
  public void getOrganization() {
    Duration duration = StepVerifier.create(service.getOrganization(
        new GetOrganizationRequest(token, organisationId)))
        .expectSubscription()
        .assertNext((r) -> assertThat(r.id(), is(organisationId)))
        .expectComplete()
        .verify();
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_not_a_member_should_fail_with_AccessPermissionException() {
    Duration duration = expectError(
        createService(testProfile5)
            .getOrganization(new GetOrganizationRequest(new Token("foo", "bar"),
                organisationId)), AccessPermissionException.class);
    assertNotNull(duration);
  }

  private Organization getOrganizationFromRepository(String organisationId) {
    return organizationRepository
        .findById(organisationId)
        .orElseThrow(IllegalStateException::new);
  }

  @Test
  public void getOrganization_should_fail_with_EntityNotFoundException() {
    Duration duration = expectError(
        service.getOrganization(new GetOrganizationRequest(token, "bla")),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_should_fail_with_InvalidAuthenticationToken() {
    Duration duration = expectError(
        createService(invalidProfile).getOrganization(
            new GetOrganizationRequest(token, organisationId)),
        InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_with_empty_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(
        createService(testProfile).getOrganization(
            new GetOrganizationRequest(token, "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_with_null_id_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        createService(testProfile).getOrganization(
            new GetOrganizationRequest(token, null)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_with_null_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        createService(testProfile).getOrganization(
            new GetOrganizationRequest(null, organisationId)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_with_invalid_token_should_fail_with_InvalidAuthenticationToken() {
    Duration duration = expectError(
        createService(invalidProfile).getOrganization(
            new GetOrganizationRequest(token , organisationId)),
        InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_with_id_not_exists_should_fail_with_EntityNotFoundException() {
    Duration duration = expectError(
        createService(testProfile).getOrganization(
            new GetOrganizationRequest(token , "orgIdNotExists")),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_with_empty_token_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(
        createService(testProfile).getOrganization(
            new GetOrganizationRequest(new Token(null, "") , "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void delete_organization_invalid_token_should_fail_with_InvalidAuthenticationToken() {
    Duration duration = expectError(
        createService(invalidProfile)
        .deleteOrganization(
        new DeleteOrganizationRequest(token, organisationId)), InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void delete_organization_with_id_not_exists_should_fail_with_EntityNotFoundException() {
    Duration duration = expectError(
        createService(testProfile)
            .deleteOrganization(
                new DeleteOrganizationRequest(token, "orgIdNotExists")),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void delete_organization_with_empty_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(
        createService(testProfile)
            .deleteOrganization(
                new DeleteOrganizationRequest(token, "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void delete_organization_with_null_id_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        createService(testProfile)
            .deleteOrganization(
                new DeleteOrganizationRequest(token, null)),
          NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void delete_organization_with_null_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        createService(testProfile)
            .deleteOrganization(
                new DeleteOrganizationRequest(null, organisationId)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void delete_organization_with_empty_token_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(
        createService(testProfile)
            .deleteOrganization(
                new DeleteOrganizationRequest(new Token(null, ""), organisationId)),
        IllegalArgumentException.class);
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
  public void update_organization_with_id_not_exists_should_fail_with_EntityNotFoundException() {
    Duration duration = expectError(
        service.updateOrganization(new UpdateOrganizationRequest(
            "orgNotExists",
            token,
            "update_name",
            "update@email")), EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void update_organization_with_empty_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(
        service.updateOrganization(new UpdateOrganizationRequest(
            "",
            token,
            "update_name",
            "update@email")), IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void update_organization_with_invalid_token_should_fail_with_InvalidToken() {
    Duration duration = expectError(
        createService(invalidProfile).updateOrganization(new UpdateOrganizationRequest(
            organisationId,
            token,
            "update_name",
            "update@email")), InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void update_organization_with_null_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        createService(invalidProfile).updateOrganization(new UpdateOrganizationRequest(
            organisationId,
            null,
            "update_name",
            "update@email")), NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void update_organization_with_null_name_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        createService(testProfile).updateOrganization(new UpdateOrganizationRequest(
            organisationId,
            token,
            null,
            "update@email")), NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void update_organization_with_null_email_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        createService(testProfile).updateOrganization(new UpdateOrganizationRequest(
            organisationId,
            token,
            "name",
            null)), NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void update_organization_with_empty_name_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(
        createService(testProfile).updateOrganization(new UpdateOrganizationRequest(
            organisationId,
            token,
            "",
            "update@email")), IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void update_organization_with_empty_email_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(
        createService(testProfile).updateOrganization(new UpdateOrganizationRequest(
            organisationId,
            token,
            "name",
            "")), IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganization() {
    consume(service.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(token,
            organisationId,
            "testApiKey",
            new HashMap<>())));
    Duration duration = StepVerifier
        .create(service.updateOrganization(new UpdateOrganizationRequest(
            organisationId,
            token,
            "update_name",
            "update@email")))
        .expectSubscription()
        .assertNext((r) -> {
          assertThat("name not updated", r.name(), is("update_name"));
          assertThat("email not updated", r.email(), is("update@email"));
          assertThat("missing api key ", r.apiKeys().length, is(not(0)));
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
  public void getOrganizationMembers_empty_org_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest("", token))
        , IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembers_null_org_id_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(null, token))
        , NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembers_empty_token_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(organisationId,
            new Token(null, "")))
        , IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembers_null_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(organisationId,
            null))
        , NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembers_null_inner_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(organisationId,
            new Token(null, null)))
        , NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembers_should_fail_with_InvalidAuthenticationToken() {
    Duration duration = expectError(createService(invalidProfile)
            .getOrganizationMembers(new GetOrganizationMembersRequest(organisationId, token))
        , InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembers_should_fail_with_EntityNotFoundException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(
            "orgNotExists", token))
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
  public void inviteMember_empty_org_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, "", testProfile5.getUserId())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_null_org_id_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, null, testProfile5.getUserId())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_empty_user_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, organisationId, "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_null_user_id_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, organisationId, null)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_empty_token_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(new Token(null, ""), organisationId,
            testProfile5.getUserId())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_null_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(null, organisationId,
            testProfile5.getUserId())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_null_inner_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(new Token(null, null), organisationId,
            testProfile5.getUserId())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_org_not_exists_should_fail_with_EntityNotFoundException() {
    expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, "orgNotExists",
            testProfile5.getUserId())),
        EntityNotFoundException.class);
    assertThat(true, is(true));
  }

  @Test
  public void inviteMember_should_fail_with_InvalidAuthenticationToken() {
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
  public void kickoutMember_invalid_user_should_fail_with_InvalidAuthenticationToken() {
    Duration duration = expectError(createService(invalidProfile).kickoutMember(
        new KickoutOrganizationMemberRequest(organisationId, token, testProfile5.getUserId())),
        InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMember_empty_org_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest("", token, testProfile5.getUserId())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMember_null_org_id_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest(null, token, testProfile5.getUserId())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMember_empty_token_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest(organisationId, new Token(null, ""),
            testProfile5.getUserId())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMember_null_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest(organisationId, null, testProfile5.getUserId())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMember_null_inner_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest(organisationId, new Token(null, null),
            testProfile5.getUserId())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMember_empty_user_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest(organisationId, token,
            "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMember_null_user_id_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.kickoutMember(
        new KickoutOrganizationMemberRequest(organisationId, token, null)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void kickoutMember_org_not_exists_should_fail_with_EntityNotFoundException() {
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
  public void leaveOrganization_with_empty_org_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(token, "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganization_with_null_org_id_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(token, null)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganization_with_null_token_id_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(null, organisationId)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganization_with_empty_token_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(new Token(null, ""), organisationId)),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganization_with_null_inner_token_id_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(new Token(null, null), organisationId)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganization_org_not_exists_should_fail_with_EntityNotFoundException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(token, "orgNotExists")),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganization_invalid_user_should_fail_with_InvalidAuthenticationToken() {
    Duration duration = expectError(createService(invalidProfile).leaveOrganization(
        new LeaveOrganizationRequest(token, "orgNotExists")),
        InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }


  @Test
  public void addOrganizationApiKey() {
    final HashMap<String, String> claims = new HashMap<>();
    claims.put("role", "Owner");
    Duration duration = StepVerifier
        .create(service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                token,
                organisationId,
                "apiKey",
                claims)))
        .expectSubscription()
        .assertNext(x -> {
          Organization org = getOrganizationFromRepository(organisationId);
          assertThat(org.apiKeys()[0].name(), is("apiKey"));
        }).verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKey_not_org_owner_should_fail_with_AccessPermissionException() {
    Duration duration = expectError(createService(testProfile5).addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(token, organisationId, "api_key",
            new HashMap<>())),
        AccessPermissionException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKey_empty_org_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(token, "", "api_key",
            new HashMap<>())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKey_null_org_id_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(token, null, "api_key",
            new HashMap<>())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKey_null_apiKeyName_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(token, organisationId, null,
            new HashMap<>())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKey_empty_apiKeyName_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(token, organisationId, "",
            new HashMap<>())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKey_empty_token_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(new Token(null, ""), organisationId,
            "api_key",
            new HashMap<>())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKey_null_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(null, organisationId, "api_key",
            new HashMap<>())),
        NullPointerException.class);
    assertNotNull(duration);
  }


  @Test
  public void addOrganizationApiKey_null_inner_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(new Token(null, null), organisationId,
            "api_key",
            new HashMap<>())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKey_with_null_claims_should_pass() {
    Duration duration = StepVerifier.create(service.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(token, organisationId,
            "api_key",
            null))).expectSubscription()
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKey_org_not_exists_should_fail_with_EntityNotFoundException() {
    Duration duration = expectError(service.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(token, "bla", "api_key",
            new HashMap<>())),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKey_invalid_user_should_fail_with_InvalidAuthenticationToken() {
    Duration duration = expectError(createService(invalidProfile).addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(token, organisationId, "api_key",
            new HashMap<>())),
        InvalidAuthenticationToken.class);
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
  public void addOrganizationApiKey_user_not_owner_should_fail_with_AccessPermissionException() {
    Duration duration = expectError(createService(testProfile2).addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(token, organisationId, "api_key", null)),
        AccessPermissionException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKey_user_not_owner_should_fail_with_AccessPermissionException() {
    Duration duration = expectError(createService(testProfile2).deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, organisationId, "api_key")),
        AccessPermissionException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKey_empty_org_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, "", "api_key")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKey_org_not_exists_should_fail_with_EntityNotFoundException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, "bla", "api_key")),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKey_null_org_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, null, "api_key")),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKey_null_apiKeyName_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, organisationId, null)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKey_empty_name_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, organisationId, "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKey_invalid_user_should_fail_with_InvalidAuthenticationToken() {
    Duration duration = expectError(createService(invalidProfile).deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, organisationId, "api_key")),
        InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKey_null_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(null, organisationId, "api_key")),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKey_null_inner_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(new Token(null, null), organisationId,
            "api_key")),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKey_empty_token_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(new Token(null, ""), organisationId,
            "api_key")),
        IllegalArgumentException.class);
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