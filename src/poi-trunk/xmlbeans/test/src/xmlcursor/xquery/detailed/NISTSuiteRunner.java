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
package xmlcursor.xquery.detailed;

import junit.framework.TestSuite;
import junit.framework.Test;
import org.apache.xmlbeans.XmlException;
import tools.util.JarUtil;

import java.io.InputStream;
import java.io.IOException;

import noNamespace.TestSuiteDocument;
import noNamespace.TestCase;
import xmlcursor.xquery.common.AbstractRunner;


public class NISTSuiteRunner extends AbstractRunner


{
/**
 *

    public static Test suite()
        throws java.lang.Exception
    {
        NISTSuiteRunner suite = new NISTSuiteRunner();
        suite.addAllTestInfos();
        return suite;
    }

    public void addAllTestInfos()
    {
        NISTTest testCase;
        firstCall = true;

        try
        {
           findZip();
            NISTTest.pathToZip=pathToZip;
           while ((testCase = loadNIST()) != null)
        {
            addTest(testCase);
        }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }


    }

    private NISTTest loadNIST()
        throws XmlException, IOException
    {
        if (firstCall)
        {
            firstCall = false;
            InputStream is = ZipUtil.getStreamFromZip(pathToZip,NISTTest.zipName,
                pathToDescFile);
            TestSuiteDocument doc = TestSuiteDocument.Factory.parse(is);
            TestSuiteDocument.TestSuite suite = doc.getTestSuite();
            group = suite.getTestGroupArray();

        }
        while (groupInd < group.length)
        {
            TestCase[] tests = group[groupInd].getTestCaseArray();
            if (testInd++ < tests.length-1)
                return new NISTTest(tests[testInd]);
            else{
                testInd = -1;
                groupInd++;
            }
        }

        return null;
    }


    private boolean firstCall;
    private int testInd = -1;
    private int groupInd = 0;
    private final static String pathToDescFile =
        "testSuite/NIST/files/catalog.xml";
    TestSuiteDocument.TestSuite.TestGroup[] group;

*/
}
