package org.bq.task.messagequeue;

import org.bq.task.util.Util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.*;

public class MessageQueue {

	private static final String GOT_INTERRUPTED = "Got interrupted";

	private EnumMap<MessageType, List<Subscriber>> subscribers;
	private LinkedBlockingQueue<Message<?>> queue = new LinkedBlockingQueue<>();

	private ScheduledExecutorService scheduledExecutorService;
	private ExecutorService executorService;

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		Util.shutdown(scheduledExecutorService);
		Util.shutdown(executorService);
	}

	public MessageQueue(int queueProcessingTimeout) {
		subscribers = new EnumMap<>(MessageType.class);
		for (MessageType item : EnumSet.allOf(MessageType.class)) {
			subscribers.put(item, new CopyOnWriteArrayList<>());
		}

		executorService = Executors.newCachedThreadPool();
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

		scheduledExecutorService.scheduleWithFixedDelay(this::processQueue, 0, queueProcessingTimeout, TimeUnit.MILLISECONDS);
	}

	private void processQueue() {

		if (!queue.isEmpty()) {
			Message<?> queueMessage = MessageQueue.this.take();

			List<Subscriber> messageTypeSubscribers = MessageQueue.this.subscribers.get(queueMessage.getMessageType());

			if (!messageTypeSubscribers.isEmpty()) {

				List<Callable<Message<?>>> tasks = new ArrayList<>(messageTypeSubscribers.size());
				for (Subscriber subscriber : messageTypeSubscribers) {
					tasks.add(() -> subscriber.processMessage(queueMessage));
				}

				CompletionService<Message<?>> completionService = new ExecutorCompletionService<>(executorService);

				for (Callable<Message<?>> task : tasks) {
					completionService.submit(task);
				}

				for (int i = 0; i < tasks.size(); i++) {
					try {
						Message<?> newMessage = completionService.take().get();
						if (newMessage != null) {
							MessageQueue.this.putMessage(newMessage);
						}
					}
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						throw new RuntimeException(GOT_INTERRUPTED, e);
					}
					catch (ExecutionException e) {
						throw new RuntimeException(e);
					}
				}

			}

		}

	}

	public void addSubscriber(MessageType messageType, Subscriber subscriber) {
		subscribers.get(messageType).add(subscriber);
	}

	public void removeSubscriber(MessageType messageType, Subscriber subscriber) {
		subscribers.get(messageType).remove(subscriber);
	}

	public void putMessage(Message<?> message) {
		try {
			queue.put(message);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(GOT_INTERRUPTED, e);
		}
	}

	private Message<?> take() {
		try {
			return queue.take();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(GOT_INTERRUPTED, e);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append(" {subscribers=").append(subscribers);
		sb.append(", queue=").append(queue);
		sb.append("}");
		return sb.toString();
	}

}
