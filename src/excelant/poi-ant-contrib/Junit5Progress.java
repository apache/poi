/* ====================================================================
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==================================================================== */

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;

import org.junit.platform.engine.TestDescriptor.Type;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Custom listener class for Ants junitlauncher, because it chomps the important running details
 *
 * @see <a href="https://www.selikoff.net/2018/07/28/ant-and-junit-5-outputting-test-duration-and-failure-to-the-log/">ant and junit 5 - outputting test duration and failure to the log</a>
 **/
public class Junit5Progress implements TestExecutionListener {

    private final StringWriter inMemoryWriter = new StringWriter();

    private int numSkippedInCurrentClass;
    private int numAbortedInCurrentClass;
    private int numSucceededInCurrentClass;
    private int numFailedInCurrentClass;
    private Instant startCurrentClass;

    private void resetCountsForNewClass() {
        numSkippedInCurrentClass = 0;
        numAbortedInCurrentClass = 0;
        numSucceededInCurrentClass = 0;
        numFailedInCurrentClass = 0;
        startCurrentClass = Instant.now();
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if ("[engine:junit-jupiter]".equals(testIdentifier.getParentId().orElse(""))) {
            println("Ran " + testIdentifier.getLegacyReportingName());
            resetCountsForNewClass();
        }
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        numSkippedInCurrentClass++;
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if ("[engine:junit-jupiter]".equals(testIdentifier.getParentId().orElse(""))) {
            int totalTestsInClass = numSucceededInCurrentClass + numAbortedInCurrentClass
                + numFailedInCurrentClass + numSkippedInCurrentClass;
            Duration duration = Duration.between(startCurrentClass, Instant.now());
            double numSeconds = duration.toNanos() / (double) 1_000_000_000;
            String output = String.format("Tests run: %d, Failures: %d, Aborted: %d, Skipped: %d, Time elapsed: %f sec",
                totalTestsInClass, numFailedInCurrentClass, numAbortedInCurrentClass,
                numSkippedInCurrentClass, numSeconds);
            println(output);

        }
        // don't count containers since looking for legacy JUnit 4 counting style
        if (testIdentifier.getType() == Type.TEST) {
            if (testExecutionResult.getStatus() == Status.SUCCESSFUL) {
                numSucceededInCurrentClass++;
            } else if (testExecutionResult.getStatus() == Status.ABORTED) {
                println("  ABORTED: " + testIdentifier.getDisplayName());
                numAbortedInCurrentClass++;
            } else if (testExecutionResult.getStatus() == Status.FAILED) {
                println("  FAILED: " + testIdentifier.getDisplayName());
                numFailedInCurrentClass++;
            }
        }
    }

    private void println(String str) {
        inMemoryWriter.write(str + "\n");
    }

    /*
     * Append to file on disk since listener can't write to System.out (because legacy listeners enabled)
     *
     * Implementing/using the TestResultFormatter - mentioned in the junitlauncher ant manual -
     * doesn't work currently, because the output is truncated/overwritten with every test
     */
    private void flushToDisk() {
        String outFile = System.getProperty("junit5.progress.file", "build/status-as-tests-run.txt");
        try (FileWriter writer = new FileWriter(outFile, true)) {
            writer.write(inMemoryWriter.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        flushToDisk();
    }
}