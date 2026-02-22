# Changelog

## v1.0.0

- **CLI mode**: BRIG can now be run entirely from the command line without a GUI. Provide a reference and sequence folder as arguments to generate images non-interactively.
- **Auto BLAST download**: BRIG automatically downloads BLAST+ from NCBI if it is not found on the system.
- **Native installers**: macOS (DMG) and Windows (MSI) packages that bundle a JRE, so users don't need to install Java separately.
- **Java 25**: Updated to build and run on Java 25.
- **Maven build**: Migrated from Ant to Maven for dependency management and builds.
- **MkDocs documentation**: Full documentation site built with Material for MkDocs.
- **SLF4J logging**: Replaced custom logging with SLF4J + Logback.
- **Smart file chooser**: File dialogs now remember the last used directory and default to sensible locations based on selected files.
