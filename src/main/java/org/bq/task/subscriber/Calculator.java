package org.bq.task.subscriber;

import org.bq.task.messagequeue.Message;
import org.bq.task.messagequeue.MessageType;
import org.bq.task.messagequeue.Subscriber;

import java.util.stream.IntStream;

public class Calculator implements Subscriber {

	@Override
	public Message<?> processMessage(Message<?> message) {
		if (message.getMessageType() != MessageType.ArrayRead) {
			throw new RuntimeException("message.getMessageType() != MessageType.ArrayRead");
		}
		return new Message<>(MessageType.Write, message.getSource(), sum((int[]) message.getPayload()));
	}

	private int sum(int[] payload) {
		return IntStream.of(payload).sum();
	}

}
