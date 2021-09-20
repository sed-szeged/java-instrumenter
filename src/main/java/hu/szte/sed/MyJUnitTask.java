package hu.szte.sed;

import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;

public class MyJUnitTask extends JUnitTask {

	public MyJUnitTask() throws Exception {
		super();
	}

	@Override
	public void init() {
		super.init();

		super.setEnableTestListenerEvents(true);
		super.setHaltonerror(false);
		super.setHaltonfailure(false);
		super.setFork(true);
		super.setForkMode(new ForkMode(ForkMode.PER_TEST));

		FormatterElement fe = new FormatterElement();
		fe.setClassname(JUnitTestFormatter.class.getCanonicalName());
		fe.setUseFile(false);
		super.addFormatter(fe);

		String argLine = "-javaagent:${agent.jar}=pertestmode,includes=${agent.includes}";

		final String excludes = getProject().getProperty("agent.excludes");

		if (excludes != null) {
			argLine += ",excludes=" + excludes;
		}

		final String granularity = getProject().getProperty("agent.granularity");

		if (granularity != null) {
			argLine += ",granularity=" + granularity;
		}

		super.createJvmarg().setLine(getProject().replaceProperties(argLine));
	}

	@Override
	public void setEnableTestListenerEvents(boolean b) {
	}

	@Override
	public void setHaltonerror(boolean value) {
	}

	@Override
	public void setHaltonfailure(boolean value) {
	}

	@Override
	public void setFork(boolean value) {
	}

	@Override
	public void setForkMode(ForkMode mode) {
	}

}
