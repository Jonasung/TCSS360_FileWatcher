# TCSS360 FileWatcher

## Team Members
- Jonathan Sung
- Abdulrahman Elmi

## Iteration 1 Scope
- Project setup
- GitHub repository initialization
- Initial Software Requirements Specification (SRS)
- Functional and nonfunctional requirements definition
- Database schema draft
- Issue and time tracking using YouTrack

## Project Management
- Issue tracking and time reporting were managed using YouTrack.
- Iteration 1 total logged time: 24 hours.

## Status
Iteration 1 completed.

---

## Running (Sprint 1)

**Prerequisites:** Java 17+, Maven installed and on your PATH.

### Run the FileWatcher app (GUI)
```bash
cd TCSS360_FileWatcher
mvn compile exec:java
```
Or from your IDE: run the `main` method in `edu.tcss360.filewatcher.ui.FileWatcherApp`.

- The main window opens with a button and menu (File → Open Query Window, Help → About).
- Click **Open Query Window** to open the Query Results window (table is empty until you add query logic later).

### Run the SQLite DB spike (console)
```bash
mvn compile exec:java -Dexec.mainClass=edu.tcss360.filewatcher.persistence.DatabaseSpike
```
This creates `filewatcher_spike.db` in the project directory, creates the `file_events` table, inserts one sample row, and prints it. Use this to confirm SQLite works before integrating the DB into the app.
