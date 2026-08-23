# Contributing to StreamForge Elasticsearch Sink

Thank you for your interest in contributing.

## Development environment

The project uses:

- Java 17
- Gradle Wrapper
- Kafka Connect compatible development environment
- Elasticsearch 9.x for integration testing

## Before submitting changes

Please:

1. Run existing tests.
2. Explain the motivation for the change.
3. Keep changes focused.
4. Avoid unrelated refactoring.

For documentation changes, verify commands and configuration names against the
implementation, keep examples free of real credentials, and update links when
files move. Do not change connector defaults or deployment behavior in a docs
pull request.

## Pull requests

Pull requests should include:

- clear description
- testing information
- compatibility impact

Small, focused pull requests are easier to review. A maintainer may ask for a
reproduction case or an integration smoke test when a change affects retry,
offset, Elasticsearch, or deployment behavior.

## Reporting issues

Include the connector version, Kafka Connect and Elasticsearch versions,
deployment mode, sanitized configuration keys, task status, and relevant log
errors. Remove passwords, tokens, private URLs, and personal data before
posting. Security-sensitive reports should follow [SECURITY.md](docs/SECURITY.md).

## Code style

Prefer simple, maintainable implementations over unnecessary abstraction.

## Compatibility

Changes affecting:

- connector behavior
- offset handling
- retry logic
- Elasticsearch interaction

require additional review because they can affect existing deployments.
