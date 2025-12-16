# Get Camunda-run as base image
FROM cibseven/cibseven:run-2.1.0

# The Version of the Keycloak Identity Provider to use
ENV IDENTITY_PROVIDER_VERSION=2.0.0

# Add Keycloak Identity Provider
USER camunda
RUN wget https://artifacts.cibseven.org/repository/community/org/cibseven/bpm/extension/cibseven-keycloak-run/$IDENTITY_PROVIDER_VERSION/cibseven-keycloak-run-$IDENTITY_PROVIDER_VERSION.jar -P configuration/userlib
