package com.example.Learn.Kafka.parcel;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Validated
@RequestMapping("/api/parcels")
public class ParcelController {

	private final KafkaTemplate<String, ParcelEvent> kafkaTemplate;
	private final ParcelTimeline timeline;
	private final String parcelTopic;

	public ParcelController(
			KafkaTemplate<String, ParcelEvent> kafkaTemplate,
			ParcelTimeline timeline,
			@Value("${app.kafka.parcel-topic}") String parcelTopic
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.timeline = timeline;
		this.parcelTopic = parcelTopic;
	}

	@PostMapping("/{trackingId}/events")
	public ResponseEntity<ParcelEvent> publish(
			@PathVariable @Size(max = 40) @Pattern(regexp = "[A-Za-z0-9-]+") String trackingId,
			@Valid @RequestBody CreateParcelEventRequest request
	) {
		String normalizedTrackingId = trackingId.toUpperCase(Locale.ROOT);
		ParcelEvent event = new ParcelEvent(
				UUID.randomUUID(),
				normalizedTrackingId,
				request.status(),
				request.location().trim(),
				Instant.now()
		);
		try {
			kafkaTemplate.send(parcelTopic, normalizedTrackingId, event).get(5, TimeUnit.SECONDS);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Kafka publish interrupted", exception);
		} catch (ExecutionException | TimeoutException | RuntimeException exception) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Kafka did not accept the event", exception);
		}
		return ResponseEntity.accepted().body(event);
	}

	@GetMapping("/{trackingId}")
	public List<ConsumedParcelEvent> timeline(@PathVariable String trackingId) {
		return timeline.findByTrackingId(trackingId);
	}

	@GetMapping
	public List<ConsumedParcelEvent> allEvents() {
		return timeline.findAll();
	}
}
