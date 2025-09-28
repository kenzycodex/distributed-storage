# Versioning Strategy

This document describes the versioning strategy used for DistributedStorage.

## Version Format

We follow [Semantic Versioning (SemVer)](https://semver.org/) with the format `MAJOR.MINOR.PATCH`:

- **MAJOR**: Incremented for incompatible API changes
- **MINOR**: Incremented for backwards-compatible functionality additions
- **PATCH**: Incremented for backwards-compatible bug fixes

## Version Management

### Automated Versioning

The project uses automated version management through:

1. **Version Manager Script**: `scripts/version-manager.sh`
2. **GitHub Actions**: Automatic version tagging on main branch merges
3. **Maven Versions Plugin**: Updates all pom.xml files consistently

### Version Manager Usage

```bash
# Show current version
./scripts/version-manager.sh current

# Increment patch version (1.0.0 → 1.0.1)
./scripts/version-manager.sh patch

# Increment minor version (1.0.1 → 1.1.0)
./scripts/version-manager.sh minor

# Increment major version (1.1.0 → 2.0.0)
./scripts/version-manager.sh major
```

## Release Process

### 1. Feature Development
- Create feature branch: `feature/description`
- Develop and test changes
- Create pull request to `main`

### 2. Version Increment
When merging to `main`, determine version bump:

- **Patch**: Bug fixes, documentation updates, minor improvements
- **Minor**: New features, API additions (backwards compatible)
- **Major**: Breaking changes, API removals/changes

### 3. Automated Release
On push to `main`:
1. CI/CD pipeline runs tests and builds
2. Automatic version tag creation
3. Docker images published to registry
4. GitHub release created with artifacts

### 4. Manual Release (if needed)
```bash
# Update version manually
./scripts/version-manager.sh minor

# Push tag to trigger release
git push origin v1.1.0
```

## Branch Strategy

- **main**: Production-ready code, triggers releases
- **develop**: Integration branch (optional)
- **feature/\***: Feature development branches
- **hotfix/\***: Critical bug fixes
- **release/\***: Release preparation branches

## Version Tags

Git tags follow the format:
- `v1.0.0` - Release versions
- `v1.0.0-rc.1` - Release candidates
- `v1.0.0-beta.1` - Beta releases
- `v1.0.0-alpha.1` - Alpha releases

## Docker Image Versioning

Docker images are tagged with:
- **Latest**: `kenzycodex/distributed-storage-loadbalancer:latest`
- **Version**: `kenzycodex/distributed-storage-loadbalancer:1.0.0`
- **SHA**: `kenzycodex/distributed-storage-loadbalancer:abc1234`

## Backwards Compatibility

### Major Version (Breaking Changes)
- API endpoint changes
- Configuration format changes
- Database schema changes requiring migration
- Java version requirement changes

### Minor Version (New Features)
- New API endpoints
- New configuration options
- New load balancing strategies
- Performance improvements

### Patch Version (Bug Fixes)
- Bug fixes
- Security patches
- Documentation updates
- Dependency updates (compatible)

## Deprecation Policy

Before removing features in a major version:
1. Mark as deprecated in minor version
2. Add deprecation warnings
3. Document migration path
4. Provide at least one minor version notice

## Examples

### Version History
```
v1.0.0 - Initial release with core functionality
v1.1.0 - Added file metadata persistence
v1.2.0 - Added authentication and RBAC
v1.2.1 - Fixed security vulnerability
v1.3.0 - Added file replication
v2.0.0 - API redesign, breaking changes
```

### Breaking Changes (Major)
```java
// v1.x.x API
@PostMapping("/upload")
public ResponseEntity<String> upload(MultipartFile file)

// v2.0.0 API (breaking change)
@PostMapping("/files")
public ResponseEntity<FileResponse> createFile(FileRequest request)
```

### New Features (Minor)
```java
// v1.1.0 - Added new endpoint (non-breaking)
@GetMapping("/files/{id}/metadata")
public ResponseEntity<FileMetadata> getMetadata(@PathVariable Long id)
```

## Version File Locations

- `VERSION` - Main version file
- `pom.xml` - Maven project version
- `storage-node/pom.xml` - Storage node version
- `CHANGELOG.md` - Human-readable change log

## Automation

The versioning process is automated through:

1. **GitHub Actions**: `.github/workflows/ci.yml`
2. **Version Script**: `scripts/version-manager.sh`
3. **Maven Plugins**: Versions plugin for dependency management
4. **Docker Labels**: Images tagged with version metadata

## Best Practices

1. **Always update CHANGELOG.md** with user-facing changes
2. **Test thoroughly** before version increments
3. **Use semantic commit messages** for automatic changelog generation
4. **Tag important milestones** even if not formal releases
5. **Document breaking changes** clearly in release notes

## Monitoring Versions

- **GitHub Releases**: Track all versions and downloads
- **Docker Hub**: Monitor image pulls by version
- **Metrics**: Version-specific usage analytics
- **Dependency Scanning**: Security alerts by version