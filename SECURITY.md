# Security policy

## Supported versions

Graphite has not published its first stable release. Until then, security fixes are made on `main`.
After releases begin, this table will identify the supported release lines.

| Version | Supported |
| --- | --- |
| `main` / latest prerelease | Yes |
| Older snapshots | No |

## Reporting a vulnerability

Do not open a public issue for a suspected vulnerability. Use GitHub's
[private vulnerability reporting](https://github.com/riken127/graphite/security/advisories/new) to
send a description, affected versions, reproduction steps, and any suggested mitigation.

You should receive an acknowledgement within seven days. Valid reports will be coordinated
privately until a fix and disclosure plan are ready. If private vulnerability reporting is
temporarily unavailable, contact the repository owner through the profile linked from the project
POM and avoid including exploit details in a public message.

Graphite's security scope includes query parameterization, identifier validation, credential
handling in the Spring Boot starter, dependency vulnerabilities, and release artifact integrity.
