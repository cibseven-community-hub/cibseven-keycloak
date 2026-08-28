/*
 * Copyright CIB software GmbH and/or licensed to CIB software GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. CIB software licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cibseven.bpm.extension.keycloak.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Backward compatible alias for
 * {@link org.cibseven.community.keycloak.plugin.KeycloakIdentityProviderPlugin}.</p>
 *
 * <p>Kept only so that existing configurations referencing the old class name
 * (bpm-platform.xml, processes.xml, Spring Boot configuration) keep working after
 * the namespace change to <code>org.cibseven.community.keycloak</code>.</p>
 *
 * @deprecated as of 2.2.0, use
 *             {@link org.cibseven.community.keycloak.plugin.KeycloakIdentityProviderPlugin} instead.
 */
@Deprecated(since = "2.2.0", forRemoval = true)
public class KeycloakIdentityProviderPlugin extends org.cibseven.community.keycloak.plugin.KeycloakIdentityProviderPlugin {

	private static final Logger log = LoggerFactory.getLogger(KeycloakIdentityProviderPlugin.class);

	public KeycloakIdentityProviderPlugin() {
		log.warn("The class path org.cibseven.bpm.extension.keycloak.plugin.KeycloakIdentityProviderPlugin is deprecated "
				+ "as of version 2.2.0 and will be removed in a future release. Please migrate to the new class path "
				+ "org.cibseven.community.keycloak.plugin.KeycloakIdentityProviderPlugin instead. The old class path is "
				+ "maintained only for backward compatibility with existing configurations using it directly.");
	}
}
