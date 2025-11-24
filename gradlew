#!/usr/bin/env sh

# Gradle startup script
APP_HOME="$(cd "${0%/*}" && pwd -P)"
APP_NAME="Gradle"
APP_BASE_NAME=${0##*/}

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# OS specific support.
case "$(uname)" in
  CYGWIN*|MINGW*)
    APP_HOME=$(cygpath --path --mixed "$APP_HOME")
    CLASSPATH=$(cygpath --path --mixed "$CLASSPATH")
    ;;
 esac

DEFAULT_JVM_OPTS=""

JAVA_OPTS=""

if [ -z "$JAVA_HOME" ] ; then
  JAVA_CMD="java"
else
  JAVA_CMD="$JAVA_HOME/bin/java"
fi

exec "$JAVA_CMD" $DEFAULT_JVM_OPTS $JAVA_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
