#!/bin/bash
##############################################
#
# Must be invoked on master or the tip of an release branch
#
# Parameters:
#
# 1. Release version, overrides the version calculated from Maven pom. Optional.
# 
##############################################

VERSION_PARAM=${1}

set -u
set -e

. ./calculate_versions.sh

check_status

calculate_versions
echo "Patch Branch: ${REL_BRANCH}"
echo "Release Version: ${RELEASE_VERSION}"
echo "Next Patch Version: ${NEXT_PATCH_VERSION}"
echo "Next Development Version: ${NEXT_DEV_VERSION}"

#Check for existing tag
TEST_TAG=$(git tag -l "${RELEASE_VERSION}")
if [ -n "${TEST_TAG}" ]; then
	echo
	echo "Tag ${RELEASE_VERSION} exists already, aborting"
	exit
fi

git tag "${VERSION_START_TAG}"

REMOTE=$(git config --get "branch.${BRANCH}.remote")

# Create release branch for new release
if [ "${BRANCH}" = "master" ]; then
	git branch "${REL_BRANCH}" master
	git config "branch.${REL_BRANCH}.remote" "${REMOTE}"
	git config "branch.${REL_BRANCH}.merge" "refs/heads/${REL_BRANCH}"
	git config "branch.${REL_BRANCH}.rebase" "$(git config --get branch.${BRANCH}.rebase)"
	git checkout "${REL_BRANCH}"
fi
# Set the version to release version, commit and set appropriate tag
. ./update_version.sh ${RELEASE_VERSION}
git add ..
git commit -m "Set version to ${RELEASE_VERSION}"
git tag "${RELEASE_VERSION}"

# Set the version on release branch to next patch version and commit
. ./update_version.sh "${NEXT_PATCH_VERSION}.qualifier"
git add ..
git commit -m "Set next patch version to ${NEXT_PATCH_VERSION}"

if [ "${BRANCH}" = "master" ]; then
	git checkout master
	. ./update_version.sh "${NEXT_DEV_VERSION}.qualifier"
	git add ..
	git commit -m "Set next development version to ${NEXT_DEV_VERSION}"
fi

git tag -d "${VERSION_START_TAG}"

echo
git log -n 4 --graph --decorate --oneline master "${REL_BRANCH}"

echo
echo "Changes have been applied to local repository"
echo "To push, issue the following commands:"
echo
if [ "${BRANCH}" = "master" ]; then
	echo "git push -u ${REMOTE} ${REL_BRANCH}"
	echo "git push ${REMOTE} tag ${RELEASE_VERSION}"
	echo "git push ${REMOTE} master"
else
	echo "git push ${REMOTE} ${REL_BRANCH}"
	echo "git push ${REMOTE} tag ${RELEASE_VERSION}"
fi