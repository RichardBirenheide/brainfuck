#!/bin/bash

set -u

NEXT_VERSION=${1}

mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=${NEXT_VERSION} -DupdateVersionRangeMatchingBounds=true