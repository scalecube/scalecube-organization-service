package io.scalecube.organization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.Await;
import io.scalecube.Await.AwaitLatch;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.DeleteOrganizationResponse;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.Organization;
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
import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class Base {

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
  protected String organisationId;
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
    AwaitLatch<CreateOrganizationResponse> await =
        consume(
            service.createOrganization(
                new CreateOrganizationRequest("myTestOrg5" + rand.nextInt(50) + 1, token)));
    assertThat(await.error(), is(nullValue()));
    assertThat(await.result(), is(notNullValue()));
    assertThat(await.result().id(), is(notNullValue()));
    return await.result().id();
  }

  protected void deleteOrganization(String id) {
    consume(service.deleteOrganization(new DeleteOrganizationRequest(token, id)));
  }

  protected void addMemberToOrganization(
      String organisationId, OrganizationService service, Profile profile) {
    addMemberToOrganization(organisationId, service, profile, Role.Member);
  }

  protected void addMemberToOrganization(
      String organisationId, OrganizationService service, Profile profile, Role role) {
    consume(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                token, organisationId, profile.getUserId(), role.toString())));
  }

  protected OrganizationService createService(Profile profile) {
    return OrganizationServiceImpl.builder()
        .organizationRepository(organizationRepository)
        .organizationMembershipRepository(orgMembersRepository)
        .organizationMembershipRepositoryAdmin(admin)
        .tokenVerifier((t) -> Objects.equals(profile, invalidProfile) ? null : profile)
        .build();
  }

  protected static <T> Duration assertMonoCompletesWithError(Mono<T> mono, Class<? extends Throwable> exception) {
    return StepVerifier.create(mono).expectSubscription().expectError(exception).verify();
  }

  protected Organization getOrganizationFromRepository(String organisationId) {
    return organizationRepository.findById(organisationId).orElseThrow(IllegalStateException::new);
  }

  protected Organization createOrganization(String name) {
    AwaitLatch<CreateOrganizationResponse> await =
        consume(service.createOrganization(new CreateOrganizationRequest(name, token)));
    assertThat(await.error(), is(nullValue()));
    return organizationRepository.findById(await.result().id()).get();
  }

  @AfterEach
  public void deleteOrganizationAfterTest() {
    AwaitLatch<DeleteOrganizationResponse> await =
        consume(service.deleteOrganization(new DeleteOrganizationRequest(token, organisationId)));
    assertThat(await.error(), is(nullValue()));
    assertTrue(await.result().deleted(), "failed to delete organization");
  }

  @BeforeEach
  public void createOrganizationBeforeTest() {
    organisation = createOrganization(randomString());
    organisationId = organisation.id();
  }

  /**
   * Subscribe to the mono argument and request unbounded demand
   *
   * @param mono publisher
   * @param <T> type of response
   * @return mono consume or error
   */
  protected static <T> AwaitLatch<T> consume(Mono<T> mono) {
    AwaitLatch<T> await = Await.one();
    mono.subscribe(await::result, await::error);
    await.timeout(2, TimeUnit.SECONDS);
    return await;
  }
}
