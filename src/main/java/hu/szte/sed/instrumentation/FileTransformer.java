package hu.szte.sed.instrumentation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import hu.szte.sed.SZTELogger;
import hu.szte.sed.util.Constants;
import hu.szte.sed.util.Granularity;
import hu.szte.sed.util.Options;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.Modifier;
import javassist.bytecode.MethodInfo;

public class FileTransformer implements ClassFileTransformer {

	private static final String AGENT_PACKAGE;
	private static final String LOGGER_CLASS;

	static {
		String packageName = FileTransformer.class.getPackage().getName();
		AGENT_PACKAGE = packageName.substring(0, packageName.lastIndexOf('.')).replace('.', '/');

		LOGGER_CLASS = SZTELogger.class.getCanonicalName();
	}

	private final Options options;
	private final Pattern includes;
	private final Pattern excludes;
	private final File namesFile;
	private final Map<String, Short> idMap = new HashMap<>();

	private short maxID = 2;

	public FileTransformer(final Options opts) {
		options = opts;
		includes = Pattern.compile(options.getIncludes());
		excludes = Pattern.compile(options.getExcludes());
		namesFile = Paths.get(options.getDestdir(), options.getDestfile().replaceAll("$", ".names")).toFile();

		if (namesFile.isFile()) {
			loadNames(namesFile);
		}
	}

	public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
			final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {

		if (!filter(loader, className, protectionDomain) || classBeingRedefined != null) {
			return null;
		}

		String name = null;
		byte[] bytecode = classfileBuffer;

		try {
			ClassPool cPool = ClassPool.getDefault();
			CtClass ctClass = cPool.makeClass(new ByteArrayInputStream(bytecode));

			CtBehavior behaviors[] = ctClass.getDeclaredBehaviors(); // all constructors and methods declared in the class

			for (CtBehavior behavior : behaviors) {
				final short id = getID(behavior);

				if (Modifier.isAbstract(behavior.getModifiers())) {
					continue;
				}

				String enter = String.format(
						  "{"
						+ "  %s.%s(%s, (short)%d);"
						+ "}",
						LOGGER_CLASS,
						"enter",
						"Thread.currentThread().getId()",
						id);
				behavior.insertBefore(enter);

				if (options.getGranularity() == Granularity.CHAIN) {
					String leave = String.format(
							  "{"
							+ "  %s.%s(%s, (short)%d);"
							+ "}",
							LOGGER_CLASS,
							"leave",
							"Thread.currentThread().getId()",
							id);
					behavior.insertAfter(leave, true);
				}
			}

			bytecode = ctClass.toBytecode();
		} catch (IOException e) {
			System.out.println("Bibi1");
			throw new IllegalClassFormatException(e.getMessage());
		} catch (RuntimeException e) {
			System.out.println("Bibi2");
			throw new IllegalClassFormatException(e.getMessage());
		} catch (CannotCompileException e) {
			System.out.println("Bibi3 " + name + " " + e.getMessage());
			throw new IllegalClassFormatException(e.getMessage());
		}

		return bytecode;
	}

	private boolean filter(final ClassLoader loader, final String className, final ProtectionDomain protectionDomain) {
		if (loader == null) {
			return false;
		}

		final URL sourceLocation = getSourceLocation(protectionDomain);

		if (sourceLocation == null) {
			return false;
		}

		if (className.contains(AGENT_PACKAGE)) {
			return false;
		}

		final String fullName = getFullName(sourceLocation, className);

		if (!includes.matcher(fullName).matches() || excludes.matcher(fullName).matches()) {

			System.err.println(fullName);
			return false;
		}

		return true;
	}

	private URL getSourceLocation(final ProtectionDomain protectionDomain) {
		if (protectionDomain == null) {
			return null;
		}

		final CodeSource codeSource = protectionDomain.getCodeSource();

		if (codeSource == null) {
			return null;
		}

		return codeSource.getLocation();
	}
	
	private String getFullName(URL location, String className) {
		final StringBuffer sb = new StringBuffer();

		final String locationString = location.toString();

		sb.append(locationString);

		if (!locationString.endsWith("/")) {
			sb.append('/');
		}

		sb.append(className);

		return sb.toString();
	}

	private short getID(final CtBehavior behavior) {
		final String methodName = getName(behavior);

		Short id = idMap.get(methodName);

		if (id == null) {
			id = ++maxID;

			idMap.put(methodName, id);
		}

		return id;
	}

	private String getName(final CtBehavior behavior) {
		final StringBuffer sb = new StringBuffer();

		sb.append(behavior.getDeclaringClass().getName());

		if (behavior instanceof CtConstructor) {
			final CtConstructor constructor = (CtConstructor) behavior;

			if (constructor.isConstructor()) {
				sb.append('.').append(MethodInfo.nameInit);
			} else if (constructor.isClassInitializer()) {
				sb.append('.').append(MethodInfo.nameClinit);
			}
		} else {
			sb.append('.').append(behavior.getName());
		}

		sb.append(behavior.getSignature());

		return sb.toString();
	}

	public void loadNames(final File file) {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				final String[] splitted = line.trim().split(Constants.ID_MAP_FIELD_SEPARATOR);

				final short methodID = Short.parseShort(splitted[0]);
				final String methodName = splitted[1];

				idMap.put(methodName, methodID);

				if (methodID > maxID) {
					maxID = methodID;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void dumpNames() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(namesFile))) {
			final List<Entry<String, Short>> sortedEntries = new ArrayList<>(idMap.entrySet());

			Collections.sort(sortedEntries, new Comparator<Entry<String, Short>>() {
				@Override
				public int compare(Entry<String, Short> a, Entry<String, Short> b) {
					return a.getValue().compareTo(b.getValue());
				}
			});

			final StringBuffer line = new StringBuffer();

			for (Entry<String, Short> entry : sortedEntries) {
				line.append(entry.getValue())
					.append(Constants.ID_MAP_FIELD_SEPARATOR)
					.append(entry.getKey())
					.append('\n');

				writer.write(line.toString());

				line.setLength(0);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
