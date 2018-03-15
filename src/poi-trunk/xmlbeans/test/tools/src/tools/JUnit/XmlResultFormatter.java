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
import junit.framework.TestCase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.*;
import noNamespace.TestResultContainerDocument;
import noNamespace.TestResultContainerDocument.TestResultContainer;
import noNamespace.TestResultType;
import noNamespace.TestResultType.ExecutionOutput;
import org.apache.xmlbeans.XmlOptions;


/**
 * Prints plain text output of the test to a specified Writer.
 *
 * @author Stefan Bodewig
 */

public class XmlResultFormatter implements JUnitResultFormatter {

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

    JUnitTest currentSuite;
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

    public XmlResultFormatter() {
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
        currentSuite = suite;
        records = new ArrayList();
    }

    /**
     * The whole testsuite ended.
     */
    public void endTestSuite(JUnitTest suite) throws BuildException
    {
        // Write out the XmlReport for this suite
        writeOutXmlResult();
    }

    /**
     * Interface TestListener.
     *
     * <p>A new Test is started.
     */
    public void startTest(Test test) {
        // Lets start the capture
        System.out.flush();
        System.err.flush();

        _out = System.out;
        _err = System.err;
        bOut.reset();
        bErr.reset();
        System.setOut(new PrintStream(bOut));
        System.setErr(new PrintStream(bErr));

        // Discard the previous record
        String fullTestName = test.toString();
        testRecord = new TestRecord(fullTestName);
        testRecord.setStartTime(System.currentTimeMillis());
        testStarts.put(test, new Long(System.currentTimeMillis()));
        failed.put(test, Boolean.FALSE);

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

        // this is a little hack for our reporting..
        // We could be on shaky ground if the behaviour of the JUnit task
        // ever changes.. OH Well...
        String fullTestName = test.toString();
        // Test-unit is between '(' and ')'
        int startindex = fullTestName.indexOf("(");
        int lastindex = fullTestName.indexOf(")");
        String testUnit;
        if (startindex >= 0 && lastindex > startindex)
            testUnit = fullTestName.substring(startindex+1, lastindex);
        else
            testUnit = fullTestName;
        String testMethod = ((TestCase) test).getName();
        // Get the last token from testUnit for the logical name
        startindex = testUnit.lastIndexOf(".");
        String baseClass = testUnit.substring(startindex+1);

        // update the extra fields of TestRecord
        testRecord.setTestUnitName(testUnit);
        testRecord.setTestLogicalName(baseClass + "." + testMethod);

        // If the test did not fail, record it as a success
        if (!testRecord.isFailure())
            testRecord.setStatus(TEST_SUCCESS);

        // Add it to the set
        records.add(testRecord);
        // set testRecord to null..
        testRecord = null;

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
        if (testRecord == null)
            testRecord = getMissingTestRecord();
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
        if (testRecord == null)
            testRecord = getMissingTestRecord();
        testRecord.setStatus(TEST_ERROR);
        testRecord.setThrowable(t);
        // Special case when test class is missing...
        if (t.toString().indexOf("ClassNotFoundException") > -1)
        {
            records.add(testRecord);
            testRecord = null;
        }
    }

    /**
     * Write out the current TestRecord as an Xml
     * Ant's JUnit Task gives us a OutputStream to write out too.
     */
    private void writeOutXmlResult()
    {
        // First build the XmlBean
        XmlOptions opts = new XmlOptions();
        opts.setSavePrettyPrint();

        TestResultContainerDocument doc =
                        TestResultContainerDocument.Factory.newInstance();
        TestResultContainer container = doc.addNewTestResultContainer();

        Iterator itr = records.iterator();
        int count = 0;
        while (itr.hasNext())
        {
            TestRecord rec = (TestRecord) itr.next();
            container.addNewTestResult();
            container.setTestResultArray(count++, getTestResultType(rec));
        }

        if (out != null)
        {
            try {
                out.write(doc.xmlText(opts).getBytes());
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
     * Creates the TestResultDocument and returns the Xml as a string
     */
    private TestResultType getTestResultType(TestRecord rec)
    {
        TestResultType tr = TestResultType.Factory.newInstance();
        // Children of TestResult
        TestResultType.TestCase tc = tr.addNewTestCase();
        TestResultType.ExecutionOutput exo = tr.addNewExecutionOutput();

        // Set the logical test name... 'Class.Methodname'
        tr.setLogicalname(rec.getTestLogicalName());
        // Set the test Start time as a String
        tr.setExectime(new java.util.Date(rec.getStartTime()).toString());
        String status = rec.getStatusString();
        // Set the test result
        if (status.equals("SUCCESS"))
            tr.setResult(TestResultType.Result.SUCCESS);
        else
            tr.setResult(TestResultType.Result.FAILURE);
        // Set the test execution time.. in milliseconds
        String dur = Long.toString(rec.getEndTime() - rec.getStartTime());
        tr.setDuration(dur);
        // Set the completion status..
        tr.setIsdone(TestResultType.Isdone.TRUE);

        // Setup the children elements
        // test-case
        tc.setTestcasename(rec.getTestLogicalName());
        tc.setTestunit(rec.getTestUnitName());
        // This should ideally be the whole path to the class...
        tc.setTestpath(rec.getTestname());

        // execution-output
        // if FAILURE.. set erroname attribute
        if (rec.isFailure())
        {
            String exp = rec.getThrowable().toString();
            int index = exp.indexOf(":");
            // the above line is very flaky..
            if (index < 0) index = exp.length();
            exo.setErrorname(exp.substring(0, index));
        }
        StringBuffer output = new StringBuffer();
        String eol = System.getProperty("line.separator");
        output.append("[STDOUT]").append(eol);
        output.append(rec.getSysout()).append(eol);
        output.append("[STDERR]").append(eol);
        output.append(rec.getSyserr()).append(eol);
        if (rec.isFailure())
        {
            output.append("[EXCEPTION]").append(eol);
            output.append(JUnitTestRunner.getFilteredTrace(rec.getThrowable()));
        }

        exo.setOutputDetails(output.toString());

        return tr;
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

        public String getTestUnitName()
        {
            return testUnitName;
        }

        public void setTestUnitName(String testUnitName)
        {
            this.testUnitName = testUnitName;
        }

        public String getTestLogicalName()
        {
            return testLogicalName;
        }

        public void setTestLogicalName(String testLogicalName)
        {
            this.testLogicalName = testLogicalName;
        }

    }

    private TestRecord getMissingTestRecord()
    {
        TestRecord tr = new TestRecord("Missing");
        tr.setStartTime(System.currentTimeMillis());
        tr.setEndTime(System.currentTimeMillis());
        tr.setStatus(TEST_ERROR);
        tr.setTestLogicalName("Missing");
        tr.setTestUnitName(currentSuite.getName());
        return tr;
    }


} // PlainJUnitResultFormatter
