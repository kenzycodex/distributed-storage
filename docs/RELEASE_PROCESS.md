# Release Process

This document outlines the process for creating and publishing releases of the DistributedStorage project. It serves as a reference for project maintainers to ensure consistent and reliable releases.

## Table of Contents

- [Release Schedule](#release-schedule)
- [Versioning Scheme](#versioning-scheme)
- [Release Types](#release-types)
- [Release Preparation](#release-preparation)
- [Release Execution](#release-execution)
- [Post-Release Activities](#post-release-activities)
- [Hotfix Process](#hotfix-process)

## Release Schedule

- **Major Releases**: Planned on a quarterly basis, or as needed for significant changes
- **Minor Releases**: Monthly, or as needed when new features are ready
- **Patch Releases**: As needed for bug fixes and security updates
- **Release Candidates**: Published 1-2 weeks before planned major/minor releases

## Versioning Scheme

DistributedStorage follows [Semantic Versioning](https://semver.org/) (SemVer):

- **MAJOR version (X.0.0)**: Incompatible API changes
- **MINOR version (0.X.0)**: New functionality in a backward compatible manner
- **PATCH version (0.0.X)**: Backward compatible bug fixes

Additional labels for pre-release versions:
- **Alpha**: `1.0.0-alpha.1`
- **Beta**: `1.0.0-beta.1`
- **Release Candidate**: `1.0.0-rc.1`

## Release Types

### Standard Releases

Standard releases follow the full release cycle and are thoroughly tested.

### Hotfix Releases

Hotfix releases address critical issues (security vulnerabilities, serious bugs) in the current release and follow an expedited process.

## Release Preparation

### 1. Release Planning

Start 1-2 weeks before the intended release date:

- [ ] Review and prioritize issues and pull requests
- [ ] Create or update milestone for the release
- [ ] Assign relevant issues to the milestone
- [ ] Communicate release timeline to contributors

### 2. Code Freeze

- [ ] Announce code freeze for upcoming release
- [ ] Merge only bug fixes, no new features
- [ ] Ensure all tests pass on the develop branch
- [ ] Verify documentation is up-to-date

### 3. Changelog Preparation

- [ ] Update CHANGELOG.md with all notable changes since the last release
- [ ] Group changes into categories:
    - Added: New features
    - Changed: Changes in existing functionality
    - Deprecated: Soon-to-be removed features
    - Removed: Removed features
    - Fixed: Bug fixes
    - Security: Vulnerability fixes
- [ ] Include issue/PR numbers and credit contributors

### 4. Version Update

- [ ] Update version numbers in:
    - pom.xml
    - README.md (if version is mentioned)
    - Any other files referencing the version number
- [ ] Update documentation if necessary

## Release Execution

### 1. Create Release Branch

For major and minor releases:

```bash
git checkout develop
git pull origin develop
git checkout -b release/vX.Y.Z
git push origin release/vX.Y.Z
```

For patch releases, branch from main:

```bash
git checkout main
git pull origin main
git checkout -b release/vX.Y.Z
git push origin release/vX.Y.Z
```

### 2. Final Testing

- [ ] Run the full test suite
- [ ] Perform manual testing of key functionality
- [ ] Deploy to staging environment if applicable
- [ ] Address any critical issues found

### 3. Release Approval

- [ ] Create a pull request from the release branch to main
- [ ] Request review from at least one other maintainer
- [ ] Ensure all CI checks pass
- [ ] Get approval before proceeding

### 4. Merge and Tag

After approval:

- [ ] Merge the release branch into main
- [ ] Tag the release:
  ```bash
  git checkout main
  git pull origin main
  git tag -a vX.Y.Z -m "Release vX.Y.Z"
  git push origin vX.Y.Z
  ```

### 5. Create GitHub Release

- [ ] Go to Releases section in GitHub
- [ ] Click "Create a new release" or "Draft a new release"
- [ ] Select the tag you just created
- [ ] Set the title to "vX.Y.Z" or something more descriptive
- [ ] Copy the relevant section from CHANGELOG.md as the description
- [ ] Attach any binaries or artifacts (JARs, etc.)
- [ ] Mark as pre-release if applicable (RC, beta, alpha)
- [ ] Publish release

### 6. Merge Changes to Develop

```bash
git checkout develop
git pull origin develop
git merge main
git push origin develop
```

## Post-Release Activities

### 1. Announcements

- [ ] Post announcement in GitHub Discussions
- [ ] Update documentation website if applicable
- [ ] Announce in other relevant channels/forums

### 2. Artifact Publication

- [ ] Ensure artifacts are published to Maven Central (if applicable)
- [ ] Update Docker images (if applicable)

### 3. Next Development Cycle

- [ ] Create new milestone for the next planned release
- [ ] Update version in develop branch to next development version (e.g., X.Y.Z-SNAPSHOT)
- [ ] Review and update roadmap if necessary

## Hotfix Process

For critical issues in the released version:

### 1. Create Hotfix Branch

```bash
git checkout main
git pull origin main
git checkout -b hotfix/vX.Y.Z+1
```

### 2. Fix the Issue

- [ ] Make minimal changes to address the issue
- [ ] Add tests to cover the fix
- [ ] Update CHANGELOG.md
- [ ] Increment patch version number

### 3. Review and Testing

- [ ] Create PR from hotfix branch to main
- [ ] Get thorough review
- [ ] Run full test suite

### 4. Release

- [ ] Follow expedited version of standard release process
- [ ] Merge to main and tag
- [ ] Create GitHub release
- [ ] Merge changes to develop as well

---

[//]: # (This release process should be reviewed and updated periodically as the project evolves and processes improve. It is important to maintain consistency and transparency in the release process to ensure a high-quality product and a positive experience for users and contributors.)