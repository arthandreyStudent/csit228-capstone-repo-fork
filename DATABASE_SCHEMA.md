# Database Schema

## Overview
The database uses a relational model (MySQL) focused around Users, Tickets, and organizational structure (Departments, Jobs).

## Primary Entities

### 1. `user`
- Main identity table holding authentication and profiling logic.
- **Key Columns**:
  - `id` (PK, INT, Auto-increment)
  - `password_hash`, `username` (Credentials)
  - `firstname`, `lastname` (Display names)
  - `user_type` (FK to `type.id` mapping the hierarchical role: Member, Editor, Exec)
  - `department_id` (FK to `department.id`)

### 2. `ticket`
- Core operational table holding state and deadlines.
- **Key Columns**:
  - `id` (PK)
  - `title`, `description`
  - `priority` (ENUM: LOW, MEDIUM, HIGH, etc.)
  - `status` (ENUM: OPEN, IN_PROGRESS, COMPLETED, RESOLVED)
  - `created_by` (FK to `user.id`) - Who spawned the task.
  - `assigned_to` (FK to `user.id`, Nullable) - Current owner.
  - `department_id` (FK to `department.id`, Nullable)
  - `return_reason` (TEXT, Nullable) - Freeform rejection note filled by an Editor when returning a `COMPLETED` ticket to `IN_PROGRESS`. Non-null value surfaces the "RETURNED" badge and "Changes Requested" notice in the Member detail modal.
  - `date_created`, `last_updated`, `deadline` (DATETIME)

### 3. `department`
- Organizational buckets for scoping tickets and users.
- **Key Columns**: `id`, `name`, `description`.

### 4. `comment` (Inferred functionality)
- Allows conversational threading on tickets.
- **Key Columns**: `id`, `ticket_id` (FK to `ticket.id`), `user_id` (FK to `user.id`), `content`, `date_created`.

### 5. Types & Jobs (Taxonomy)
- `type`: Defines access roles (`role`: Executive, Editor, Member).
- `job`: Granular titles associated dynamically with departments via `job_department` associating to `user_job`.

## Relationships & Cardinality
- **User to Department (1:N)**: A department has multiple users.
- **User to Ticket (1:N)**: A user can create many tickets; a user can be assigned to many tickets.
- **Department to Ticket (1:N)**: A specific department can be targeted by multiple tickets.
- **Ticket to Comment (1:N)**: A ticket holds multiple conversational artifacts.

## Architecture Observations & Indexing Concerns
- **Heavy View Reliance**: The `TicketDAO` handles aggregations natively in SQL by joining `ticket t`, `user u`, and `department d`.
  - *Recommendation*: Ensure indexes exist on `department_id`, `created_by`, `assigned_to`, and `status` in the `ticket` table to prevent full table scans when dashboards load.
- **Data Integrity**: `assigned_to` correctly supports `NULL` to reflect the "unassigned/volunteer-ready" state.

