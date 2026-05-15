# UI / UX Guidelines

## Design Principles
The JavaFX application targets an organizational productivity tool standard: clean, heavily padded, card-driven lists, and distinct color-coded status badges. 

## Layout Hierarchy
- **Left Sidebar**: High-contrast, dark-themed navigation menu (e.g. `#121d49`) containing the system brand, page selection, and primary profile/logout functions. Used across all layout base views (`MainView`, `DashboardView`).
- **Main Content Anchors**: Soft, light backgrounds (e.g. `#eef2fb`) housing distinct bounding cards (white background, rounded corners radius `14` or `16`). 

## Typography & Color Conventions
- **Font**: Inter (Custom loaded via `.ttf` files). Heavy usage of font-weights (Bold, Black) to denote hierarchy.
- **Status/Action Colors**:
  - `RESOLVED` / Approved: Green (`#4bcc8a`)
  - `COMPLETED` / Awaiting Review: Yellow/Orange (`#ff9900`)
  - `IN_PROGRESS`: Blue (`#2f95ff`)
  - `OPEN` / Send Back / Overdue: Red (`#ef4c56` / `#f14d5a`)
  - **Neutral / Text**: Dark Navy (`#1c2b63` for primary titles, `#9faad2` or `#6f7aa7` for subtitles and metadata).

## Core UX Patterns

### 1. Ticket Card Rows (`ListRowItem`)
- Avoid standard JavaFX `TableView` for ticket displays. Instead, use custom `VBox`/`HBox` driven row components (`ListRowItem`) loaded into `ScrollPane`. 
- **Action Buttons**: Kept to the right side of the row. Use rounded pill buttons.
- State mutation happens inline on the row cards via callbacks passed from the controller (e.g. `row.setAction(...)`).

### 2. Modals Structure
- **Behavior**: Detail reviews, creations, and edits happen in pop-up modal stages blocking background action (`APPLICATION_MODAL`).
- **Data View Pattern**: 
  - If a field is interactive, display traditional controls (`ComboBox`, `TextField`).
  - If a state blocks interaction (e.g., ticket is assigned), hide the dropdown and surface a read-only `Label`. (Do not fall back to disabled dropdowns unless specifically necessary, as it feels restrictive to users).

### 3. Missing States & Interaction Safety
- **Loading States**: Since JDBC connectivity is synchronous on the JavaFX thread, large database fetches inherently freeze the UI. 
  - *Recommendation*: Consider `Task`/`Service` background threads for DAO fetches, coupled with a spinning `ProgressIndicator` to improve UX.
- **Empty States**: Review queues should output an aesthetic "No tickets found" card when filtered lists yield 0 bounds. Ensure VBoxes don't appear as empty voids. 

