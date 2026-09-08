# JCube Roadmap

Living reference for what's left to build, kept up to date as work lands. Intended to be read
cold by a future chat/session with no other context — if a section here goes stale, update it
rather than leaving it to rot.

## Status snapshot

Steps 1–7 (data layer, app shell, cube/session management, StatsService, Timer with solve
history, Settings, and the full visual/UX redesign) are functionally and visually complete.
What follows is what's left.

## Remaining build steps

### Step 8 — Comments & cleanup
An audit pass over the non-obvious logic specifically, not a fresh feature. Candidates:
- `ScrambleGenerator`'s move-cancellation/opposite-face-sorting logic
- `DatabaseManager`'s path resolution and the `ADD COLUMN` duplicate-column migration workaround
- `TimeController`'s state transitions (why DNF short-circuits `startRun` instead of only relying
  on `tick()`, why hold-start resets on every press during `INSPECTION`)
- `SolveCsvService`'s DNF/+2 detection rules (Time vs. P.1 columns)
- The click-away/reselect-by-id pattern shared by `CubeViewController`, `SessionViewController`,
  and `TimerViewController` (via `ViewUtils.isDescendant`) — why row re-selection has to be
  excluded from the generic click filter, and why action buttons clear selection explicitly
  rather than relying on filter timing
- `MainWindow.buildCss`'s named-`{{placeholder}}` template approach — worth a one-line note on why
  it replaced positional `String.formatted(%s...)` (a mismatched arg count crashed the app on
  launch once already)

### Step 9 — Final packaging
Not started. `mvn package` produces a fat JAR but it hasn't been smoke-tested on a clean directory
with no dev environment present. Needs:
- Confirm the `%APPDATA%\JCube` DB path resolves correctly with no JAVA_HOME/Maven on PATH
- Confirm double-clicking the JAR launches it (no console needed for `Enable-Native-Access`, since
  that's a manifest attribute, not a launch flag)
- `scripts/package.sh` exists but is an empty stub — decide whether to fill it in as a one-command
  build+smoke-test script, or drop it if `mvn package` alone is sufficient
- Optional: note in the README that running the JAR from a terminal (not double-click) may still
  print the `sun.misc.Unsafe` deprecation warning from JavaFX's Marlin rasterizer, since that flag
  has no manifest-attribute equivalent (see the dev-run warning fixes in the commit history for
  the full explanation)

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

Originally tracked here as "planned, not started"; it's since shipped in full and gone well beyond
the original CSS/FXML-only scope:
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

## Difficulty guide for future changes

Pure visual restyling (spacing, colors already exposed via the color scheme system, fonts) is
low-risk and mostly CSS-template/FXML-only. Adding a genuinely new setting or minor behavioral
tweak is also low-to-medium effort and follows the established patterns throughout the codebase
(see the Extensibility note above). Anything that changes what data is stored (schema) or how core
interactions work (the timer state machine, navigation history stack, click-away/selection logic)
is the higher-effort/higher-risk category — the navigation stack and click-away filters in
particular are subtle enough that Step 8's comment pass should prioritize them.
