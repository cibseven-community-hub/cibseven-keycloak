package org.cibseven.community.keycloak.test;

import junit.framework.Test;

/**
 * Tests tenant related queries with the default configuration, i.e. {@code organizationsEnabled} and
 * {@code organizationsExtendedQueriesEnabled} switched off (backward compatibility).
 * <p>
 * Runs without any additional permissions for the service account: with organizations disabled the
 * organization endpoints must not be called at all.
 */
public class KeycloakTenantQueryDisabledTest extends AbstractKeycloakTenantQueryTest {

	public static Test suite() {
		return tenantSuite(KeycloakTenantQueryDisabledTest.class, "camunda.cfg.xml");
	}

	public void testTenantQueryReturnsNothing() {
		assertEquals(0, identityService.createTenantQuery().list().size());
		assertEquals(0, identityService.createTenantQuery().count());
		assertNull(identityService.createTenantQuery().tenantId(TENANT_ID_ONE).singleResult());
		assertEquals(0, identityService.createTenantQuery().tenantIdIn(TENANT_ID_ONE, TENANT_ID_TWO).count());
	}

	public void testTenantQueryByUserMemberReturnsNothing() {
		assertEquals(0, identityService.createTenantQuery().userMember(USER_ID_GUNNAR).count());
	}

	public void testFindTenantByIdReturnsNull() {
		assertNull(findTenantById(TENANT_ID_ONE));
	}

	public void testUserQueryMemberOfTenantReturnsNothing() {
		// used to throw an UnsupportedOperationException before tenants were supported
		assertEquals(0, identityService.createUserQuery().memberOfTenant(TENANT_ID_ONE).list().size());
		assertEquals(0, identityService.createUserQuery().memberOfTenant(TENANT_ID_ONE).memberOfGroup(GROUP_ID_ADMIN).count());
	}

	public void testUserQueryWithoutTenantIsUnaffected() {
		assertNotNull(identityService.createUserQuery().userId(USER_ID_CAMUNDA).singleResult());
	}
}
