package org.bq.task.subscriber;

import org.bq.task.messagequeue.Message;
import org.bq.task.messagequeue.MessageType;
import org.bq.task.messagequeue.Subscriber;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Writer implements Subscriber {

	private static final String DST = ".dst";

	private String rootPath;

	public Writer(String rootPath) {
		this.rootPath = rootPath;
	}

	@Override
	public Message<?> processMessage(Message<?> message) {
		if (message.getMessageType() != MessageType.Write) {
			throw new RuntimeException("message.getMessageType() != MessageType.Write");
		}
		String source = message.getSource();
		String payload = String.valueOf(message.getPayload());
		try {
			Files.write(Paths.get(rootPath, source + DST), payload.getBytes());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

}
