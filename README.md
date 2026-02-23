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


## Iteration 2 Progress

### What was implemented

- Proof-of-concept structure for FileWatcher system
- Core domain models:
    - EventType
    - FileEvent
    - QueryCriteria
- Core service contracts:
    - FileMonitorService
    - FileEventListener
- Application skeleton (MainApp)
- Initial project package structure under src/main/java

### Project Direction

This iteration focuses on establishing the system architecture and defining clear contracts between components before implementing monitoring logic and database integration.

### Current Status

Domain and contract layer completed.
Ready for implementation of monitoring logic and SQLite integration.

## Iteration 3 Work Summary
- Implemented real-time directory monitoring using Java WatchService (create/modify/delete events)
- Added extension filtering via QueryCriteria + ExtensionFilter
- Implemented MonitorController to connect UI actions to the monitoring service
- Built a functional Swing UI (MainWindow) to select a folder, start/stop monitoring, and display event logs

## Next: 3 Sprints to Full Spec

- **Sprint 4:** SQLite + write-to-DB + on-exit prompt (Abdulrahman) | Menus, toolbar, shortcuts, enable/disable, extension chooser (Jonathan)
- **Sprint 5:** Query window, extension query, clear DB, results table (Abdulrahman) | Date range, activity, path queries + error handling (Jonathan)
- **Sprint 6:** CSV export + optional email (Abdulrahman) | About/Help, polish, zip + README (Jonathan)

See **docs/SPRINT_OUTLINE.md** for the full task breakdown and **docs/PACKAGE_STRUCTURE.md** for package/folder responsibilities.
