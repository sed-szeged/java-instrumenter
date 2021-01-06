package hu.szte.sed;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import hu.szte.sed.coverage.BinaryMethodCoverageData;
import hu.szte.sed.coverage.ChainCoverageData;
import hu.szte.sed.coverage.CoverageData;
import hu.szte.sed.coverage.NumericMethodCoverageData;
import hu.szte.sed.util.Options;

public class SZTELogger {

	private static SZTELogger instance;

	private final Options options;
	private final Map<Long, CoverageData<Short>> data = new TreeMap<>();

	private boolean started = false;

	private SZTELogger(final Options opts) {
		options = opts;
	}

	private void createEmptyCoverageDataForThread(final long threadId) {
		switch (options.getGranularity()) {
			case BINARY:
				data.put(threadId, new BinaryMethodCoverageData<Short>());
				break;
			case COUNT:
				data.put(threadId, new NumericMethodCoverageData<Short>());
				break;
			case CHAIN:
				data.put(threadId, new ChainCoverageData<Short>());
				break;
			default:
				throw new IllegalArgumentException();
		}
	}

	public static synchronized void setup(final Options opts) throws IOException {
		if (instance == null) {
			instance = new SZTELogger(opts);
		}

		instance.createEmptyCoverageDataForThread(1L);
	}

	public static synchronized void start() {
		instance.started = true;
	}

	public static synchronized void quit() {
		instance.started = false;
	}

	public static synchronized void enter(final long threadId, final short id) {
		if (!instance.started) {
			return;
		}

		if (!instance.data.containsKey(threadId)) {
			instance.createEmptyCoverageDataForThread(threadId);
		}

		instance.data.get(threadId).enter(id);
	}

	public static synchronized void leave(final long threadId, final short id) {
		if (!instance.started) {
			return;
		}

		instance.data.get(threadId).leave(id);
	}

	public static synchronized void resetData() {
		for (long threadId : instance.data.keySet()) {
			instance.data.get(threadId).reset();
		}
	}

	public static synchronized void dumpData() {
		dumpData(instance.options.getDestfile());
	}

	public static synchronized void dumpData(final String fileName) {
		for (long threadId : instance.data.keySet()) {
			final File dataFile = Paths.get(instance.options.getDestdir(), fileName.replace(".trc", String.format(".%d.trc", threadId))).toFile();

			instance.data.get(threadId).saveData(dataFile);
		}
	}

}
