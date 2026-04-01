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

-- Seed the settings row so it always exists
INSERT OR IGNORE INTO settings (id, theme, show_scramble)
VALUES (1, 'dark', 1);