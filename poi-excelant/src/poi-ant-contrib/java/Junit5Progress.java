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

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

/**
 * Custom listener class for Ants junitlauncher, because it chomps the important running details
 *
 * @see <a href="https://bz.apache.org/bugzilla/show_bug.cgi?id=64836">Bug 64836 - junitlauncher poor summary</a>
 **/
public class Junit5Progress implements TestExecutionListener {
    private final AtomicInteger numSkippedInTestSet = new AtomicInteger();
    private final AtomicInteger numAbortedInTestSet = new AtomicInteger();
    private final AtomicInteger numSucceededInTestSet = new AtomicInteger();
    private final AtomicInteger numFailedInTestSet = new AtomicInteger();
    private Instant testSetStartTime;

    final PrintStream out;

    public Junit5Progress() {
        this.out = System.out;
    }

    private void resetCountsForNewTestSet() {
        this.numSkippedInTestSet.set(0);
        this.numAbortedInTestSet.set(0);
        this.numSucceededInTestSet.set(0);
        this.numFailedInTestSet.set(0);
        this.testSetStartTime = Instant.now();
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        Optional<String> parentId = testIdentifier.getParentId();
        if (parentId.isPresent() && parentId.get().indexOf('/') < 0) {
            println("\nRunning " + testIdentifier.getLegacyReportingName());
            resetCountsForNewTestSet();
        }
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        this.numSkippedInTestSet.incrementAndGet();
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        Optional<String> parentId = testIdentifier.getParentId();
        if (parentId.isPresent() && parentId.get().indexOf('/') < 0) {
            int totalTestsInClass = this.numSucceededInTestSet.get() + this.numAbortedInTestSet.get() + this.numFailedInTestSet.get()
                + this.numSkippedInTestSet.get();
            Duration duration = Duration.between(this.testSetStartTime, Instant.now());
            double numSeconds = (double) duration.toMillis() / 1_000;
            String summary = String.format("Tests run: %d, Failures: %d, Aborted: %d, Skipped: %d, Time elapsed: %f sec", totalTestsInClass,
                this.numFailedInTestSet.get(), this.numAbortedInTestSet.get(), this.numSkippedInTestSet.get(), numSeconds);
            println(summary);
        } else {
            switch (testExecutionResult.getStatus()) {
                case SUCCESSFUL:
                    this.numSucceededInTestSet.incrementAndGet();
                    break;
                case ABORTED:
                    println("   Aborted: " + testIdentifier.getDisplayName() + ": " + testExecutionResult.getThrowable().orElse(null));
                    this.numAbortedInTestSet.incrementAndGet();
                    break;
                case FAILED:
                    println("   Failed: " + testIdentifier.getDisplayName() + ": " + testExecutionResult.getThrowable().orElse(null));
                    this.numFailedInTestSet.incrementAndGet();
                    break;
            }
        }
    }

    private void println(String str) {
        this.out.println(str);
    }
}