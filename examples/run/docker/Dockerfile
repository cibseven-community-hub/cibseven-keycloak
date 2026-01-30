# Get cibseven-run as base image
FROM cibseven/cibseven:run-2.1.0

# The Version of the Keycloak Identity Provider to use
ENV IDENTITY_PROVIDER_VERSION=2.1.0
# Note: If using SNAPSHOT versions (e.g., 2.1.0-SNAPSHOT), update the repository URL below
# from 'repository/community' to 'repository/community-snapshots' and use the absolute path with the specific timestamped version (e.g., 2.1.0-20251216.104226-1)

# Add Keycloak Identity Provider
USER camunda
RUN wget https://artifacts.cibseven.org/repository/community/org/cibseven/bpm/extension/cibseven-keycloak-run/$IDENTITY_PROVIDER_VERSION/cibseven-keycloak-run-$IDENTITY_PROVIDER_VERSION.jar -P configuration/userlib
