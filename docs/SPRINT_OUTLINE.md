# TCSS360 FileWatcher — Sprints 4–6 Outline

**Team:** Jonathan Sung, Abdulrahman Elmi  
**Goal:** Meet full spec (menus, toolbar, SQLite, query window, export, optional email) with work split evenly across Sprints 4–6.

---

## Current State (Before Sprints 4–6)

- ✅ WatchService monitoring (create/modify/delete)
- ✅ Extension filter (QueryCriteria + ExtensionFilter)
- ✅ Basic Swing UI: directory picker, extension text field, Start/Stop, event log area
- ❌ No SQLite
- ❌ No menu strip (no Help/About, no shortcuts)
- ❌ No toolbar with icons/tooltips
- ❌ No “write to DB” or on-exit prompt
- ❌ No query window or query types
- ❌ No CSV export or email

---

## Package / Folder Structure (Separation of Concerns)

Code is organized by responsibility. New code goes in the right package; existing classes can stay in the root package or be moved when convenient.

| Package / Folder | Responsibility | Key types (existing or new) |
|------------------|----------------|-----------------------------|
| **app** | Entry point, wiring, controller | MainApp, MonitorController |
| **domain** | Domain models and value objects | EventType, FileEvent, QueryCriteria |
| **monitor** | File system watching | FileMonitorService, WatchServiceMonitor, FileEventListener, ExtensionFilter |
| **database** | SQLite access and schema | EventRepository/DAO, schema init |
| **ui** | Main window, menus, toolbar, extension chooser | MainWindow, menu/toolbar setup |
| **ui.query** | Query dialog and query logic | QueryWindow, query-by-extension/date/activity/path |
| **export** | CSV and email (extra credit) | CsvExporter, EmailSender |

See **docs/PACKAGE_STRUCTURE.md** for where to put each new class and how to migrate existing ones.

---

# Sprint 4 — Database + Menus/Toolbar and Controls

**Theme:** SQLite in place, main window has proper menus/toolbar and control state.

## Abdulrahman — Database & “Write to DB”

1. **SQLite setup**
   - Add **sqlite-jdbc** (or sqlite.jar) to the project (e.g. lib/ + classpath or Gradle/Maven).
   - Create package **database** and a class that:
     - Opens/creates a SQLite DB (e.g. `filewatcher.db` in user dir or project).
     - Creates table with columns: **file name**, **absolute path**, **event type** (create/change/delete/rename), **date and time** (separate field). Add other columns only if needed.
   - Implement **insert(event)** to persist one file event (from `FileEvent` / domain).

2. **Write current list to DB**
   - From the in-memory list of events currently shown in the UI (or from a buffer the monitor feeds), implement “write all current events to DB” and call it when the user clicks the toolbar/menu option “Write to database”.
   - Ensure only not-yet-written events are written (or document “append all currently displayed” if spec allows).

3. **On-exit prompt**
   - On application exit (window close / File → Exit):
     - If there are unwritten events, show a dialog: “Write current contents to the database before exiting?” Yes / No / Cancel.
     - Yes → write then exit; No → exit without writing; Cancel → stay in app.

**Deliverables:** DB created and writable; “Write to DB” button/menu works; exit prompt works.

---

## Jonathan — Menus, Toolbar, and Control State

1. **Menu strip**
   - **File:** Exit (shortcut e.g. Alt+F then E, or Ctrl+Q). Optionally “Write to database” here too.
   - **Monitor (or View):** Start monitoring, Stop monitoring (with shortcuts, e.g. Ctrl+S for start, Ctrl+T for stop — pick consistent ones).
   - **Help:** About. Shortcut e.g. F1 or Alt+H then A.
   - **About dialog:** Program name, short usage (e.g. “Monitor a folder and log file events; optional SQLite and queries”), version (e.g. 1.0), developer names (Jonathan Sung, Abdulrahman Elmi).
   - Every menu and option must have a keyboard shortcut as required by the spec.

2. **Toolbar (button strip)**
   - Buttons: **Start monitoring**, **Stop monitoring**, **Write current list to database**. Add others as needed (e.g. Choose folder, Open query window).
   - Use icons (images or simple Swing icons) and **tooltips** so each button’s purpose is clear.
   - Toolbar buttons must mirror menu actions (shortcuts to menu options).

3. **Enable/disable controls**
   - When monitoring is **running:** disable Start and “Choose folder” (or directory field); enable Stop.
   - When monitoring is **stopped:** enable Start and directory selection; disable Stop.
   - “Write to database” enabled when there is something to write (e.g. at least one event in the current list).

4. **Extension choice**
   - Keep or add a **basic list** of extensions (e.g. txt, java, pdf, docx) in a combo box or list.
   - Allow **user-specified** extension (e.g. combo box editable or a separate text field). Combined with the list, this satisfies “basic list + allow user to specify one”.

**Deliverables:** Full menu strip with shortcuts, toolbar with icons/tooltips, correct enable/disable behavior, extension chooser (list + custom).

---

## Sprint 4 Integration

- Abdulrahman exposes “write events to DB” and “get list of current events / has unsaved events” for Jonathan’s toolbar and exit logic.
- Jonathan wires toolbar/menu “Write to database” to Abdulrahman’s write method and uses the “unsaved events?” check for exit dialog and button state.
- Both run and test: start monitor → generate events → write to DB → exit with prompt.

---

# Sprint 5 — Query Window and Query Types

**Theme:** Separate query window; run queries (extension + at least three more); show results; clear DB option.

## Abdulrahman — Query Window and Extension + Clear DB

1. **Query menu and window**
   - Menu option: **Query database** (e.g. under File or a “Database” menu). Shortcut e.g. Ctrl+Q or F2. Toolbar button that opens the same.
   - Opening it shows a **separate window** (or tab) — “Query window”.
   - Window has a way to **return to the main window** (e.g. “Back to monitor”, “Close”, or a menu in the query window). No closing the app; just hide query window and show main window.

2. **Query by extension**
   - UI: dropdown or list of **basic extensions** (same as main window) plus option to **choose or type an extension**.
   - Run query: “all files of this extension”. Show results in a table (see below).

3. **Clear database**
   - Menu option (in query window or main): **Clear contents of the database**. Confirm dialog: “Clear all records?” Yes/No. On Yes, delete all rows (or drop/recreate table per your schema).

4. **Results display**
   - Results shown in **tabular form**: each column = one field (file name, extension, path, event type, date/time). Use a `JTable` or similar. All file details returned as required.

**Deliverables:** Query window opens from menu/toolbar; extension query works; results in table; clear DB with confirm; graceful return to main window.

---

## Jonathan — Additional Query Types and Results Polish

1. **At least three more query types** (beyond “by extension”):
   - **By date range:** user picks “from date” and “to date”; query events in that range.
   - **By activity/event type:** user picks one of created / modified / deleted (and rename if you support it); query by that type.
   - **By path/directory:** user enters or selects a path; query events whose path starts with that (or equals), so “files in a particular directory”.

2. **Query UI in query window**
   - For each query type: simple form (dates, dropdown for activity, path field). “Run query” button (and optional “Clear results”).
   - One result table for all query types; columns: file name, extension, path, activity, date/time.

3. **Error handling**
   - No crashes: invalid path, empty DB, invalid date range, DB locked, etc. Show user-friendly messages (e.g. `JOptionPane.showMessageDialog`) and log if needed.
   - Apply same idea on main window: invalid directory, DB errors when writing, etc.

**Deliverables:** Date range, activity, and path queries implemented; results shown in same table; robust error handling in query and main flow.

---

## Sprint 5 Integration

- Abdulrahman’s query window and table are the single place for all query results; Jonathan’s query logic fills that table.
- Agree on a simple “query result” DTO or table model so both can contribute (e.g. list of rows with file name, path, event type, date/time).
- Test: run each query type, clear DB, return to main window, run monitor again and write to DB, query again.

---

# Sprint 6 — Export (CSV + Email), Polish, and Submission

**Theme:** Export query results to CSV; optional email; final polish and zip for turn-in.

## Abdulrahman — CSV Export and Optional Email (Extra Credit)

1. **Export query results to CSV**
   - In query window: button/menu **“Write results to file”**.
   - User chooses file name and location (e.g. `JFileChooser` with default name like `query_results.csv`). Save as **.csv** (comma-separated).
   - **File contents:**
     - **Start of file:** short lines describing the query (e.g. “Query: by extension = java”, “Run at: 2025-02-18 14:30”).
     - **Then:** tabular data, one row per line, each column labeled (file name, extension, path, activity, date/time). Each data item in its own column.

2. **Email generated file (extra credit)**
   - After user saves a CSV (or from a “Email results” path): prompt for **email address** (assume valid per spec).
   - Use Gmail (or another provider) to send that file as an attachment. Research: Gmail App Password / OAuth, and Java mail library (e.g. JavaMail / Jakarta Mail + SMTP). Document in README how to set up (e.g. app password, enable “less secure app” if applicable).
   - Handle errors (wrong credentials, network) with a message, no crash.

**Deliverables:** CSV export with query info header and labeled columns; optional email with attachment; README note on email setup.

---

## Jonathan — Polish, About, and Submission

1. **About and Help**
   - Finalize **About** content: program usage (step-by-step), version, both developer names. Ensure Help menu and shortcut work.

2. **Icons and tooltips**
   - Ensure every toolbar button and important control has an icon (or clear text) and tooltip. Menus have shortcuts.

3. **Robustness**
   - Review: disable/enable states, exit prompt, DB errors, invalid paths, empty queries. No uncaught exceptions; all errors show a message or log.

4. **Submission**
   - **Zip:** all source, SQLite JAR (or dependency instructions), and any DB file if required. Name: `SungElmifilewatcherproject.zip` (last names + filewatcherproject).
   - README: how to compile and run, where to put sqlite.jar, optional email setup (Gmail app password, etc.), and any extra-credit notes.

**Deliverables:** About finalized; UI polished; errors handled; zip and README ready for turn-in.

---

## Sprint 6 Integration

- Abdulrahman’s CSV/email uses the same query result set and table model as the query window.
- Jonathan’s README documents CSV location and email steps. Both test full flow: monitor → write to DB → query → export to CSV → (optional) email.

---

## Summary Table

| Sprint | Abdulrahman | Jonathan |
|--------|----------|----------|
| **1** | SQLite schema + insert; “Write to DB” action; on-exit prompt | Menu strip + shortcuts; toolbar + icons/tooltips; enable/disable; extension list + custom |
| **2** | Query window; extension query; clear DB; results table; return to main | Date range, activity, path queries; query UI; error handling |
| **3** | CSV export (query info + table); email (extra credit) | About/Help final; icons/tooltips; robustness; zip + README |

---

## Extra Credit Checklist (Document in README)

- [ ] Query by date range, extension, activity, path.
- [ ] Results to screen in tabular form.
- [ ] Option to write results to user-named .csv with query info at start and labeled columns.
- [ ] Option to email generated file; accept email address; Gmail (or other) with researched setup and packages.

---

## Notes

- **Rename event:** Spec mentions “rename”. WatchService does not always fire a distinct “rename”; it may be create + delete. You can add a RENAMED event type and heuristics later, or document “rename treated as create/delete” for now.
- **Dependencies:** Add sqlite-jdbc (e.g. from https://github.com/xerial/sqlite-jdbc) and, for email, JavaMail API + SMTP (or Jakarta Mail). Use **System.Data.SQLite** only for C#; for Java use **sqlite.jar** or sqlite-jdbc as required.
- Keep using the existing **WatchService** and **domain** types; add DB and UI on top so work stays parallel and merge-friendly.
