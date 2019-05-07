package org.bq.task.reader;

import org.bq.task.messagequeue.Message;
import org.bq.task.messagequeue.MessageQueue;
import org.bq.task.messagequeue.MessageType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Arrays;

public class ArrayReader extends Reader {

	@Override
	protected void processFile(Path path, String charsetName, MessageQueue messageQueue) throws IOException {
		String[] stringArray = (new String(Files.readAllBytes(path), charsetName)).split("\\s");
		int[] intArray = Arrays.stream(stringArray).mapToInt(Integer::parseInt).toArray();
		messageQueue.putMessage(new Message<>(MessageType.ArrayRead, MessageFormat.format("{0}", path.getFileName()), intArray));
	}

}
