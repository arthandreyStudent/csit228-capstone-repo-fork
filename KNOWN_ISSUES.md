# Known Issues & Technical Debt

## Architectural & Code Debt

### 1. Main Thread UI Freezes
- **Severity**: High
- **Issue**: Most `TicketDAO` and `UserDAO` database transactions (including complex JOIN selects) are executing directly on the primary JavaFX Application Thread.
- **Impact**: Noticeable stutter or freeze when transitioning screens if the network latency to MySQL is high.
- **Recommendation**: Implement `javafx.concurrent.Task` wrappers around DAO calls. Add loading indicators to UI views.

### 2. DAO Caching & State Mechanics
- **Severity**: Medium
- **Issue**: `TicketDAO` uses a local static `List<TicketView>` with a boolean `ticketsDirty` flag to determine when to hit the database. 
- **Impact**: Relying on singletons to track dirty states in a distributed scenario leads to ghost data. If another user in a different deployment changes a ticket, this client's `ticketsDirty` flag won't toggle, leading to stale visibility.
- **Recommendation**: Either poll periodically, implement a lightweight pub-sub (like WebSocket or SSE), or accept a UX design requiring manual "Refresh" pushes if the database is remote.

### 3. Controller Coupling
- **Severity**: Medium
- **Issue**: Specific layout parameters and button behaviors are hardcoded into standard `Controller` classes (e.g., UI hiding manipulation `button.setManaged(false)`). 
- **Impact**: Makes testing logic separately from UI difficult; creates massive controller file sizes.
- **Recommendation**: Consolidate UI generation elements fully onto the `ListRowItem` factory, or adopt an MVVM standard for two-way bindings.

## Business Rule & UX Disconnects

### 1. "Silent" Reassignments
- **Severity**: Medium
- **Issue**: Editors changing an assignee simply overwrite `assigned_to` in SQL. 
- **Impact**: The previous assignee receives no notice in the system unless they reload their dashboard and wonder where their ticket went.
- **Recommendation**: Generate a generic `Notification` entry pointing to the old assignee whenever an Editor performs an update on `IN_PROGRESS` items, firing off via `NotificationDAO`.

### 2. Modal Refresh Loop
- **Severity**: Low
- **Issue**: Closing Modals often triggers a blind, heavyweight `refreshDashboard()` loop which destroys and rebuilds the entire DOM layout of the `VBox` lists.
- **Impact**: Screen flicker and wasted memory allocations parsing FXML models repetitively.
- **Recommendation**: Use targeted row updates returning the mutated `TicketView` to the parent window, updating only the isolated labels.

