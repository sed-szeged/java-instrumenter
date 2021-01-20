package hu.szte.sed.util;

public enum Granularity {

	BINARY(1, "binary"), COUNT(2, "count"), CHAIN(4, "chain");

	private int id;
	private String text;

	private Granularity(final int id, final String text) {
		this.id = id;
		this.text = text;
	}

	public int getID() {
		return id;
	}

	public String getText() {
		return text;
	}

	public static Granularity fromText(final String text) {
		switch (text) {
			case "binary":
				return BINARY;
			case "count":
				return COUNT;
			case "chain":
				return CHAIN;
			default:
				throw new IllegalArgumentException(String.format("Invalid granularity value '%s'", text));
		}
	}

}
