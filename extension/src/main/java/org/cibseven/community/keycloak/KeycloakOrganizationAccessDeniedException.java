package org.cibseven.community.keycloak;

import org.cibseven.bpm.engine.impl.identity.IdentityProviderException;

/**
 * Thrown in case Keycloak denies access (403) to an organization endpoint,
 * typically because the service account is missing the role {@code manage-realm}
 * required for the extended organization queries.
 * <p>
 * Unchecked, so that it can pass through the query cache computation. Handled by
 * {@link KeycloakIdentityProviderSession}, which logs a warning and returns no tenants.
 */
public class KeycloakOrganizationAccessDeniedException extends IdentityProviderException {

	/** This class' serial version UID. */
	private static final long serialVersionUID = -3457204718239058812L;

	/**
	 * Creates a new KeycloakOrganizationAccessDeniedException.
	 * @param message the message
	 * @param cause the original cause
	 */
	public KeycloakOrganizationAccessDeniedException(String message, Throwable cause) {
		super(message, cause);
	}

}
