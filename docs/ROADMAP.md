# JCube Roadmap

Living reference for what's left to build, kept up to date as work lands. Intended to be read
cold by a future chat/session with no other context — if a section here goes stale, update it
rather than leaving it to rot.

## Status snapshot

Steps 1–9 (data layer, app shell, cube/session management, StatsService, Timer with solve
history, Settings, the full visual/UX redesign, the comment/cleanup pass, and final packaging)
are all complete. There is no active "remaining build steps" list right now — what follows is
context for future work, not a backlog.

## Step 8 — Comments & cleanup (completed)

Non-obvious logic that got an explanatory comment pass:
- `ScrambleGenerator`'s move-cancellation/opposite-face-sorting algorithm — why moves are kept as
  repeated characters internally, what `sortOppositePairs`/`cancelFours` are each doing and why
  `validateScramble` runs both after every appended move rather than once at the end
- `DatabaseManager`'s per-OS data-directory resolution
- `TimeController`'s DNF short-circuit in `startRun` (why it can't just rely on `tick()`) and why
  `holdStartMs` resets on every press during `INSPECTION`, not just the first
- `SolveCsvService`'s +2 detection (the Time/P.1 gap is the only signal, since the format has no
  explicit penalty marker)
- The click-away/reselect-by-id pattern shared by `CubeViewController`, `SessionViewController`,
  and `TimerViewController` (via `ViewUtils.isDescendant`), and `MainWindow.buildCss`'s
  named-`{{placeholder}}` templating — both already carried their explanatory comments from when
  they were built during the visual/UX redesign, so no further work was needed there.

## Step 9 — Final packaging (completed)

- `scripts/package.sh` is filled in: builds the fat JAR (`mvn clean package`), smoke-tests it by
  actually launching it and confirming it stays running for a few seconds (not just trusting a
  green Maven build), then — on Windows, using `$JAVA_HOME/bin/jpackage` explicitly rather than
  whatever `jpackage` happens to resolve to on `PATH` (an unrelated older JDK's jpackage would
  bundle a runtime too old to run these class files) — builds a native app-image at
  `target/dist/JCube/JCube.exe` with the JCube icon embedded, using a generated `packaging/icon.ico`.
- The app-image bundles its own Java runtime, so `JCube.exe` runs standalone with no system-wide
  Java install required, and its DB path resolution (`%APPDATA%\JCube`) was confirmed working from
  that standalone launch.
- Deliberately produces an app-image, not an `.exe`/`.msi` installer: jpackage's installer types
  need the WiX Toolset installed separately, which the script doesn't install on its own (that's a
  real system-level install, not something to do silently). Revisit if a proper installer is
  wanted — just install WiX and change `--type app-image` to `--type exe` (or `msi`) in
  `scripts/package.sh`.
- Confirmed the packaged app needs an explicit `--java-options --enable-native-access=ALL-UNNAMED`
  passed to jpackage — the fat JAR's own manifest attribute handles this fine for a plain
  `java -jar` launch, but jpackage's generated launcher doesn't honor that manifest attribute the
  same way, so without the flag `JCube.exe` logs a native-access warning on every launch.
- README documents both the plain `mvn package` fat-JAR path and the `scripts/package.sh` native
  build, plus a note that running the JAR from a terminal (not double-click) may print the
  `sun.misc.Unsafe` deprecation warning from JavaFX's Marlin rasterizer — harmless, no
  manifest-attribute equivalent exists to silence it, and it's invisible on a double-click launch
  anyway since there's no console attached.

## Icon / branding

The JCube logo (`src/main/resources/images/JCubeLogo.png`, currently 16×16 — fine for a title bar,
but will look soft if used somewhere larger like the Alt-Tab switcher; a higher-res source would
help there) is wired in two places:
- `MainWindow.applyIcon(Stage)` sets it as the title bar/taskbar icon for the main window and both
  modal dialogs (`NameDialogController`, `AddStatDialogController`).
- `packaging/icon.ico` (generated from the same PNG) is what jpackage embeds into `JCube.exe`, so
  it's also what shows in File Explorer / when pinned to the taskbar or Start menu.

## Settings inventory — implemented vs candidate

**Implemented:**
- Full per-role custom color scheme (9 roles: background, surface, primary/secondary text, accent,
  button, button hover, danger, border), with one-click Dark/Light presets, hot-applied everywhere
  including dialogs
- Show scramble during solves
- Confirm before deleting cubes/sessions/solves
- Spacebar mechanics (WCA hold-to-arm vs. Simple)
- Inspection timing (WCA 15s/17s penalties vs. Simple/untimed)
- Customizable stats list on the timer screen (any `aoN` / Mean / PB, add/remove freely)
- Decimal places shown in times (0–3, default 2), applied everywhere a time is rendered

**Candidate settings raised but explicitly declined (do not build unless asked again):**
- Per-cube scramble type/algorithm (declined — scrambling stays 3x3-only for now)
- Jump to last-used session on opening a cube (declined)

**Candidate settings not yet raised — surface these to the user before building, don't assume:**
- WCA hold-to-arm threshold as a configurable duration (currently a hardcoded 500ms constant in
  `TimeController.WCA_HOLD_THRESHOLD_MS`)
- Inspection countdown length itself configurable (currently hardcoded 15s/17s WCA constants)
- Sound/audio cues (inspection warning beeps, solve-complete sound) — not implemented at all
- Keyboard shortcut remapping beyond spacebar (e.g., a dedicated key for +2/DNF instead of only
  clicking buttons)
- Default sort order or filtering for the solve history list

Extensibility note: adding a new setting is currently cheap and consistent — one field on
`Settings.java`, one `ALTER TABLE settings ADD COLUMN ...` line in `schema.sql` (already tolerant
of being re-run every startup via `DatabaseManager`'s duplicate-column handling), one read/write
line in `SettingsDB`, one control in `SettingsView.fxml`. No generic key-value settings store was
built — deliberate, to avoid indirection nobody asked for. Revisit only if the number of settings
grows large enough that this per-setting boilerplate becomes the actual bottleneck.

## Visual/UX redesign — completed

Shipped in full, well beyond the original CSS/FXML-only scope it was first scoped as:
- Cubes ("Home"), Sessions, and Timer screens rebuilt around a consistent left-list/right-detail
  (or right-timer) layout, with a fixed-height header bar across all three so the panel doesn't
  shift between screens
- Timer is the visually dominant element on its screen; solve history moved to the left panel with
  per-solve rolling stats (ao5/ao12/etc. *as of that solve*) shown inline, number de-emphasized and
  time bolded for scannability
- Single-click a cube/session/solve to see its details fill the main pane (general app info /
  session-specific guidance shown by default); double-click (cube/session) to drill in; click
  anywhere else, or another list item, or any page button, returns to the default view
- +2/DNF now keep the recorded time visible with a `(+2)`/`(DNF)` marker instead of DNF blanking
  the time entirely
- Global top nav bar (persistent back arrow + Home + Settings tabs) backed by a real navigation
  history stack in `AppController`, replacing per-screen Back buttons
- Right-click context menus (Rename/Delete, +2/DNF/Delete) replacing inline per-row buttons
  everywhere
- Full custom color scheme system (see Settings inventory above) replacing the old static
  dark.css/light.css swap
- Footer watermark ("Alex James © 2026"), centered, on every page
- Window/taskbar icon (see Icon / branding above)

## Known simplifications / minor adjustments worth revisiting

Not blockers, just things flagged along the way that a future pass might want to fix:

- **No comment field on solves.** The CSV import format has a Comment column; JCube's schema has
  no equivalent, so comments are silently dropped on import and always written empty on export.
- **Solve history has no pagination/virtualization concerns tested at scale.** JavaFX `ListView`
  virtualizes rendering automatically, so this is probably fine even for large sessions (tested
  informally with real import data), but never explicitly load-tested with truly huge sessions.
- **Each modal dialog (`NameDialogController`, `AddStatDialogController`) is its own `Stage`/`Scene`**
  rather than a shared dialog framework. Fine functionally; a design pass might consolidate this.
- **No undo for delete.** Deleting a cube/session/solve (confirmed or not, depending on the
  setting) is immediate and permanent — no soft-delete or undo buffer.
- **CSV import assumes the exact 6-column format given.** No column-header-based flexible parsing;
  if a different tool's export has columns in a different order, it won't parse. Fine for now
  since only one format was specified, but worth knowing if a second import source ever comes up.
- **No installer, just an app-image.** See Step 9 above — a proper `.exe`/`.msi` installer needs
  the WiX Toolset installed on the build machine, which hasn't been done.
- **Logo source is only 16×16.** Fine for the title bar; a higher-res version would help anywhere
  the icon gets scaled up.

## Difficulty guide for future changes

Pure visual restyling (spacing, colors already exposed via the color scheme system, fonts) is
low-risk and mostly CSS-template/FXML-only. Adding a genuinely new setting or minor behavioral
tweak is also low-to-medium effort and follows the established patterns throughout the codebase
(see the Extensibility note above). Anything that changes what data is stored (schema) or how core
interactions work (the timer state machine, navigation history stack, click-away/selection logic)
is the higher-effort/higher-risk category. Packaging changes (moving to a real installer, signing,
auto-update) are their own category — self-contained, but each needs its own tooling (WiX,
codesigning certs, an update-check mechanism) not currently present in this repo.
