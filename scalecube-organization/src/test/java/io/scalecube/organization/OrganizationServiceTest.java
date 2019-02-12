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
import io.scalecube.account.api.GetMembershipRequest;
import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.KickoutOrganizationMemberRequest;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.NotAnOrganizationMemberException;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationMemberRoleRequest;
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
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class OrganizationServiceTest {

  private static KeyPairGenerator keyPairGenerator;

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
  private final Profile testAdminProfile = new Profile(
      "12",
      null,
      "user1@gmail.com",
      true,
      "adminUser",
      "fname",
      "lname",
      new HashMap<String, Object>()
      {{
        put("role", "Admin");
      }});

  private OrganizationService service;
  private String organisationId;
  private Organization organisation;
  private Token token = new Token("google", "user1");
  private Repository<Organization, String> organizationRepository;
  private UserOrganizationMembershipRepository orgMembersRepository;
  private OrganizationMembersRepositoryAdmin admin;

  @BeforeAll
  static void beforeAll() throws NoSuchAlgorithmException {
    keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
  }

  public OrganizationServiceTest() throws NoSuchAlgorithmException {
    init();
    service = createService(testProfile);
  }

  private void init() throws NoSuchAlgorithmException {
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

  private String randomString() {
    int leftLimit = 97; // letter 'a'
    int rightLimit = 122; // letter 'z'
    int targetStringLength = 10;
    Random random = new Random();
    StringBuilder buffer = new StringBuilder(targetStringLength);
    for (int i = 0; i < targetStringLength; i++) {
      int randomLimitedInt = leftLimit + (int)
          (random.nextFloat() * (rightLimit - leftLimit + 1));
      buffer.append((char) randomLimitedInt);
    }
    String generatedString = buffer.toString();

    return generatedString;
  }

  @BeforeEach
  public void createOrganizationBeforeTest() {
    organisation = createOrganization(randomString());
    organisationId = organisation.id();
  }

  private Organization createOrganization(String name) {
    AwaitLatch<CreateOrganizationResponse> await = consume(service.createOrganization(
        new CreateOrganizationRequest(name, token, "email")));
    assertThat(await.error(), is(nullValue()));
    return organizationRepository.findById(await.result().id()).get();
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
    String orgId1 = createOrganization("testOrg1").id();
    String orgId2 = createOrganization("testOrg2").id();
    addMemberToOrganization(organisationId, service, testProfile);
    addMemberToOrganization(orgId1, service, testProfile);
    addMemberToOrganization(orgId2, service, testProfile);

    assertNotNull(
      StepVerifier.create(
          service.getUserOrganizationsMembership(
              new GetMembershipRequest(token)))
          .assertNext(r -> {
              assertThat("expected 3 memberships", r.organizations().length, is(3));
            Supplier<Stream<String>> ids = () ->
                Arrays.stream(r.organizations()).map(OrganizationInfo::id);
            assertThat(orgId1 + " is expected",
                ids.get().anyMatch(i -> Objects.equals(orgId1, i)));
            assertThat(orgId2 + " is expected",
                ids.get().anyMatch(i -> Objects.equals(orgId2, i)));
            assertThat(organisationId + " is expected",
                ids.get().anyMatch(i -> Objects.equals(organisationId, i)));
          })
          .verifyComplete()
    );

    deleteOrganization(orgId1);
    deleteOrganization(orgId2);
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
            new CreateOrganizationRequest(organisation.name(), token, "email"))
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


  @Test
  public void
    update_organization_with_existing_org_name_should_fail_with_NameAlreadyInUseException() {
    Organization localOrganization = createOrganization(randomString());

    Duration duration = expectError(
        service.updateOrganization(new UpdateOrganizationRequest(
            organisationId,
            token,
            localOrganization.name(),
            "update@email")), NameAlreadyInUseException.class);
    assertNotNull(duration);
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
            "update@email")), EntityNotFoundException.class);
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
  public void updateOrganization_not_a_member_should_fail() {
    expectError(createService(testProfile5)
        .updateOrganization(new UpdateOrganizationRequest(
        organisationId,
        token,
        "update_name",
        "update@email")), AccessPermissionException.class);
  }

  @Test
  public void updateOrganization_not_admin_should_fail() {
    orgMembersRepository.addMember(getOrganizationFromRepository(organisationId),
        new OrganizationMember(testProfile2.getUserId(), Role.Member.toString()));
    expectError(createService(testProfile2)
        .updateOrganization(new UpdateOrganizationRequest(
            organisationId,
            token,
            "update_name",
            "update@email")), AccessPermissionException.class);
  }

  @Test
  public void updateOrganization() {
    orgMembersRepository.addMember(getOrganizationFromRepository(organisationId),
        new OrganizationMember(testAdminProfile.getUserId(), Role.Admin.toString()));
    consume(service.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(token,
            organisationId,
            "testApiKey",
            new HashMap<>())));
    Duration duration = StepVerifier
        .create(createService(testAdminProfile).updateOrganization(new UpdateOrganizationRequest(
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
          assertThat(r.members().length, is(3));
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

  @Test
  public void inviteMember_null_role_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
      new InviteOrganizationMemberRequest(token, organisationId,
        testProfile5.getUserId(), null)),
      IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_empty_role_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
      new InviteOrganizationMemberRequest(token, organisationId,
        testProfile5.getUserId(), "")),
      IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_invalid_role_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
      new InviteOrganizationMemberRequest(token, organisationId,
        testProfile5.getUserId(), "bla")),
      IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_higher_role_should_fail_with_AccessPermissionException() {
    addMemberToOrganization(organisationId, service, testProfile2, Role.Admin);

    Duration duration = expectError(createService(testProfile2).inviteMember(
      new InviteOrganizationMemberRequest(token, organisationId,
        testProfile5.getUserId(), Role.Owner.toString())),
      AccessPermissionException.class);
    assertNotNull(duration);
  }



  @Test
  public void inviteMember_empty_org_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, "",
          testProfile5.getUserId(), Role.Member.toString())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_null_org_id_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, null,
          testProfile5.getUserId(), Role.Member.toString())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_empty_user_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, organisationId,
          "", Role.Member.toString())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_null_user_id_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, organisationId,
          null, Role.Member.toString())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_empty_token_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(new Token(null, ""), organisationId,
            testProfile5.getUserId(), Role.Member.toString())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_null_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(null, organisationId,
            testProfile5.getUserId(), Role.Member.toString())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_null_inner_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(new Token(null, null), organisationId,
            testProfile5.getUserId(), Role.Member.toString())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember_org_not_exists_should_fail_with_EntityNotFoundException() {
    expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, "orgNotExists",
            testProfile5.getUserId(), Role.Member.toString())),
        EntityNotFoundException.class);
    assertThat(true, is(true));
  }

  @Test
  public void inviteMember_should_fail_with_InvalidAuthenticationToken() {
    Duration duration = expectError(createService(invalidProfile).inviteMember(
        new InviteOrganizationMemberRequest(token, organisationId,
          testProfile5.getUserId(), Role.Member.toString())),
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
                  .map(OrganizationMember::id).collect(Collectors.toList());
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

  @Test
  public void kickoutMember_not_org_owner_should_fail_with_AccessPermissionException() {
    Duration duration = expectError(createService(testProfile5).kickoutMember(
        new KickoutOrganizationMemberRequest(organisationId,
            token, testProfile5.getUserId())),
        AccessPermissionException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganization() {
    addMemberToOrganization(organisationId, service, testProfile5);
    Duration duration = StepVerifier
        .create(createService(testProfile5).leaveOrganization(
            new LeaveOrganizationRequest(token, organisationId)))
        .expectSubscription()
        .assertNext(x -> StepVerifier
            .create(service.getOrganizationMembers(
                new GetOrganizationMembersRequest(organisationId, token)))
            .expectSubscription()
            .assertNext(r -> assertThat(Arrays.asList(r.members()),
                not(hasItem(
                    new OrganizationMember(testProfile5.getUserId(), Role.Member.toString())))))
            .verifyComplete())
        .verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationMemberRole() {
    addMemberToOrganization(organisationId, service, testProfile5);
    Duration duration = StepVerifier
        .create(service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                token, organisationId, testProfile5.getUserId(), Role.Admin.toString())))
        .expectSubscription()
        .assertNext(x -> StepVerifier
            .create(createService(testProfile5).getOrganizationMembers(
                new GetOrganizationMembersRequest(organisationId, token)))
            .expectSubscription()
            .assertNext(r -> assertTrue(Arrays.stream(r.members()).anyMatch(i ->
                Objects.equals(i.id(), testProfile5.getUserId())
                    && Objects.equals(i.role(), Role.Admin.toString()))))
            ).verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationMemberRole_not_a_member_should_fail() {
    expectError(service.updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            token, organisationId, testProfile5.getUserId(), Role.Admin.toString())),
        NotAnOrganizationMemberException.class);
  }

  @Test
  public void updateOrganizationMemberRole_not_a_super_user_should_fail() {
    addMemberToOrganization(organisationId, service, testProfile5);
    addMemberToOrganization(organisationId, service, testProfile2);
    expectError(createService(testProfile2).updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            token, organisationId, testProfile5.getUserId(), Role.Admin.toString())),
        AccessPermissionException.class);
  }

  @Test
  public void
  updateOrganizationMemberRole_caller_not_owner_trying_to_promote_to_owner_should_fail() {
    addMemberToOrganization(organisationId, service, testProfile5);
    addMemberToOrganization(organisationId, service, testProfile2);

    // upgrade to admin
    consume(service.updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            token, organisationId, testProfile2.getUserId(), Role.Admin.toString())));
    expectError(createService(testProfile2).updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            token, organisationId, testProfile5.getUserId(), Role.Owner.toString())),
        AccessPermissionException.class);
  }

  @Test
  public void updateOrganizationMemberRole_caller_not_owner_trying_to_downgrade_user_should_fail() {
    addMemberToOrganization(organisationId, service, testProfile5);
    addMemberToOrganization(organisationId, service, testProfile2);

    // upgrade to owner
    consume(service.updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            token, organisationId, testProfile5.getUserId(), Role.Owner.toString())));
    // upgrade to admin
    consume(service.updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            token, organisationId, testProfile2.getUserId(), Role.Admin.toString())));

    // admin tries to downgrade an owner should fail
    expectError(createService(testProfile2).updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            token, organisationId, testProfile5.getUserId(), Role.Admin.toString())),
        AccessPermissionException.class);
  }

  @Test
  public void
  updateOrganizationMemberRole_with_null_user_id_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            token, organisationId, null, Role.Admin.toString())),
        NullPointerException.class);
    assertNotNull(duration);

  }

  @Test
  public void
  updateOrganizationMemberRole_with_empty_user_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            token, organisationId, "", Role.Admin.toString())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void
  updateOrganizationMemberRole_with_null_org_id_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            token, null, testProfile5.getUserId(), Role.Admin.toString())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void
  updateOrganizationMemberRole_with_empty_org_id_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            token, "", testProfile5.getUserId(), Role.Admin.toString())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void
  updateOrganizationMemberRole_with_non_exist_org_should_fail_with_EntityNotFoundException() {
    Duration duration = expectError(service.updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            token, "bla", testProfile5.getUserId(), Role.Admin.toString())),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void
  updateOrganizationMemberRole_with_null_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            null, organisationId, testProfile5.getUserId(), Role.Admin.toString())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void
  updateOrganizationMemberRole_with_null_inner_token_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            new Token(null, null), organisationId, testProfile5.getUserId(),
            Role.Admin.toString())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void
  updateOrganizationMemberRole_with_empty_inner_token_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            new Token(null, ""), organisationId, testProfile5.getUserId(),
            Role.Admin.toString())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void
  updateOrganizationMemberRole_with_empty_role_should_fail_with_IllegalArgumentException() {
    Duration duration = expectError(service.updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            token, organisationId, testProfile5.getUserId(),
            "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void
  updateOrganizationMemberRole_with_null_role_should_fail_with_NullPointerException() {
    Duration duration = expectError(service.updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            token, organisationId, testProfile5.getUserId(),
            null)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void
  updateOrganizationMemberRole_invalid_role_enum_value_should_fail_with_IllegalArgumentException() {
    addMemberToOrganization(organisationId, service, testProfile5);
    Duration duration = expectError(service.updateOrganizationMemberRole(
        new UpdateOrganizationMemberRoleRequest(
            token, organisationId, testProfile5.getUserId(),
            "invalid role enum value")),
        IllegalArgumentException.class);
    assertNotNull(duration);
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
  public void addOrganizationApiKey_user_not_admin_should_fail_with_AccessPermissionException() {
    addMemberToOrganization(organisationId, service, testProfile2);

    Duration duration = expectError(createService(testProfile2).addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(token, organisationId, "api_key", null)),
            AccessPermissionException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKey_by_admin() {
    Profile adminUser = testProfile2;
    addMemberToOrganization(organisationId, service, adminUser);

    // upgrade user to admin role
    consume(service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                    token, organisationId, adminUser.getUserId(), Role.Admin.toString())));

    // add api key by admin
    Duration duration = StepVerifier
            .create(createService(adminUser).addOrganizationApiKey(
                    new AddOrganizationApiKeyRequest(
                            token,
                            organisationId,
                            "apiKey",
                            null)))
            .expectSubscription()
            .assertNext(x -> {
              Organization org = getOrganizationFromRepository(organisationId);
              assertThat(org.apiKeys()[0].name(), is("apiKey"));
            }).verifyComplete();
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

  private void addMemberToOrganization(String organisationId,
      OrganizationService service, Profile profile) {
    addMemberToOrganization(organisationId, service, profile, Role.Member);
  }

  private void addMemberToOrganization(String organisationId,
                                       OrganizationService service, Profile profile, Role role) {
    consume(service.inviteMember(
      new InviteOrganizationMemberRequest(token, organisationId, profile.getUserId(),
        role.toString())));
  }

  private OrganizationService createService(Profile profile) {
    return OrganizationServiceImpl
        .builder()
        .organizationRepository(organizationRepository)
        .organizationMembershipRepository(orgMembersRepository)
        .organizationMembershipRepositoryAdmin(admin)
        .tokenVerifier((t) -> Objects.equals(profile, invalidProfile) ? null : profile)
        .keyPairGenerator(keyPairGenerator)
        .build();
  }

  private <T> Duration expectError(Mono<T> mono, Class<? extends Throwable> exception) {
    return StepVerifier
        .create(mono)
        .expectSubscription()
        .expectError(exception)
        .verify();
  }

  private Organization getOrganizationFromRepository(String organisationId) {
    return organizationRepository
        .findById(organisationId)
        .orElseThrow(IllegalStateException::new);
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
}
