package hu.szte.sed.instrumentation;

import hu.szte.inf.sed.fl.coverage.data.MethodCoverageData;
import hu.szte.inf.sed.fl.coverage.data.TestExecution;
import hu.szte.inf.sed.fl.coverage.data.TestOutcome;
import hu.szte.sed.CoverageCollector;
import hu.szte.sed.util.Options;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class InstrumenterAgent {

	public static void premain(String args, Instrumentation instrumentation) {
		setup(args, instrumentation);
	}

	public static void agentmain(String args, Instrumentation instrumentation) {
		setup(args, instrumentation);
	}

	private static void setup(String agentArgs, Instrumentation instrumentation) {
		System.err.println("agent.setup");

		final Options options = new Options(agentArgs);

		Paths.get(options.getDestdir()).toFile().mkdirs();

		final FileTransformer transformer = new FileTransformer(options);
		instrumentation.addTransformer(transformer);


		CoverageCollector.setup(options);

		if (!options.getPerTestMode()) {
			CoverageCollector.start();
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (!options.getPerTestMode()) {
					CoverageCollector.quit();

					for (final Map.Entry<Long, MethodCoverageData<Short, Long>> entry : CoverageCollector.getCoverage().entrySet()) {
						TestExecution testExecution = new TestExecution();

						testExecution.setTestName("global-" + entry.getKey());
						testExecution.setExecutionTime(-1);
						testExecution.setCoverage(entry.getValue());
						testExecution.setOutcome(TestOutcome.PASS);

						Path output = Paths.get(CoverageCollector.getOptions().getDestdir(), CoverageCollector.getOptions().getDestfile());

						try {
							testExecution.writer().writeTo(new DataOutputStream(Files.newOutputStream(output)));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}

				transformer.dumpNames();
			}
		});
	}
}
