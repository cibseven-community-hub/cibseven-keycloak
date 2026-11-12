package org.cibseven.community.keycloak.test;

import org.cibseven.community.keycloak.test.util.CacheAwareKeycloakIdentityProviderPluginForTest;
import org.cibseven.community.keycloak.test.util.CountingHttpRequestInterceptor;
import org.cibseven.community.keycloak.test.util.PredictableTicker;

import junit.framework.Test;

/**
 * Tests tenant queries with caching enabled (cache expiration timeout: 5 minutes).
 */
public class KeycloakTenantQueryTestWithCaching extends AbstractKeycloakTenantQueryTest {

	public static Test suite() {
		return tenantSuite(KeycloakTenantQueryTestWithCaching.class, "camunda.enableCachingAndOrganizations.cfg.xml",
				MANAGE_REALM);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		PredictableTicker.reset();
		CountingHttpRequestInterceptor.resetCount();
	}

	public void testCacheEnabledQueryWithNoFilter() {
		int countBefore = CountingHttpRequestInterceptor.getHttpRequestCount();

		assertEquals(2, identityService.createTenantQuery().list().size());

		// non cached query: http request count should have increased
		int countAfterFirstQuery = CountingHttpRequestInterceptor.getHttpRequestCount();
		assertTrue(countAfterFirstQuery > countBefore);

		// run query again: served from cache
		assertEquals(2, identityService.createTenantQuery().list().size());
		assertEquals(2, identityService.createTenantQuery().count());
		assertEquals(countAfterFirstQuery, CountingHttpRequestInterceptor.getHttpRequestCount());
	}

	public void testCacheEnabledQueryByUserMember() {
		int countBefore = CountingHttpRequestInterceptor.getHttpRequestCount();

		assertEquals(2, identityService.createTenantQuery().userMember(USER_ID_GUNNAR).count());
		int countAfterFirstQuery = CountingHttpRequestInterceptor.getHttpRequestCount();
		assertTrue(countAfterFirstQuery > countBefore);

		assertEquals(2, identityService.createTenantQuery().userMember(USER_ID_GUNNAR).count());
		assertEquals(countAfterFirstQuery, CountingHttpRequestInterceptor.getHttpRequestCount());

		// different filter: different cache key, new request
		assertEquals(1, identityService.createTenantQuery().userMember(USER_ID_CAMUNDA).count());
		assertTrue(CountingHttpRequestInterceptor.getHttpRequestCount() > countAfterFirstQuery);
	}

	public void testOrderingAndPagingAreAppliedToCachedResult() {
		assertEquals(2, identityService.createTenantQuery().list().size());
		int countAfterFirstQuery = CountingHttpRequestInterceptor.getHttpRequestCount();

		// ordering and paging are not part of the cache key: served from the cached result
		assertEquals("Tenant Two", identityService.createTenantQuery().orderByTenantName().desc().listPage(0, 1).get(0).getName());
		assertEquals("Tenant One", identityService.createTenantQuery().orderByTenantName().desc().listPage(1, 1).get(0).getName());
		assertEquals(countAfterFirstQuery, CountingHttpRequestInterceptor.getHttpRequestCount());
	}

	public void testNameFilterIsPartOfCacheKey() {
		assertEquals(2, identityService.createTenantQuery().list().size());
		int countAfterFirstQuery = CountingHttpRequestInterceptor.getHttpRequestCount();

		// name filter is part of the cache key: new request, then served from cache
		assertEquals(1, identityService.createTenantQuery().tenantNameLike("Tenant O*").count());
		int countAfterNameQuery = CountingHttpRequestInterceptor.getHttpRequestCount();
		assertTrue(countAfterNameQuery > countAfterFirstQuery);

		assertEquals(1, identityService.createTenantQuery().tenantNameLike("Tenant O*").count());
		assertEquals(countAfterNameQuery, CountingHttpRequestInterceptor.getHttpRequestCount());
	}

	public void testClearCacheAlsoClearsTenantCache() {
		identityService.createTenantQuery().list();
		assertFalse(tenantCacheIsEmpty());

		clearCache();
		assertTrue(tenantCacheIsEmpty());
	}

	public void testCacheEntriesEvictedWhenCacheTimeoutIsReached() {
		identityService.createTenantQuery().list();
		assertFalse(tenantCacheIsEmpty());

		PredictableTicker.moveTimeForwardByMinutes(4);
		assertFalse(tenantCacheIsEmpty());

		PredictableTicker.moveTimeForwardByMinutes(2);
		assertTrue(tenantCacheIsEmpty());
	}

	public void testAuthorizationIsAppliedToCachedResult() {
		// fill cache without authorization
		assertEquals(2, identityService.createTenantQuery().count());
		assertEquals(2, identityService.createTenantQuery().userMember(USER_ID_GUNNAR).count());

		processEngineConfiguration.setAuthorizationEnabled(true);
		identityService.setAuthenticatedUserId(USER_ID_JOHN);

		// same cache keys, but john has no READ permission on any tenant
		assertEquals(0, identityService.createTenantQuery().count());
		assertEquals(0, identityService.createTenantQuery().userMember(USER_ID_GUNNAR).count());
		// own tenants are still visible
		assertEquals(1, identityService.createTenantQuery().userMember(USER_ID_JOHN).count());
	}

	private static boolean tenantCacheIsEmpty() {
		CacheAwareKeycloakIdentityProviderPluginForTest.tenantQueryCache.cleanUp();
		return CacheAwareKeycloakIdentityProviderPluginForTest.tenantQueryCache.asMap().isEmpty();
	}
}
