package hu.szte.sed;

import hu.szte.inf.sed.fl.coverage.data.BinaryMethodCoverageData;
import hu.szte.inf.sed.fl.coverage.data.MethodCoverageData;
import hu.szte.inf.sed.fl.coverage.data.NumericMethodCoverageData;
import hu.szte.sed.util.Options;

import java.util.Map;
import java.util.TreeMap;

public class CoverageCollector {

	private static CoverageCollector instance;

	private final Options options;
	private final Map<Long, MethodCoverageData<Short, Long>> data = new TreeMap<>();

	private boolean started = false;

	private CoverageCollector(final Options opts) {
		options = opts;
	}

	private void createEmptyCoverageDataForThread(final long threadId) {
		switch (options.getGranularity()) {
			case BINARY:
				data.put(threadId, new BinaryMethodCoverageData());
				break;
			case COUNT:
				data.put(threadId, new NumericMethodCoverageData());
				break;
			case CHAIN:
				//data.put(threadId, new ChainCoverageData<Short>());
				//break;
			default:
				throw new IllegalArgumentException();
		}
	}

	public static synchronized void setup(final Options opts) {
		if (instance == null) {
			instance = new CoverageCollector(opts);
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

		instance.data.get(threadId).onMethodEntry(id);
	}

	public static synchronized void leave(final long threadId, final short id) {
		if (!instance.started) {
			return;
		}

		instance.data.get(threadId).onMethodExit(id);
	}

	public static synchronized void resetData() {
		for (long threadId : instance.data.keySet()) {
			instance.data.get(threadId).clear();
		}
	}

	public static synchronized Map<Long, MethodCoverageData<Short, Long>> getCoverage() {
		return instance.data;
	}

	public static synchronized Options getOptions() {
		return instance.options;
	}

}
