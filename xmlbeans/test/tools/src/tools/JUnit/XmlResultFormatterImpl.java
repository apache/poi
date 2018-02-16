/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package tools.JUnit;

import java.util.*;

import java.io.*;

import java.text.SimpleDateFormat;



import junit.framework.Test;

import junit.framework.AssertionFailedError;

import junit.framework.TestCase;



import noNamespace.TestResultContainerDocument.TestResultContainer;

import noNamespace.*;

import noNamespace.TestResultType.ExecutionOutput;

import org.apache.xmlbeans.XmlOptions;

import tools.io.TeeOutputStream;

import org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner;

/**
 * Implementation of JUnitXResultFormatter that records JUnit results
 * and publishes the result as a XML Document
 */
public class XmlResultFormatterImpl implements JUnitXResultFormatter
{
    public static int TEST_SUCCESS = 0;
    public static int TEST_FAILURE = 1;
    public static int TEST_ERROR = 2;
    public static int TEST_UNKNOWN = 3;

    // Lets capture STDOUT and STDERR
    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    ByteArrayOutputStream bErr = new ByteArrayOutputStream();

    TeeOutputStream tOut;
    TeeOutputStream tErr;

    PrintStream _out;
    PrintStream _err;

    private TestRecord testRecord;
    Collection records;

    // By default write to StdOut
    OutputStream logOut = System.out;

    // Record Stats
    int testCount = 0;
    int passCount = 0;
    int failCount = 0;
    long startTime = 0;

    boolean showOutput = false;

    public void startRun()
    {
        records = new ArrayList();

        // Reset stats
        testCount = 0;
        passCount = 0;
        failCount = 0;
        startTime = System.currentTimeMillis();
    }

    public void endRun()
    {
        System.out.println("=================================================");
        System.out.println("Tests Ran: " + testCount);
        System.out.println("Success:   " + passCount);
        System.out.println("Failures:  " + failCount);

        // Generate log:
        System.out.println("Starting Publish: " + System.currentTimeMillis());
        publishResults();
        System.out.println("Finished Publish: " + System.currentTimeMillis());
    }

    public void setOutput(OutputStream out)
    {
        if (out != null)
            logOut = out;
    }

    public void showTestOutput(boolean show)
    {
        showOutput = show;
    }

    public void info(Object msg)
    {
        if (showOutput)
            System.out.println(msg);
    }

    /*
    * Implementation of TestListener
    */
	public synchronized void startTest(Test test)
    {
        String fullTestName = test.toString();
        info("Starting Test: " + fullTestName);

        // Lets start the capture
        System.out.flush();
        System.err.flush();

        _out = System.out;
        _err = System.err;

        bOut.reset();
        bErr.reset();

        // Redirect Stdout & Stderr to both console and our capture stream
        if (showOutput)
        {
            tOut = new TeeOutputStream(_out, bOut);
            tErr = new TeeOutputStream(_err, bErr);
            System.setOut(new PrintStream(tOut));
            System.setErr(new PrintStream(tErr));
        }
        else
        {
            System.setOut(new PrintStream(bOut));
            System.setErr(new PrintStream(bErr));
        }

        // Discard the previous record
        testRecord = new TestRecord(fullTestName);
        testRecord.setStartTime(System.currentTimeMillis());
	}

	public synchronized void endTest(Test test)
    {
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
        {
            testRecord.setStatus(TEST_SUCCESS);
            passCount++;
        }
        else
            failCount++;

        testCount++;

        // Add it to the set
        records.add(testRecord);

        // set testRecord to null..
        // Restore STDOUT and STDERR
        System.setOut(_out);
        System.setErr(_err);

        info("Finished Test: " + fullTestName + " "
                           + testRecord.getStatusString() + "\n");

        // Reset TestRecord
        testRecord = null;
	}

	public synchronized void addError(final Test test, final Throwable t)
    {
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

	public synchronized void addFailure(final Test test, final AssertionFailedError t)
    {
        if (testRecord == null)
            testRecord = getMissingTestRecord();

        testRecord.setStatus(TEST_FAILURE);
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
        tr.setTestUnitName("Missing");
        return tr;
    }

    public void publishResults()
    {
        TestLogDocument logDoc = TestLogDocument.Factory.newInstance();
        TestLogDocument.TestLog log = logDoc.addNewTestLog();

        // Populate the attributes for test-log
        // testtype
        String testtype = System.getProperty("TESTTYPE", "AUTO");

        if (testtype.equalsIgnoreCase("AUTO"))
            log.setTesttype(TestLogDocument.TestLog.Testtype.AUTOMATED);
        else
            log.setTesttype(TestLogDocument.TestLog.Testtype.MANUAL);

        // runid
        String dateFormatStr = "_yy_MMM_dd_HH_mm_ss_SS";
        String dateStr = new SimpleDateFormat(dateFormatStr).format(new java.util.Date(startTime));
        String defRunId = System.getProperty("user.name").toUpperCase() + dateStr;
        String runId = System.getProperty("RUNID", defRunId);
        log.setRunid(runId);

        // hostname
        String hostname;

        try
        {
            hostname = java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e)
        {
            // Ignore.. not critical
            hostname = "UNKNOWN_HOST";
        }

        log.setHostname(hostname);

        // TODO: set Defaults/check sysprop for other attributes

        // Add <environment> element
        EnvironmentType env = log.addNewEnvironment();
        Map envMap = new HashMap();
        envMap.put("JVM_NAME", System.getProperty("java.vm.name"));
        envMap.put("JVM_VENDOR", System.getProperty("java.vm.vendor"));
        envMap.put("JVM_VERSION", System.getProperty("java.vm.version"));
        envMap.put("OS", System.getProperty("os.name"));
        String defFreq = "checkin";
        envMap.put("Frequency", System.getProperty("test.run.frequency", defFreq));

        Iterator itr = envMap.keySet().iterator();

        int envCount = 0;
        while (itr.hasNext())
        {
            EnvironmentType.EnvAttribute envAttr = env.addNewEnvAttribute();
            String name = (String) itr.next();
            String value = (String) envMap.get(name);
            envAttr.setValue(value);
            envAttr.setName(name);
        }

        // Add <header-info> element
        TestLogDocument.TestLog.HeaderInfo hdrInfo = log.addNewHeaderInfo();
        hdrInfo.setResultcount(Integer.toString(testCount));
        hdrInfo.setChecksum(Integer.toString(testCount));
        hdrInfo.setExecdate(new java.util.Date(startTime).toString());
        hdrInfo.setExecaccount(System.getProperty("user.name"));

        // Add test-results
        Iterator rItr = records.iterator();

        while (rItr.hasNext())
        {
            TestResultType tr = log.addNewTestResult();
            tr.set(getTestResultType((TestRecord) rItr.next()));
        }

        // Publish it to the outputStream
        XmlOptions opts = new XmlOptions().setSavePrettyPrint();
        try
        {
            logOut.write(logDoc.xmlText(opts).getBytes());
        } catch (IOException ioe)
        {
            System.out.println("XmlResultFormatter: Unable to publish results");
            System.out.println(ioe.toString());
        }

    }

     /**
     * Creates the TestResultDocument and returns the Xml
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
        else if (status.equals("FAILURE"))
            tr.setResult(TestResultType.Result.FAILURE);
        else
            tr.setResult(TestResultType.Result.ABORT);

        // Set the test execution time.. in milliseconds
        String dur = java.lang.Long.toString(rec.getEndTime() - rec.getStartTime());
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

}

