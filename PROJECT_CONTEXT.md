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
- **Task Execution**: A member starts work (OPEN -> IN PROGRESS) and marks it completed when done (IN PROGRESS -> COMPLETED).
- **Review Process**: Editors review completed tickets. They either approve (RESOLVED) or reject (Sent back to IN PROGRESS).

## Tech Stack
- **Language**: Java 21
- **UI Framework**: JavaFX (with FXML views)
- **Database Architecture**: MySQL relational database accessed via raw JDBC.
- **Build Tool**: Maven (with `javafx-maven-plugin`).
- **Icons**: Ikonli (FontAwesome, Material).

## Architecture Summary
The application follows a traditional **MVC (Model-View-Controller)** paired with a **DAO (Data Access Object)** pattern for data persistence:
- **Views**: FXML files in `src/main/resources/com/csit228/capstone/view/`.
- **Controllers**: Java classes handling UI interactions (`src/main/java/com/csit228/capstone/controller/`).
- **Models**: POJOs representing business entities (`Ticket`, `User`, `Department`). Includes specialized view-models like `TicketView` for UI data binding.
- **DAOs**: JDBC wrappers managing database CRUD operations (`TicketDAO`, `UserDAO`, etc.).
- **Utils**: Shared helpers (`AppSession`, `Formatter`, `Controls`, `DBConnector`).

## Current Project Maturity
The system is in active development (Capstone project). Core UI navigation and database mapping are established, but some state transitions and view integrations are actively being refined.

## Major Dependencies
- `javafx.controls`, `javafx.fxml`, `javafx.web`
- `org.controlsfx.controls`
- `org.kordamp.ikonli.*`
- `mysql:mysql-connector-j`

## Important Design Decisions
- **Custom View Models (`TicketView`)**: Allows complex UI tables and lists to bypass N+1 queries by joining multiple tables directly in SQL (e.g. fetching author names instead of IDs).
- **Role-based Dashboards**: Distinct FXML layouts and base controllers for different user capabilities rather than rendering conditional UI inside a single massive dashboard controller.

