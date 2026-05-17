# Known Issues & Technical Debt

## Completed Fixes & Resolved Items

### ✅ Volunteer Board Modal Redirect
- **Fixed**: Volunteer Board card clicks and "Volunteer" buttons now route through `openTicketDetailModal()` (same modal as "My Work") instead of immediately calling `ticketDAO.assignTicket()`. This opens a proper pre-assignment preview dialog for the member before any state change occurs.

### ✅ Overdue Counters in Summary Cards
- **Fixed**: Each of the four Member Dashboard summary cards (To Do, In Progress, Completed, Resolved) now shows an overdue counter badge (red circle + "{N} overdue") when overdue tickets exist in that category. A dedicated `isOverdueInDeadline()` method with an extension point was added.

### ✅ Ticket Detail Modal — Dynamic Button Bar
- **Fixed**: The action button bar in `BaseTicketDetailModalView` now uses `getDynamicActionButtonInfo()` (shared broker from `BaseTicketDetailModalController`) to conditionally render between 0 and 2 buttons (BACK + primary action). The right container is collapsed (`setManaged(false)`) when there is no primary action. Active handlers are accessed via `setOnAction(...)` callbacks set by the caller.

### ✅ Tabular Row Buttons — Dynamic Injection via Broker
- **Fixed**: `ListRowItem.getDynamicActionButtonInfo()` (shared broker from `BaseTicketDetailModalController`) now drives the row-level action button, replacing all hard-coded `ButtonAction` strings in `DashboardMemberController.renderMyWorkTickets()` and `forMemberMyWorkTicket()` factory. The same broker is used in both the row factory and the modal for guaranteed consistent behaviour.

### ✅ Ticket Detail View Closed/Finalised Edges
- **Fixed**: All conditional PREVIEW-style layout collapses (activity pane, volunteer banner, changes-requested notice) now call both `setManaged(false)` and `setVisible(false)` on the node *and* its direct parent `HBox` to fully eliminate residual layout footprints.

### ✅ Observed Changes Requested Notice Hiding
- **Fixed**: Previously the "Changes Requested" notice was hidden only by setting the inner container to invisible. Now both the inner container **and** its wrapping parent `HBox` are hidden with `setManaged(false)` to fully collapse its layout contribution.

### ✅ Member "Volunteer" Action Button Mapping
- **Fixed**: The `isVolunteerTicket()` flag in `getDynamicActionButtonInfo()` now correctly surfaces a `VOLUNTEER` action button in both the row table and the detail modal view for OPEN, unassigned, "Volunteer"-department tickets.

### ✅ DashboardMemberController Cache Refresh
- **Fixed**: Member actions now use `TicketWatcher` (observer pattern) to propagate cache updates to all dashboards automatically. The explicit `ticketDAO.getTicketViews()` + `refreshDashboard()` call-after every mutation is no longer the sole mechanism for the Member Dashboard seeing fresh data.

---

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
- **Recommendation**: Either poll periodically (partially addressed by `TicketWatcher`), implement a lightweight pub-sub (like WebSocket or SSE), or accept a UX design requiring manual "Refresh" pushes if the database is remote.

### 3. Controller Coupling
- **Severity**: Medium → Low
- **Issue**: Previously, specific layout parameters and button behaviours were hardcoded into standard Controller classes (e.g. UI hiding manipulation `button.setManaged(false)`). This is being addressed by the `getDynamicActionButtonInfo()` broker and `ListRowItem` factory.
- **Impact**: Makes testing logic separately from UI difficult; creates massive controller file sizes.
- **Current Status**: The `ListRowItem` factory now owns all row-level visual construction. `BaseTicketDetailModalController` owns all modal-level construction. Button text/icons/colours are all enum-driven. Controllers only set callbacks via `setAction(...)`. Remaining coupling: `DashboardMemberController.renderMyWorkTickets()` still assembles the 9 computed booleans inline before calling the factory — extracting this into a ticket-level helper (e.g. `TicketStatusUtils.toFlags(ticket)`) would further reduce the controller surface.
- **Recommendation**: Finish migrating remaining inline flag computation into stateless helpers, and move `handleMemberAction()` body into `DashboardMemberController` sub-action methods so the catch-block switch only delegates.

### 4. "Silent" Reassignments
- **Severity**: Medium
- **Issue**: Editors changing an assignee simply overwrite `assigned_to` in SQL.
- **Impact**: The previous assignee receives no notice in the system unless they reload their dashboard and wonder where their ticket went.
- **Recommendation**: Generate a generic `Notification` entry pointing to the old assignee whenever an Editor performs an update on `IN_PROGRESS` items, firing off via `NotificationDAO`.

### 5. Modal Refresh Loop — Partially Mitigated
- **Severity**: Low → Low (решена)
- **Issue**: Previously, closing Modals triggers a blind, heavyweight `refreshDashboard()` loop which destroys and rebuilds the entire DOM layout of the `VBox` lists.
- **Mitigation**: With `TicketWatcher` + `DashboardObserver`, dashboards only re-fire `renderDashboard()` when the DAO is *actually* dirty. Targeted row updates could further reduce re-render scope, but the observer approach already eliminates frequency of full rebuilds.

---

## Open TODOs & Unimplemented Features

### 1. Action Handler Stubs — Highest Priority
All `handleMemberAction()` switches in both `DashboardMemberController` and `BaseTicketDetailModalController` contain placeholder `break` statements:
- `TAKE` / `VOLUNTEER` → `TicketDAO.assignTicket()`
- `START_TASK` → `TicketDAO.updateTicketStatus(ticketId, IN_PROGRESS)`
- `SUBMIT` / `RESUBMIT` / `SUBMIT_LATE` → `TicketDAO.updateTicketStatus(ticketId, COMPLETED)` + `last_updated`
- `VIEW_DETAILS` / `BACK` → open / close the detail modal.

### 2. My Tasks Screen — Partial
The sidebar row "My Tasks" is partially wired: clicking it hides the main dashboard pane. The actual "My Tasks" `Pane` / screen (a filtered view of My Work or a separate summary) has not been implemented.

### 3. Editor & Executive — EditorAssignedAndCreateSection / ExecutiveAssignedAndCreateSection
`BaseTicketDetailModalController.editorAssignedAndCreateSection()` and `executiveAssignedAndCreateSection()` are empty shells — the role-specific rendered content in the detail modal for those roles is not yet complete.

### 4. Member Activity — Hardcoded Content
`loadMemberActivity()` currently builds a `Notification` object per assigned ticket instead of reading real `NotificationDAO` entries. The feed will not reflect historical changes until wired to the actual DAO.

### 5. Empty States
No "No tickets found" card has been added to any filtered view. When a filter returns zero results the `VBox` will simply appear empty.

### 6. Background Threads for DAO
Even with `TicketWatcher` polling, the actual `TicketDAO` fetch still runs on the JavaFX Application Thread. No `javafx.concurrent.Service` / `Task` wrappers are in place.

## Root Causes of Problems
- **Database Design**: Certain queries are too slow due to lack of proper indexing or because they hit the database too frequently.
- **Network Latency**: High latency between the application and the remote MySQL database can cause timeouts and slow responses. *(Note: Temporarily mitigated by switching to a local MySQL instance via XAMPP instead of a remote groupmate's server, significantly improving execution speed).*
- **Insufficient Caching**: Lack of effective caching mechanisms for frequently accessed data leads to repetitive database hits.
- **Tight Coupling**: UI and business logic are too tightly coupled, making it difficult to isolate and test components.
- **Lack of Notifications**: System does not adequately notify users of changes that occur as a result of their actions or the actions of others.
