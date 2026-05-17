# Business Rules & Logic

## 1. Ticket Lifecycle and Workflow
Tickets track tasks through the system via an explicitly defined state machine using `TicketStatus` (`OPEN`, `IN_PROGRESS`, `COMPLETED`, `RESOLVED`).

- **OPEN**: Ticket is created but work hasn't explicitly begun. It can be unassigned (available on Volunteer Boards) or assigned to a specific member.
- **IN_PROGRESS**: A Member has acknowledged the assignment and clicked "Start Work".
- **COMPLETED**: The Member has finished the task and submits it for review. The ticket is now awaiting Editor validation.
- **RESOLVED**: An Editor has vetted the completed task and approved it. Action on this ticket is locked.
- **RETURNED** (informal / UI label): An Editor found a `COMPLETED` ticket unsatisfactory and set the ticket back to `IN_PROGRESS`, adding a non-null `return_reason`. In the UI the status still reads "In Progress" but a "RETURNED" badge (warm red-orange, handled by `UIStyler.makeReturnedBadge()`) is displayed beneath it. A "Changes Requested" alert panel is conditionally rendered for the ticket's **Member** inform them of the required revisions (`BaseTicketDetailModalController.handleChangesRequestedNotice()`).
  - The modal-level `ButtonAction` broker maps this state to `RESUBMIT`, surfaced **only** to the `MEMBER` to whom it is returned.

## 2. Assignment Rules & Workflow
- **Unassigned Tickets (OPEN)**: Will be visible to Members on their Volunteer Board. Members can proactively take ownership ("Volunteer") which assigns it to them.
- **Reassignment Constraints**:
  - Members **cannot** reassign tickets.
  - Editors/Executives can reassign tickets that are `OPEN` or `IN_PROGRESS`.
  - Reassigning an `IN_PROGRESS` task is an administrative override and requires updating the old assignee's dashboard state gracefully.
  - `RESOLVED` and `COMPLETED` tickets cannot be directly reassigned without changing status.
- **Approval Rejection**: If an Editor finds a `COMPLETED` ticket unsatisfactory, they return it to `IN_PROGRESS`, placing it back on the original assignee's plate.
- **Volunteer Eligibility** (UI-level only): A ticket is considered a "Volunteer" ticket in the UI (`BaseTicketDetailModalController.isVolunteerTicket()`) when it satisfies all three:
  1. Department name is `"Volunteer"`.
  2. `assigned_to` is `NULL` (unassigned).
  3. Status is `OPEN`.
  This mirrors the `dept = 'Volunteer'` convention from the database and powers the Volunteer Board listing and the "Volunteer" action button.
- **Department Siloing**: Tickets attached to a specific `department_id` must only pull `User` entities working inside that department when generating dropdown lists for assignment. If a ticket has no department, it is accessible organization-wide.
  - In the Member Dashboard the "Available" filter uses `BaseDashboardController.isAvailableUnderDept()` to restrict the list to unassigned `OPEN` tickets belonging to the member's own department, enforced by `DepartmentDAO.getDepartmentNameByID()`.

## 3. Member Dashboard Filtering & Action Rules
The following state machine is resolved on every filter load by `getDynamicActionButtonInfo()` (shared between `ListRowItem`, `DashboardMemberController`, and `BaseTicketDetailModalController`):

| Condition | Member Action Button |
|---|---|
| `COMPLETED` / `RESOLVED` | `VIEW_DETAILS` (read-only) |
| `IN_PROGRESS` && overdue | `SUBMIT_LATE` (late submission) |
| `IN_PROGRESS` && `return_reason` non-null | `RESUBMIT` (re-submit after rejection) |
| `IN_PROGRESS` (active) | `SUBMIT` |
| `OPEN` && deadline passed | `SUBMIT_LATE` |
| `OPEN` && assigned to current user | `START_TASK` |
| `OPEN` && unassigned && same department | `TAKE` |
| `OPEN` && volunteer dept tag | `VOLUNTEER` |
| Fallback | `START_TASK` |

The `SUBMIT_LATE` state is the same as `SUBMIT` in terms of DAO mutation (IN_PROGRESS → COMPLETED, `last_updated` set to now) — the label change is purely a UX affordance.

**Dynamic action button dispatch** (`DashboardMemberController.` `handleMemberAction()`):
```
TAKE       → TicketDAO.assignTicket(userId, ticketId)
START_TASK → TicketDAO.updateTicketStatus(ticketId, IN_PROGRESS)
SUBMIT     → TicketDAO.updateTicketStatus + set last_updated → COMPLETED
RESUBMIT   → TicketDAO.updateTicketStatus + set last_updated → COMPLETED
SUBMIT_LATE→ TicketDAO.updateTicketStatus + set last_updated → COMPLETED
VIEW_DETAILS→ openTicketDetailModal (no mutation)
```
All of the above are currently **TODO** stubs in both `DashboardMemberController` and `BaseTicketDetailModalController` — the DAO calls have not been wired in yet.

## 4. Overdue Detection
- **Row-level** (`BaseDashboardController.isOverdue()`): `LocalDate.now().isAfter(deadline.toLocalDate())`. Tickets with status `COMPLETED` or `RESOLVED` are explicitly excluded.
- **Summary-cards** (`DashboardMemberController.isOverdueInDeadline()`): Same date comparison but does **not** exclude `COMPLETED`/`RESOLVED`, reflecting the design intent to always surface an overdue count on each summary card regardless of status.

## 5. Role Permissions and Access

| Action | Executive | Editor | Member |
|---|---|---|---|
| Create Tickets | Yes | Yes | No |
| Volunteer for Tasks | No | No | Yes |
| Direct Assignment | Yes | Yes (Open/Progress) | No |
| Review (Approve/Reject)| No | Yes | No |
| View System Analytics | Yes | Partial (Own stats) | Individual only |

## 6. Important Edge Cases & Hidden Rules
- **Null Assignments**: In the database, an unassigned ticket is stored with `assigned_to = NULL` (instead of `-1`), maintaining foreign key integrity and representing the true state in SQL.
- **Overdue Detection**: Handled dynamically during UI rendering (e.g., comparing `deadline` to `LocalDate.now()`). Row-level overdue logic skips `RESOLVED` or `COMPLETED` tickets; summary-card overdue counting does not.
- **Changes Requested Notification**: When an Editor rejects a completed ticket and sets it back to `IN_PROGRESS` (with `return_reason` filled), a dynamic alert block appears exclusively for the ticket's **MEMBER** inside the detail modal. The alert is completely hidden from `EXECUTIVE` and `EDITOR`.
- **Volunteer Department Tag**: The front-end treats a ticket as a Volunteering opportunity when `department_name = "Volunteer"`, status is `OPEN`, and `assigned_to` is `NULL`. The Volunteer Board and modal banner react to this triple condition.
- **"Start Work" Separation**: An `OPEN` ticket assigned to a member will appear in their "My Tasks" but remains `OPEN`. The ticket will transition to `IN_PROGRESS` only when the member explicitly initiates it through the UI, ensuring timeline metrics reflect actual active working time.
