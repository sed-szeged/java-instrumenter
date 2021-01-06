package hu.szte.sed.instrumentation;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Paths;

import hu.szte.sed.SZTELogger;
import hu.szte.sed.util.Options;

public class InstrumenterAgent {

	public static void premain(String args, Instrumentation instrumentation) {
		setup(args, instrumentation);
	}

	public static void agentmain(String args, Instrumentation instrumentation) {
		setup(args, instrumentation);
	}

	private static void setup(String agentArgs, Instrumentation instrumentation) {
		final Options options = new Options(agentArgs);

		Paths.get(options.getDestdir()).toFile().mkdirs();

		final FileTransformer transformer = new FileTransformer(options);
		instrumentation.addTransformer(transformer);

		try {
			SZTELogger.setup(options);

			if (!options.getPerTestMode()) {
				SZTELogger.start();
			}

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					if (!options.getPerTestMode()) {
						SZTELogger.quit();
						SZTELogger.dumpData();
					}

					transformer.dumpNames();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Problems with creating the logger.");
		}
	}
}
