# BRIG v1.1.1

## Highlights

- **Multithreaded BLAST** (#42) — adds `-num_threads` to all BLAST calls; defaults to all available CPUs; configurable via `--threads <n>` CLI option or `blastThreads` XML profile attribute
- **Fix same-filename collision** (#53) — BLAST output `.tab` files are now prefixed with ring/sequence indices (`r{ring}s{seq}_`) so files with the same name in different directories never overwrite each other
- **Configurable GC skew/content window** (#56) — new `--gc-window <n>` CLI option and `gcWindow` XML attribute let users set the window size for GC calculations; falls back to auto-scaling when not set
- **Auto-release workflow** — merging a PR labelled `release` into `master` automatically bumps the patch version, creates a tag, and triggers the existing release pipeline to build DMG/MSI/JAR packages

## Other changes

- Bump `actions/upload-artifact` to v7, `actions/download-artifact` to v8
- Bump `maven-shade-plugin` from 3.6.1 to 3.6.2
- CI now installs BLAST+ via pixi and runs E2E tests
- Added `pixi.toml` for reproducible BLAST+ dependency management

## Downloads

| File | Description |
|------|-------------|
| `BRIG.jar` | Cross-platform fat JAR (GUI) |
| `brig-cli.jar` | Command-line interface JAR |
| `BRIG-1.1.0.dmg` | macOS installer |
| `BRIG-1.1.0.msi` | Windows installer |

---

# BRIG v1.0.0

First official release of BRIG (BLAST Ring Image Generator) with a modernised build and native installers.

## Highlights

- **Maven build** — migrated from legacy Ant scripts to a Maven-based build with reproducible dependency management
- **Native installers** — macOS DMG and Windows MSI produced via `jpackage`, no JRE installation required
- **CLI mode** — new headless `brig-cli.jar` for scripted/server-side usage alongside the GUI
- **Java 25** — updated to target the latest JDK
- **Documentation site** — Material for MkDocs documentation deployed to GitHub Pages
- **CI/CD** — GitHub Actions pipelines for continuous integration, docs deployment, and automated releases
- **Test suite** — JUnit 5 tests with JaCoCo code coverage

## Downloads

| File | Description |
|------|-------------|
| `BRIG.jar` | Cross-platform fat JAR (GUI) |
| `brig-cli.jar` | Command-line interface JAR |
| `BRIG-1.0.0.dmg` | macOS installer |
| `BRIG-1.0.0.msi` | Windows installer |

## What is BRIG?

BRIG generates circular comparison images of multiple genomes using BLAST. It visualises sequence similarity, GC content, read coverage, and custom annotations as concentric rings around a reference genome.
