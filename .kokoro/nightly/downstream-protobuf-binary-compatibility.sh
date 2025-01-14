#!/bin/bash
# Copyright 2024 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -eo pipefail

# Comma-delimited list of repos to test with the local java-shared-dependencies
if [ -z "${REPOS_UNDER_TEST}" ]; then
  echo "REPOS_UNDER_TEST must be set to run downstream-protobuf-source-compatibility.sh"
  echo "Expects a comma-delimited list: i.e REPOS_UNDER_TEST=\"java-bigtable,java-bigquery\""
  exit 1
fi

# Version of Protobuf-Java runtime to compile with
if [ -z "${PROTOBUF_RUNTIME_VERSION}" ]; then
  echo "PROTOBUF_RUNTIME_VERSION must be set to run downstream-protobuf-source-compatibility.sh"
  echo "Expects a single Protobuf-Java runtime version i.e. PROTOBUF_RUNTIME_VERSION=\"4.28.3\""
  exit 1
fi

git clone https://github.com/lqiu96/cloud-opensource-java.git
pushd cloud-opensource-java
git checkout source-filter
mvn -B -ntp clean compile
pushd dependencies

for repo in ${REPOS_UNDER_TEST//,/ }; do # Split on comma
  # Perform source-compatibility testing on main (latest changes)
  git clone "https://github.com/googleapis/$repo.git" --depth=1
  pushd "$repo"
  mvn -B -ntp clean install -T 1C -DskipTests -Dclirr.skip

  # Match all artifacts that start with google-cloud (rules out proto and grpc modules)
  # Exclude any matches to BOM artifacts or emulators
  ARTIFACT_LIST=$(cat "versions.txt" | grep "^google-cloud" | grep -vE "(bom|emulator)" | tr '\n' ',')
  ARTIFACT_LIST=${ARTIFACT_LIST%,}

  echo "Found artifacts ${ARTIFACT_LIST}"
  popd

  for artifact in ${ARTIFACT_LIST//,/ }; do
    artifact_id=$(echo "${artifact}" | tr ':' '\n' | head -n 1)
    version=$(echo "${artifact}" | tr ':' '\n' | tail -n 1)

    maven_coordinates="com.google.cloud:${artifact_id}:${version}"
    echo "Using ${maven_coordinates}"

    # The `-s` argument filters the linkage check problems that stem from the artifact
    program_args="-r --artifacts ${maven_coordinates},com.google.protobuf:protobuf-java:${PROTOBUF_RUNTIME_VERSION} -s ${maven_coordinates}"
    echo "Linkage Checker Program Arguments: ${program_args}"
    mvn -B -ntp exec:java -Dexec.mainClass="com.google.cloud.tools.opensource.classpath.LinkageCheckerMain" -Dexec.args="${program_args}"
  done
done
popd
popd
