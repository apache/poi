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
import tools.util.JarUtil;
import org.apache.xmlbeans.XmlException;

import java.io.IOException;
import java.io.InputStream;

import xbeans.cases.xmlcursor.xquery.useCases.XmlQueryUseCasesDocument;
import xmlcursor.xquery.common.AbstractRunner;

/**
 *
 */
public class XQUseCaseRunner
    extends AbstractRunner

{

    public static Test suite()
        throws java.lang.Exception
    {
        XQUseCaseRunner suite = new XQUseCaseRunner();
        suite.addAllTestInfos();
        return suite;
    }

    public void addAllTestInfos()
    {
        UseCaseTest testCase;
        firstCall = true;

        try
        {
            findZip();
            UseCaseTest.pathToZip = pathToZip;
            while ((testCase = loadUseCaseTest()) != null)
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

    private UseCaseTest loadUseCaseTest()
        throws XmlException, IOException
    {
        if (firstCall)
        {
            firstCall = false;
            InputStream is=null;
            try
            {
                is = ZipUtil.getStreamFromZip(UseCaseTest.pathToZip,
                    UseCaseTest.zipName, pathToDescFile);
            }
            catch (java.util.zip.ZipException e)
            {
                if (is == null)
                {
                    //the zip with cases was not found
                    return null;
                }
            }
            XmlQueryUseCasesDocument doc = XmlQueryUseCasesDocument.Factory.parse(
                is);
            XmlQueryUseCasesDocument.XmlQueryUseCases suite = doc.getXmlQueryUseCases();
            group = suite.getGroupArray();

        }
        while (groupInd < group.length)
        {
            XmlQueryUseCasesDocument.XmlQueryUseCases.Group.Test[]
                tests = group[groupInd].getTestArray();
            if (testInd++ < tests.length - 1)
                return new UseCaseTest(tests[testInd],
                    group[groupInd].getName());
            else
            {
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
        "xmlUseCases/catalog.xml";
    XmlQueryUseCasesDocument.XmlQueryUseCases.Group[] group;

}

 
