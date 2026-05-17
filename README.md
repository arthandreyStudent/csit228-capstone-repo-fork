# TIX.org

### Group Members:
+ Bajenting, Jake
+ Endrina, Arth
+ Gabison, Joshua
+ Horigome, Ken
+ Tagalog, Prince Darens

---
### Product Description:
Tix.org is a ticketing system that allows organizations to create, assign, and update tasks all in one centralized platform. It aims to boost organizational productivity through collaborative works by assigning or self-assigning tickets or tasks to which in turn promotes voluntarism between members.

---
### Proposed Features:

| **Feature** | **Description** |
| :--- | :--- | 
| **Ticket Management** | Allows for creation, reading and updating as well as resolution of tickets shared across users.  |
| **Volunteer Board** | Acts as a centralized platform where members can self-assign or volunteer to tasks to boost organizational productivity. |
| **Role-Based Dashboards** | Each member has a dedicated view based on their organizational roles. Executives or people who overlook would have a more extensive dashboard view as to members under their care. |
| **Visual Analytics** | Shows an updated view of the progress such as completed vs pending tasks, or number of on-going works. |
| **Priority Alerts** | Acts as a notification system for upcoming deadlines of tickets. |

---
### Planned Technologies
+ Java
+ JavaFX
+ JDBC
+ Database (MySQL)

---
### Evaluation Criteria Mapping
#### 1.OOP: Planned use of classes such as:
+ **User** - holds user’s ID, username, passwords
+ **Heads & Members** - subclasses of parent class User, with its respective behaviors
+ **Tickets** - encapsulates data with regards to the ticket such as ID, description and etc.
+ **DatabaseManager** - handles all JDBC operations.

#### 2.GUI: JavaFX with FXML views:
+ **LoginView.fxml** - serves as the initial login screen where users authenticate before accessing the application.
+ **MainView.fxml** - serves as the application shell after login, with a left sidebar, user profile area, and a main content region for navigation and page switching.
+ **DashboardView.fxml** - presents the main dashboard overview, including ticket statistics, available tickets, pending tasks, and recent activity.
+ **AddTicketView.fxml** - provides the ticket creation form with fields for title, description, category, and deadline, along with submit and cancel actions.

#### Running the JavaFX app in IntelliJ
To avoid the error `The JavaFX runtime is not configured...`, run the project through Maven instead of using a plain Java `main()` run configuration.

1. Use a JDK that matches the project source level (`Java 21`).
2. In IntelliJ, import the project as a Maven project so dependencies are resolved automatically.
   - If IntelliJ still shows the JavaFX runtime warning, right-click `pom.xml` and choose **Add as Maven Project**.
3. Run the Maven goal `javafx:run`.
4. The application starts from `com.csit228.capstone.MainApplication`, which opens `LoginView.fxml` first.

#### 3.UML: Use Case and Class Diagram included:
The Use Case Diagram presents the main interactions within the TIX.org system through two primary actors: Head / Executive and Member. The Head / Executive is associated with functions such as logging in, viewing the dashboard, creating tickets, assigning tickets, managing members, and viewing analytics. The Member is associated with functions such as logging in, viewing the dashboard, viewing the volunteer board, viewing assigned tasks, volunteering for tickets, and receiving priority alerts. Include and extend relationships are also shown to represent dependent actions within the system, such as assigning a ticket after creating it, volunteering through the volunteer board, and receiving alerts when necessary.

The Class Diagram presents the structural design of the system through the main classes: User, Head, Member, Ticket, VolunteerBoard, Dashboard, NotificationService, DatabaseManager, and UserFactory. The User class serves as the parent class containing common attributes and methods, while Head and Member are specialized subclasses with role-specific behaviors. The Ticket class contains the core task-related information, including title, description, priority, deadline, and status. Supporting classes such as VolunteerBoard, Dashboard, and NotificationService reflect the major system features involving volunteer task assignment, role-based views, analytics, and alerts. DatabaseManager and UserFactory are also included to support database access and object creation within the system.

These diagrams reflect the overall functionality and structure of TIX.org in relation to ticket management, volunteer participation, monitoring of tasks, and role-based system access.

#### 4.Design Pattern: (Tentative)
+ **Singleton** - Centralized connection point for MySQL Database
+ **Factory**  - Creation of Users based on type
+ **Composite UI** - `MasterTicketDetailModalView.fxml` acts as the single source for rendering ticket details rather than separate Views per role or ticket state. This is reconfigured at runtime by the Controller strategy.
