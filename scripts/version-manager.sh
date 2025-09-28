#!/bin/bash

# Version Manager for DistributedStorage
# Usage: ./scripts/version-manager.sh [major|minor|patch|current]

set -e

VERSION_FILE="VERSION"
CURRENT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

# Get current version
get_current_version() {
    if [[ -f "$VERSION_FILE" ]]; then
        cat "$VERSION_FILE"
    else
        echo "1.0.0"
    fi
}

# Parse version into components
parse_version() {
    local version=$1
    IFS='.' read -r major minor patch <<< "$version"
    echo "$major $minor $patch"
}

# Increment version
increment_version() {
    local type=$1
    local current_version=$(get_current_version)
    read -r major minor patch <<< $(parse_version "$current_version")

    case $type in
        major)
            major=$((major + 1))
            minor=0
            patch=0
            ;;
        minor)
            minor=$((minor + 1))
            patch=0
            ;;
        patch)
            patch=$((patch + 1))
            ;;
        *)
            error "Invalid version type: $type. Use major, minor, or patch"
            ;;
    esac

    echo "$major.$minor.$patch"
}

# Update version in files
update_version_files() {
    local new_version=$1

    log "Updating version to $new_version in project files..."

    # Update VERSION file
    echo "$new_version" > "$VERSION_FILE"

    # Update pom.xml files
    mvn versions:set -DnewVersion="$new_version" -DgenerateBackupPoms=false
    cd storage-node && mvn versions:set -DnewVersion="$new_version" -DgenerateBackupPoms=false && cd ..

    # Update CHANGELOG.md
    local today=$(date +%Y-%m-%d)
    if [[ -f "CHANGELOG.md" ]]; then
        # Create backup
        cp CHANGELOG.md CHANGELOG.md.bak

        # Add new version entry
        {
            head -n 5 CHANGELOG.md
            echo ""
            echo "## [v$new_version] - $today"
            echo ""
            echo "### Added"
            echo "- Version bump to $new_version"
            echo ""
            tail -n +6 CHANGELOG.md
        } > CHANGELOG.md.tmp

        mv CHANGELOG.md.tmp CHANGELOG.md
        rm CHANGELOG.md.bak
    fi

    success "Version updated to $new_version"
}

# Create git tag
create_git_tag() {
    local version=$1
    local tag="v$version"

    log "Creating git tag $tag..."

    if git rev-parse "$tag" >/dev/null 2>&1; then
        warning "Tag $tag already exists"
        return
    fi

    git add .
    git commit -m "Bump version to $version" || true
    git tag -a "$tag" -m "Release version $version"

    success "Created git tag $tag"
    log "To push the tag: git push origin $tag"
}

# Main function
main() {
    local action=${1:-current}

    case $action in
        current)
            local current=$(get_current_version)
            echo "Current version: $current"
            ;;
        major|minor|patch)
            local current=$(get_current_version)
            local new_version=$(increment_version "$action")

            log "Current version: $current"
            log "New version: $new_version"

            read -p "Do you want to update to version $new_version? (y/N): " -n 1 -r
            echo

            if [[ $REPLY =~ ^[Yy]$ ]]; then
                update_version_files "$new_version"

                read -p "Create git tag? (y/N): " -n 1 -r
                echo

                if [[ $REPLY =~ ^[Yy]$ ]]; then
                    create_git_tag "$new_version"
                fi
            else
                log "Version update cancelled"
            fi
            ;;
        help|--help|-h)
            cat << EOF
Version Manager for DistributedStorage

Usage: $0 [COMMAND]

Commands:
  current    Show current version
  major      Increment major version (x.0.0)
  minor      Increment minor version (x.y.0)
  patch      Increment patch version (x.y.z)
  help       Show this help message

Examples:
  $0 current     # Show current version
  $0 patch       # Increment patch version
  $0 minor       # Increment minor version
  $0 major       # Increment major version

The script will:
1. Update VERSION file
2. Update Maven pom.xml files
3. Update CHANGELOG.md
4. Optionally create git commit and tag
EOF
            ;;
        *)
            error "Unknown command: $action. Use 'help' for usage information"
            ;;
    esac
}

# Check if running from correct directory
if [[ ! -f "pom.xml" ]] || [[ ! -d "storage-node" ]]; then
    error "Please run this script from the project root directory"
fi

main "$@"