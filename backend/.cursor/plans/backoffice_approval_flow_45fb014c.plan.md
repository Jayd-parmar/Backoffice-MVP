---
name: Backoffice Approval Flow
overview: A fresh Kotlin Spring Boot backend with users and applications in MongoDB. A customer saves one application, reviews it, then submits. Approver 1 must approve before Approver 2 can see it. Approver 2 approval sets the application to APPROVED.
todos:
  - id: scaffold
    content: Create a new Kotlin Spring Boot Gradle project with Web, MongoDB, Security, and Validation
    status: completed
  - id: auth-roles
    content: Add users collection with CUSTOMER, APPROVER_1, APPROVER_2 plus POST /api/auth/register and /api/auth/login
    status: completed
  - id: application-form
    content: Applications collection. POST creates DRAFT, GET shows it, POST /api/applications/{id} edits it, POST submit sets PENDING_APPROVER_1
    status: completed
  - id: approver-1
    content: GET /api/approvals/pending for PENDING_APPROVER_1, then approve to PENDING_APPROVER_2 or reject with a comment
    status: completed
  - id: approver-2
    content: Pending list is only PENDING_APPROVER_2. Approve sets APPROVED. Reject sets REJECTED with a comment
    status: completed
isProject: false
---

# Backoffice two-level approval backend

Fresh project. Do not extend the current Spring Boot basics app. Stack: Kotlin, Spring Boot, MongoDB, Spring Security, JWT.

Two collections: `users` and `applications`.

## Customer workflow

`POST /api/applications` saves the four sections as `DRAFT` and returns the application id. The next screen loads that id with GET. Edit is `POST /api/applications/{id}` with the same body. Submit calls `/submit` and sets `PENDING_APPROVER_1`. There is no PUT.

```mermaid
flowchart TD
  register[Register]
  login[Login]
  selectCustomer[Select CUSTOMER]
  enterDetails[Enter personal forex address and travel details]
  createDraft["POST /api/applications status DRAFT"]
  showDetails["GET /api/applications/id"]
  choice{Edit or Submit}
  edit["POST /api/applications/id"]
  submit["POST /api/applications/id/submit"]
  pending1[PENDING_APPROVER_1]

  register --> login --> selectCustomer --> enterDetails --> createDraft --> showDetails --> choice
  choice -->|Edit| edit --> showDetails
  choice -->|Submit| submit --> pending1
```

## Approver 1 workflow

`GET /api/approvals/pending` returns applications in `PENDING_APPROVER_1`. Approve and reject use the application id.

```mermaid
flowchart TD
  login[Login]
  pendingList["GET /api/approvals/pending"]
  openApp[Open customer application]
  review[Review details]
  decision{Approve or Reject}
  approved[PENDING_APPROVER_2]
  sendNext[Send to APPROVER_2]
  rejected[REJECTED]
  comment[Store comment on approval.approver1]

  login --> pendingList --> openApp --> review --> decision
  decision -->|Approve| approved --> sendNext
  decision -->|Reject| rejected --> comment
```

Approve writes `approval.approver1` (`userId`, `action`, `actionAt`) and sets status to `PENDING_APPROVER_2`. Reject sets status to `REJECTED` and stores the reason in `approval.approver1.comment`. A rejected application is not sent to Approver 2.

## Approver 2 workflow

```mermaid
flowchart TD
  login[Login]
  approvedList[See applications in PENDING_APPROVER_2]
  review[Review]
  decision{Approve or Reject}
  approved[APPROVED]
  rejected[REJECTED]
  comment[Store comment on approval.approver2]

  login --> approvedList --> review --> decision
  decision -->|Approve| approved
  decision -->|Reject| rejected --> comment
```

Approver 2's pending list contains only `PENDING_APPROVER_2`. Approve sets status to `APPROVED` and fills `approval.approver2`. Reject sets `REJECTED` and stores the comment on `approval.approver2`.

## users

Role enum: `CUSTOMER`, `APPROVER_1`, `APPROVER_2`.

`password` is the BCrypt hash. Never store or return the plain password.

Customer:

```json
{
  "_id": "ObjectId",
  "email": "jay@example.com",
  "password": "$2a$10$...",
  "role": "CUSTOMER",
  "name": "Jaydip",
  "phone": "9876543210",
  "active": true,
  "createdAt": "2026-10-07T10:00:00Z",
  "updatedAt": "2026-10-07T10:00:00Z"
}
```

Approver (`phone` omitted):

```json
{
  "_id": "ObjectId",
  "email": "approver1@company.com",
  "password": "$2a$10$...",
  "role": "APPROVER_1",
  "name": "Approver One",
  "active": true,
  "createdAt": "2026-10-07T10:00:00Z",
  "updatedAt": "2026-10-07T10:00:00Z"
}
```

Unique index on `email`.

## applications

One application per customer. `customerId` is unique. The API still uses the application id, because GET, the update POST, submit, approve, and reject all target that document.

```json
{
  "_id": "ObjectId",
  "customerId": "ObjectId",
  "status": "DRAFT",
  "personalDetails": {
    "firstName": "Jaydip",
    "lastName": "Parmar",
    "dateOfBirth": "1998-01-01",
    "gender": "MALE",
    "panNumber": "ABCDE1234F",
    "email": "jay@example.com",
    "mobile": "9876543210"
  },
  "forexCardDetails": {
    "cardType": "MULTI_CURRENCY",
    "currency": "USD",
    "amount": 5000,
    "purpose": "TRAVEL"
  },
  "addressDetails": {
    "addressLine1": "ABC Street",
    "city": "Rajkot",
    "state": "Gujarat",
    "country": "India",
    "pincode": "360001"
  },
  "travelDetails": {
    "destinationCountry": "USA",
    "departureDate": "2026-12-01",
    "returnDate": "2026-12-15",
    "purpose": "BUSINESS"
  },
  "approval": {
    "approver1": {
      "userId": null,
      "action": null,
      "comment": null,
      "actionAt": null
    },
    "approver2": {
      "userId": null,
      "action": null,
      "comment": null,
      "actionAt": null
    }
  },
  "createdAt": "2026-10-07T10:00:00Z",
  "updatedAt": "2026-10-07T10:00:00Z"
}
```

`ApplicationStatus`:

- `DRAFT` — saved by POST, still editable with `POST /api/applications/{id}`
- `PENDING_APPROVER_1` — customer submitted; Approver 1 pending list
- `PENDING_APPROVER_2` — Approver 1 approved; Approver 2 pending list
- `APPROVED` — Approver 2 approved
- `REJECTED` — either approver rejected; the comment lives on that approver's block

Submit sets `PENDING_APPROVER_1`. The sample value `SUBMITTED` is not in this enum.

`POST /api/applications/{id}` is allowed while status is `DRAFT` or `REJECTED`. After `REJECTED`, that same call updates the application, and submit clears both approval blocks and sets `PENDING_APPROVER_1`.

Mask `panNumber` in the approver list (last 4 characters). The open-application GET can return the full value to the assigned approver.

## API

Auth, public:

- `POST /api/auth/register` body: `email`, `password`, `role`, `name`, optional `phone`
- `POST /api/auth/login` body: `email`, `password` returns `accessToken`

Customer, JWT required, role `CUSTOMER`. The customer can only read and change their own application.

- `POST /api/applications` creates `DRAFT` and returns the id
- `GET /api/applications/{id}` review screen, all four sections
- `POST /api/applications/{id}` edit, only in `DRAFT` or `REJECTED`. Same body as create. The id in the route is what makes this an update
- `POST /api/applications/{id}/submit` validates every section and sets `PENDING_APPROVER_1`

Approver, JWT required:

- `GET /api/approvals/pending` — Approver 1 receives `PENDING_APPROVER_1`; Approver 2 receives `PENDING_APPROVER_2`
- `POST /api/approvals/{id}/approve`
- `POST /api/approvals/{id}/reject` body: `comment`

`ApprovalService` is the only writer of `status` and `approval`. Approver 2 cannot approve an application still in `PENDING_APPROVER_1`.

## Country approval sequence

Customers register and sign in on port 8081. Approvers register and sign in on port 8082. Submitting copies the destination's document groups onto the review. Each completed group publishes `application.decided`. Nepal finishes after the passport group.

| Destination | Approver 1 | Approver 2 | Approver 3 |
|---|---|---|---|
| India | Aadhaar and PAN | Voter ID and driving licence | Passport |
| UK | Aadhaar and PAN | Passport | Voter ID and driving licence |
| US | Passport | Driving licence | Aadhaar and PAN |
| Nepal | Passport, then approved | — | — |

```mermaid
sequenceDiagram
  participant Customer
  participant UserApp as user_application
  participant Kafka
  participant Backoffice as backoffice_approval
  participant Level1 as ApproverLevel1
  participant Level2 as ApproverLevel2
  participant Level3 as ApproverLevel3

  Customer->>UserApp: Register and login on port 8081
  Level1->>Backoffice: Register APPROVER_1 and login on port 8082
  Level2->>Backoffice: Register APPROVER_2 and login on port 8082
  Level3->>Backoffice: Register APPROVER_3 and login on port 8082

  Customer->>UserApp: Save draft with every document field
  Customer->>UserApp: Submit
  UserApp->>UserApp: Status PENDING_APPROVAL
  UserApp->>Kafka: application.submitted
  Kafka->>Backoffice: Consume submitted application
  Backoffice->>Backoffice: Copy static groups for the destination

  alt Destination is India
    Level1->>Backoffice: Open queue and verify Aadhaar and PAN
    Backoffice->>Kafka: application.decided priority 1 complete
    Kafka->>UserApp: Store verified Aadhaar and PAN
    Level2->>Backoffice: Verify voter ID and driving licence
    Backoffice->>Kafka: application.decided priority 2 complete
    Kafka->>UserApp: Store verified voter ID and driving licence
    Level3->>Backoffice: Verify passport
    Backoffice->>Kafka: application.decided APPROVED
    Kafka->>UserApp: Status APPROVED
  else Destination is Nepal
    Level1->>Backoffice: Verify passport or Approve all
    Backoffice->>Kafka: application.decided APPROVED
    Kafka->>UserApp: Status APPROVED
  end

  alt Approver rejects the current group
    Level1->>Backoffice: Reject with a comment
    Backoffice->>Kafka: application.decided REJECTED
    Kafka->>UserApp: Status REJECTED
  end
```

## How to start

1. New Kotlin Spring Boot Gradle project: Web, Spring Data MongoDB, Security, Validation. Java 17. Database name in `application.properties`.
2. `users` plus register and login. BCrypt password. JWT carries user id and role.
3. `applications` plus the customer create POST, GET, update POST with `{id}`, and submit routes.
4. Approver pending list, approve, and reject, with the status rules above.
