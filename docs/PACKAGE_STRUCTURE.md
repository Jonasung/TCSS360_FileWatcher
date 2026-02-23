# Package Structure — Separation of Concerns

This document defines where new and existing code lives so you can split work cleanly and avoid merge conflicts.

## Directory Layout

```
src/main/java/TCSS_FileWatcher/
├── app/              # Application entry and wiring
├── domain/           # Domain models and value objects
├── monitor/          # File system monitoring
├── database/         # SQLite persistence (new)
├── ui/               # Main window, menus, toolbar (new or migrated)
├── ui/query/         # Query window and query logic (new)
└── export/           # CSV and email export (new, extra credit)
```

## Package Purposes and Ownership

### `TCSS_FileWatcher.app`
- **Purpose:** Start the app and wire main components (monitor, controller, main window).
- **Current:** `MainApp.java`, `MonitorController.java` (can stay in root package until you migrate).
- **When to add here:** If you refactor and move `MainApp` and `MonitorController` into a dedicated package.

### `TCSS_FileWatcher.domain`
- **Purpose:** Shared domain types used by monitor, UI, and database. No UI or I/O.
- **Current:** `EventType.java`, `FileEvent.java`, `QueryCriteria.java` (in root).
- **Move here (optional):** When you want a clear “domain” package. Update `package` and all `import` statements.

### `TCSS_FileWatcher.monitor`
- **Purpose:** Watching the file system and notifying listeners. Depends on domain only.
- **Current:** `FileMonitorService.java`, `WatchServiceMonitor.java`, `FileEventListener.java`, `ExtensionFilter.java` (in root).
- **Move here (optional):** Same as domain; move when convenient and fix imports.

### `TCSS_FileWatcher.database` (new — Sprint 1, Person A)
- **Purpose:** SQLite schema, connection, and persistence. All DB code lives here.
- **Add:**
  - A class to init DB and create table (e.g. `EventRepository` or `DatabaseHelper`).
  - Table columns: file name, absolute path, event type, date/time (each its own column).
  - Methods: `insert(FileEvent)` (or DTO), `clearAll()`, and query methods used by the query window (by extension, date range, activity, path), returning a list of rows or DTOs.
- **Uses:** Domain types (`FileEvent`, `EventType`). No Swing.
- **Used by:** `app`/controller and `ui.query` (and optionally `ui` for “write to DB” and exit prompt).

### `TCSS_FileWatcher.ui` (new or migrated — Sprint 1, Person B)
- **Purpose:** Main window: layout, menu strip, toolbar, extension chooser, event list, start/stop and “write to DB” actions.
- **Current:** `MainWindow.java` (in root). Can stay there or move to `ui` and add menus/toolbar in the same class.
- **Add:** Menu bar (File, Monitor, Help with About), toolbar with icons and tooltips, enable/disable logic. Extension combo/list + custom extension input.
- **Uses:** `domain`, `monitor` (via controller), `database` (for write and “has unsaved?”).

### `TCSS_FileWatcher.ui.query` (new — Sprint 2)
- **Purpose:** Query UI and running queries. Separate window (or tab) with forms and result table.
- **Add:**
  - `QueryWindow` (or `QueryDialog`): open from menu/toolbar, contains query controls and result table, “Back to main” / close.
  - Query forms: extension, date range, activity type, path. “Run query” and “Clear results.”
  - “Clear database” menu option with confirmation.
- **Uses:** `database` for all queries and clear; `domain` for event types/labels. Display only; no monitoring.

### `TCSS_FileWatcher.export` (new — Sprint 3, Person A, extra credit)
- **Purpose:** Export query results to CSV and send by email.
- **Add:**
  - `CsvExporter`: given query description and result rows, write header (query info) then tabular CSV with labeled columns; user picks file path/name.
  - `EmailSender` (optional): attach a file and send via Gmail (or other SMTP); accept recipient email; handle errors.
- **Uses:** Query result data (list of rows or DTOs). No DB or UI logic; just file and email I/O.
- **Used by:** `ui.query` (button “Write results to file” and “Email”).

## Migration Strategy

- **Option A (minimal change):** Keep existing classes in the root package `TCSS_FileWatcher`. Put only **new** code in the new packages (`database`, `ui`, `ui.query`, `export`). Easiest for Sprint 1.
- **Option B (full structure):** In Sprint 1, move existing classes into `domain` and `monitor` (and optionally `app`), then add new code in `database` and `ui`. Cleaner long term.

Use the same strategy as a team so imports and refactors are consistent.

## Dependency Direction

- `app` → `monitor`, `domain`, `ui`, (and optionally `database` if controller triggers write).
- `ui` → `domain`, `app` (controller), `database` (for write/unsaved check).
- `ui.query` → `database`, `domain`, `export`.
- `database` → `domain` only.
- `monitor` → `domain` only.
- `export` → no project packages (only standard library / CSV/email libs).

This keeps database and export independent of Swing and allows both of you to work in parallel (one in `database` + `export`, one in `ui` + `ui.query`).
