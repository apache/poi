/*
 * Copyright  2000-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package tools.JUnit;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.*;


/**
 * Prints plain text output of the test to a specified Writer.
 *
 * @author Stefan Bodewig
 */
public class SimpleResultFormatter implements JUnitResultFormatter {

     public static int TEST_SUCCESS = 0;
     public static int TEST_FAILURE = 1;
     public static int TEST_ERROR = 2;
     public static int TEST_UNKNOWN = 3;

    // Lets capture STDOUT and STDERR
    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    ByteArrayOutputStream bErr = new ByteArrayOutputStream();

    PrintStream _out;
    PrintStream _err;
    private TestRecord testRecord;
    Collection records;

    /**
     * Formatter for timings.
     */
    private NumberFormat nf = NumberFormat.getInstance();
    /**
     * Timing helper.
     */
    private Hashtable testStarts = new Hashtable();
    /**
     * Where to write the log to.
     */
    private OutputStream out;
    /**
     * Helper to store intermediate output.
     */
    private StringWriter inner;
    /**
     * Convenience layer on top of {@link #inner inner}.
     */
    private PrintWriter wri;
    /**
     * Suppress endTest if testcase failed.
     */
    private Hashtable failed = new Hashtable();

    private String systemOutput = null;
    private String systemError = null;

    public SimpleResultFormatter() {
        inner = new StringWriter();
        wri = new PrintWriter(inner);
    }

    public void setOutput(OutputStream out) {
        this.out = out;
    }

    public void setSystemOutput(String out) {
        // We will be capturing Stdout and Stderr internally so this is
        // redundant
        //systemOutput = out;
    }

    public void setSystemError(String err) {
        // We will be capturing Stdout and Stderr internally so this is
        // redundant
        //systemError = err;
    }

    /**
     * Signals starting of a Suite of tests
     */
    public void startTestSuite(JUnitTest suite) {
        records = new ArrayList();
    }

    /**
     * The whole testsuite ended.
     */
    public void endTestSuite(JUnitTest suite) throws BuildException {
        String newLine = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer("Testsuite: ");
        sb.append(suite.getName());
        sb.append(newLine);
        sb.append("Tests run: ");
        sb.append(suite.runCount());
        sb.append(", Failures: ");
        sb.append(suite.failureCount());
        sb.append(", Errors: ");
        sb.append(suite.errorCount());
        sb.append(", Time elapsed: ");
        sb.append(nf.format(suite.getRunTime() / 1000.0));
        sb.append(" sec");
        sb.append(newLine);
        //sb.append("DEBUG: " + records.size());
        sb.append("======================================================");
        Iterator it = records.iterator();

        while (it.hasNext())
        {
            TestRecord rec = (TestRecord) it.next();
            sb.append(newLine);
            sb.append("Test: " + rec.getTestname()).append(newLine);
            sb.append("Result: " + rec.getStatusString()).append(newLine);
            float diff = rec.getEndTime() - rec.getStartTime();
            sb.append("Start time: " + rec.getStartTime()).append(newLine);
            sb.append("End time  : " + rec.getEndTime()).append(newLine);
            sb.append("Execution time: " + diff/1000).append(newLine);
            sb.append("[STDOUT]").append(newLine);
            sb.append(rec.getSysout()).append(newLine);
            sb.append("[STDERR]").append(newLine);
            sb.append(rec.getSyserr()).append(newLine);

            if (rec.isFailure())
            {
                sb.append("[EXCEPTION]").append(newLine);
                sb.append(JUnitTestRunner.getFilteredTrace(rec.getThrowable()));
                sb.append(newLine);
            }
            sb.append("======================================================");
        }



        /*
        // append the err and output streams to the log
        if (systemOutput != null && systemOutput.length() > 0) {
            sb.append("------------- Standard Output ---------------")
                .append(newLine)
                .append(systemOutput)
                .append("------------- ---------------- ---------------")
                .append(newLine);
        }

        if (systemError != null && systemError.length() > 0) {
            sb.append("------------- Standard Error -----------------")
                .append(newLine)
                .append(systemError)
                .append("------------- ---------------- ---------------")
                .append(newLine);
        }

        sb.append(newLine);
        */
        if (out != null) {
            try {
                out.write(sb.toString().getBytes());
                wri.close();
                //out.write(inner.toString().getBytes());
                out.flush();
            } catch (IOException ioex) {
                throw new BuildException("Unable to write output", ioex);
            } finally {
                if (out != System.out && out != System.err) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }

    }

    /**
     * Interface TestListener.
     *
     * <p>A new Test is started.
     */
    public void startTest(Test t) {
        // Lets start the capture
        System.out.flush();
        System.err.flush();

        _out = System.out;
        _err = System.err;
        bOut.reset();
        bErr.reset();
        System.setOut(new PrintStream(bOut));
        System.setErr(new PrintStream(bErr));

        // Store the previous record

        testRecord = new TestRecord(t.toString());
        testRecord.setStartTime(System.currentTimeMillis());
        testStarts.put(t, new Long(System.currentTimeMillis()));
        failed.put(t, Boolean.FALSE);

    }

    /**
     * Interface TestListener.
     *
     * <p>A Test is finished.
     */
    public void endTest(Test test) {
        long endTime = System.currentTimeMillis();

        System.out.flush();
        System.err.flush();
        // Update the test record
        testRecord.setSysout(bOut.toString());
        testRecord.setSyserr(bErr.toString());
        testRecord.setEndTime(endTime);
        // If the test did not fail, record it as a success
        if (!testRecord.isFailure())
            testRecord.setStatus(TEST_SUCCESS);

        // Add it to the set
        records.add(testRecord);

        System.setOut(_out);
        System.setErr(_err);
    }

    /**
     * Interface TestListener for JUnit &gt; 3.4.
     *
     * <p>A Test failed.
     */
    public void addFailure(Test test, AssertionFailedError t) {
        //addFailure(test, (Throwable) t);
        testRecord.setStatus(TEST_FAILURE);
        testRecord.setThrowable(t);
        // Record end time now..
    }

    /**
     * Interface TestListener.
     *
     * <p>An error occured while running the test.
     */
    public void addError(Test test, Throwable t) {
        testRecord.setStatus(TEST_ERROR);
        testRecord.setThrowable(t);
    }

    /**
     * Utility class to record per test data like test name, status, start
     * and end time, STDOUT, STDERR from text execution etc.
     */
    private class TestRecord
    {
        public TestRecord(String name)
        {
            setTestname(name);
        }

        private String testname;
        private String sysout;
        private String syserr;
        private Throwable t;
        private long startTime;
        private long endTime;
        private int status;
        boolean failed = false;

        private String testUnitName;
        private String testLogicalName;

        public void setTestname(String name)
        {
            this.testname = name;
        }

        public String getTestname()
        {
            return testname;
        }

        public void setStatus(int status)
        {
            this.status = status;
            if (status == TEST_ERROR || status == TEST_FAILURE)
                failed = true;
        }

        public int getStatus()
        {
            return status;
        }

        public String getStatusString()
        {
            return (status == TEST_SUCCESS)?"SUCCESS":
                        (status == TEST_ERROR)?"ERROR":"FAILURE";
        }

        public boolean isFailure()
        {
            return failed;
        }

        public String getSysout()
        {
            return sysout;
        }

        public void setSysout(String sysout)
        {
            this.sysout = sysout;
        }

        public String getSyserr()
        {
            return syserr;
        }

        public void setSyserr(String syserr)
        {
            this.syserr = syserr;
        }

        public Throwable getThrowable()
        {
            return t;
        }

        public void setThrowable(Throwable t)
        {
            this.t = t;
        }

       public long getStartTime()
        {
            return startTime;
        }

        public void setStartTime(long startTime)
        {
            this.startTime = startTime;
        }

        public long getEndTime()
        {
            return endTime;
        }

        public void setEndTime(long endTime)
        {
            this.endTime = endTime;
        }

    }



} // PlainJUnitResultFormatter
