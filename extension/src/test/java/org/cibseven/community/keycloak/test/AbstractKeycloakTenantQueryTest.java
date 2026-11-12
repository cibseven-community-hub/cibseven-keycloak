package org.cibseven.community.keycloak.test;

import static org.cibseven.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;

import org.cibseven.bpm.engine.ProcessEngineConfiguration;
import org.cibseven.bpm.engine.authorization.Authorization;
import org.cibseven.bpm.engine.authorization.Permission;
import org.cibseven.bpm.engine.authorization.Resource;
import org.cibseven.bpm.engine.identity.Tenant;
import org.cibseven.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.cibseven.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.cibseven.community.keycloak.plugin.KeycloakIdentityProviderPlugin;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Super class for tenant (= Keycloak organization) tests.
 * <p>
 * The Keycloak organization endpoints require additional permissions for the service account of the
 * test client. These permissions are granted only while a tenant test class is running, see
 * {@link #tenantSuite(Class, String, String...)}, so all other tests keep running with the minimal set of permissions.
 */
public abstract class AbstractKeycloakTenantQueryTest extends AbstractKeycloakIdentityProviderTest {

	/**
	 * Required for the organization endpoints used by {@code organizationsExtendedQueriesEnabled}
	 * (list all organizations, lookup by ID, members of an organization), otherwise Keycloak answers with 403.
	 * Not required for {@code organizationsEnabled} (organizations of a user).
	 */
	protected static final String MANAGE_REALM = "manage-realm";

	protected static final String USER_ID_CAMUNDA = "camunda@accso.de";
	protected static final String USER_ID_GUNNAR = "gunnar.von-der-beck@accso.de";
	protected static final String USER_ID_HANS = "hans.mustermann@tradermail.info";
	protected static final String USER_ID_JOHN = "johnfoo@gmail.com";

	/**
	 * Creates the test suite for a tenant test class.
	 * @param testClass the test class
	 * @param configResource the process engine configuration to use
	 * @param realmManagementRoles the additional realm-management roles granted to the service account while the suite is running
	 * @return the test suite
	 */
	protected static Test tenantSuite(Class<? extends TestCase> testClass, String configResource, String... realmManagementRoles) {
		return new TestSetup(new TestSuite(testClass)) {

			// @BeforeClass
			@Override
			protected void setUp() throws Exception {
				// grant before building the engine - the roles must be contained in the access token of the plugin
				grantServiceAccountRealmManagementRoles(realmManagementRoles);
				ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
								.createProcessEngineConfigurationFromResource(configResource);
				configureKeycloakIdentityProviderPlugin(config);
				PluggableProcessEngineTestCase.cachedProcessEngine = config.buildProcessEngine();
			}

			// @AfterClass
			@Override
			protected void tearDown() throws Exception {
				try {
					PluggableProcessEngineTestCase.cachedProcessEngine.close();
					PluggableProcessEngineTestCase.cachedProcessEngine = null;
				} finally {
					revokeServiceAccountRealmManagementRoles(realmManagementRoles);
				}
			}
		};
	}

	@Override
	protected void tearDown() throws Exception {
		processEngineConfiguration.setAuthorizationEnabled(false);
		identityService.clearAuthentication();
		// delete all created authorizations
		authorizationService.createAuthorizationQuery().list()
				.forEach(a -> authorizationService.deleteAuthorization(a.getId()));
		clearCache();
		super.tearDown();
	}

	/**
	 * clears the query caches so each test can start with a clean slate
	 */
	protected void clearCache() {
		processEngineConfiguration.getProcessEnginePlugins()
						.stream()
						.filter(KeycloakIdentityProviderPlugin.class::isInstance)
						.map(KeycloakIdentityProviderPlugin.class::cast)
						.forEach(KeycloakIdentityProviderPlugin::clearCache);
	}

	/**
	 * Calls {@code ReadOnlyIdentityProvider.findTenantById} - not reachable via the public API.
	 * @param tenantId the tenant ID
	 * @return the tenant or {@code null}
	 */
	protected Tenant findTenantById(String tenantId) {
		return processEngineConfiguration.getCommandExecutorTxRequired()
				.execute(commandContext -> commandContext.getReadOnlyIdentityProvider().findTenantById(tenantId));
	}

	protected void createGrantAuthorization(Resource resource, String resourceId, String userId, Permission... permissions) {
		Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
		authorization.setResource(resource);
		authorization.setResourceId(resourceId);
		authorization.setUserId(userId);
		for (Permission permission : permissions) {
			authorization.addPermission(permission);
		}
		authorizationService.saveAuthorization(authorization);
	}
}
