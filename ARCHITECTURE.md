# Architecture Documentation

## Architecture Pattern
The project implements a **Model-View-Controller (MVC)** pattern combined with a **Data Access Object (DAO)** pattern for database interactions. State management heavily relies on singletons and a static `AppSession`.

## Layer Responsibilities

### 1. View Layer (JavaFX + FXML)
- **Responsibility**: Define the UI structure, layouts, and data binding boundaries. Uses internal CSS (`app.css`) for styling.
- **Location**: `src/main/resources/com/csit228/capstone/view/`
- **Key Files**: `DashboardExecutiveView.fxml`, `DashboardMemberView.fxml`, `LoginView.fxml`, etc.

### 2. Controller Layer
- **Responsibility**: Handle user events, validate form inputs, interact with DAOs, and coordinate navigation/modal presentation.
- **Location**: `src/main/java/com/csit228/capstone/controller/`
- **Design Notes**: Implements abstraction via base classes like `StaffDashboardController` and `BaseCreateTicketModalController` to reduce duplication across similar views.

### 3. Service & Utility Layer
- **Responsibility**: Handle UI utility mechanisms, session storage, authentication hashing, and shared components.
- **Location**: `src/main/java/com/csit228/capstone/utils/`
- **Key Components**: 
  - `AppSession`: Holds the currently logged-in user.
  - `ListRowItem`: UI factory generating highly customized dynamically bound row cards for ticket lists.
  - `DBConnector`: Singleton handling raw JDBC connections.

### 4. Data Access Layer (DAO)
- **Responsibility**: Translates domain objects into row-level database structures. Contains raw SQL queries.
- **Location**: `src/main/java/com/csit228/capstone/dao/`
- **Design Notes**: `TicketDAO` maintains an internal cached list (`List<TicketView> tickets`) with a dirty-checking mechanism (`ticketsDirty`) to minimize database hits during rapid UI refreshes.

### 5. Domain Model Layer
- **Responsibility**: Pure Java entities carrying state.
- **Location**: `src/main/java/com/csit228/capstone/model/`
- **Role Inheritance**: `User` acts as the base class for `Executive`, `Editor`, and `Member`.

## Data and Request Flow
1. **User Action**: The user clicks a button (e.g., "Assign Ticket").
2. **Controller Intercept**: The bound FXML controller catches the event.
3. **Validation & Business Logic**: The controller queries the current UI state or `AppSession` to determine validity.
4. **DAO Execution**: The controller calls `TicketDAO.assignTicket(userId, ticketId)`.
5. **Persistence**: `TicketDAO` acquires a JDBC Connection via `DBConnector`, executes the `UPDATE` SQL, and marks its cache as dirty.
6. **UI Refresh**: The controller calls `refreshDashboard()`, triggering the DAO to rebuild the cache, and the UI re-renders the ticket list using `ListRowItem`. 

## Strengths
- **Decoupled Dashboards**: Using separate views/controllers for Executives, Editors, and Members prevents one massive "god controller".
- **Optimized Ticket Views**: Standard POJOs are separated from `TicketView` instances, allowing DAOs to issue highly efficient, joined SQL queries specifically purposed for rendering lists.

## Weaknesses & Scalability Concerns
- **Tight Coupling to UI Thread**: Database calls are often executed on the main JavaFX Application Thread, leading to potential freezing UI when under load or high latency.
- **Static Session State**: Relying heavily on `AppSession.currentUser` and static caches in DAOs makes concurrent testing difficult, though acceptable in a single-instance desktop architecture.
- **DAO Cache Invalidation**: The `ticketsDirty` flag approach can lead to race conditions or stale data if updates are missed or error states are poorly handled.

