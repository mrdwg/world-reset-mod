#!/bin/bash
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/default-java}
DIR="$( cd "$( dirname "$0" )" && pwd )"
exec "$DIR/gradle/wrapper/gradle-wrapper.jar" "$@"
