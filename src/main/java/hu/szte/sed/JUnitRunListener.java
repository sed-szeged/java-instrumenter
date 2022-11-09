package hu.szte.sed;

import hu.szte.inf.sed.fl.coverage.data.MethodCoverageData;
import hu.szte.inf.sed.fl.coverage.data.TestExecution;
import hu.szte.inf.sed.fl.coverage.data.TestOutcome;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class JUnitRunListener extends RunListener {

	private boolean fail;
	private long startTime;

	@Override
	public void testStarted(Description description) throws Exception {
		CoverageCollector.resetData();
		CoverageCollector.start();

		fail = false;
		startTime = System.currentTimeMillis();
	}

	@Override
	public void testFailure(Failure failure) throws Exception {
		fail = true;
	}

	@Override
	public void testFinished(Description description) throws Exception {
		CoverageCollector.quit();

		for (final Map.Entry<Long, MethodCoverageData<Short, Long>> entry : CoverageCollector.getCoverage().entrySet()) {
			TestExecution testExecution = new TestExecution();

			testExecution.setTestName(getTestName(description) + '-' + entry.getKey());
			testExecution.setExecutionTime(System.currentTimeMillis() - startTime);
			testExecution.setCoverage(entry.getValue());
			testExecution.setOutcome(fail ? TestOutcome.FAIL : TestOutcome.PASS);

			Path output = Paths.get(CoverageCollector.getOptions().getDestdir(), testExecution.getTestName() + ".trc");

			testExecution.writer().writeTo(new DataOutputStream(Files.newOutputStream(output)));
		}
	}

	private String getTestName(Description description) {
		String name = description.getClassName() + '.' + description.getMethodName();

		return name.replaceAll("[^a-zA-Z0-9_.-]+", "_");
	}

}
