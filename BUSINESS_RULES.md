# Business Rules & Logic

## 1. Ticket Lifecycle and Workflow
Tickets track tasks through the system via an explicitly defined state machine using `TicketStatus` (`OPEN`, `IN_PROGRESS`, `COMPLETED`, `RESOLVED`).

- **OPEN**: Ticket is created but work hasn't explicitly begun. It can be unassigned (available on Volunteer Boards) or assigned to a specific member.
- **IN_PROGRESS**: A Member has acknowledged the assignment and clicked "Start Work".
- **COMPLETED**: The Member has finished the task and submits it for review. The ticket is now awaiting Editor validation.
- **RESOLVED**: An Editor has vetted the completed task and approved it. Action on this ticket is locked.

## 2. Assignment Rules & Workflow
- **Unassigned Tickets (OPEN)**: Will be visible to Members on their Volunteer Board. Members can proactively take ownership ("Volunteer") which assigns it to them.
- **Reassignment Constraints**: 
  - Members **cannot** reassign tickets.
  - Editors/Executives can reassign tickets that are `OPEN` or `IN_PROGRESS`.
  - Reassigning an `IN_PROGRESS` task is an administrative override and requires updating the old assignee's dashboard state gracefully.
  - `RESOLVED` and `COMPLETED` tickets cannot be directly reassigned without changing status. 
- **Approval Rejection**: If an Editor finds a `COMPLETED` ticket unsatisfactory, they return it to `IN_PROGRESS`, placing it back on the original assignee's plate.

## 3. Role Permissions and Access

| Action | Executive | Editor | Member |
|---|---|---|---|
| Create Tickets | Yes | Yes | No |
| Volunteer for Tasks | No | No | Yes |
| Direct Assignment | Yes | Yes (Open/Progress) | No |
| Review (Approve/Reject)| No | Yes | No |
| View System Analytics | Yes | Partial (Own stats) | Individual only |

## 4. UI/Business Synchronization Assumptions
- **"Start Work" Separation**: An `OPEN` ticket assigned to a member will appear in their "My Tasks" but remains `OPEN`. The ticket will transition to `IN_PROGRESS` only when the member explicitly initiates it through the UI, ensuring timeline metrics reflect actual active working time.
- **Department Siloing**: Tickets attached to a specific `department_id` must only pull `User` entities working inside that department when generating dropdown lists for assignment. If a ticket has no department, it is accessible organization-wide.

## 5. Important Edge Cases & Hidden Rules
- **Null Assignments**: In the database, an unassigned ticket is stored with `assigned_to = NULL` (instead of `-1`), maintaining foreign key integrity and representing the true state in SQL.
- **Overdue Detection**: Handled dynamically during UI rendering (e.g., comparing `deadline` to `LocalDate.now()`). Overdue logic is skipped if the ticket is `RESOLVED` or `COMPLETED`. 

