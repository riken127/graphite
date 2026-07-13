# Versioning and compatibility policy

Graphite uses Semantic Versioning.

* `0.y.z`: the API is still stabilizing. Minor releases may break APIs when necessary, with changelog
  and migration guidance.
* `1.y.z` and later: breaking public API or behavior changes require a major release.
* Patch releases contain compatible fixes and documentation updates.

Public API includes documented public types in published modules, generated metamodel source shape,
configuration properties, BOM coordinates, and documented runtime behavior. Internal implementation
details, test fixtures, and the `graphite-examples` module are not compatibility commitments.

Deprecations should identify the replacement and remain for at least one minor release after 1.0.
Security fixes may require faster removal when retaining behavior would leave users exposed.

Every release must update `CHANGELOG.md`. A release containing user action items must add a migration
section or a versioned document under `docs/migrations/`. Binary API comparison will be introduced
after the first published baseline exists; until then, source review and external consumer projects
guard the public surface.
