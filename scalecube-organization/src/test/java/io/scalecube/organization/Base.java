package io.scalecube.organization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import io.scalecube.organization.repository.inmem.InMemoryOrganizationRepository;
import io.scalecube.organization.repository.inmem.InMemoryUserOrganizationMembershipRepository;
import io.scalecube.security.Profile;
import java.io.File;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class Base {

  protected static KeyPairGenerator keyPairGenerator;

  protected final Profile testProfile =
      Profile.builder()
          .userId("1")
          .email("user1@gmail.com")
          .emailVerified(true)
          .name("foo")
          .familyName("fname")
          .givenName("lname")
          .build();
  protected final Profile testProfile2 =
      Profile.builder()
          .userId("2")
          .email("user2@gmail.com")
          .emailVerified(true)
          .name("foo2")
          .familyName("fname2")
          .givenName("lname2")
          .build();
  protected final Profile invalidProfile =
      Profile.builder()
          .userId("3")
          .email("user3@gmail.com")
          .emailVerified(true)
          .name("foo3")
          .familyName("fname3")
          .givenName("lname3")
          .build();
  protected final Profile testProfile4 =
      Profile.builder()
          .userId("4")
          .email("user4@gmail.com")
          .emailVerified(true)
          .name("foo4")
          .familyName("fname4")
          .givenName("lname4")
          .build();
  protected final Profile testProfile5 =
      Profile.builder()
          .userId("5")
          .email("user5@gmail.com")
          .emailVerified(true)
          .name("foo5")
          .familyName("fname5")
          .givenName("lname5")
          .build();
  protected final Profile testAdminProfile =
      Profile.builder()
          .userId("12")
          .email("user1@gmail.com")
          .emailVerified(true)
          .name("adminUser")
          .familyName("fname")
          .givenName("lname")
          .claims(Collections.singletonMap("role", "Admin"))
          .build();

  protected OrganizationService service;
  protected String organizationId;
  protected Organization organisation;
  protected Token token = new Token("user1");
  protected Repository<Organization, String> organizationRepository;
  protected UserOrganizationMembershipRepository orgMembersRepository;
  private OrganizationMembersRepositoryAdmin admin;

  protected Base() {
    orgMembersRepository = new InMemoryUserOrganizationMembershipRepository();
    organizationRepository = new InMemoryOrganizationRepository();
    admin =
        new OrganizationMembersRepositoryAdmin() {
          @Override
          public void createRepository(Organization organization) {
            // dummy body
            System.out.print(".");
          }

          @Override
          public void deleteRepository(Organization organization) {
            // dummy body
            System.out.print("'");
          }
        };
    service = createService(testProfile);
    new File("keystore.properties").deleteOnExit();
    // init with couchbase
    //    orgMembersRepository = CouchbaseRepositoryFactory.organizationMembers();
    //    organizationRepository = CouchbaseRepositoryFactory.organizations();
    //    admin = CouchbaseRepositoryFactory.organizationMembersRepositoryAdmin();
  }

  @BeforeAll
  static void beforeAll() throws NoSuchAlgorithmException {
    keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
  }

  protected static String randomString() {
    int targetStringLength = 10;

    return new Random()
        .ints(targetStringLength, 'a', 'z')
        .collect(
            StringBuilder::new, (builder, i) -> builder.append((char) i), StringBuilder::append)
        .toString();
  }

  protected String createRandomOrganization() {
    Random rand = new Random();
    AtomicReference<CreateOrganizationResponse> createdOrg = new AtomicReference<>();
    StepVerifier.create(
            service.createOrganization(
                new CreateOrganizationRequest("myTestOrg5" + rand.nextInt(50) + 1, token)))
        .consumeNextWith(createdOrg::set)
        .verifyComplete();

    assertThat(createdOrg.get().id(), notNullValue());
    return createdOrg.get().id();
  }

  protected void deleteOrganization(String id) {
    StepVerifier.create(service.deleteOrganization(new DeleteOrganizationRequest(token, id)))
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
  }

  protected void addMemberToOrganization(String organisationId, Profile profile) {
    addMemberToOrganization(organisationId, profile, Role.Member);
  }

  protected void addMemberToOrganization(String organisationId, Profile profile, Role role) {
    StepVerifier.create(
            this.service.inviteMember(
                new InviteOrganizationMemberRequest(
                    token, organisationId, profile.getUserId(), role.toString())))
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
  }

  protected OrganizationService createService(Profile profile) {
    return OrganizationServiceImpl.builder()
        .organizationRepository(organizationRepository)
        .organizationMembershipRepository(orgMembersRepository)
        .organizationMembershipRepositoryAdmin(admin)
        .tokenVerifier((t) -> Objects.equals(profile, invalidProfile) ? null : profile)
        .keyPairGenerator(keyPairGenerator)
        .build();
  }

  protected static <T> void assertMonoCompletesWithError(
      Mono<T> mono, Class<? extends Throwable> exception) {
    StepVerifier.create(mono).expectSubscription().expectError(exception).verify();
  }

  protected Organization getOrganizationFromRepository(String organisationId) {
    return organizationRepository.findById(organisationId).orElseThrow(IllegalStateException::new);
  }

  protected Organization createOrganization(String name) {
    AtomicReference<CreateOrganizationResponse> createdOrganizationId = new AtomicReference<>();
    StepVerifier.create(service.createOrganization(new CreateOrganizationRequest(name, token)))
        .consumeNextWith(createdOrganizationId::set)
        .verifyComplete();

    return organizationRepository.findById(createdOrganizationId.get().id()).get();
  }

  @AfterEach
  public void deleteOrganizationAfterTest() {
    StepVerifier.create(
            service.deleteOrganization(new DeleteOrganizationRequest(token, organizationId)))
        .assertNext(result -> assertTrue(result.deleted(), "failed to delete organization"))
        .verifyComplete();
  }

  @BeforeEach
  public void createOrganizationBeforeTest() {
    organisation = createOrganization(randomString());
    organizationId = organisation.id();
  }
}
