# Team Process Document

This document defines how our team organizes and collaborates during the project.

---

## 1. Code Review Process

- All code changes must be submitted via **Pull Requests (PRs)**.
- Each PR should:
  - Contain a clear description of the change.
  - Reference the related issue (if applicable).
  - Be small and focused (avoid large, mixed changes).
- At least **one reviewer’s approval** is required before merging.
- Reviewers will check for:
  - Code readability and maintainability.
  - Proper documentation and comments where needed.
  - Tests and correctness of the implementation.

---

## 2. Communication Protocol

- Communication: Telegram message, three times a week: (Sunday, Tuesday and Thursday).
- In person meeting each Monday, from 10 to 12.
- Complex merges or conflicts:
  - The developer encountering the conflict must notify the team immediately on Telegram.
  - If needed, schedule a short call to resolve conflicts together with the concerned developers.
- Important decisions will be documented in a shared document.

---

## 3. Branch Naming Conventions

We will use the following naming conventions:

- `feature/<short-description>` → for new features.  
  Example: `feature/login-page`
- `bugfix/<issue-number>` → for bug fixes.  
  Example: `bugfix/42-crash-on-start`
- `hotfix/<short-description>` → for urgent fixes.  
  Example: `hotfix/missing-env-var`

The `main` branch is always deployable.

---

## 4. Stand-Up Meetings

- The stand up meetings are on Telegram, three times a week. See section 2 above.

---

## 5. Scrum Roles

- Each Sprint will have:
  - **Scrum Master (SM)**: Facilitates meetings, ensures process is followed.
  - **Product Owner (PO)**: Manages backlog, clarifies priorities, aligns features with goals.
- Both SM and PO also contribute as Developers.
- Rotation: Roles will rotate every Sprint to ensure all members gain experience.
- Before each sprint meeting the team decides together what the SM and PO of the week will say during the meeting.

---

## 6. Sprint Planning & Retrospectives

- **Sprint Planning**: At the start of each Sprint, we define Sprint Goals and assign tasks.
- **Sprint Retrospective**: At the end of each Sprint, we discuss:
  - What went well
  - What could be improved
  - Action items for the next Sprint

---

## 7. Definition of Done

A task/feature is considered **Done** when:
- Code is implemented and reviewed.
- Tests (if applicable) are written and passing.
- Documentation is updated.
- The feature/bugfix branch is merged into `main`.

---

**Last Updated:** 29.09.2025
