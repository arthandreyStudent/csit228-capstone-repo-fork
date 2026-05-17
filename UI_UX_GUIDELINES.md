# UI / UX Guidelines

## Design Principles
The JavaFX application targets an organizational productivity tool standard: clean, heavily padded, card-driven lists, and distinct color-coded status badges.

## Layout Hierarchy
- **Left Sidebar**: High-contrast, dark-themed gradient navigation menu (`#0b112c` to `#1b2c6f`, applied by `UIStyler.applyLeftSideBarGradient`) containing the system brand, page selection, and primary profile/logout functions. Used across all layout base views (`MainView`, `DashboardMemberView`).
- **Main Content Anchors**: Soft, light backgrounds (e.g. `#eef2fb`) housing distinct bounding cards (white background, rounded corners radius `14` or `16`).

## Typography & Color Conventions
- **Font**: Inter (Custom loaded via `.ttf` files). Heavy usage of font-weights (Bold, Black) to denote hierarchy.
- **Status / Action Colors**:
  - `RESOLVED` / Approved: Green (`#4bcc8a`)
  - `COMPLETED` / Awaiting Review: Yellow/Orange (`#ff9900`)
  - `IN_PROGRESS`: Blue (`#2f95ff`)
  - `OPEN` / Send Back / Overdue: Red (`#ef4c56` / `#f14d5a`)
  - `RETURNED` (editor requested changes): Warm red-orange (`#da472d` / `#f5ccbc` badge background)
  - **Neutral / Text**: Dark Navy (`#1c2b63` for primary titles, `#9faad2` or `#6f7aa7` for subtitles and metadata).
- **Gradients**: `UIStyler` applies a left-to-right navy-to-blue gradient (`#1f3e8f` → `#3a7ef3`) to the ticket detail modal header, and a top-to-bottom dark-navy-to-blue gradient (`#0b112c` → `#1b2c6f`) to the left sidebar.

## Core UX Patterns

### 1. Ticket Card Rows (`ListRowItem`)
- Avoid standard JavaFX `TableView` for ticket displays. Instead, use custom `VBox`/`HBox` driven row components (`ListRowItem`) loaded into `ScrollPane`.
- **Action Buttons**: Kept to the right side of the row. Use rounded pill buttons. Button type is computed by `BaseTicketDetailModalController.getDynamicActionButtonInfo()` / `ListRowItem.getDynamicActionButtonInfo()` based on ticket status flags — no hard-coded button text strings in the controller.
- **Badge overlays**: `IN_PROGRESS` + overdue → stacks a red `OVERDUE` badge beneath the status badge. `IN_PROGRESS` with `return_reason` → stacks a red-orange `RETURNED` badge beneath.
- **Status mutation**: Happens inline on the row cards via callbacks passed from the controller (e.g. `row.setAction(event -> handleMemberAction(ticket, action))`).
- **Smart deadline label**: Overdue tickets show a FontAwesome exclamation icon; completed/resolved tickets are never highlighted even if the deadline has passed.
- **Hover state**: Rows switch background from white to `#f8faff` on mouse enter.

### 2. Member Dashboard — "My Work" Section
- **8-filter system**: The ticket list exposes eight filter buttons — All, Available, To Do, In Progress, Overdue, Returned, Completed, Resolved — each with its own colour-coded active style class.
- **To Do** filter = tickets assigned to the current user in `OPEN` status.
- **Overdue** filter = any non-resolved ticket whose deadline is before `LocalDate.now()`.
- **Returned** filter = member-assigned `IN_PROGRESS` tickets carrying a non-null `return_reason`.
- **Available** filter = unassigned `OPEN` tickets belonging to the member's department (department silo).
- **My Work** label: renamed from "Tickets Table" to reflect that the list also contains department-level unassigned tickets available for volunteering, not only tickets assigned to the member.
- **Action routing**: The `handleMemberAction()` method in `DashboardMemberController` maps each `ButtonAction` enum to its DAO call. Implementations are currently TODO stubs.
- **Deadline sort**: A `ComboBox` above the list lets the member sort tickets by Nearest or Farthest deadline.

### 3. Summary Cards
The Member Dashboard top row contains four summary cards — To Do, In Progress, Completed, Resolved — each displaying:
- A coloured count label (setting the count text).
- An inline overdue indicator: a small FontAwesome red circle + "{N} overdue" text, only shown when at least one overdue ticket exists in that category.
  - `UIStyler` drives the badge colours; the overdue indicator uses `#f55353` for both the icon and the text.
  - `isOverdueInDeadline()` checks `LocalDate.now().isAfter(deadline)` without excluding any status (used only for summary-card counting). The row-level `isOverdue()` additionally skips `RESOLVED` tickets.

### 4. Modals Structure
- **Behavior**: Detail reviews, creations, and edits happen in pop-up modal stages blocking background action (`APPLICATION_MODAL`).
- **Data View Pattern**:
  - If a field is interactive, display traditional controls (`ComboBox`, `TextField`).
  - If a state blocks interaction (e.g., ticket is assigned), hide the dropdown and surface a read-only `Label`. (Do not fall back to disabled dropdowns unless specifically necessary, as it feels restrictive to users).
- **Master Ticket Detail Pattern**: We utilise a singular `BaseTicketDetailModalView.fxml` combined with programmatic conditional rendering. Dynamic states (e.g., "Changes Requested" alert box) collapse their parent layouts gracefully (`setManaged(false)`) when not appropriate to the user's role to prevent layout gaps.
- **Programmatic Badges**: Status and Priority badges are generated via `UIStyler` factory methods.
- **Dynamic Button Bar**: `handleButtons()` calls `getDynamicActionButtonInfo()` to render 0–2 action buttons (BACK on the left, primary on the right). If no primary action exists, the right container is hidden (`setManaged(false)`) to avoid layout gaps.
- **Header Gradient**: `UIStyler.applyNavyBlueHeaderGradient()` applies a `LinearGradient` to the detail modal header, replacing hard-coded hex strings in the style file.
- **Volunteer Banner**: `handleVolunteerMsgBox()` shows a green info bar ("Unassigned · Open for volunteering") at the top of the modal for Volunteer-tagged tickets; hidden otherwise.
- **Changes Requested Notice**: `handleChangesRequestedNotice()` renders a red-toned alert panel ("Changes Requested" + the persisted `return_reason` + returned timestamp) for **Member** users only — hidden for all other roles. The container (and its parent `HBox`) are both hidden via `setManaged(false)` + `setVisible(false)` when not shown.
- **Role-based Sections**: The "Assigned To" and "Activity / Comments" sections are dynamically shown or hidden depending on the current user's role (`MEMBER`, `EDITOR`, `EXECUTIVE`).

### 5. Volunteer Board
- Located on the Member Dashboard below "My Work". Shows up to 8 cards per load.
- **Card**: Small card (`SMALL_CARD_WIDTH = 299px`) with title, description, deadline, priority badge, and a green "Volunteer" pill button. Hover state uses `#f8faff` background.
- **Click interaction**: Clicking anywhere on the card (not the button itself) opens the **Volunteer Ticket View Modal** — the same `BaseTicketDetailModalView.fxml` with the volunteer-specific banner and action button injected.
- **Volunteer button click**: Also opens the Volunteer Ticket View Modal (same route), decoupling from the old direct-assignment behaviour.
- **Volunteer Ticket Detail**: Once the modal is open for a Volunteer-tagged ticket, the green "Unassigned · Open for volunteering" banner is rendered at the top, and the right-button bar shows a `VOLUNTEER` action. The `handleMemberAction()` implementation for `VOLUNTEER` is a TODO stub.
- **Filters**: Volunteer tickets are only listed in the Volunteer Board; they are also reachable via the "Available" filter in "My Work" if they belong to the member's department.

### 6. My Activity Feed
The bottom of the Member Dashboard renders a chronological "My Activity" feed — up to 8 entries — constructed from `Notification` entities. Currently maps every ticket assigned to the member to a `ListRowItem.forActivity` display. Entries show the title + current status in the message label and the computed timestamp.

### 7. Missing States & Interaction Safety
- **Loading States**: Since JDBC connectivity is synchronous on the JavaFX thread, large database fetches inherently freeze the UI.
  - *Recommendation*: Consider `Task` / `Service` background threads for DAO fetches, coupled with a spinning `ProgressIndicator` to improve UX.
- **Empty States**: Review queues should output an aesthetic "No tickets found" card when filtered lists yield 0 bounds. Ensure VBoxes don't appear as empty voids.

### 8. Sidebar Navigation Stub (Member Dashboard)
The left sidebar in `DashboardMemberView.fxml` now has a "Dashboard" row and a "My Tasks" row.
- `DashboardMemberController.setupSideBar()` pre-selects "Dashboard" by applying the `"clicked"` style class.
- `onDashboardRowClicked()` / `onMyTasksRowClicked()` swap the active style class.
- Only the "My Tasks" handler is partially wired: it hides the `rightPane` as a visual effect. The actual "My Tasks" `Pane`/screen implementation is yet to be built.
