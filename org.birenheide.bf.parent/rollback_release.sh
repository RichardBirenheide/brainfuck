#!/bin/bash
##############################################
#
# Immediate rollback of a prepare_release.sh call.
# Do not call after subsequent changes to the git tree.
# Must be invoked on master or a release branch
#
# Parameters:
#
# 1. Version to delete, mandatory 
#
##############################################

set -u

VERSION_PARAM=${1}

. ./calculate_versions.sh

calculate_versions

check_status

#Check for existing tag
TEST_TAG=$(git tag -l "${RELEASE_VERSION}")
if [ -z "${TEST_TAG}" ]; then
	echo
	echo "Tag ${RELEASE_VERSION} does not exist, aborting"
	exit
fi

if [ "${BRANCH}" = "master" ]; then
	git reset --hard HEAD^1
	git tag -d "${RELEASE_VERSION}"
	git branch -D "${REL_BRANCH}"
else
	git tag -d "${RELEASE_VERSION}"
	git reset --hard HEAD^1
	git reset --hard HEAD^1
fi

echo
echo "Local changes rolled back"
