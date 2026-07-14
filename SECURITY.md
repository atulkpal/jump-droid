# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in Jump Droid, please report it privately by opening a GitHub issue with the label `security`.

**Do not** create a public issue for security vulnerabilities.

We will acknowledge receipt within 48 hours and provide a timeline for a fix.

## Scope

Security issues include, but are not limited to:

- Remote code execution vulnerabilities
- Data exposure or privacy violations
- Credential or token leaks in the repository
- Malicious code in dependencies
- Vulnerabilities in the build or release pipeline

## Supported Versions

| Version | Supported |
|---------|-----------|
| v1.5.x | ✅ |
| Older versions | ❌ |

## Security Practices

- No hardcoded credentials or secrets in the repository
- Signing credentials are resolved via environment variables or gitignored file
- APK and AAB are signed with a release keystore (not committed)
- Dependencies are regularly updated
- Crashlytics monitors runtime issues

## Response Process

1. Issue reported privately
2. Acknowledgment within 48 hours
3. Assessment and prioritization
4. Fix developed and tested
5. Patch release created
6. Issue disclosed after patch is published
