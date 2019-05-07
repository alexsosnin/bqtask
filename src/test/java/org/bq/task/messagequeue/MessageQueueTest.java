package org.bq.task.messagequeue;

import org.bq.task.reader.ArrayReader;
import org.bq.task.reader.StringReader;
import org.bq.task.subscriber.Calculator;
import org.bq.task.subscriber.RevertString;
import org.bq.task.subscriber.Writer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class MessageQueueTest {

	private static final String ROOT_PATH = "./test.files.folder/";
	private static final String CHARSET_NAME = StandardCharsets.UTF_8.name();

	private static final String STRINGS_SRC = "strings.src";
	private static final String STRING_DELIMITER = "\r\n";

	private static final String NUMBERS_SRC = "numbers.src";
	private static final String NUMBERS_DELIMITER = " ";

	private static final String DST = ".dst";

	private String stringsFileContent;
	private int[] numbers;

	private MessageQueue messageQueue;

	@Before
	public void setUp() throws Exception {
		Path rootPath = Paths.get(ROOT_PATH);
		Files.createDirectories(rootPath);
		assertTrue(Files.exists(rootPath));

		stringsFileContent = String.join(STRING_DELIMITER,
			"В Edge появится режим IE mode, ",
			"который позволит запускать Internet Explorer ",
			"непосредственно во вкладке Microsoft Edge. ",
			"Это шаг навстречу корпоративным клиентам, ",
			"которые не желают отказываться от Internet Explorer"
		);
		Path stringsSrcPath = Paths.get(ROOT_PATH, STRINGS_SRC);
		Files.write(stringsSrcPath, stringsFileContent.getBytes(CHARSET_NAME));
		assertTrue(Files.exists(stringsSrcPath));

		numbers = new int[]{25, 10, 35, 20, 65, 30, 15};
		String numbersFileContent = IntStream.of(numbers)
			.mapToObj(String::valueOf)
			.collect(Collectors.joining(NUMBERS_DELIMITER));
		Path numbersSrcPath = Paths.get(ROOT_PATH, NUMBERS_SRC);
		Files.write(numbersSrcPath, numbersFileContent.getBytes(CHARSET_NAME));
		assertTrue(Files.exists(numbersSrcPath));
	}

	@After
	public void tearDown() throws Exception {
		Path rootPath = Paths.get(ROOT_PATH);
		Files.walk(rootPath)
			.sorted(Comparator.reverseOrder())
			.map(Path::toFile)
			.forEach(File::delete);
		assertFalse(Files.exists(rootPath));
	}

	@Test
	public void testMessageQueue() {
		try {
			messageQueue = new MessageQueue(100);
			messageQueue.addSubscriber(MessageType.StringRead, new RevertString());
			messageQueue.addSubscriber(MessageType.ArrayRead, new Calculator());
			messageQueue.addSubscriber(MessageType.Write, new Writer(ROOT_PATH));

			run(() -> (new StringReader()).processFile(ROOT_PATH, STRINGS_SRC, CHARSET_NAME, messageQueue));
			run(() -> (new ArrayReader()).processFile(ROOT_PATH, NUMBERS_SRC, CHARSET_NAME, messageQueue));

			sleep(5000);

			String[] stringsDataArray = stringsFileContent.split(STRING_DELIMITER);
			for (int i = 0; i < stringsDataArray.length; i++) {
				String expected = (new StringBuilder(stringsDataArray[i])).reverse().toString();
				Path path = Paths.get(ROOT_PATH, STRINGS_SRC + "." + i + DST);
				assertTrue(Files.exists(path));
				String actual = new String(Files.readAllBytes(path), CHARSET_NAME);
				assertEquals(expected, actual);
			}

			int expectedNumbersSum = IntStream.of(numbers).sum();
			Path path = Paths.get(ROOT_PATH, NUMBERS_SRC + DST);
			assertTrue(Files.exists(path));
			int actualNumbersSum = Integer.valueOf(new String(Files.readAllBytes(path), CHARSET_NAME));
			assertEquals(expectedNumbersSum, actualNumbersSum);
		}
		catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testMessageQueueRemoveSubscriber() {
		try {
			messageQueue = new MessageQueue(100);
			messageQueue.addSubscriber(MessageType.StringRead, new RevertString());
			messageQueue.addSubscriber(MessageType.ArrayRead, new Calculator());
			Writer writer = new Writer(ROOT_PATH);
			messageQueue.addSubscriber(MessageType.Write, writer);
			messageQueue.removeSubscriber(MessageType.Write, writer);

			run(() -> (new StringReader()).processFile(ROOT_PATH, STRINGS_SRC, CHARSET_NAME, messageQueue));
			run(() -> (new ArrayReader()).processFile(ROOT_PATH, NUMBERS_SRC, CHARSET_NAME, messageQueue));

			sleep(5000);

			String[] stringsDataArray = stringsFileContent.split(STRING_DELIMITER);
			for (int i = 0; i < stringsDataArray.length; i++) {
				Path path = Paths.get(ROOT_PATH, STRINGS_SRC + "." + i + DST);
				assertFalse(Files.exists(path));
			}

			Path path = Paths.get(ROOT_PATH, NUMBERS_SRC + DST);
			assertFalse(Files.exists(path));
		}
		catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	private void run(Runnable runnable) {
		(new Thread(runnable)).start();
	}

	private void sleep(long timeout) {
		CountDownLatch latch = new CountDownLatch(1);
		try {
			latch.await(timeout, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Got interrupted", e);
		}
	}

}