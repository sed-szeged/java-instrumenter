# Java Instrumenter

## Build

```
mvn clean package
```

Use the JAR file that has the `-jar-with-dependencies` suffix as it includes all the required dependencies.

## Usage

### Configure the POM of the program under test

 - Use the `<argLine>` tag to add the agent JAR to the arguments of the JVM used by the Surefire plugin.
 - Configure which classes will be instrumented with the `includes=[regex]` option (e.g. `includes=.*org.joda.time.*`).
 - Attach the [JUnit run listener](https://junit.org/junit4/javadoc/4.13/org/junit/runner/notification/RunListener.html) by adding a new property.

```
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  ...
  <configuration>
    ...
    <argLine>-javaagent:/home/example/java-instrumenterAgent/target/method-agent-0.0.4-jar-with-dependencies.jar=includes=.*some.package.*</argLine>
    <properties>
      <property>
        <name>listener</name>
        <value>hu.szte.sed.JUnitRunListener</value>
      </property>
    </properties>
    ...
  </configuration>
</plugin>
```
 
> **Note:** JUnit version on the classpath should be at least 4.0 (this is the first version that supports run listeners).
> You can set the desired version by configuring the dependencies of PUT or adding an [appropriate provider](https://maven.apache.org/surefire/maven-surefire-plugin/examples/providers.html) to the Surefire plugin.

> **Note:** The `includes=[regex]` option should match the full name of classes which is a file URI (for example: `file:/home/example/target/classes/org/joda/time/Partial`).
> The `${project.build.outputDirectory}` Maven property can be used to make sure that only production classes are included.

#### JUnit dependency

```
<dependencies>
  ...
  <dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13</version>
    <scope>test</scope>
  </dependency>
  ...
</dependencies>
```

#### Surefire provider

```
<plugins>
  ...
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0-M3</version>
    <dependencies>
      <dependency>
        <groupId>org.apache.maven.surefire</groupId>
        <artifactId>surefire-junit47</artifactId>
        <version>3.0.0-M3</version>
      </dependency>
    </dependencies>
  </plugin>
  ...
</plugins>
```

### Run the tests of PUT

```
mvn clean test -Dmaven.test.failure.ignore=true
```

#### Results

Results will be generated into the `coverage` directory inside the root directory of the project (`${project.basedir}/coverage`).

The `trace.trc.names` file contains the list of the instrumented methods in the `<ID>:<FULLY_QUALIFIED_METHOD_NAME>` format.

One or more `.trc` files are generated for each unit test.
File names follow this template: `<TEST_NAME>-<TEST_RESULT>.<THREAD_ID>.trc` (e.g. `org.joda.time.tz.TestCompiler.testCompile-PASS.1.trc`).
These files contain the call chains in binary format.

##### Coverage granularity levels, binary formats

With the `granularity` option of the agent you can specify the granularity of the collected coverage data.
Possible values for this argument are the following:
- `binary` - Classic hit/miss coverage. For each test the output files contain the ids (short integer, 2 bytes) of the covered code elements.
- `count` - The extended version of the binary coverage. For each test the output files contain a list of (id,count) tuples where id (short integer, 2 bytes) is the identifier of a covered code element and count represents how many times a particular test has executed the given code element.
- `chain` - Count interpreted on call chains. For each test the output files contain a list of (length,chain,count) tuples where length (integer, 4 bytes) is the length of the call chain, chain is a list of code element ids (short integer, 2 bytes) and count represents how many times a particular test has yielded the given call chain.
