package com.example.Learn.Kafka.parcel;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {

	@Bean
	NewTopic parcelEventsTopic(@Value("${app.kafka.parcel-topic}") String topicName) {
		return TopicBuilder.name(topicName)
				.partitions(3)
				.replicas(1)
				.build();
	}
}
