package org.bq.task.reader;

import org.bq.task.messagequeue.Message;
import org.bq.task.messagequeue.MessageQueue;
import org.bq.task.messagequeue.MessageType;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;

public class StringReader extends Reader {

	@Override
	protected void processFile(Path path, String charsetName, MessageQueue messageQueue) throws IOException {
		List<String> strings = Files.readAllLines(path, Charset.forName(charsetName));
		for (int i = 0; i < strings.size(); i++) {
			messageQueue.putMessage(new Message<>(MessageType.StringRead, MessageFormat.format("{0}.{1}", path.getFileName(), i), strings.get(i)));
		}
	}

}
