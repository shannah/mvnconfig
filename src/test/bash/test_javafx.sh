#!/bin/bash
set -e
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
source "$SCRIPTPATH/env.sh"

T="$TEST_DIR/test_javafx_app"
cd "$TEST_DIR"

mvn archetype:generate \
        -DarchetypeGroupId=org.openjfx \
        -DarchetypeArtifactId=javafx-archetype-simple \
        -DarchetypeVersion=0.0.3 \
        -DgroupId=ca.weblite.test \
        -DartifactId=test_javafx_app \
        -Dversion=1.0.0 \
        -Djavafx-version=17.0.1 \
        -DinteractiveMode=false


cat << EOF > "$T/mvnconfig.toml"
[run]
label="Run Project"
command=["\$mvn\$", "javafx:run"]
run=true

[jlink]
label="Build Project with jLink"
command=["\$mvn\$", "javafx:jlink"]
build=true

[update]
label="Update Dependencies"
command=["\$mvn\$", "package", "-U"]
group="Tools"
EOF

$MVNCONFIG -i "IntelliJ" "$T"
$MVNCONFIG -i "NetBeans" "$T"
$MVNCONFIG -i "VSCode" "$T"
