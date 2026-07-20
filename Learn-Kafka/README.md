# Parcel Pulse

Parcel Pulse is a small Kafka learning project built with Spring Boot. Publish parcel status updates from the browser and watch the consumer timeline expose each record's partition and offset.

## What It Demonstrates

- Producing JSON events with `KafkaTemplate`
- Using `trackingId` as a message key
- Preserving per-parcel ordering through Kafka partitions
- Consuming events with `@KafkaListener`
- Reading partition and offset metadata from `ConsumerRecord`
- Testing the complete HTTP-to-Kafka flow with an embedded broker

## Event Flow

```text
Browser
  -> POST /api/parcels/{trackingId}/events
  -> parcel-events topic (3 partitions)
  -> parcel-pulse-dashboard consumer group
  -> in-memory timeline
  -> GET /api/parcels
  -> Browser
```

Events with the same tracking ID use the same Kafka key. Kafka therefore routes them to the same partition and keeps their order within that partition.

## Run Locally

Requirements:

- Java 25
- Docker with Docker Compose

Start Kafka:

```powershell
docker compose up -d
```

Wait until the broker is healthy, then start Spring Boot:

```powershell
.\mvnw.cmd spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080).

Stop Kafka when finished:

```powershell
docker compose down
```

Use `docker compose down -v` only when you also want to delete the local Kafka data.

## API

Publish an event:

```http
POST /api/parcels/TRK-101/events
Content-Type: application/json

{
  "status": "IN_TRANSIT",
  "location": "Yangon Hub"
}
```

Read all consumed events:

```http
GET /api/parcels
```

Read one parcel's timeline:

```http
GET /api/parcels/TRK-101
```

Supported statuses are `CREATED`, `PICKED_UP`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, and `DELIVERED`.

## Tests

```powershell
.\mvnw.cmd test
```

The event-flow test starts an embedded Kafka broker, so the Docker broker is not required for tests.

## Learning Limitation

The consumed timeline is intentionally stored in memory. Restarting the application clears the dashboard projection even though Kafka still retains its records. Persisting the projection or deliberately replaying the topic is a useful next learning slice.
