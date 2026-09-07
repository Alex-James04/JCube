# JCube Roadmap

Living reference for what's left to build, kept up to date as work lands. Intended to be read
cold by a future chat/session with no other context — if a section here goes stale, update it
rather than leaving it to rot.

## Status snapshot

Steps 1–6 (data layer, app shell, cube/session management, StatsService, Timer with
solve history, Settings) are functionally complete. What follows is what's left.

## Remaining build steps

### Step 7 — Settings, closing gaps
Functionally done: theme (hot-applies), show/hide scramble, spacebar mechanics (WCA/Simple),
inspection timing (WCA/Simple), confirm-before-delete, customizable stat list, CSV import/export.
No FXML visual polish was applied — plain controls in a VBox — since a full UI redesign is planned
separately (see "Visual styling" below).

### Step 8 — Comments & cleanup
An audit pass over the non-obvious logic specifically, not a fresh feature. Candidates:
- `ScrambleGenerator`'s move-cancellation/opposite-face-sorting logic
- `DatabaseManager`'s path resolution and the `ADD COLUMN` duplicate-column migration workaround
- `TimeController`'s state transitions (why DNF short-circuits `startRun` instead of only relying
  on `tick()`, why hold-start resets on every press during `INSPECTION`)
- `SolveCsvService`'s DNF/+2 detection rules (Time vs. P.1 columns)

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

## Visual styling (UI) — planned rework, not started

Explicitly flagged by the project owner as something they intend to redesign. Current state:
- `dark.css` / `light.css` are minimal (background, text, button colors only)
- All FXML files use plain, unstyled `HBox`/`VBox`/`ListView` layouts with no custom CSS classes
- No custom fonts, icons, spacing system, or component styling beyond JavaFX's default Modena-derived look

When this work happens, see "Difficulty of visual-only changes" below — it's expected to be a
CSS/FXML-only effort with no controller logic changes required, *provided* the restyling doesn't
change what data is shown or how navigation works.

## Settings inventory — implemented vs. candidate

**Implemented:**
- Theme (dark/light, hot-applies)
- Show scramble during solves
- Confirm before deleting cubes/sessions/solves
- Spacebar mechanics (WCA hold-to-arm vs. Simple)
- Inspection timing (WCA 15s/17s penalties vs. Simple/untimed)
- Customizable stats list on the timer screen (any `aoN` / Mean / PB, add/remove freely)

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
- Number of decimal places shown in times

Extensibility note: adding a new setting is currently cheap and consistent — one field on
`Settings.java`, one `ALTER TABLE settings ADD COLUMN ...` line in `schema.sql` (already tolerant
of being re-run every startup via `DatabaseManager`'s duplicate-column handling), one read/write
line in `SettingsDB`, one control in `SettingsView.fxml`. No generic key-value settings store was
built — deliberate, to avoid indirection nobody asked for. Revisit only if the number of settings
grows large enough that this per-setting boilerplate becomes the actual bottleneck.

## Known simplifications / minor adjustments worth revisiting

Not blockers, just things flagged along the way that a future pass might want to fix:

- **Settings are read once per view load, not live.** Changing spacebar/inspection mode or the
  stat list while a Timer view is already open won't affect that open session until you navigate
  away and back. Same for theme changes made from a dialog that's already open.
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

See the answer given directly to the user in the conversation this file was created from — the
short version: pure visual restyling is FXML/CSS-only and low-risk; adding a genuinely new setting
or minor behavioral tweak is also low-to-medium effort and follows established patterns throughout
the codebase; anything that changes what data is stored (schema) or how core interactions work
(the timer state machine, navigation flow) is the higher-effort/higher-risk category.
