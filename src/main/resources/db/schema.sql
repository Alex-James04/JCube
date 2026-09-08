PRAGMA foreign_keys = ON;

-- Cube profiles
CREATE TABLE IF NOT EXISTS cubes (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    name       TEXT    NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Sessions belonging to a cube
CREATE TABLE IF NOT EXISTS sessions (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    cube_id    INTEGER NOT NULL,
    name       TEXT    NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cube_id) REFERENCES cubes(id) ON DELETE CASCADE
);

-- Individual solves belonging to a session
CREATE TABLE IF NOT EXISTS solves (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER NOT NULL,
    time_ms    INTEGER NOT NULL,
    penalty    TEXT    NOT NULL DEFAULT 'none',
    scramble   TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE
);

-- Global app settings (single row, id is always 1)
CREATE TABLE IF NOT EXISTS settings (
    id           INTEGER PRIMARY KEY CHECK (id = 1),
    theme        TEXT    NOT NULL DEFAULT 'dark',
    show_scramble INTEGER NOT NULL DEFAULT 1
);

-- Migrations for columns added after the initial release. SQLite has no "ADD COLUMN IF NOT
-- EXISTS", so DatabaseManager tolerates the duplicate-column error these raise on later runs.
ALTER TABLE settings ADD COLUMN spacebar_mode TEXT NOT NULL DEFAULT 'WCA';
ALTER TABLE settings ADD COLUMN inspection_mode TEXT NOT NULL DEFAULT 'WCA';
ALTER TABLE settings ADD COLUMN stat_specs TEXT NOT NULL DEFAULT 'AO5,AO12,MEAN,PB';
ALTER TABLE settings ADD COLUMN confirm_deletes INTEGER NOT NULL DEFAULT 1;
ALTER TABLE settings ADD COLUMN decimal_places INTEGER NOT NULL DEFAULT 2;

-- Seed the settings row so it always exists
INSERT OR IGNORE INTO settings (id, theme, show_scramble)
VALUES (1, 'dark', 1);

-- Per-role custom color scheme (single row, id is always 1). Superseded the old dark.css/light.css
-- file swap — the app now builds its stylesheet dynamically from these 9 values.
CREATE TABLE IF NOT EXISTS color_scheme (
    id             INTEGER PRIMARY KEY CHECK (id = 1),
    background     TEXT NOT NULL DEFAULT '#1e1e1e',
    surface        TEXT NOT NULL DEFAULT '#2a2a2a',
    text_primary   TEXT NOT NULL DEFAULT '#e0e0e0',
    text_secondary TEXT NOT NULL DEFAULT '#c2c2c2',
    accent         TEXT NOT NULL DEFAULT '#4a9eff',
    button         TEXT NOT NULL DEFAULT '#333333',
    button_hover   TEXT NOT NULL DEFAULT '#444444',
    danger         TEXT NOT NULL DEFAULT '#c0392b',
    border         TEXT NOT NULL DEFAULT '#3a3a3a'
);

INSERT OR IGNORE INTO color_scheme (id) VALUES (1);