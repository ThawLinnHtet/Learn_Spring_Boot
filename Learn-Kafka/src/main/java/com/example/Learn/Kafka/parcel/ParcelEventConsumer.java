package com.example.Learn.Kafka.parcel;

import java.time.Instant;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ParcelEventConsumer {

	private final ParcelTimeline timeline;

	public ParcelEventConsumer(ParcelTimeline timeline) {
		this.timeline = timeline;
	}

	@KafkaListener(topics = "${app.kafka.parcel-topic}")
	public void consume(ConsumerRecord<String, ParcelEvent> record) {
		timeline.record(new ConsumedParcelEvent(
				record.value(),
				record.partition(),
				record.offset(),
				Instant.now()
		));
	}
}
