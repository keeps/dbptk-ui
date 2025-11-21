# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v3.3.0] - 2025-10-29

### Added

- Support for loading and browsing SIARD 2.2 (2983bbabe07c393ede327c0d7f3e0068405ca973)
- Added jquery as webjar (https://github.com/keeps/dbptk-ui/commit/5684811d5654cb0af46830f34437fe4547e83a6c)

### Fixed

- Fixed bug where clear search button would not show when using the new link to go from the database search panel to a table's search panel (a83bd9374dd613ba63dd39b89c54d6eecf74aaf7)
- Added new join filter parameter that is compatible with sharded collections for transform column support (https://github.com/keeps/dbptk-ui/commit/ec1863dcbd727cd1e841ae428222a5b77444380d)
- Fix NullPointerException in database creation error handling (https://github.com/keeps/dbptk-ui/commit/7d298b6c4bc2420e4da055295041d58ed2032d2d)
- Supporting old saved searches (https://github.com/keeps/dbptk-ui/commit/79866b2e1463b143fe8fd8309309662d6b581014)

### Changed

- Updated dbptk-developer version to 4.0.0
- Switched Archival Data column for Data Origin Timestamp column in the home page database overview table (3a90b6796b08f581855dda1a95411e601cdcd86f)
-  Several dependencies bump

[v3.3.0]: https://github.com/keeps/dbptk-ui/compare/v3.2.0...v3.3.0
