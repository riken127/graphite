# Releasing Graphite

Releases are tag-driven and publish signed artifacts through the Maven Central Publisher Portal.
`graphite-examples` is intentionally not published.

## One-time repository setup

1. Verify ownership of the `io.github.riken127` namespace in Maven Central.
2. Create a GitHub environment named `maven-central`.
3. Add environment secrets:
   * `CENTRAL_USERNAME` and `CENTRAL_PASSWORD` from a Central user token;
   * `MAVEN_GPG_PRIVATE_KEY`, containing an ASCII-armored private key;
   * `MAVEN_GPG_PASSPHRASE`.
4. Publish the corresponding public signing key to a commonly used key server.

The workflow has only `contents: write` permission so it can create the GitHub release after Central
confirms publication.

## Release procedure

1. Confirm `main` is green and the changelog's Unreleased section is complete.
2. Set the reactor version, for example:

   ```bash
   ./mvnw versions:set -DnewVersion=0.1.0 -DgenerateBackupPoms=false
   ```

3. Update `project.build.outputTimestamp`, consumer-test `graphite.version`, README coordinates, and
   changelog links for the release.
4. Run the full local checks:

   ```bash
   ./mvnw -Dgraphite.requireDocker=true clean verify
   ./mvnw -DskipTests -Djacoco.skip=true install
   ./mvnw -f consumer-tests/pom.xml -Dgraphite.version=0.1.0 verify
   ```

5. Commit the release preparation, then create and push an annotated tag matching the POM version:

   ```bash
   git tag -s v0.1.0 -m "Graphite 0.1.0"
   git push origin main v0.1.0
   ```

The release workflow rejects mismatched or snapshot versions, reruns the Docker-backed verification,
signs the POMs and binary/source/Javadoc artifacts, waits for Central publication, and creates the
GitHub release.

After publication, set the next snapshot version on `main` and move changelog entries into the
released section. Never reuse or move a published version tag.
