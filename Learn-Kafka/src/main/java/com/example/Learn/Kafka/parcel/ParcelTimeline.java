package com.example.Learn.Kafka.parcel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

@Component
public class ParcelTimeline {

	private final Map<String, List<ConsumedParcelEvent>> eventsByTrackingId = new ConcurrentHashMap<>();

	public void record(ConsumedParcelEvent consumedEvent) {
		eventsByTrackingId
				.computeIfAbsent(consumedEvent.event().trackingId(), ignored -> new CopyOnWriteArrayList<>())
				.add(consumedEvent);
	}

	public List<ConsumedParcelEvent> findByTrackingId(String trackingId) {
		return List.copyOf(eventsByTrackingId.getOrDefault(trackingId, List.of()));
	}

	public List<ConsumedParcelEvent> findAll() {
		return eventsByTrackingId.values().stream()
				.flatMap(List::stream)
				.sorted((left, right) -> right.consumedAt().compareTo(left.consumedAt()))
				.toList();
	}
}
