package org.bq.task.messagequeue;

import java.util.Objects;

public class Message<T> {

	private final MessageType messageType;
	private final String source;
	private final T payload;

	public Message(MessageType messageType, String source, T payload) {
		this.messageType = messageType;
		this.source = source;
		this.payload = payload;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public String getSource() {
		return source;
	}

	public T getPayload() {
		return payload;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Message<?> message = (Message<?>) o;
		return messageType == message.messageType &&
			Objects.equals(source, message.source) &&
			Objects.equals(payload, message.payload);
	}

	@Override
	public int hashCode() {
		return Objects.hash(messageType, source, payload);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append(" {messageType=").append(messageType);
		sb.append(", source=\"").append(source).append("\"");
		sb.append(", payload=").append(payload);
		sb.append("}");
		return sb.toString();
	}

}