package io.scalecube.organization.it;

import io.scalecube.organization.fixtures.IntegrationEnvironmentFixture;
import io.scalecube.organization.scenario.DeleteOrganizationScenario;
import io.scalecube.test.fixtures.Fixtures;
import io.scalecube.test.fixtures.WithFixture;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(Fixtures.class)
@WithFixture(value = IntegrationEnvironmentFixture.class)
class DeleteOrganizationIT extends DeleteOrganizationScenario {}
