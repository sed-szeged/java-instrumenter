package hu.szte.sed;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;

import java.io.OutputStream;

public class JUnitTestFormatter implements JUnitResultFormatter {

	private boolean fail;

	@Override
	public void startTest(Test test) {
		CoverageCollector.resetData();
		CoverageCollector.start();

		fail = false;
	}

	@Override
	public void addError(Test test, Throwable e) {
		fail = true;
	}

	@Override
	public void addFailure(Test test, AssertionFailedError e) {
		fail = true;
	}

	@Override
	public void endTest(Test test) {
		CoverageCollector.quit();
//		CoverageCollector.dumpData(getName(test) + "-" + (fail ? "FAIL" : "PASS") + ".trc");
	}

	@Override
	public void endTestSuite(JUnitTest arg0) throws BuildException {
	}

	@Override
	public void setOutput(OutputStream arg0) {
	}

	@Override
	public void setSystemError(String arg0) {
	}

	@Override
	public void setSystemOutput(String arg0) {
	}

	@Override
	public void startTestSuite(JUnitTest arg0) throws BuildException {
	}

	private String getName(Test test) {
		final StringBuffer name = new StringBuffer();
		final String originalName = test.toString(); // testMethod(testClass)
		final int pos = originalName.indexOf('(');

		name.append(originalName.substring(pos + 1, originalName.length() - 1))
			.append('.')
			.append(originalName.substring(0, pos));

		return name.toString().replaceAll("[^a-zA-Z0-9_.-]+", "_");
	}

}
