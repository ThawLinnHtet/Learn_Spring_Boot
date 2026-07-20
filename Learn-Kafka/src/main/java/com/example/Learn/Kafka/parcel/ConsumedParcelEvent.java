package com.example.Learn.Kafka.parcel;

import java.time.Instant;

public record ConsumedParcelEvent(
		ParcelEvent event,
		int partition,
		long offset,
		Instant consumedAt
) {
}
