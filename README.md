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
dependencies. No installation required beyond having Java on your machine.

Run
```bash
java -jar jcube.jar
```
or double click the jcube.jar file.

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

Requirements: Java 17+, Maven 3.9+
```bash
git clone https://github.com/yourname/jcube.git
cd jcube
mvn package
java -jar target/jcube-1.0-SNAPSHOT.jar
```

---

*Built primarily for personal.*