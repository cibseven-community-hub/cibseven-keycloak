package org.cibseven.community.keycloak.test;

import junit.framework.Test;

/**
 * Tests tenant related queries with {@code organizationsEnabled} and {@code organizationsExtendedQueriesEnabled}
 * switched on, but without the {@code manage-realm} role for the service account.
 * <p>
 * Keycloak answers the organization endpoints of the extended queries with 403. The plugin must not fail
 * (this would e.g. break the Admin webapp), but return no result and log a warning.
 * The organizations of a user do not require {@code manage-realm}, see {@link KeycloakTenantQueryBasicTest}.
 */
public class KeycloakTenantQueryAccessDeniedTest extends AbstractKeycloakTenantQueryTest {

	public static Test suite() {
		// intentionally no MANAGE_REALM
		return tenantSuite(KeycloakTenantQueryAccessDeniedTest.class, "camunda.enableOrganizations.cfg.xml");
	}

	public void testTenantQueryReturnsNothing() {
		assertEquals(0, identityService.createTenantQuery().list().size());
		assertEquals(0, identityService.createTenantQuery().count());
		assertNull(identityService.createTenantQuery().tenantId(TENANT_ID_ONE).singleResult());
	}

	public void testFindTenantByIdReturnsNull() {
		assertNull(findTenantById(TENANT_ID_ONE));
	}

	public void testUserQueryMemberOfTenantReturnsNothing() {
		assertEquals(0, identityService.createUserQuery().memberOfTenant(TENANT_ID_ONE).list().size());
	}

	public void testUserQueryWithoutTenantIsUnaffected() {
		assertNotNull(identityService.createUserQuery().userId(USER_ID_CAMUNDA).singleResult());
	}
}
