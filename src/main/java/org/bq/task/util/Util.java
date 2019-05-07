package org.bq.task.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Util {

	public static void shutdown(ExecutorService executorService) {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
				if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
					throw new RuntimeException("ExecutorService did not terminate");
				}
			}
		}
		catch (InterruptedException ie) {
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

}
