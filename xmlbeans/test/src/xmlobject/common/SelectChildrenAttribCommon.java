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
package xmlobject.common;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import junit.framework.TestCase;
import tools.util.JarUtil;
import tools.xml.XmlComparator;


/**
 *
 *
 */
public class SelectChildrenAttribCommon
        extends TestCase
{
    public SelectChildrenAttribCommon(String name)
    {
        super(name);
    }

    public void setUp()
    {
        opts = new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(2);
    }


    // Common
    public XmlOptions opts;


    //////////////////////////////////////////////////////////////////
    // Helper methods
    public void validateTest(String testName, String[] exps, XmlObject[] act)
        throws Exception
    {
        assertTrue(testName + ": Return array has more/less elements than expected: "
                   + act.length, act.length == exps.length);
        boolean passed = true;

        for (int i = 0; i < act.length; i++)
        {
            boolean res;
            res = XComp(convertFragToDoc(act[i].xmlText()), exps[i], true);
            if (res == false)
            {
                System.out.println("Expected value differs from actual: Index=" + i);
                System.out.println("Expected: " + exps[i]);
                System.out.println("Actual: " + act[i].xmlText());
                passed = false;
            }
        }
        assertTrue(testName + ": Failed!", passed);
    }


    public void validateTest(String testName, String exp, XmlObject xml)
        throws Exception
    {
        if (xml == null)
            fail(testName + ": XmlObject Recevied is null");

        boolean res = XComp(convertFragToDoc(xml.xmlText()), exp, true);
        assertTrue("Expected value differs from actual\n" +
                   "Expected: " + exp + "\n" +
                   "Actual: " + xml.xmlText(),
                   res);
    }


    public static String getXml(String file)
        throws java.io.IOException
    {
        return JarUtil.getResourceFromJar(file);
    }


    /**
     * Just a thin wrapper around XmlComparator
     */
    public static boolean XComp(String actual, String expected, boolean verbose)
        throws org.apache.xmlbeans.XmlException
    {
        boolean same;
        XmlComparator.Diagnostic diag = new XmlComparator.Diagnostic();
        same = XmlComparator.lenientlyCompareTwoXmlStrings(actual, expected, diag);
        if (!same && verbose)
            System.out.println(diag.toString());

        return same;
    }

    /**
     * This is a workaround for using XmlComparator to compare XML that are just
     * a single value like '7' wrapped in <xml-fragemnt> tags. Inside
     * XmlComparator creates XmlObjects and <xml-fragment> tags are ignored. So
     * this method will replace that with something like <xm> so that they look
     * like Xml Docs...
     */
    public static String convertFragToDoc(String xmlFragment)
    {
        String startFragStr = "<xml-fragment";
        String endFragStr   = "</xml-fragment>";
        String startReplacementStr = "<xm";
        String endReplacementStr = "</xm>";

        Pattern pattern = Pattern.compile(startFragStr);
        Matcher matcher = pattern.matcher(xmlFragment);

        String xmlDoc = matcher.replaceAll(startReplacementStr);

        pattern = Pattern.compile(endFragStr);
        matcher = pattern.matcher(xmlDoc);

        xmlDoc = matcher.replaceAll(endReplacementStr);

        return xmlDoc;
    }


}
