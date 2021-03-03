package hu.szte.sed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import hu.szte.sed.util.Options;

public class OptionsTest {

	@Test
	public void testDefaults() {
		Options options = new Options("");

		assertEquals(Options.DEFAULT_PERTESTMODE, options.getPerTestMode());
		assertEquals(Options.DEFAULT_DESTFILE, options.getDestfile());
		assertEquals(Options.DEFAULT_DESTDIR, options.getDestdir());
		assertEquals(Options.DEFAULT_INCLUDES, options.getIncludes());
		assertEquals(Options.DEFAULT_EXCLUDES, options.getExcludes());
		assertEquals(Options.DEFAULT_GRANULARITY, options.getGranularity());
	}

	@Test
	public void testDefaultsWithNull() {
		Options options = new Options(null);

		assertEquals(Options.DEFAULT_PERTESTMODE, options.getPerTestMode());
		assertEquals(Options.DEFAULT_DESTFILE, options.getDestfile());
		assertEquals(Options.DEFAULT_DESTDIR, options.getDestdir());
		assertEquals(Options.DEFAULT_INCLUDES, options.getIncludes());
		assertEquals(Options.DEFAULT_EXCLUDES, options.getExcludes());
		assertEquals(Options.DEFAULT_GRANULARITY, options.getGranularity());
	}

	@Test
	public void testDefaultsWithGibberish() {
		Options options = new Options("=asdf,,==,");

		assertEquals(Options.DEFAULT_PERTESTMODE, options.getPerTestMode());
		assertEquals(Options.DEFAULT_DESTFILE, options.getDestfile());
		assertEquals(Options.DEFAULT_DESTDIR, options.getDestdir());
		assertEquals(Options.DEFAULT_INCLUDES, options.getIncludes());
		assertEquals(Options.DEFAULT_EXCLUDES, options.getExcludes());
		assertEquals(Options.DEFAULT_GRANULARITY, options.getGranularity());
	}

	@Test
	public void testPerTestModeTrue() {
		boolean value = true;
		String str = String.format("%s=%s", Options.PERTESTMODE, value);

		Options options = new Options(str);

		assertEquals(value, options.getPerTestMode());
	}

	@Test
	public void testPerTestModeFalse() {
		boolean value = false;
		String str = String.format("%s=%s", Options.PERTESTMODE, value);

		Options options = new Options(str);

		assertEquals(value, options.getPerTestMode());
	}

	@Test
	public void testPerTestModeFlag() {
		String str = String.format("%s", Options.PERTESTMODE);

		Options options = new Options(str);

		assertTrue(options.getPerTestMode());
	}

	@Test
	public void testDestfile() {
		String value = "something.trc";
		String str = String.format("%s=%s", Options.DESTFILE, value);

		Options options = new Options(str);

		assertEquals(value, options.getDestfile());
	}

	@Test
	public void testDestdir() {
		String value = "/foo/bar";
		String str = String.format("%s=%s", Options.DESTDIR, value);

		Options options = new Options(str);

		assertEquals(value, options.getDestdir());
	}

	@Test
	public void testIncludes() {
		String value = "[a-zA-Z0-9]+";
		String str = String.format("%s=%s", Options.INCLUDES, value);

		Options options = new Options(str);

		assertEquals(value, options.getIncludes());
	}

	@Test
	public void testExcludes() {
		String value = "[a-zA-Z0-9]+";
		String str = String.format("%s=%s", Options.EXCLUDES, value);

		Options options = new Options(str);

		assertEquals(value, options.getExcludes());
	}

	@Test
	public void testMultiple() {
		boolean autoinit = false;
		String destfile = "anything.txt";
		String includes = "([\\w]+)";
		String str = String.format("%s=%s,%s=%s,%s=%s", Options.PERTESTMODE, autoinit, Options.DESTFILE, destfile,
				Options.INCLUDES, includes);

		Options options = new Options(str);

		assertEquals(autoinit, options.getPerTestMode());
		assertEquals(destfile, options.getDestfile());
		assertEquals(includes, options.getIncludes());
	}

	@Test
	public void testMultipleAndFlag() {
		String destfile = "anything.txt";
		String includes = "([\\w]+)";
		String str = String.format("%s,%s=%s,%s=%s", Options.PERTESTMODE, Options.DESTFILE, destfile, Options.INCLUDES,
				includes);

		Options options = new Options(str);

		assertTrue(options.getPerTestMode());
		assertEquals(destfile, options.getDestfile());
		assertEquals(includes, options.getIncludes());
	}

}
