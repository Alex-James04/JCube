# JCube

JCube is a personal desktop speedcubing timer and statistics tracker built in Java.
It was born out of a re-obsession with cubing and a desire to have a dedicated local
app for tracking personal progress — something more personal and purpose-built than
existing websites or mobile apps, and one that lives entirely on my own machine.

---

## What it is

JCube is a standalone desktop application that lets you create and manage cube profiles,
time your solves, and track session statistics over time. Rather than relying on a
web app or phone, everything runs locally on your machine with no internet connection
required.

The goal is a clean, fast, distraction-free timer that I personally want to use.

---

## Features

**Cubes & sessions**
- Create and manage any number of freely-named cube profiles (not locked to a predefined
  puzzle list — see [Cube support](#cube-support))
- Session-based solve tracking per cube, with rename/delete (right-click) on both cubes
  and sessions
- A Home screen and Sessions screen that show either a general/session-specific intro or
  the selected cube's/session's stats (best single, ao5, ao12, mean, solve count),
  depending on what's selected

**Timer**
- Configurable spacebar mechanics (WCA hold-to-arm vs. Simple) and inspection timing
  (WCA 15s/17s penalties vs. Simple/untimed)
- 3x3 scramble generation
- Solve history with per-solve rolling stats — ao5/ao12/etc. shown *as of that solve*, not
  just the current session-wide value — plus the current and best-ever value for each
  configured stat
- +2/DNF penalty editing (right-click a solve, or select it to edit from the main timer
  pane) that keeps the recorded time visible with a `(+2)`/`(DNF)` marker, rather than
  hiding what was actually recorded
- CSV import/export in the semicolon-delimited `No.;Time;Comment;Scramble;Date;P.1` format
  used by common third-party cube timers

**Settings**
- A fully custom, per-role color scheme (background, surface, primary/secondary text,
  accent, button, button hover, danger, border) with one-click Dark/Light presets — not
  just a theme toggle, every color is independently editable and hot-applies everywhere,
  including dialogs
- Customizable stats list on the timer screen (any `aoN` / Mean / PB, add or remove freely)
- Decimal places shown in times (0–3, default 2)
- Confirm-before-delete for cubes, sessions, and solves
- Show/hide scramble during solves

**Everything else**
- Full offline support — no account, no internet, no tracking; all data lives in a local
  SQLite database
- A custom JCube icon in the title bar, taskbar, and (for the native Windows build) File
  Explorer

---

## Cube support

JCube supports any cube or puzzle you want to track. Cube profiles are user-defined
by name rather than locked to a predefined list, so whether you're timing a 3x3,
a Pyraminx, a Clock, or anything else, you can create a profile for it. Scramble
generation itself is currently 3x3-only regardless of the cube profile's name.

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

Solve data is stored locally in an embedded SQLite database (`%APPDATA%\JCube` on
Windows), meaning your times stay on your machine and the app works completely offline.

---

## Tech stack

- Java 25
- JavaFX 23 — desktop UI
- SQLite via sqlite-jdbc — local embedded database
- Maven — build and dependency management
- jpackage — native Windows app-image packaging

---

## Project status

JCube's core feature set — cube/session management, the timer, solve history and
stats, settings, and packaging — is complete and in daily personal use. Development
continues incrementally on top of that (new settings, refinements, and whatever else
comes up from actually using it), rather than working toward an initial 1.0.

---

## Building from source

Requirements: Java 25+, Maven 3.9+
```bash
git clone https://github.com/Alex-James04/JCube.git
cd JCube
mvn package
java -jar target/jcube.jar
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

*Built primarily for personal use.*
