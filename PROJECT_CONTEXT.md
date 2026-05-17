# TIX.org - Project Context

## System Overview
TIX.org is a JavaFX-based desktop application designed for ticketing and task management within organizations. It provides a centralized platform to create, assign, and track tasks (tickets), while promoting voluntarism through a self-assignment Volunteer Board.

## Target Users & User Roles
1. **Executive / Head**: High-level managers. They create tickets, directly assign tasks, view organizational analytics, and manage members.
2. **Editor**: Reviewers or middle-managers. They review tickets marked as completed by members. They can approve them (marking them resolved), send them back (in progress), and reassign open or in-progress tickets.
3. **Member**: General staff. They view their assigned tasks on a personal dashboard, volunteer for open unassigned tickets via a Volunteer Board, and work on tasks to push them from "Open" to "In Progress" and eventually "Completed".

## Core Workflows
- **Ticket Creation**: Executives and Editors can create tickets, defining priority, deadline, description, and target department.
- **Assignment / Volunteering**: Tickets can be manually assigned by Executives/Editors or self-assigned by Members through the Volunteer Board.
- **Task Execution**: A member starts work (OPEN → IN_PROGRESS) and marks it completed when done (IN_PROGRESS → COMPLETED).
- **Review Process**: Editors review completed tickets. They either approve (RESOLVED) or reject (Sent back to IN_PROGRESS).
- **Volunteer Board**: The Member Dashboard shows an OPEN, department-scoped "Volunteer" ticket card grid. Clicking a card ("Volunteer" or "View Details") opens the `BaseTicketDetailModalView` in a dedicated volunteer experience mode with a green "Open for volunteering" banner and Volunteer action button.

## Tech Stack
- **Language**: Java 21
- **UI Framework**: JavaFX (with FXML views)
- **Database Architecture**: MySQL relational database accessed via raw JDBC.
- **Build Tool**: Maven (with `javafx-maven-plugin`).
- **Icons**: Ikonli (FontAwesome, Material).

## Architecture Summary
The application follows a traditional **MVC (Model-View-Controller)** paired with a **Data Access Object (DAO)** pattern for data persistence:
- **Views**: FXML files in `src/main/resources/com/csit228/capstone/view/`.
- **Controllers**: Java classes handling UI interactions (`src/main/java/com/csit228/capstone/controller/`).
- **Models**: POJOs representing business entities (`Ticket`, `User`, `Department`). Includes specialized view-models like `TicketView` for UI data binding.
- **DAOs**: JDBC wrappers managing database CRUD operations (`TicketDAO`, `UserDAO`, etc.).
- **Utils / UI helpers**: Shared helpers (`AppSession`, `Formatter`, `Controls`, `DBConnector`, `ListRowItem`, `UIStyler`, `TicketDeadlineComparator`, `TicketWatcher` / `DashboardObserver`).

## Current Project Maturity
The system is in active development (Capstone project). Core UI navigation and database mapping are established. Recent work has delivered the Member Dashboard summary cards with overdue indicators, 8-filter ticket list, dynamic row action buttons, fully structured detail modal with volunteer support, sidebar navigation stub, and the `TicketWatcher` observer for auto-refresh. Core state-transition logic in action handlers and the My Tasks sidebar screen are still pending implementation.

## Major Dependencies
- `javafx.controls`, `javafx.fxml`, `javafx.web`
- `org.controlsfx.controls`
- `org.kordamp.ikonli.*`
- `mysql:mysql-connector-j`

## Important Design Decisions
- **Custom View Models (`TicketView`)**: Allows complex UI tables and lists to bypass N+1 queries by joining multiple tables directly in SQL (e.g. fetching author names instead of IDs).
- **Role-based Dashboards**: Distinct FXML layouts and base controllers for different user capabilities rather than rendering conditional UI inside a single massive dashboard controller.
- **Master FXML + Dynamic Injection**: The application uses a single `BaseTicketDetailModalView.fxml` for viewing ticket details across all roles and ticket statuses, dynamically injecting actions and UI elements via `BaseTicketDetailModalController` to strictly adhere to DRY principles.
- **Centralised Visual Language (`UIStyler`)**: All badge colour assignments and gradient applications are centralised in `com.csit228.capstone.utils.UIStyler`. The header and sidebar gradients, status badges, priority badges, overdue- and returned-overlay badges are all factory methods, eliminating inline style duplication.
- **`ButtonAction` Enum**: Action button text, icon, and colours are driven by a typed enum (`BaseTicketDetailModalController.ButtonAction` / `ListRowItem.ButtonAction`) with semantic names (`TAKE`, `START_TASK`, `SUBMIT`, `RESUBMIT`, `SUBMIT_LATE`, `VIEW_DETAILS`, `BACK`, `VOLUNTEER`). This keeps all dynamic button state policy in one place.
- **`getDynamicActionButtonInfo(...)` (Broker Pattern)**: Both `DashboardMemberController`, `ListRowItem`, and `BaseTicketDetailModalController` call a shared static method that maps boolean status flags → `ButtonAction`(s) to render. This makes the row-level and modal-level logic behave identically and guarantees DRY from a single source of truth.
- **Observer-backed Auto-Refresh (`TicketWatcher`)**: Polls `TicketDAO` on a configurable interval (2 s) and notifies every `DashboardObserver` on data changes, replacing the manual `refreshDashboard()` reentrancy with an architectural push pattern.
- **Conditional Layout Collapse**: In `BaseTicketDetailModalController`, unused containers (activity pane, changes-notice, volunteer banner, right-button column) are hidden with `setManaged(false)` / `setVisible(false)` rather than merely made transparent, preventing residual layout gaps following the Figma-driven design.
