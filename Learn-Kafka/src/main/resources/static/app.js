const form = document.querySelector("#event-form");
const eventList = document.querySelector("#event-list");
const publishButton = document.querySelector("#publish-button");
const formMessage = document.querySelector("#form-message");
const connection = document.querySelector(".connection");
const connectionLabel = document.querySelector("#connection-label");
let refreshInProgress = false;
let lastPayload = "";

const statusLabels = {
	CREATED: "Created",
	PICKED_UP: "Picked up",
	IN_TRANSIT: "In transit",
	OUT_FOR_DELIVERY: "Out for delivery",
	DELIVERED: "Delivered"
};

function element(tag, className, text) {
	const node = document.createElement(tag);
	if (className) node.className = className;
	if (text !== undefined) node.textContent = text;
	return node;
}

function renderEvent(consumedEvent) {
	const event = consumedEvent.event;
	const card = element("article", "event-card");
	const badge = element("span", "partition-badge", `P${consumedEvent.partition}`);
	const content = element("div", "event-main");
	const topline = element("div", "event-topline");
	const statusClass = event.status === "DELIVERED" ? " is-delivered" : event.status === "CREATED" ? " is-created" : "";

	topline.append(
		element("strong", "", event.trackingId),
		element("span", `status-tag${statusClass}`, statusLabels[event.status] || event.status)
	);

	const metadata = element("div", "event-meta");
	metadata.append(
		element("span", "", `offset ${consumedEvent.offset}`),
		element("span", "", `key ${event.trackingId}`),
		element("span", "", event.eventId.slice(0, 8))
	);

	content.append(topline, element("p", "event-location", event.location), metadata);
	card.append(badge, content, element("time", "event-time", new Date(event.occurredAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit", second: "2-digit" })));
	return card;
}

function render(events) {
	eventList.replaceChildren();
	eventList.setAttribute("aria-busy", "false");

	if (events.length === 0) {
		const empty = element("div", "empty-state");
		empty.append(element("h3", "", "Waiting for the first event"), element("p", "", "Use the producer panel to put a parcel update onto the topic."));
		eventList.append(empty);
	} else {
		events.forEach(event => eventList.append(renderEvent(event)));
	}

	const trackingIds = new Set(events.map(item => item.event.trackingId));
	const partitions = new Set(events.map(item => item.partition));
	document.querySelector("#event-count").textContent = events.length;
	document.querySelector("#parcel-count").textContent = trackingIds.size;
	document.querySelector("#partition-count").textContent = `${partitions.size}/3`;
}

async function refreshEvents() {
	if (refreshInProgress) return;
	refreshInProgress = true;
	try {
		const response = await fetch("/api/parcels", { headers: { Accept: "application/json" } });
		if (!response.ok) throw new Error(`API returned ${response.status}`);
		const events = await response.json();
		const payload = JSON.stringify(events);
		if (payload !== lastPayload) {
			render(events);
			lastPayload = payload;
		}
		connection.className = "connection is-online";
		connectionLabel.textContent = "Stream online";
	} catch (error) {
		connection.className = "connection is-offline";
		connectionLabel.textContent = "API unavailable";
		console.error(error);
	} finally {
		refreshInProgress = false;
	}
}

form.addEventListener("submit", async event => {
	event.preventDefault();
	publishButton.disabled = true;
	formMessage.className = "form-message";
	formMessage.textContent = "Publishing event...";

	const data = new FormData(form);
	const trackingId = data.get("trackingId").trim();

	try {
		const response = await fetch(`/api/parcels/${encodeURIComponent(trackingId)}/events`, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ status: data.get("status"), location: data.get("location").trim() })
		});
		if (!response.ok) throw new Error(`Publish failed with status ${response.status}`);
		formMessage.textContent = `Accepted. Kafka is routing ${trackingId}.`;
		setTimeout(refreshEvents, 250);
	} catch (error) {
		formMessage.className = "form-message is-error";
		formMessage.textContent = "Could not publish. Is Kafka running?";
		console.error(error);
	} finally {
		publishButton.disabled = false;
	}
});

refreshEvents();
setInterval(refreshEvents, 2000);
