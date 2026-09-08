#!/usr/bin/env bash
# Builds the fat JAR, smoke-tests that it actually launches (rather than trusting a green `mvn
# package` alone), and — on Windows, using JDK 25's own jpackage rather than whatever version
# happens to be earlier on PATH — produces a native .exe with the JCube icon embedded.
set -euo pipefail
cd "$(dirname "$0")/.."

JAR_NAME="jcube.jar"
JAR_PATH="target/$JAR_NAME"

echo "==> Building fat JAR (mvn clean package)..."
mvn -q clean package

if [ ! -f "$JAR_PATH" ]; then
    echo "ERROR: expected $JAR_PATH after mvn package, but it wasn't produced." >&2
    exit 1
fi

echo "==> Smoke-testing $JAR_PATH..."
java -jar "$JAR_PATH" &
JAR_PID=$!
sleep 4
if ! kill -0 "$JAR_PID" 2>/dev/null; then
    echo "ERROR: the JAR exited within 4 seconds instead of staying open — check for a startup crash." >&2
    exit 1
fi
kill "$JAR_PID" 2>/dev/null || true
wait "$JAR_PID" 2>/dev/null || true
echo "    JAR launched and stayed running — smoke test passed."

# Native packaging is Windows-only for now since that's the only platform this has been built and
# tested on; jpackage's --type/--icon options differ per OS (msi/exe + .ico on Windows, dmg/pkg +
# .icns on Mac, deb/rpm + .png on Linux), so extending this to other platforms needs its own icon
# assets and testing, not just a different --type flag.
#
# Produces an app-image (a folder containing JCube.exe + a bundled runtime) rather than an
# exe/msi installer: an installer needs the WiX Toolset installed separately, which this script
# won't install on its own. The app-image's JCube.exe already carries the embedded icon and is
# what actually shows up in File Explorer / when pinned — an installer is a nicer distribution
# format on top of that, not a requirement for the icon itself. Switch --type to exe or msi (and
# install WiX) if a real installer is wanted later.
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*)
        if [ -z "${JAVA_HOME:-}" ]; then
            echo "==> Skipping native packaging: JAVA_HOME is not set, and jpackage must come from" >&2
            echo "    the same JDK the app was compiled with (an unrelated jpackage earlier on" >&2
            echo "    PATH would bundle a runtime too old to run these class files)." >&2
            exit 0
        fi
        JPACKAGE="$JAVA_HOME/bin/jpackage"
        if [ ! -x "$JPACKAGE" ]; then
            echo "==> Skipping native packaging: no jpackage found at $JPACKAGE." >&2
            exit 0
        fi
        echo "==> Building native app-image with jpackage ($("$JPACKAGE" --version))..."
        rm -rf target/dist
        "$JPACKAGE" \
            --input target \
            --main-jar "$JAR_NAME" \
            --main-class Main \
            --name JCube \
            --app-version 1.0.0 \
            --icon packaging/icon.ico \
            --type app-image \
            --dest target/dist \
            --java-options "--enable-native-access=ALL-UNNAMED" \
            --java-options "--sun-misc-unsafe-memory-access=allow"
        # The fat JAR's own manifest already grants Enable-Native-Access when run directly via
        # `java -jar` (confirmed by the smoke test above producing no such warning) — but
        # jpackage's generated launcher doesn't honor that manifest attribute the same way, so the
        # equivalent --java-options flag has to be passed explicitly here or JCube.exe logs a
        # native-access warning on every launch.
        echo "    Native app-image written to target/dist/JCube/ (launch via target/dist/JCube/JCube.exe)"
        ;;
    *)
        echo "==> Skipping native packaging: only implemented for Windows so far."
        ;;
esac
