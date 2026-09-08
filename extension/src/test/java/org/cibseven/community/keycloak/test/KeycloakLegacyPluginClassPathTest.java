package org.cibseven.community.keycloak.test;

import java.util.List;

import org.cibseven.bpm.engine.ProcessEngineConfiguration;
import org.cibseven.bpm.engine.identity.Group;
import org.cibseven.bpm.engine.identity.User;
import org.cibseven.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.cibseven.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.cibseven.bpm.engine.impl.test.PluggableProcessEngineTestCase;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Backward compatibility test for the class path deprecated as of 2.2.0.
 * <p>
 * The engine of this test is configured through {@code camunda.legacyPluginClassPath.cfg.xml},
 * which declares the plugin using the old
 * {@code org.cibseven.bpm.extension.keycloak.plugin.KeycloakIdentityProviderPlugin} class path.
 * It therefore fails as soon as that alias is removed, renamed, or stops inheriting the
 * configuration and behaviour of its replacement - i.e. as soon as existing configurations
 * written before the namespace change would break.
 */
public class KeycloakLegacyPluginClassPathTest extends AbstractKeycloakIdentityProviderTest {

	public static Test suite() {
		return new TestSetup(new TestSuite(KeycloakLegacyPluginClassPathTest.class)) {

			// @BeforeClass
			protected void setUp() throws Exception {
				ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
						.createProcessEngineConfigurationFromResource("camunda.legacyPluginClassPath.cfg.xml");
				configureKeycloakIdentityProviderPlugin(config);
				PluggableProcessEngineTestCase.cachedProcessEngine = config.buildProcessEngine();
			}

			// @AfterClass
			protected void tearDown() throws Exception {
				PluggableProcessEngineTestCase.cachedProcessEngine.close();
				PluggableProcessEngineTestCase.cachedProcessEngine = null;
			}
		};
	}

	// ------------------------------------------------------------------------
	// Tests
	// ------------------------------------------------------------------------

	/**
	 * The plugin instantiated from the deprecated class path must still be usable
	 * wherever its replacement is expected.
	 */
	public void testDeprecatedClassPathIsStillAPluginOfTheCurrentType() {
		List<ProcessEnginePlugin> plugins = processEngineConfiguration.getProcessEnginePlugins();

		ProcessEnginePlugin plugin = plugins.stream()
				.filter(org.cibseven.bpm.extension.keycloak.plugin.KeycloakIdentityProviderPlugin.class::isInstance)
				.findFirst()
				.orElse(null);
		assertNotNull("Plugin was not instantiated from the deprecated class path", plugin);

		// the whole point of the alias: old configurations remain assignment compatible
		assertTrue("Deprecated class path is no longer a KeycloakIdentityProviderPlugin",
				plugin instanceof org.cibseven.community.keycloak.plugin.KeycloakIdentityProviderPlugin);
	}

	/**
	 * The identity provider must actually be installed and functional, not merely loadable.
	 */
	public void testIdentityProviderIsInstalledAndQueryable() {
		assertEquals(5, identityService.createUserQuery().count());
		assertTrue(identityService.createGroupQuery().count() > 0);
	}

	/**
	 * Configuration declared against the deprecated class path must still be applied,
	 * i.e. the inherited setters are wired. This engine sets useEmailAsCamundaUserId=true,
	 * so the mail address is expected to be the user ID.
	 */
	public void testInheritedConfigurationIsApplied() {
		User user = identityService.createUserQuery().userId("camunda@accso.de").singleResult();
		assertNotNull("useEmailAsCamundaUserId was not inherited from the current implementation", user);
		assertEquals("camunda@accso.de", user.getId());

		Group group = identityService.createGroupQuery().groupName("camunda-admin").singleResult();
		assertNotNull(group);
	}

}
