package com.example.Learn.Kafka.parcel;

import java.time.Instant;
import java.util.UUID;

public record ParcelEvent(
		UUID eventId,
		String trackingId,
		ParcelStatus status,
		String location,
		Instant occurredAt
) {
}
