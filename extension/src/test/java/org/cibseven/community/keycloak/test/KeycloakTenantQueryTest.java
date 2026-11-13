package org.cibseven.community.keycloak.test;

import static org.cibseven.bpm.engine.authorization.Permissions.READ;
import static org.cibseven.bpm.engine.authorization.Resources.TENANT;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.cibseven.bpm.engine.identity.Tenant;
import org.cibseven.bpm.engine.identity.User;
import org.cibseven.community.keycloak.CacheableKeycloakTenantQuery;
import org.cibseven.community.keycloak.CacheableKeycloakUserQuery;
import org.cibseven.community.keycloak.KeycloakTenantQuery;
import org.cibseven.community.keycloak.KeycloakUserQuery;

import junit.framework.Test;

/**
 * Tests tenant queries (Keycloak organizations = CIB seven tenants) and user queries filtered by tenant
 * with {@code organizationsEnabled}.
 * <p>
 * Test data: Tenant One = camunda, gunnar / Tenant Two = gunnar, johnfoo / hans is member of no tenant.
 */
public class KeycloakTenantQueryTest extends AbstractKeycloakTenantQueryTest {

	public static Test suite() {
		return tenantSuite(KeycloakTenantQueryTest.class, "camunda.enableOrganizations.cfg.xml", VIEW_ORGANIZATIONS);
	}

	// ------------------------------------------------------------------------
	// Tenant queries
	// ------------------------------------------------------------------------

	public void testQueryNoFilter() {
		List<Tenant> tenants = identityService.createTenantQuery().list();
		assertEquals(2, tenants.size());
		assertEquals(2, identityService.createTenantQuery().count());
	}

	public void testFilterByTenantId() {
		Tenant tenant = identityService.createTenantQuery().tenantId(TENANT_ID_ONE).singleResult();
		assertNotNull(tenant);
		assertEquals(TENANT_ID_ONE, tenant.getId());
		assertEquals("Tenant One", tenant.getName());

		assertNull(identityService.createTenantQuery().tenantId("non-existent").singleResult());
	}

	public void testFindTenantById() {
		Tenant tenant = findTenantById(TENANT_ID_TWO);
		assertNotNull(tenant);
		assertEquals(TENANT_ID_TWO, tenant.getId());
		assertEquals("Tenant Two", tenant.getName());

		assertNull(findTenantById("non-existent"));
	}

	public void testFilterByTenantIdIn() {
		List<Tenant> tenants = identityService.createTenantQuery().tenantIdIn(TENANT_ID_ONE, TENANT_ID_TWO).list();
		assertEquals(2, tenants.size());

		tenants = identityService.createTenantQuery().tenantIdIn(TENANT_ID_TWO, "non-existent").list();
		assertEquals(1, tenants.size());
		assertEquals("Tenant Two", tenants.get(0).getName());
	}

	public void testFilterByTenantName() {
		Tenant tenant = identityService.createTenantQuery().tenantName("Tenant One").singleResult();
		assertNotNull(tenant);
		assertEquals(TENANT_ID_ONE, tenant.getId());

		assertNull(identityService.createTenantQuery().tenantName("whatever").singleResult());
	}

	public void testFilterByTenantNameLike() {
		assertEquals(2, identityService.createTenantQuery().tenantNameLike("Tenant*").count());
		assertEquals(2, identityService.createTenantQuery().tenantNameLike("Tenant%").count());

		Tenant tenant = identityService.createTenantQuery().tenantNameLike("Tenant O*").singleResult();
		assertNotNull(tenant);
		assertEquals(TENANT_ID_ONE, tenant.getId());

		assertEquals(0, identityService.createTenantQuery().tenantNameLike("what*").count());
	}

	public void testFilterByUserMember() {
		assertEquals(tenantIds(TENANT_ID_ONE, TENANT_ID_TWO),
				ids(identityService.createTenantQuery().userMember(USER_ID_GUNNAR).list()));
		assertEquals(tenantIds(TENANT_ID_ONE),
				ids(identityService.createTenantQuery().userMember(USER_ID_CAMUNDA).list()));
		assertEquals(tenantIds(TENANT_ID_TWO),
				ids(identityService.createTenantQuery().userMember(USER_ID_JOHN).list()));
		assertEquals(0, identityService.createTenantQuery().userMember(USER_ID_HANS).count());
		assertEquals(0, identityService.createTenantQuery().userMember("non-existing").count());
	}

	public void testFilterByUserMemberAndTenantId() {
		Tenant tenant = identityService.createTenantQuery().userMember(USER_ID_GUNNAR).tenantId(TENANT_ID_TWO).singleResult();
		assertNotNull(tenant);
		assertEquals(TENANT_ID_TWO, tenant.getId());

		assertNull(identityService.createTenantQuery().userMember(USER_ID_CAMUNDA).tenantId(TENANT_ID_TWO).singleResult());
	}

	public void testFilterByUserMemberAndTenantName() {
		Tenant tenant = identityService.createTenantQuery().userMember(USER_ID_GUNNAR).tenantName("Tenant One").singleResult();
		assertNotNull(tenant);
		assertEquals(TENANT_ID_ONE, tenant.getId());
	}

	public void testFilterByGroupMember() {
		// group based tenant membership is not supported - documented limitation
		assertEquals(0, identityService.createTenantQuery().groupMember(GROUP_ID_ADMIN).count());
	}

	public void testOrderByTenantId() {
		List<String> expected = Arrays.asList(TENANT_ID_ONE, TENANT_ID_TWO).stream().sorted().collect(Collectors.toList());

		List<Tenant> tenants = identityService.createTenantQuery().orderByTenantId().asc().list();
		assertEquals(expected, tenants.stream().map(Tenant::getId).collect(Collectors.toList()));

		tenants = identityService.createTenantQuery().orderByTenantId().desc().list();
		assertEquals(Arrays.asList(expected.get(1), expected.get(0)), tenants.stream().map(Tenant::getId).collect(Collectors.toList()));
	}

	public void testOrderByTenantName() {
		List<Tenant> tenants = identityService.createTenantQuery().orderByTenantName().asc().list();
		assertEquals(Arrays.asList("Tenant One", "Tenant Two"), tenants.stream().map(Tenant::getName).collect(Collectors.toList()));

		tenants = identityService.createTenantQuery().orderByTenantName().desc().list();
		assertEquals(Arrays.asList("Tenant Two", "Tenant One"), tenants.stream().map(Tenant::getName).collect(Collectors.toList()));
	}

	public void testQueryPaging() {
		List<Tenant> first = identityService.createTenantQuery().orderByTenantName().asc().listPage(0, 1);
		List<Tenant> second = identityService.createTenantQuery().orderByTenantName().asc().listPage(1, 1);
		List<Tenant> beyond = identityService.createTenantQuery().orderByTenantName().asc().listPage(2, 1);

		assertEquals(1, first.size());
		assertEquals(1, second.size());
		assertEquals(0, beyond.size());
		assertEquals("Tenant One", first.get(0).getName());
		assertEquals("Tenant Two", second.get(0).getName());
	}

	public void testAuthenticatedUserCanQueryOwnTenants() {
		processEngineConfiguration.setAuthorizationEnabled(true);
		identityService.setAuthenticatedUserId(USER_ID_JOHN);

		// own tenants are visible without any authorization
		assertEquals(tenantIds(TENANT_ID_TWO), ids(identityService.createTenantQuery().userMember(USER_ID_JOHN).list()));

		// other tenants require READ permission
		assertEquals(0, identityService.createTenantQuery().count());
		assertEquals(0, identityService.createTenantQuery().userMember(USER_ID_CAMUNDA).count());
	}

	public void testAuthorizedUserCanReadTenant() {
		// create authorization before enabling authorization checks
		createGrantAuthorization(TENANT, TENANT_ID_ONE, USER_ID_JOHN, READ);

		processEngineConfiguration.setAuthorizationEnabled(true);
		identityService.setAuthenticatedUserId(USER_ID_JOHN);

		assertEquals(tenantIds(TENANT_ID_ONE), ids(identityService.createTenantQuery().list()));
		assertEquals(tenantIds(TENANT_ID_ONE), ids(identityService.createTenantQuery().userMember(USER_ID_CAMUNDA).list()));
	}

	public void testQueryObjectEquality() {
		KeycloakTenantQuery q1 = (KeycloakTenantQuery) identityService.createTenantQuery();
		KeycloakTenantQuery q2 = (KeycloakTenantQuery) identityService.createTenantQuery();

		assertNotSame(q1, q2);
		assertNotEquals(q1, q2);

		assertNotSame(CacheableKeycloakTenantQuery.of(q1), CacheableKeycloakTenantQuery.of(q2));
		assertEquals(CacheableKeycloakTenantQuery.of(q1), CacheableKeycloakTenantQuery.of(q2));
		assertEquals(CacheableKeycloakTenantQuery.of(q1).hashCode(), CacheableKeycloakTenantQuery.of(q2).hashCode());

		q1.tenantId(TENANT_ID_ONE);
		assertNotEquals(CacheableKeycloakTenantQuery.of(q1), CacheableKeycloakTenantQuery.of(q2));
		q2.tenantId(TENANT_ID_ONE);
		assertEquals(CacheableKeycloakTenantQuery.of(q1), CacheableKeycloakTenantQuery.of(q2));

		q1.userMember(USER_ID_GUNNAR);
		assertNotEquals(CacheableKeycloakTenantQuery.of(q1), CacheableKeycloakTenantQuery.of(q2));
		q2.userMember(USER_ID_GUNNAR);
		assertEquals(CacheableKeycloakTenantQuery.of(q1), CacheableKeycloakTenantQuery.of(q2));

		q1.tenantIdIn(TENANT_ID_ONE, TENANT_ID_TWO);
		assertNotEquals(CacheableKeycloakTenantQuery.of(q1), CacheableKeycloakTenantQuery.of(q2));
		q2.tenantIdIn(TENANT_ID_ONE, TENANT_ID_TWO);
		assertEquals(CacheableKeycloakTenantQuery.of(q1), CacheableKeycloakTenantQuery.of(q2));
	}

	// ------------------------------------------------------------------------
	// User queries filtered by tenant
	// ------------------------------------------------------------------------

	public void testUserQueryMemberOfTenant() {
		assertEquals(userIds(USER_ID_CAMUNDA, USER_ID_GUNNAR),
				ids(identityService.createUserQuery().memberOfTenant(TENANT_ID_ONE).list()));
		assertEquals(userIds(USER_ID_GUNNAR, USER_ID_JOHN),
				ids(identityService.createUserQuery().memberOfTenant(TENANT_ID_TWO).list()));
		assertEquals(2, identityService.createUserQuery().memberOfTenant(TENANT_ID_ONE).count());
	}

	public void testUserQueryMemberOfTenantAndGroup() {
		// intersection of tenant members and group members
		assertEquals(userIds(USER_ID_GUNNAR),
				ids(identityService.createUserQuery().memberOfTenant(TENANT_ID_ONE).memberOfGroup(GROUP_ID_MANAGER).list()));
		assertEquals(userIds(USER_ID_CAMUNDA),
				ids(identityService.createUserQuery().memberOfTenant(TENANT_ID_ONE).memberOfGroup(GROUP_ID_ADMIN).list()));
		// hans is teamlead, but member of no tenant
		assertEquals(userIds(USER_ID_GUNNAR),
				ids(identityService.createUserQuery().memberOfTenant(TENANT_ID_TWO).memberOfGroup(GROUP_ID_TEAMLEAD).list()));
		assertEquals(0, identityService.createUserQuery().memberOfTenant(TENANT_ID_TWO).memberOfGroup(GROUP_ID_ADMIN).count());
	}

	public void testUserQueryMemberOfTenantWithFilter() {
		User user = identityService.createUserQuery().memberOfTenant(TENANT_ID_ONE).userFirstName("Admin").singleResult();
		assertNotNull(user);
		assertEquals(USER_ID_CAMUNDA, user.getId());

		user = identityService.createUserQuery().memberOfTenant(TENANT_ID_ONE).userId(USER_ID_GUNNAR).singleResult();
		assertNotNull(user);
		assertEquals("Gunnar", user.getFirstName());

		assertNull(identityService.createUserQuery().memberOfTenant(TENANT_ID_ONE).userId(USER_ID_JOHN).singleResult());
	}

	public void testUserQueryMemberOfUnknownTenant() {
		assertEquals(0, identityService.createUserQuery().memberOfTenant("non-existent").count());
	}

	public void testUserQueryObjectEqualityWithTenant() {
		KeycloakUserQuery q1 = (KeycloakUserQuery) identityService.createUserQuery();
		KeycloakUserQuery q2 = (KeycloakUserQuery) identityService.createUserQuery();
		assertEquals(CacheableKeycloakUserQuery.of(q1), CacheableKeycloakUserQuery.of(q2));

		q1.memberOfTenant(TENANT_ID_ONE);
		assertNotEquals(CacheableKeycloakUserQuery.of(q1), CacheableKeycloakUserQuery.of(q2));
		q2.memberOfTenant(TENANT_ID_TWO);
		assertNotEquals(CacheableKeycloakUserQuery.of(q1), CacheableKeycloakUserQuery.of(q2));

		KeycloakUserQuery q3 = (KeycloakUserQuery) identityService.createUserQuery();
		q3.memberOfTenant(TENANT_ID_ONE);
		assertEquals(CacheableKeycloakUserQuery.of(q1), CacheableKeycloakUserQuery.of(q3));
		assertEquals(CacheableKeycloakUserQuery.of(q1).hashCode(), CacheableKeycloakUserQuery.of(q3).hashCode());
	}

	// ------------------------------------------------------------------------
	// Group queries filtered by tenant
	// ------------------------------------------------------------------------

	public void testGroupQueryMemberOfTenantIsNotSupported() {
		try {
			identityService.createGroupQuery().memberOfTenant(TENANT_ID_ONE);
			fail("UnsupportedOperationException expected");
		} catch (UnsupportedOperationException expected) {
			// documented limitation
		}
	}

	// ------------------------------------------------------------------------
	// Helpers
	// ------------------------------------------------------------------------

	private static Set<String> tenantIds(String... ids) {
		return Arrays.stream(ids).collect(Collectors.toSet());
	}

	private static Set<String> userIds(String... ids) {
		return Arrays.stream(ids).collect(Collectors.toSet());
	}

	private static Set<String> ids(List<?> entities) {
		return entities.stream()
				.map(e -> e instanceof Tenant ? ((Tenant) e).getId() : ((User) e).getId())
				.collect(Collectors.toSet());
	}
}
