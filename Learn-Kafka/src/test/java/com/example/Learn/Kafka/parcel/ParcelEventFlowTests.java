package com.example.Learn.Kafka.parcel;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(
		partitions = 3,
		topics = "parcel-events",
		bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class ParcelEventFlowTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void dashboardIsAvailableAtTheApplicationRoot() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(forwardedUrl("index.html"));

		mockMvc.perform(get("/index.html"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Parcel Pulse")));
	}

	@Test
	void sameParcelEventsStayOrderedInOnePartition() throws Exception {
		mockMvc.perform(post("/api/parcels/TRK-101/events")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "status": "IN_TRANSIT",
						  "location": "Yangon Hub"
						}
						"""))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.trackingId").value("TRK-101"))
				.andExpect(jsonPath("$.status").value("IN_TRANSIT"));

		mockMvc.perform(post("/api/parcels/TRK-101/events")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "status": "DELIVERED",
						  "location": "Customer"
						}
						"""))
				.andExpect(status().isAccepted());

		Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
		String timeline = "";
		while (Instant.now().isBefore(deadline)) {
			timeline = mockMvc.perform(get("/api/parcels/TRK-101"))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();
			if (timeline.contains("Customer")) {
				break;
			}
			Thread.sleep(100);
		}

		mockMvc.perform(get("/api/parcels/TRK-101"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].event.trackingId").value("TRK-101"))
				.andExpect(jsonPath("$[0].event.location").value("Yangon Hub"))
				.andExpect(jsonPath("$[1].event.status").value("DELIVERED"))
				.andExpect(jsonPath("$[0].partition").isNumber())
				.andExpect(jsonPath("$[0].offset").isNumber());

		List<Integer> partitions = JsonPath.read(timeline, "$[*].partition");
		List<Integer> offsets = JsonPath.read(timeline, "$[*].offset");
		org.assertj.core.api.Assertions.assertThat(partitions).containsOnly(partitions.getFirst());
		org.assertj.core.api.Assertions.assertThat(offsets.get(1)).isGreaterThan(offsets.getFirst());
	}

	@Test
	void blankLocationIsRejectedBeforePublishing() throws Exception {
		mockMvc.perform(post("/api/parcels/TRK-INVALID/events")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "status": "IN_TRANSIT",
						  "location": "   "
						}
						"""))
				.andExpect(status().isBadRequest());
	}
}
