package org.bq.task.subscriber;

import org.bq.task.messagequeue.Message;
import org.bq.task.messagequeue.MessageType;
import org.bq.task.messagequeue.Subscriber;

public class RevertString implements Subscriber {

	@Override
	public Message<?> processMessage(Message<?> message) {
		if (message.getMessageType() != MessageType.StringRead) {
			throw new RuntimeException("message.getMessageType() != MessageType.StringRead");
		}
		return new Message<>(MessageType.Write, message.getSource(), reverse((String) message.getPayload()));
	}

	private String reverse(String payload) {
		return (new StringBuilder(payload)).reverse().toString();
	}

}
