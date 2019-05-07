package org.bq.task.messagequeue;

public enum MessageType {

	StringRead(Constants.STRING_READ),
	ArrayRead(Constants.ARRAY_READ),
	Write(Constants.WRITE);

	private static class Constants {
		private static final String STRING_READ = "STRING_READ";
		private static final String ARRAY_READ = "ARRAY_READ";
		private static final String WRITE = "WRITE";
	}

	private String label;

	MessageType(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" (\"").append(label).append("\"");
		sb.append(")");
		return sb.toString();
	}

}
