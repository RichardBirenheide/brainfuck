#!/bin/bash
##############################################
# Parameters:
#
# Variable VERSION_PARAM must be set.
#
##############################################

set -u
set -e

VERSION_START_TAG="new-version-start"

BRANCH=$(git rev-parse --abbrev-ref HEAD)

function check_status() {
	STATUS="$(git status --porcelain)"
	if [ -n "${STATUS}" ]; then
		echo
		echo "$STATUS"
		echo
		echo "There are uncommitted changes, aborting"
		exit
	fi
}

function calculate_versions() {
	if [ -n "${VERSION_PARAM}" ]; then
		RELEASE_VERSION="${VERSION_PARAM}"
	else
		# https://stackoverflow.com/questions/15880730/how-to-extract-the-gav-from-a-pom-xml-file-in-a-shell-script
		RAW_VERSION="$(mvn help:evaluate -N -Dexpression="project.version" | grep -v '\[')"
		# see https://stackoverflow.com/questions/428109/extract-substring-in-bash
		RELEASE_VERSION=$( echo ${RAW_VERSION} | sed "s/\(.\+\)-SNAPSHOT/\1/p" -n)
	fi 
	if [ -z "${RELEASE_VERSION}" ]; then
		echo
		echo "Script should start only on a SNAPSHOT version, but was: ${RAW_VERSION}"
		exit
	fi 
	
	# https://stackoverflow.com/questions/918886/how-do-i-split-a-string-on-a-delimiter-in-bash#918931
	IFS="." read -ra VERSION_PARTS <<< "${RELEASE_VERSION}"
	
	MAJOR="${VERSION_PARTS[0]}"
	MINOR="${VERSION_PARTS[1]}"
	MICRO="${VERSION_PARTS[2]}"
	
	if [ -z "${MAJOR}" ] || [ -z "${MINOR}" ] || [ -z "${MICRO}" ]; then
		echo
		echo "${RELEASE_VERSION} did not parse correctly to MAJOR.MINOR.MICRO, aborting"
		exit
	fi
	
	if [ "${BRANCH}" = "master" ]; then
		REL_BRANCH="rel-${MAJOR}.${MINOR}"
		NEXT_PATCH_VERSION="${MAJOR}.${MINOR}.$((MICRO + 1))"
		NEXT_DEV_VERSION="${MAJOR}.$((MINOR + 1)).0"
	else
		REL_BRANCH="${BRANCH}"
		NEXT_PATCH_VERSION="${MAJOR}.${MINOR}.$((MICRO + 1))"
		NEXT_DEV_VERSION="${RELEASE_VERSION}"
	fi
}