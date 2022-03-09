#!/bin/bash
set -e
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
source "$SCRIPTPATH/env.sh"

T="$TEST_DIR/test_vscode_empty"
cd "$TEST_DIR"

mvn archetype:generate -DgroupId=ca.weblite.test -DartifactId=test_vscode_empty -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false -DarchetypeVersion=1.4


cat << EOF > "$T/mvnconfig.toml"
[run]
label="Run Project"
command=["\$mvn\$", "verify", "-DskipTests", "-Dfoo.bar=baz"]
run=true

[build]
label="Build Project"
command=["\$mvn\$", "package"]
build=true
EOF

$MVNCONFIG -i "VSCode" "$T"
