package io.scalecube.organization.ut;

import io.scalecube.organization.fixtures.InMemoryOrganizationServiceFixture;
import io.scalecube.organization.scenario.InviteMemberScenario;
import io.scalecube.test.fixtures.Fixtures;
import io.scalecube.test.fixtures.WithFixture;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(Fixtures.class)
@WithFixture(value = InMemoryOrganizationServiceFixture.class)
class InviteMemberTest extends InviteMemberScenario {}
