# JCube

JCube is a personal desktop speedcubing timer and statistics tracker built in Java.
It was born out of a re-obsession with cubing and a desire to have a dedicated local
app for tracking personal progress — something more personal and purpose-built than
existing websites or mobile apps, and one that lives entirely on my own machine.

> **This project is a work in progress.** Features are being added incrementally
> and the application is not yet in a finished state.

---

## What it is

JCube is a standalone desktop application that lets you create and manage cube profiles,
time your solves, and track session statistics over time. Rather than relying on a
web app or phone, everything runs locally on your machine with no internet connection
required.

The goal is a clean, fast, distraction-free timer that I personally want to use.

---

## Features (planned / in progress)

- Create and manage multiple named cube profiles
- Session-based solve tracking per cube
- Scramble generation
- Session statistics (ao5, ao12, mean, PB)
- Inspection timer
- Dark and light theme
- Settings per session
- Full offline support — no account, no internet, no tracking

---

## Cube support

JCube supports any cube or puzzle you want to track. Cube profiles are user-defined
by name rather than locked to a predefined list, so whether you're timing a 3x3,
a Pyraminx, a Clock, or anything else, you can create a profile for it.

---

## How it works

JCube runs as a fat JAR — a single self-contained executable file that bundles all
dependencies. No installation required beyond having **Java 25 or newer** on your
machine — the JAR won't run on an older Java (you'll see an `UnsupportedClassVersionError`
if you try).

Run
```bash
java -jar jcube.jar
```
or double click the jcube.jar file.

> Running the JAR from a terminal may print a `WARNING: sun.misc.Unsafe::allocateMemory
> has been called...` message. That's JavaFX's own Marlin rasterizer, not JCube's code,
> and it's harmless — there's no manifest-attribute equivalent to silence it the way the
> native-access warning is silenced, and it's invisible anyway when the JAR is launched
> by double-clicking rather than from a console.

A native Windows build (a `JCube.exe` with a bundled runtime — no separate Java install
needed to run it, and the JCube icon shown in File Explorer / the taskbar) can be built
with `scripts/package.sh`; see [Building from source](#building-from-source).

Solve data is stored locally in an embedded SQLite database, meaning your times
stay on your machine and the app works completely offline.

---

## Tech stack

- Java 25
- JavaFX 23 — desktop UI
- SQLite via sqlite-jdbc — local embedded database
- Maven — build and dependency management

---

## Project status

JCube is actively being developed as a personal project. The architecture and
core data layer are being built out first, with the UI to follow. Expect
frequent changes to the structure and features as the project evolves.

---

## Building from source

Requirements: Java 25+, Maven 3.9+
```bash
git clone https://github.com/Alex-James04/JCube.git
cd JCube
mvn package
java -jar target/jcube-1.0-SNAPSHOT.jar
```

To also build the native Windows app (`target/dist/JCube/JCube.exe`), which bundles its
own Java runtime so it runs without a system-wide Java install and carries the JCube
icon, run `scripts/package.sh` instead of `mvn package` directly. It builds the fat JAR,
smoke-tests that it actually launches, and then packages the native app via `jpackage`
(requires JDK 25 — set `JAVA_HOME` to it if it isn't the JDK earlier on `PATH`, since
jpackage must come from the same JDK the app was compiled with). This currently produces
an app-image (a runnable folder), not a `.msi`/`.exe` installer — that needs the WiX
Toolset installed separately, which the script doesn't do on its own.

---

*Built primarily for personal.*