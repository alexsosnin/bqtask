package org.bq.task.messagequeue;

public interface Subscriber {

	Message<?> processMessage(Message<?> message);

}
