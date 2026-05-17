# Architecture Documentation

## Architecture Pattern
The project implements a **Model-View-Controller (MVC)** pattern combined with a **Data Access Object (DAO)** pattern for database interactions. State management heavily relies on singletons and a static `AppSession`.

## Layer Responsibilities

### 1. View Layer (JavaFX + FXML)
- **Responsibility**: Define the UI structure, layouts, and data binding boundaries. Uses internal CSS (`app.css`) for styling.
- **Location**: `src/main/resources/com/csit228/capstone/view/`
- **Key Files**: `DashboardExecutiveView.fxml`, `DashboardMemberView.fxml`, `LoginView.fxml`, `BaseTicketDetailModalView.fxml`
- **Design Notes**: Employs a composite UI architecture. Rather than duplicating `.fxml` files per role/status state (e.g., 3 roles × 4 statuses = 12 views), a single `BaseTicketDetailModalView.fxml` is dynamically reconfigured at runtime by its controller.

### 2. Controller Layer
- **Responsibility**: Handle user events, validate form inputs, interact with DAOs, and coordinate navigation/modal presentation.
- **Location**: `src/main/java/com/csit228/capstone/controller/`
- **Key Base Classes**:
  - `StaffDashboardController`: Shares search, sorting, ticket-helpers, and profile code across dashboards.
  - `BaseDashboardController`: Implements `DashboardObserver`, encapsulates `TicketWatcher` registration, `TicketDAO` access, `getSortedTicketsByDeadline()`, and shared status-check helpers.
  - `BaseTicketDetailModalController`: Dynamic modal controller that decides badge visibility, button bar composition, role-specific sections (assigned-to, activity, notice) for `BaseTicketDetailModalView.fxml`.
- **Role-Specific Controllers**: `DashboardMemberController`, `DashboardEditorController`, `DashboardExecutiveController`.

### 3. Service & Utility Layer
- **Responsibility**: Handle UI utility mechanisms, session storage, authentication hashing, shared components, and styling helpers.
- **Location**: `src/main/java/com/csit228/capstone/utils/`
- **Key Components**:
  - `AppSession`: Holds the currently logged-in user.
  - `ListRowItem`: UI factory generating highly customized dynamically bound row cards for ticket lists across all dashboard types.
  - `UIStyler`: Centralised badge and gradient factory — applies the header gradient (`applyNavyBlueHeaderGradient`), sidebar gradient (`applyLeftSideBarGradient`), and produces colour-coded overlays and badges for status, priority, overdue, and returned states.
  - `DBConnector`: Singleton handling raw JDBC connections.

### 4. Data Access Layer (DAO)
- **Responsibility**: Translates domain objects into row-level database structures. Contains raw SQL queries.
- **Location**: `src/main/java/com/csit228/capstone/dao/`
- **Design Notes**: `TicketDAO` maintains an internal cached list (`List<TicketView> tickets`) with a dirty-checking mechanism (`ticketsDirty`) to minimize database hits during rapid UI refreshes.

### 5. Domain Model Layer
- **Responsibility**: Pure Java entities carrying state.
- **Location**: `src/main/java/com/csit228/capstone/model/`
- **Role Inheritance**: `User` acts as the base class for `Executive`, `Editor`, and `Member`.
- **Key Model**: `TicketView` — enriched view model used across all dashboards and the detail modal.

### 6. Observer Layer
- **Responsibility**: Watches for ticket data changes and notifies registered dashboards.
- **Location**: `src/main/java/com/csit228/capstone/observer/`
- **Key Components**:
  - `DashboardObserver`: Interface implemented by all dashboard controllers.
  - `TicketWatcher`: Background polling loop that periodically calls `TicketDAO.getTicketViews()` and fires `onDataChanged` to keep views in sync without an explicit refresh button.
  - Observers are auto-deregistered via `stopWatching()` on logout to prevent leaks.

## Data and Request Flow
1. **User Action**: The user clicks a button (e.g., "Take", "Start Task", "Submit", "Volunteer").
2. **Controller Intercept**: The bound FXML controller catches the event. `DashboardMemberController` delegates to `handleMemberAction(ticket, action)`.
3. **Dynamic Action Resolution**: Both `ListRowItem.getDynamicActionButtonInfo(...)` (row-level) and `BaseTicketDetailModalController.getDynamicActionButtonInfo(...)` (modal-level) inspect the ticket's status flags and decide which `ButtonAction` to surface — preventing hard-coded button text in controllers.
4. **Action Routing**: `handleMemberAction` maps each `ButtonAction` enum to the correct `TicketDAO` mutation call (assignment, status update, etc.). `ButtonAction.BACK` in the detail modal closes the modal; outside the modal it de-escalates a submit to a read-only view.
5. **Validation & Business Logic**: The controller queries the current UI state or `AppSession` to determine validity.
6. **DAO Execution**: The controller calls `TicketDAO` mutation methods (assign, update status, update last_updated).
7. **Persistence**: `TicketDAO` acquires a JDBC Connection via `DBConnector`, executes the `UPDATE` SQL, and marks its cache as dirty. `TicketWatcher.poll()` detects the stale flag and refreshes all registered dashboard observers.
8. **UI Refresh**: `onDataChanged()` fires, controllers receive the fresh `TicketView` list, and both the list pane and the modal re-render in place using `ListRowItem` helpers.
9. **Detail Modal Route**: Clicking a row invokes `openTicketDetailModal(ticket)` which calls `BaseTicketDetailModalController.loadTicket(ticket)`. That method calls `populateBadges()`, `handleChangesRequestedNotice()`, `handleAssignedAndCreatedSection()`, `handleVolunteerMsgBox()`, `handleButtons()`, and `handleActivitySection()` — each of which conditionally manages components via `setManaged(false)` to eliminate layout gaps.

## UIStyler — Centralised Visual Language
`UIStyler` (in `com.csit228.capstone.utils`) replaces ad-hoc inline style strings with a typed badge factory:
- `applyNavyBlueHeaderGradient(Region)` — left-to-right navy-to-blue gradient applied via `LinearGradient` to the ticket detail modal header.
- `applyLeftSideBarGradient(Region)` — top-to-bottom dark-navy gradient applied to the member dashboard sidebar.
- `makeStatusBadge(String)` / `makeStatusBadge(String, int)` — maps `IN_PROGRESS`, `RESOLVED`, `APPROVED`, `OVERDUE`, `COMPLETED`, and default open to distinct colour pairs.
- `makePriorityBadge(String)` / `makePriorityBadge(String, int)` — maps HIGH/LOW/MEDIUM to red/green/yellow.
- `makeOverdueBadge()` / `makeOverdueBadge(int)` — standalone muted red "OVERDUE" label for tickets past deadline.
- `makeReturnedBadge()` / `makeReturnedBadge(int)` — standalone warm-red-orange "RETURNED" label for editor-returned tickets.
- `makeBadge(String, String, String)` — core builder, applies padding, border-radius, and Inter font-family.

## Strengths
- **Decoupled Dashboards**: Using separate views/controllers for Executives, Editors, and Members prevents one massive "god controller".
- **Optimised Ticket Views**: Standard POJOs are separated from `TicketView` instances, allowing DAOs to issue highly efficient, joined SQL queries specifically purposed for rendering lists.
- **Centralised Visual Language**: `UIStyler` eliminates duplicated style definitions and enforces consistency across all badge types and gradients.
- **Dynamic Action Injection**: `ButtonAction` enum and `getDynamicActionButtonInfo()` single-source the button state policy. Controllers, row items, and the detail modal all share the same semantic mapping.
- **Observer-backed Auto-Refresh**: `DashboardObserver` + `TicketWatcher` keep every registered dashboard in sync after any DAO mutation; no manual `refreshDashboard()` call is needed after the initial load.

## Weaknesses & Scalability Concerns
- **Tight Coupling to UI Thread**: Database calls are often executed on the main JavaFX Application Thread, leading to potential freezing UI when under load or high latency.
- **Static Session State**: Relying heavily on `AppSession.currentUser` and static caches in DAOs makes concurrent testing difficult, though acceptable in a single-instance desktop architecture.
- **DAO Cache Invalidation**: The `ticketsDirty` flag approach can lead to race conditions or stale data if updates are missed or error states are poorly handled.
- **Unimplemented Action Handlers**: `DashboardMemberController.handleMemberAction()` and `BaseTicketDetailModalController.handleMemberAction()` contain `TODO` stub switches for TAKE, START_TASK, SUBMIT, RESUBMIT, SUBMIT_LATE, VOLUNTEER, and BACK/VIEW_DETAILS — the business-logic integration is not yet wired to `TicketDAO`.
