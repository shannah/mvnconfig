#!/bin/bash
set -e
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
source "$SCRIPTPATH/env.sh"

T="$TEST_DIR/test_netbeans_empty"
cd "$TEST_DIR"

mvn archetype:generate -DgroupId=ca.weblite.test -DartifactId=test_netbeans_empty -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false -DarchetypeVersion=1.4


cat << EOF > "$T/mvnconfig.toml"
[run]
label="Run Project"
command=["\$mvn\$", "verify", "-DskipTests", "-Dfoo.bar=baz"]
run=true

[build]
label="Build Project"
command=["\$mvn\$", "package"]
build=true

[special-run]
label="Special Run Project"
command=["\$mvn\$", "verify", "-DskipTests", "-Dspecial.run=true"]
run=true
group="Special"
EOF

$MVNCONFIG -i "NetBeans" "$T"
