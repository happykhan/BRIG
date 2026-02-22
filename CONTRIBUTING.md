# Contributing to BRIG

Thank you for your interest in contributing to BRIG! Here's how you can help.

## Reporting Issues

Please report bugs and request features on the [GitHub Issues page](https://github.com/happykhan/BRIG/issues).

When reporting a bug, include:

- Your operating system and Java version
- Steps to reproduce the issue
- Any error messages or log output

## Development Setup

BRIG requires:

- **Java 25** (or later)
- **Maven** for building

To build from source:

```bash
git clone https://github.com/happykhan/BRIG.git
cd BRIG
mvn clean package -q
```

The built JAR will be at `target/BRIG.jar`.

## Pull Request Workflow

1. Fork the repository
2. Create a feature branch from `master`
3. Make your changes
4. Ensure `mvn clean package -q` builds without errors
5. Run `mvn test` and confirm all tests pass
6. Submit a pull request against `master`

## Code Style

- Follow existing code conventions in the project
- Keep changes focused and minimal
