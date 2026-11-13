package org.cibseven.community.keycloak.test;

import junit.framework.Test;

/**
 * Tests tenant related queries with {@code organizationsEnabled} switched on, but without the
 * {@code view-organizations} role for the service account.
 * <p>
 * Keycloak answers the organization endpoints with 403. The plugin must not fail (this would e.g. break
 * the login of the webapps, which query the tenants of the user), but return no tenants and log a warning.
 */
public class KeycloakTenantQueryAccessDeniedTest extends AbstractKeycloakTenantQueryTest {

	public static Test suite() {
		// intentionally no VIEW_ORGANIZATIONS
		return tenantSuite(KeycloakTenantQueryAccessDeniedTest.class, "camunda.enableOrganizations.cfg.xml");
	}

	public void testTenantQueryReturnsNothing() {
		assertEquals(0, identityService.createTenantQuery().list().size());
		assertEquals(0, identityService.createTenantQuery().count());
		assertNull(identityService.createTenantQuery().tenantId(TENANT_ID_ONE).singleResult());
	}

	public void testTenantQueryByUserMemberReturnsNothing() {
		assertEquals(0, identityService.createTenantQuery().userMember(USER_ID_GUNNAR).count());
	}

	public void testUserQueryMemberOfTenantReturnsNothing() {
		assertEquals(0, identityService.createUserQuery().memberOfTenant(TENANT_ID_ONE).list().size());
	}

	public void testUserQueryWithoutTenantIsUnaffected() {
		assertNotNull(identityService.createUserQuery().userId(USER_ID_CAMUNDA).singleResult());
	}
}
