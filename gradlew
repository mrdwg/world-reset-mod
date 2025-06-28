#!/bin/sh
##############################################################################
## Gradle start script for UN*X
##############################################################################
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/default-java}
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
exec "$DIR/gradle/wrapper/gradle-wrapper.jar" "$@"