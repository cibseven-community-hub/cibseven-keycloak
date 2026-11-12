package org.cibseven.community.keycloak.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.cibseven.bpm.engine.identity.Tenant;

import junit.framework.Test;

/**
 * Tests tenant related queries with {@code organizationsEnabled} only ({@code organizationsExtendedQueriesEnabled}
 * switched off). Pins down that both flags work independently.
 * <p>
 * Runs without any additional permissions for the service account: the organizations of a user can be
 * looked up without {@code manage-realm}.
 */
public class KeycloakTenantQueryBasicTest extends AbstractKeycloakTenantQueryTest {

	public static Test suite() {
		// intentionally no MANAGE_REALM
		return tenantSuite(KeycloakTenantQueryBasicTest.class, "camunda.enableOrganizationsBasic.cfg.xml");
	}

	// user specific queries work

	public void testTenantQueryByUserMember() {
		Set<String> tenantIds = identityService.createTenantQuery().userMember(USER_ID_GUNNAR).list()
				.stream().map(Tenant::getId).collect(Collectors.toSet());
		assertEquals(new HashSet<>(Arrays.asList(TENANT_ID_ONE, TENANT_ID_TWO)), tenantIds);
	}

	public void testTenantQueryByUserMemberAndTenantId() {
		Tenant tenant = identityService.createTenantQuery().userMember(USER_ID_GUNNAR).tenantId(TENANT_ID_ONE).singleResult();
		assertNotNull(tenant);
		assertEquals("Tenant One", tenant.getName());
	}

	// queries requiring the extended flag return nothing

	public void testUserQueryMemberOfTenantReturnsNothing() {
		assertEquals(0, identityService.createUserQuery().memberOfTenant(TENANT_ID_ONE).list().size());
		assertEquals(0, identityService.createUserQuery().memberOfTenant(TENANT_ID_ONE).count());
	}

	public void testUnfilteredTenantQueryReturnsNothing() {
		assertEquals(0, identityService.createTenantQuery().count());
	}

	public void testTenantQueryByIdReturnsNothing() {
		assertNull(identityService.createTenantQuery().tenantId(TENANT_ID_ONE).singleResult());
	}

	public void testFindTenantByIdReturnsNull() {
		assertNull(findTenantById(TENANT_ID_ONE));
	}
}
