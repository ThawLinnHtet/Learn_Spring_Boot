package com.example.Learn.Kafka.parcel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateParcelEventRequest(
		@NotNull ParcelStatus status,
		@NotBlank @Size(max = 80) String location
) {
}
