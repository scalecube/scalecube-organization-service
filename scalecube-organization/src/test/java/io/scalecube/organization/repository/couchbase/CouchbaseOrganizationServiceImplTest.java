package io.scalecube.organization.repository.couchbase;

//import io.scalecube.Await;
//import io.scalecube.Await.AwaitLatch;
//import io.scalecube.account.api.*;
//import io.scalecube.organization.OrganizationServiceImpl;
//import io.scalecube.organization.repository.Repository;
//import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
//import io.scalecube.organization.repository.exception.EntityNotFoundException;
//import io.scalecube.organization.repository.exception.InvalidInputException;
//import io.scalecube.organization.repository.exception.NameAlreadyInUseException;
import io.scalecube.testlib.BaseTest;
//import org.junit.jupiter.api.*;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.util.*;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//import java.util.function.Supplier;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//import java.util.stream.Stream;
//
//import static org.hamcrest.CoreMatchers.hasItem;
//import static org.hamcrest.CoreMatchers.not;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.emptyArray;
//import static org.hamcrest.core.Is.is;
//import static org.hamcrest.core.IsNull.notNullValue;
//import static org.junit.jupiter.api.Assertions.assertTrue;

public class CouchbaseOrganizationServiceImplTest extends BaseTest {

//    private static OrganizationService service;
//    private Token token = new Token("google", "user1");
//    private static final User testUser = new User("1", "user1@gmail.com", true, "name 1",
//            "http://picture.jpg", "EN", "fname", "lname", null);
//    private static final User testUser2 = new User("2", "user2@gmail.com", true, "name 2",
//            "http://picture2.jpg", "EN", "fname2", "lname2", null);
//    private final User invalidUser = new User("3", "user3@gmail.com", true, "name 3",
//            "http://picture=3.jpg", "EN", "fname3", "lname3", null);
//    private static final User testUser4 = new User("4", "user4@gmail.com", true, "name 3",
//            "http://picture4.jpg", "EN", "fname4", "lname4", null);
//    private static final User testUser5 = new User("5", "user5@gmail.com", true, "name 3",
//            "http://picture5.jpg", "EN", "fname5", "lname5", null);
//
//    private static Repository<User, String> userRepository;
//    private static Repository<Organization, String> organizationRepository;
//    private static UserOrganizationMembershipRepository orgMembersRepository;
//    private static CouchbaseOrganizationMembersRepositoryAdmin membersRepositoryAdmin;
//
//    public CouchbaseOrganizationServiceImplTest() {
//    }
//
//    private String organisationId;
//    private Organization organisation;
//
//    @BeforeAll
//    public static void beforeAll() {
//        userRepository = CouchbaseRepositoryFactory.users();
//        organizationRepository = CouchbaseRepositoryFactory.organizations();
//        orgMembersRepository = CouchbaseRepositoryFactory.organizationMembers();
//        membersRepositoryAdmin = new CouchbaseOrganizationMembersRepositoryAdmin.Builder().build();
//        userRepository.save("1", testUser);
//        userRepository.save("2", testUser2);
//        userRepository.save("4", testUser4);
//        userRepository.save("5", testUser5);
//        service = OrganizationServiceImpl
//                .builder()
//                .organizationRepository(organizationRepository)
//                .userRepository(userRepository)
//                .organizationMembershipRepository(orgMembersRepository)
//                .organizationMembershipRepositoryAdmin(CouchbaseRepositoryFactory.organizationMembersRepositoryAdmin())
//                .tokenVerifier((t) -> testUser)
//                .build();
//    }
//
//    @AfterAll
//    public static void afterAll() {
//        IntStream.range(1,6).filter(i -> i != 3).forEach((i) -> userRepository.deleteById(String.valueOf(i)));
//    }
//
//    @BeforeEach
//    public void createOrganizationBeforeTest() {
//        organisation =  Organization.builder()
//                .name("myTestOrg5")
//                .id(UUID.randomUUID().toString())
//                .email("email")
//                .ownerId("1")
//                .build();
//        membersRepositoryAdmin.createRepository(organisation);
//        organisationId = organizationRepository.save(organisation.id(), organisation).id();
//    }
//
//
//    @AfterEach
//    public void deleteOrganizationAfterTest() {
//        membersRepositoryAdmin.deleteRepository(organisation);
//        organizationRepository.deleteById(organisationId);
//    }
//
//    @Test
//    public void doNothing(){
//
//    }
//
//    @Test
//    public void createOrganizationTest() {
//        CreateOrganizationResponse result = consume(service.createOrganization(
//                new CreateOrganizationRequest("myTestOrg-" + System.currentTimeMillis(), token, "email"))).result();
//
//        String id = result.id();
//        StepVerifier
//                .create(service.getOrganization(new GetOrganizationRequest(token, id)))
//                .expectSubscription()
//                .assertNext((r)-> assertThat(r.id(), is(id)))
//        .verifyComplete();
//        consume(service.deleteOrganization(new DeleteOrganizationRequest(token, id)));
//    }
//
//    @Test
//    public void createDuplicateOrganizationNameShouldFail() {
//        expectError(service.createOrganization(
//                new CreateOrganizationRequest("myTestOrg5", token, "email")),
//                NameAlreadyInUseException.class);
//    }
//
//    @Test
//    public void createInvalidOrganizationNameShouldFail() {
//        expectError(service.createOrganization(
//                new CreateOrganizationRequest("myORG/myTestOrg5", token, "email")),
//                InvalidInputException.class);
//    }
//
//    @Test
//    public void createEmptyOrganizationNameShouldFail() {
//        expectError(service.createOrganization(
//                new CreateOrganizationRequest("", token, "email")),
//                InvalidInputException.class);
//    }
//
//
//    @Test
//    public void createNullOrganizationNameShouldFail() {
//        expectError(service.createOrganization(
//                new CreateOrganizationRequest(null, token, "email")),
//                InvalidInputException.class);
//    }
//
//    @Test
//    public void getOrganizationsMember() {
//        addMemberToOrganization(organisationId, service, testUser2);
//        assertTrue(orgMembersRepository.isMember(testUser2, organisation));
//    }
//
//
//    @Test
//    public void getOrganization() {
//        StepVerifier.create(service.getOrganization(
//                    new GetOrganizationRequest(token, organisationId)))
//                .expectSubscription()
//                .assertNext((r)-> assertThat(r.id(), is(organisationId)))
//                .expectComplete()
//                .verify();
//    }
//
//    private Organization getOrganizationFromRepository(String organisationId) {
//        return organizationRepository
//                .findById(organisationId)
//                .orElseThrow(IllegalStateException::new);
//    }
//
//    @Test
//    public void getOrganizationShouldFailWithEntityNotFoundException() {
//        expectError(service.getOrganization(
//                new GetOrganizationRequest(token, "bla")),EntityNotFoundException.class);
//    }
//
//    @Test
//    public void deleteOrganization() {
//        StepVerifier.create(service.deleteOrganization(new DeleteOrganizationRequest(token, organisationId)))
//                .expectSubscription()
//                .assertNext((r)-> assertThat(r.deleted(), is(true)))
//                .expectComplete()
//                .verify();
//    }
//
//    @Test
//    public void updateOrganizationShouldFailWithOrgNotFoundException() {
//        expectError(
//            service.updateOrganization(new UpdateOrganizationRequest(
//                "",
//                token,
//                "update_name",
//                "update@email")),EntityNotFoundException.class);
//    }
//
//
//    @Test
//    public void updateOrganization() {
//        StepVerifier.create(service.updateOrganization(new UpdateOrganizationRequest(
//                organisationId,
//                token,
//                "update_name",
//                "update@email")))
//                .expectSubscription()
//                .assertNext((r) -> {
//                    assertThat(r.name(), is("update_name"));
//                    assertThat(r.email(), is("update@email"));
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    public void getOrganizationMembers() {
//        addMemberToOrganization(organisationId, service, testUser4);
//        addMemberToOrganization(organisationId, service, testUser5);
//
//        StepVerifier.create(service.getOrganizationMembers(
//                new GetOrganizationMembersRequest(organisationId, token)))
//                .expectSubscription()
//                .assertNext((r) -> {
//                    Supplier<Stream<OrganizationMember>> members = () -> Arrays.stream(r.members());
//                    assertThat(r.members().length, is(2));
//                    long membersCount = members.get()
//                            .filter((m) -> Objects.equals(m.role(), Role.Member.toString())).count();
//                    assertThat(membersCount, is(2L));
//                    List<String> ids = members.get().map((i) -> i.user().id()).collect(Collectors.toList());
//                    assertThat(ids, hasItem(testUser4.id()));
//                    assertThat(ids, hasItem(testUser5.id()));
//
//                }).verifyComplete();
//    }
//
//    @Test
//    public void getOrganizationMembersShouldFailOrgNotFound() {
//        expectError(service.getOrganizationMembers(new GetOrganizationMembersRequest("bla", token))
//                , EntityNotFoundException.class);
//    }
//
//    @Test
//    public void inviteMember() {
//        addMemberToOrganization(organisationId, service, testUser2);
//        StepVerifier.create(service.getOrganizationMembers(new GetOrganizationMembersRequest(organisationId, token)))
//                .expectSubscription()
//                .assertNext((r) -> {
//                    Supplier<Stream<OrganizationMember>> members = () -> Arrays.stream(r.members());
//
//                    assertThat(members.get().map(i -> i.user().id()).collect(Collectors.toList()), hasItem(testUser2.id()));
//                    assertThat(members.get().filter(i -> Objects.equals(i.user().id(), testUser2.id()))
//                            .findFirst().orElseThrow(IllegalStateException::new), is(notNullValue()));
//
//                })
//                .verifyComplete();
//    }
//
//
//
//
//    /**
//     * Subscribe to the mono argument and request unbounded demand
//     * @param mono publisher
//     * @param <T> type of response
//     * @return mono consume or error
//     */
//    private <T> AwaitLatch<T> consume(Mono<T> mono) {
//        AwaitLatch<T> await = Await.one();
//        mono.subscribe(await::result, await::error);
//        await.timeout(2, TimeUnit.SECONDS);
//        return await;
//    }
//
//    @Test
//    public void inviteMemberShouldFailWithUserNotFound() {
//        expectError(service.inviteMember(
//                    new InviteOrganizationMemberRequest(token, organisationId, invalidUser.id())),
//                EntityNotFoundException.class);
//    }
//
//    @Test
//    public void inviteMemberShouldFailOrgNotFound() {
//        expectError(service.inviteMember(
//                new InviteOrganizationMemberRequest(token, "", testUser5.id())),
//                EntityNotFoundException.class);
//    }
//
//
//    @Test
//    public void kickoutMember() {
//        addMemberToOrganization(organisationId, service, testUser5);
//        StepVerifier
//                .create(service.kickoutMember(
//                        new KickoutOrganizationMemberRequest(organisationId, token, testUser5.id())))
//                .expectSubscription()
//                .assertNext(x -> StepVerifier
//                        .create(service.getOrganizationMembers(
//                                new GetOrganizationMembersRequest(organisationId, token)))
//                        .expectSubscription()
//                        .assertNext(r ->  {
//                            List<String> members = Arrays.stream(r.members())
//                                    .map(i -> i.user().id()).collect(Collectors.toList());
//                            assertThat(members, not(hasItem(testUser5.id())));
//                        })
//                        .verifyComplete()).verifyComplete();
//
//    }
//
//    @Test
//    public void kickoutMemberShouldFailWithUserNotFound() {
//        expectError(service.kickoutMember(
//                new KickoutOrganizationMemberRequest(organisationId, token, invalidUser.id())),
//                EntityNotFoundException.class);
//    }
//
//
//    @Test
//    public void kickoutMemberShouldFailWithOrgNotFound() {
//        expectError(service.kickoutMember(
//                    new KickoutOrganizationMemberRequest("", token, testUser5.id())),
//                EntityNotFoundException.class);
//    }
//
//    ///////////////////////////////////////////////////////////
//    @Test
//    public void leaveOrganization() {
//        addMemberToOrganization(organisationId, service, testUser);
//        StepVerifier
//                .create(service.leaveOrganization(
//                        new LeaveOrganizationRequest(token, organisationId)))
//                .expectSubscription()
//                .assertNext(x -> StepVerifier
//                        .create(service.getOrganizationMembers(
//                                new GetOrganizationMembersRequest(organisationId, token)))
//                        .expectSubscription()
//                        .assertNext(r -> assertThat(Arrays.asList(r.members()),
//                                not(hasItem(new OrganizationMember(testUser, Role.Owner.toString()))))).verifyComplete())
//                .verifyComplete();
//    }
//
//    private void addMemberToOrganization(String organisationId,
//                                         OrganizationService service,
//                                         User user) {
//        consume(service.inviteMember(
//                new InviteOrganizationMemberRequest(token, organisationId, user.id())));
//    }
//
//
//    @Test
//    public void leaveOrganizationShouldFailWithUserNotFound() {
//        expectError(createService(invalidUser).leaveOrganization(
//                    new LeaveOrganizationRequest(token, organisationId)),
//                InvalidAuthenticationToken.class);
//    }
//
//    @Test
//    public void leaveOrganizationShouldFailWithOrgNotFound() {
//        expectError(service.leaveOrganization(
//                    new LeaveOrganizationRequest(token, "bla")),
//                EntityNotFoundException.class);
//    }
//
//
//
//    @Test
//    public void addOrganizationApiKey() {
//        StepVerifier
//                .create(service.addOrganizationApiKey(
//                        new AddOrganizationApiKeyRequest(
//                                token,
//                                organisationId,
//                                "apiKey",
//                                Arrays.asList("assertion").stream().collect(Collectors.toMap(x->x, x->x)))))
//                .expectSubscription()
//                .assertNext(x -> {
//                    Organization org = getOrganizationFromRepository(organisationId);
//                    assertThat(org.apiKeys()[0].name(), is("apiKey"));
//                }).verifyComplete();
//    }
//
//    @Test
//    public void addOrganizationApiKeyWithUserNotFound() {
//        expectError(createService(invalidUser).addOrganizationApiKey(
//                new AddOrganizationApiKeyRequest(token, organisationId, "", new HashMap<>())),
//                InvalidAuthenticationToken.class);
//    }
//
//    @Test
//    public void addOrganizationApiKeyWithOrgNotFound() {
//        expectError(service.addOrganizationApiKey(
//                new AddOrganizationApiKeyRequest(token, "bla", "", new HashMap<>())),
//                EntityNotFoundException.class);
//    }
//
//    @Test
//    public void deleteOrganizationApiKey() {
//        StepVerifier
//                .create(service.addOrganizationApiKey(
//                        new AddOrganizationApiKeyRequest(
//                                token,
//                                organisationId,
//                                "apiKey",
//                                Arrays.asList("assertion").stream().collect(Collectors.toMap(x->x, x->x)))))
//                .expectSubscription()
//                .assertNext(x -> StepVerifier
//                        .create(service.deleteOrganizationApiKey(
//                                new DeleteOrganizationApiKeyRequest(
//                                        token,
//                                        organisationId,
//                                        "apiKey")))
//                        .expectSubscription()
//                        .assertNext(k -> {
//                            Organization org = getOrganizationFromRepository(organisationId);
//                            assertThat(org.apiKeys(), emptyArray());
//                        }).verifyComplete()).verifyComplete();
//    }
//
//    @Test
//    public void deleteOrganizationApiKeyWithOrgNotFound() {
//        expectError(service.deleteOrganizationApiKey(
//                new DeleteOrganizationApiKeyRequest(token, "bla", "")),
//                EntityNotFoundException.class);
//    }
//
//
//    private OrganizationService createService(User user) {
//        return OrganizationServiceImpl
//                .builder()
//                .organizationRepository(organizationRepository)
//                .userRepository(userRepository)
//                .tokenVerifier((t) -> user)
//                .build();
//    }
//
//    private <T> void expectError(Mono<T> mono,  Class<? extends Throwable> exception) {
//        StepVerifier
//                .create(mono)
//                .expectSubscription()
//                .expectError(exception)
//                .verify();
//    }
//
}