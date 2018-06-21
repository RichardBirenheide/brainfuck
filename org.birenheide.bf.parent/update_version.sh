#!/bin/bash

set -u

NEXT_VERSION=${1}

mvn org.eclipse.tycho:tycho-versions-plugin:set-version -N -q -DnewVersion=${NEXT_VERSION} -DupdateVersionRangeMatchingBounds=true

echo "Version set to: ${NEXT_VERSION}"