package org.bq.task.reader;

import org.bq.task.messagequeue.MessageQueue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Reader {

	public void processFile(String rootPath, String fileName, String charsetName, MessageQueue messageQueue) {
		Path path = Paths.get(rootPath, fileName);
		if (Files.exists(path)) {
			try {
				processFile(path, charsetName, messageQueue);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected abstract void processFile(Path path, String charsetName, MessageQueue messageQueue) throws IOException;

}
