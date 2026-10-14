# Backoffice MVP

Two services. Kafka is the only link between them. Each service has its own MongoDB database and its own users.

## user-application

- Port `8081`
- MongoDB database `user_application`
- Customers register and log in (`CUSTOMER` only)
- `POST /api/applications`, `GET /api/applications/{id}`, `POST /api/applications/{id}`, `POST /api/applications/{id}/submit`
- Publishes `application.submitted` (application id, customer id, status, and the four form sections)
- Consumes `application.decided` (group `user-application`) and updates status and approval history

## backoffice-approval

- Port `8082`
- MongoDB database `backoffice_approval`
- Approvers register and log in (`APPROVER_1` and `APPROVER_2` only)
- `GET /api/approvals/pending`, `GET /api/approvals/{id}`, `POST /api/approvals/{id}/approve`, `POST /api/approvals/{id}/reject`
- Consumes `application.submitted` (group `backoffice-approval`) and stores a review copy of the form
- Publishes `application.decided`

`GET /api/approvals/pending` masks the PAN. `GET /api/approvals/{id}` returns the full PAN to the approver who currently owns that queue.

## Run

Point `MONGODB_URI` at the MongoDB cluster. Each service selects its own database. Set `JWT_SECRET_BASE64` for each process. Kafka defaults to `localhost:9092` (`KAFKA_BOOTSTRAP_SERVERS`).

```bash
./gradlew :user-application:bootRun
./gradlew :backoffice-approval:bootRun
```
