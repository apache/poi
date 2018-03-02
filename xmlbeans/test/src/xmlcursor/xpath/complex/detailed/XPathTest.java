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

package xmlcursor.xpath.complex.detailed;

import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;
import xmlcursor.xpath.common.XPathCommon;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlException;
import tools.util.JarUtil;
import tools.xml.XmlComparator;

import java.io.IOException;

/**
 * Verifies XPath impl using examples from
 * http://www.zvon.org/xxl/XPathTutorial/Output/example1.html
 * includes expanded notations as well
 *
 */
public class XPathTest
    extends BasicCursorTestCase
{
    public XPathTest(String sName)
    {
        super(sName);
    }

    public static Test suite()
    {
        return new TestSuite(XPathTest.class);
    }

    static String fixPath(String path)
    {
        // return "$this" + path;
        return path;
    }


    /**
     * @throws Exception
     */
    public void testZvonExample1()
        throws Exception
    {
        System.out.println("====== Example-1 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon1.xml"));

        String ex1Simple = "/AAA";
        String ex1Expanded = "";

        String ex1R1 = "<AAA><BBB/><CCC/><BBB/><BBB/><DDD><BBB/></DDD><CCC/></AAA>";
        XmlObject[] exXml1 = new XmlObject[]{XmlObject.Factory.parse(ex1R1)};

        String ex2Simple = "/AAA/CCC";
        String ex2Expanded = "";

        String ex2R1 = "<CCC/>";

        XmlObject[] exXml2 = new XmlObject[]{XmlObject.Factory.parse(ex2R1),
                                             XmlObject.Factory.parse(ex2R1)};


        String ex3Simple = "/AAA/DDD/BBB";
        String ex3Expanded = "";

        //<BBB/><CCC/></AAA>
        String ex3R1 = "<BBB/>";
        XmlObject[] exXml3 = new XmlObject[]{XmlObject.Factory.parse(ex3R1)};

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        XPathCommon.compare(x1, exXml1);
        x1.dispose();


        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        XPathCommon.compare(x2, exXml2);
        x2.dispose();


        System.out.println("Test 3: " + ex3Simple);
        XmlCursor x3 = xDoc.newCursor();
        x3.selectPath(fixPath(ex3Simple));
        XPathCommon.compare(x3, exXml3);
        x3.dispose();
    }

    /**
     * @throws Exception
     */
    public void testZvonExample2()
        throws Exception
    {
        System.out.println("====== Example-2 ==========");

        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon2.xml"));
        String ex1Simple = "//BBB";
        String ex1Expanded = "";

        String ex1R1 = "<BBB/>"; // * 5
        XmlObject[] exXml1 = new XmlObject[]{XmlObject.Factory.parse(ex1R1),
                                             XmlObject.Factory.parse(ex1R1),
                                             XmlObject.Factory.parse(ex1R1),
                                             XmlObject.Factory.parse(ex1R1),
                                             XmlObject.Factory.parse(ex1R1)};

        String ex2Simple = "//DDD/BBB";
        String ex2Expanded = "";
        XmlObject[] exXml2 = new XmlObject[]{XmlObject.Factory.parse(ex1R1),
                                             XmlObject.Factory.parse(ex1R1),
                                             XmlObject.Factory.parse(ex1R1)};


        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        XPathCommon.compare(x1, exXml1);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        XPathCommon.compare(x2, exXml2);
        x2.dispose();

    }

    /**
     * @throws Exception
     */
    public void testZvonExample3()
        throws Exception
    {
        System.out.println("====== Example-3 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon3.xml"));
        String ex1Simple = "/AAA/CCC/DDD/*";
        String ex1R1 = "<BBB/>"; // * 5
        String ex1R2 = "<EEE/>";
        String ex1R3 = "<FFF/>";

        XmlObject[] exXml1 = new XmlObject[]{XmlObject.Factory.parse(ex1R1),
                                             XmlObject.Factory.parse(ex1R1),
                                             XmlObject.Factory.parse(ex1R2),
                                             XmlObject.Factory.parse(ex1R3)};
        String ex2Simple = "/*/*/*/BBB";
        String ex2R1 = "<BBB/>"; // * 5
        String ex2R2 = "<BBB><BBB/></BBB>";
        XmlObject[] exXml2 = new XmlObject[]{XmlObject.Factory.parse(ex2R1),
                                             XmlObject.Factory.parse(ex2R1),
                                             XmlObject.Factory.parse(ex2R1),
                                             XmlObject.Factory.parse(ex2R1),
                                             XmlObject.Factory.parse(ex2R2)};
        String ex3Simple = "//*";
        /* String ex3R0 = "<AAA><XXX><DDD><BBB/><BBB/><EEE/><FFF/></DDD></XXX><CCC><DDD><BBB/><BBB/><EEE/><FFF/></DDD></CCC><CCC><BBB><BBB><BBB/></BBB></BBB></CCC></AAA>";
         String ex3R1 = "<XXX><DDD><BBB/><BBB/><EEE/><FFF/></DDD></XXX>";
         String ex3R2 = "<CCC><DDD><BBB/><BBB/><EEE/><FFF/></DDD></CCC>";
         String ex3R3 = "<CCC><BBB><BBB><BBB/></BBB></BBB></CCC>";
         String ex3R4 = "<DDD><BBB/><BBB/><EEE/><FFF/></DDD>";
         String ex3R5 = "<DDD><BBB/><BBB/><EEE/><FFF/></DDD>";
         String ex3R6 = "<BBB><BBB><BBB/></BBB></BBB>";
         String ex3R7 = "<BBB/>";
         String ex3R8 = "<BBB/>";
         String ex3R9 = "<EEE/>";
         String ex3R10 = "<FFF/>";
         String ex3R11 = "<BBB/>";
         String ex3R12 = "<BBB/>";
         String ex3R13 = "<EEE/>";
         String ex3R14 = "<FFF/>";
         String ex3R15 = "<BBB><BBB/></BBB>";
         String ex3R16 = "<BBB/>";
        */
        //according to Galax the document order is :
        String ex3R0 = "<AAA><XXX><DDD><BBB/><BBB/><EEE/><FFF/></DDD></XXX><CCC><DDD><BBB/><BBB/><EEE/><FFF/></DDD></CCC><CCC><BBB><BBB><BBB/></BBB></BBB></CCC></AAA>";
        String ex3R1 = "<XXX><DDD><BBB/><BBB/><EEE/><FFF/></DDD></XXX>";

        String ex3R2 = "<DDD><BBB/><BBB/><EEE/><FFF/></DDD>";
        String ex3R3 = "<BBB/>";
        String ex3R4 = "<BBB/>";
        String ex3R5 = "<EEE/>";
        String ex3R6 = "<FFF/>";
        String ex3R7 = "<CCC><DDD><BBB/><BBB/><EEE/><FFF/></DDD></CCC>";

        String ex3R8 = "<DDD><BBB/><BBB/><EEE/><FFF/></DDD>";

        String ex3R9 = "<BBB/>";
        String ex3R10 = "<BBB/>";
        String ex3R11 = "<EEE/>";
        String ex3R12 = "<FFF/>";
        String ex3R13 = "<CCC><BBB><BBB><BBB/></BBB></BBB></CCC>";
        String ex3R14 = "<BBB><BBB><BBB/></BBB></BBB>";
        String ex3R15 = "<BBB><BBB/></BBB>";
        String ex3R16 = "<BBB/>";
        XmlObject[] exXml3 = new XmlObject[]{XmlObject.Factory.parse(ex3R0),
                                             XmlObject.Factory.parse(ex3R1),
                                             XmlObject.Factory.parse(ex3R2),
                                             XmlObject.Factory.parse(ex3R3),
                                             XmlObject.Factory.parse(ex3R4),
                                             XmlObject.Factory.parse(ex3R5),
                                             XmlObject.Factory.parse(ex3R6),
                                             XmlObject.Factory.parse(ex3R7),
                                             XmlObject.Factory.parse(ex3R8),
                                             XmlObject.Factory.parse(ex3R9),
                                             XmlObject.Factory.parse(ex3R10),
                                             XmlObject.Factory.parse(ex3R11),
                                             XmlObject.Factory.parse(ex3R12),
                                             XmlObject.Factory.parse(ex3R13),
                                             XmlObject.Factory.parse(ex3R14),
                                             XmlObject.Factory.parse(ex3R15),
                                             XmlObject.Factory.parse(ex3R16)};

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, exXml1);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, exXml2);
        x2.dispose();

        System.out.println("Test 3: " + ex3Simple);

        XmlCursor x3 = xDoc.newCursor();
        x3.selectPath(fixPath(ex3Simple));
        //XPathCommon.display(x3);
        System.out.println(x3.xmlText());
        XPathCommon.compare(x3, exXml3);
        x3.dispose();

    }

    /**
     * @throws Exception
     */
    public void testZvonExample4()
        throws Exception
    {
        System.out.println("====== Example-4 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon4.xml"));
        XmlCursor xc = xDoc.newCursor();
        String ex1Simple = "/AAA/BBB[1]";
        String ex2Simple = "/AAA/BBB[last()]";
        String exR = "<BBB/>";
        XmlObject[] exXml = new XmlObject[]{XmlObject.Factory.parse(exR)};


        System.out.println("Test 1: " + fixPath(ex1Simple));
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, exXml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, exXml);
        x2.dispose();

    }

    /**
     * @throws Exception
     */
    public void testZvonExample5()
        throws Exception
    {
        System.out.println("====== Example-5 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon5.xml"));
        XmlCursor xc = xDoc.newCursor();

        String ex1Simple = "//@id";
        XmlObject[] ex1Xml = new XmlObject[]{
            XmlObject.Factory.parse("<xml-fragment id=\"b1\"/>"),
            XmlObject.Factory.parse("<xml-fragment id=\"b2\"/>")};

        String ex2Simple = "//BBB[@id]";
        XmlObject[] ex2Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BBB id = \"b1\"/>"),
            XmlObject.Factory.parse("<BBB id = \"b2\"/>")};

        String ex3Simple = "//BBB[@name]";
        XmlObject[] ex3Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BBB name=\"bbb\"/>")};

        String ex4Simple = "//BBB[@*]";
        XmlObject[] ex4Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BBB id = \"b1\"/>"),
            XmlObject.Factory.parse("<BBB id = \"b2\"/>"),
            XmlObject.Factory.parse("<BBB name=\"bbb\"/>")};

        String ex5Simple = "//BBB[not(@*)]";
        XmlObject[] ex5Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BBB/>")
        };

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

        System.out.println("Test 3: " + ex3Simple);
        XmlCursor x3 = xDoc.newCursor();
        x3.selectPath(fixPath(ex3Simple));
        //XPathCommon.display(x3);
        XPathCommon.compare(x3, ex3Xml);
        x3.dispose();

        System.out.println("Test 4: " + ex4Simple);
        XmlCursor x4 = xDoc.newCursor();
        x4.selectPath(fixPath(ex4Simple));
        //XPathCommon.display(x4);
        XPathCommon.compare(x4, ex4Xml);
        x4.dispose();

        System.out.println("Test 5: " + ex5Simple);
        XmlCursor x5 = xDoc.newCursor();
        x5.selectPath(fixPath(ex5Simple));
        //XPathCommon.display(x5);
        XPathCommon.compare(x5, ex5Xml);

        x5.dispose();
    }

    /**
     * @throws Exception
     */
    public void testZvonExample6()
        throws Exception
    {
        System.out.println("====== Example-16 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon6.xml"));
        XmlCursor xc = xDoc.newCursor();

        String ex1Simple = "//BBB[@id='b1']";
        XmlObject[] ex1Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BBB id = \"b1\"/>")};

        String ex2Simple = "//BBB[@name='bbb']";
        XmlObject[] ex2Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BBB name=\"bbb\"/>")};
        String ex3Simple = "//BBB[normalize-space(@name)='bbb']";
        XmlObject[] ex3Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BBB name=\" bbb \"/>")
            ,
            XmlObject.Factory.parse("<BBB name=\"bbb\"/>")};

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

        System.out.println("Test 3: " + ex3Simple);
        XmlCursor x3 = xDoc.newCursor();
        x3.selectPath(fixPath(ex3Simple));
        //XPathCommon.display(x3);
        XPathCommon.compare(x3, ex3Xml);
        x3.dispose();
    }

    /**
     * @throws Exception
     */
    public void testZvonExample7()
        throws Exception
    {
        System.out.println("====== Example-7 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon7.xml"));
        XmlCursor xc = xDoc.newCursor();

        String ex1Simple = "//*[count(BBB)=2]";
        XmlObject[] ex1Xml = new XmlObject[]{
            XmlObject.Factory.parse("<DDD><BBB/><BBB/></DDD>")};
        String ex2Simple = "//*[count(*)=2]";
        XmlObject[] ex2Xml = new XmlObject[]{
            XmlObject.Factory.parse("<DDD><BBB/><BBB/></DDD>"),
            XmlObject.Factory.parse("<EEE><CCC/><DDD/></EEE>")};
        String ex3Simple = "//*[count(*)=3]";
        XmlObject[] ex3Xml = new XmlObject[]{
            XmlObject.Factory.parse(
                "<AAA><CCC><BBB/><BBB/><BBB/></CCC><DDD><BBB/><BBB/></DDD><EEE><CCC/><DDD/></EEE></AAA>"),
            XmlObject.Factory.parse("<CCC><BBB/><BBB/><BBB/></CCC>")};

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

        System.out.println("Test 3: " + ex3Simple);
        XmlCursor x3 = xDoc.newCursor();
        x3.selectPath(fixPath(ex3Simple));
        //XPathCommon.display(x3);
        XPathCommon.compare(x3, ex3Xml);
        x3.dispose();
    }

    /**
     * @throws Exception
     */
    public void testZvonExample8()
        throws Exception
    {
        System.out.println("====== Example-8 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon8.xml"));
        XmlCursor xc = xDoc.newCursor();
        String ex1Simple = "//*[name()='BBB']";
        XmlObject[] ex1Xml = new XmlObject[]{XmlObject.Factory.parse("<BBB/>"),
                                             XmlObject.Factory.parse("<BBB/>"),
                                             XmlObject.Factory.parse("<BBB/>"),
                                             XmlObject.Factory.parse("<BBB/>"),
                                             XmlObject.Factory.parse("<BBB/>"),
        };
        String ex2Simple = "//*[starts-with(name(),'B')]";
        XmlObject[] ex2Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BCC><BBB/><BBB/><BBB/></BCC>"),
            XmlObject.Factory.parse("<BBB/>"),
            XmlObject.Factory.parse("<BBB/>"),
            XmlObject.Factory.parse("<BBB/>"),
            XmlObject.Factory.parse("<BBB/>"),
            XmlObject.Factory.parse("<BBB/>"),
            XmlObject.Factory.parse("<BEC><CCC/><DBD/></BEC>")};

        /**ykadiysk: Jaxen prints in BF left-to-right order
         * but XPath wants doc order
         XmlObject[] ex2Xml = new XmlObject[]{XmlObject.Factory.parse("<BCC><BBB/><BBB/><BBB/></BCC>"),

         XmlObject.Factory.parse("<BBB/>"),
         XmlObject.Factory.parse("<BBB/>"),
         XmlObject.Factory.parse("<BEC><CCC/><DBD/></BEC>"),
         XmlObject.Factory.parse("<BBB/>"),
         XmlObject.Factory.parse("<BBB/>"),
         XmlObject.Factory.parse("<BBB/>")};
         */

        String ex3Simple = "//*[contains(name(),'C')]";
        XmlObject[] ex3Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BCC><BBB/><BBB/><BBB/></BCC>"),
            XmlObject.Factory.parse("<BEC><CCC/><DBD/></BEC>"),
            XmlObject.Factory.parse("<CCC/>")};

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        System.out.println("Zvon8 Test 2");
        XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

        System.out.println("Test 3: " + ex3Simple);
        XmlCursor x3 = xDoc.newCursor();
        x3.selectPath(fixPath(ex3Simple));
        //XPathCommon.display(x3);
        XPathCommon.compare(x3, ex3Xml);
        x3.dispose();
    }

    /**
     * @throws Exception
     */
    public void testZvonExample9()
        throws Exception
    {
        System.out.println("====== Example-9 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon9.xml"));
        XmlCursor xc = xDoc.newCursor();
        String ex1Simple = "//*[string-length(name()) = 3]";
        XmlObject[] ex1Xml = new XmlObject[]{
            XmlObject.Factory.parse(
                "<AAA><Q/><SSSS/><BB/><CCC/><DDDDDDDD/><EEEE/></AAA>"),
            XmlObject.Factory.parse("<CCC/>")};
        String ex2Simple = "//*[string-length(name()) < 3]";
        XmlObject[] ex2Xml = new XmlObject[]{XmlObject.Factory.parse("<Q/>"),
                                             XmlObject.Factory.parse("<BB/>")};
        String ex3Simple = "//*[string-length(name()) > 3]";
        XmlObject[] ex3Xml = new XmlObject[]{
            XmlObject.Factory.parse("<SSSS/>"),
            XmlObject.Factory.parse("<DDDDDDDD/>"),
            XmlObject.Factory.parse("<EEEE/>")};

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

        System.out.println("Test 3: " + ex3Simple);
        XmlCursor x3 = xDoc.newCursor();
        x3.selectPath(fixPath(ex3Simple));
        //XPathCommon.display(x3);
        XPathCommon.compare(x3, ex3Xml);
        x3.dispose();
    }

    /**
     * @throws Exception
     */
    public void testZvonExample10()
        throws Exception
    {
        System.out.println("====== Example-10 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon10.xml"));
        XmlCursor xc = xDoc.newCursor();
        String ex1Simple = "$this//CCC | $this//BBB";
        //document order
        XmlObject[] ex1Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BBB/>"),
            XmlObject.Factory.parse("<CCC/>"),
            XmlObject.Factory.parse("<CCC/>")

        };
        // Nodes are returned in document order
        String ex2Simple = "$this/AAA/EEE | $this//BBB";

        XmlObject[] ex2Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BBB/>"),
            XmlObject.Factory.parse("<EEE/>")

        };

        String ex3Simple = "./AAA/EEE |.//DDD/CCC | ./AAA | .//BBB";
        XmlObject[] ex3Xml = new XmlObject[]{
            XmlObject.Factory.parse(
                            "<AAA><BBB/><CCC/><DDD><CCC/></DDD><EEE/></AAA>"),            
            XmlObject.Factory.parse("<BBB/>"),
            XmlObject.Factory.parse("<CCC/>"),
            XmlObject.Factory.parse("<EEE/>")
        };

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(ex1Simple);
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(ex2Simple);
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

        System.out.println("Test 3: " + ex3Simple);
        XmlCursor x3 = xDoc.newCursor();
        x3.selectPath(fixPath(ex3Simple));
        //XPathCommon.display(x3);
        XPathCommon.compare(x3, ex3Xml);
        x3.dispose();
    }

    /**
     * @throws Exception
     */
    public void testZvonExample11()
        throws Exception
    {
        System.out.println("====== Example-11 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon11.xml"));
        XmlCursor xc = xDoc.newCursor();
        String ex1Simple = "/AAA";
        XmlObject[] ex1Xml = new XmlObject[]{
            XmlObject.Factory.parse("<AAA><BBB/><CCC/></AAA>")};

        String ex2Simple = "/child::AAA";
        XmlObject[] ex2Xml = new XmlObject[]{
            XmlObject.Factory.parse("<AAA><BBB/><CCC/></AAA>")};

        String ex3Simple = "/AAA/BBB";
        XmlObject[] ex3Xml = new XmlObject[]{XmlObject.Factory.parse("<BBB/>")};

        String ex4Simple = "/child::AAA/child::BBB";
        XmlObject[] ex4Xml = new XmlObject[]{XmlObject.Factory.parse("<BBB/>")};

        String ex5Simple = "/child::AAA/BBB";
        XmlObject[] ex5Xml = new XmlObject[]{XmlObject.Factory.parse("<BBB/>")};


        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

        System.out.println("Test 3: " + ex3Simple);
        XmlCursor x3 = xDoc.newCursor();
        x3.selectPath(fixPath(ex3Simple));
        //XPathCommon.display(x3);
        XPathCommon.compare(x3, ex3Xml);
        x3.dispose();

        System.out.println("Test 4: " + ex4Simple);
        XmlCursor x4 = xDoc.newCursor();
        x4.selectPath(fixPath(ex4Simple));
        XPathCommon.display(x4);
        XPathCommon.display(ex4Xml);
        XPathCommon.compare(x4, ex4Xml);
        x4.dispose();

        System.out.println("Test 5: " + ex5Simple);
        XmlCursor x5 = xDoc.newCursor();
        x5.selectPath(fixPath(ex5Simple));
        //XPathCommon.display(x5);
        XPathCommon.compare(x5, ex5Xml);
        x5.dispose();
    }

    /**
     * @throws Exception
     */
    public void testZvonExample12()
        throws Exception
    {
        System.out.println("====== Example-12 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon12.xml"));
        XmlCursor xc = xDoc.newCursor();
        String ex1Simple = "/descendant::*";
        XmlObject[] ex1Xml = new XmlObject[]{
            XmlObject.Factory.parse(
                "<AAA><BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB><CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC></AAA>"),
            XmlObject.Factory.parse(
                "<BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB>"),

            XmlObject.Factory.parse("<DDD><CCC><DDD/><EEE/></CCC></DDD>"),

            XmlObject.Factory.parse("<CCC><DDD/><EEE/></CCC>"),


            XmlObject.Factory.parse("<DDD/>"),


            XmlObject.Factory.parse("<EEE/>"),
            XmlObject.Factory.parse(
                "<CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC>"),
            XmlObject.Factory.parse("<DDD><EEE><DDD><FFF/></DDD></EEE></DDD>"),
            XmlObject.Factory.parse("<EEE><DDD><FFF/></DDD></EEE>"),
            XmlObject.Factory.parse("<DDD><FFF/></DDD>"),
            XmlObject.Factory.parse("<FFF/>")};

        String ex2Simple = "/AAA/BBB/descendant::*";
        XmlObject[] ex2Xml = new XmlObject[]{
            XmlObject.Factory.parse("<DDD><CCC><DDD/><EEE/></CCC></DDD>"),
            XmlObject.Factory.parse("<CCC><DDD/><EEE/></CCC>"),
            XmlObject.Factory.parse("<DDD/>"),
            XmlObject.Factory.parse("<EEE/>")};

        String ex3Simple = "//CCC/descendant::*";
        XmlObject[] ex3Xml = new XmlObject[]{
            XmlObject.Factory.parse("<DDD/>"),
            XmlObject.Factory.parse("<EEE/>"),
            XmlObject.Factory.parse("<DDD><EEE><DDD><FFF/></DDD></EEE></DDD>"),
            XmlObject.Factory.parse("<EEE><DDD><FFF/></DDD></EEE>"),
            XmlObject.Factory.parse("<DDD><FFF/></DDD>"),
            XmlObject.Factory.parse("<FFF/>")

        };
        String ex4Simple = "//CCC/descendant::DDD";
        XmlObject[] ex4Xml = new XmlObject[]{
            XmlObject.Factory.parse("<DDD/>"),
            XmlObject.Factory.parse("<DDD><EEE><DDD><FFF/></DDD></EEE></DDD>"),
            XmlObject.Factory.parse("<DDD><FFF/></DDD>"),
        };

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

        System.out.println("Test 3: " + ex3Simple);
        XmlCursor x3 = xDoc.newCursor();
        x3.selectPath(fixPath(ex3Simple));
        //XPathCommon.display(x3);
        XPathCommon.compare(x3, ex3Xml);
        x3.dispose();

        System.out.println("Test 4: " + ex4Simple);
        XmlCursor x4 = xDoc.newCursor();
        x4.selectPath(fixPath(ex4Simple));
        //XPathCommon.display(x4);
        XPathCommon.compare(x4, ex4Xml);
        x4.dispose();
    }

    /**
     * @throws Exception
     */
    public void testZvonExample13()
        throws Exception
    {
        System.out.println("====== Example-13 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon13.xml"));
        XmlCursor xc = xDoc.newCursor();
        String ex1Simple = "//DDD/parent::*";

        XmlObject[] ex1Xml = new XmlObject[]{
            XmlObject.Factory.parse(
                "<BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB>"),

            XmlObject.Factory.parse("<CCC><DDD/><EEE/></CCC>"),
            XmlObject.Factory.parse(
                "<CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC>"),
            XmlObject.Factory.parse("<EEE><DDD><FFF/></DDD></EEE>")};

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();
    }

    /**
     * @throws Exception
     */
    public void testZvonExample14()
        throws Exception
    {
        System.out.println("====== Example-14 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon14.xml"));
        XmlCursor xc = xDoc.newCursor();
        String ex1Simple = "/AAA/BBB/DDD/CCC/EEE/ancestor::*";
        XmlObject[] ex1Xml = new XmlObject[]{
            XmlObject.Factory.parse(
                "<AAA><BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB><CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC></AAA>"),
            XmlObject.Factory.parse(
                "<BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB>"),
            XmlObject.Factory.parse("<DDD><CCC><DDD/><EEE/></CCC></DDD>"),
            XmlObject.Factory.parse("<CCC><DDD/><EEE/></CCC>")
        };

        String ex2Simple = "//FFF/ancestor::*";
        XmlObject[] ex2Xml = new XmlObject[]{
            XmlObject.Factory.parse(
                "<AAA><BBB><DDD><CCC><DDD/><EEE/></CCC></DDD></BBB><CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC></AAA>"),
            XmlObject.Factory.parse(
                "<CCC><DDD><EEE><DDD><FFF/></DDD></EEE></DDD></CCC>"),
            XmlObject.Factory.parse("<DDD><EEE><DDD><FFF/></DDD></EEE></DDD>"),
            XmlObject.Factory.parse("<EEE><DDD><FFF/></DDD></EEE>"),
            XmlObject.Factory.parse("<DDD><FFF/></DDD>")
        };

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

    }

    /**
     * @throws Exception
     */
    public void testZvonExample15()
        throws Exception
    {
        System.out.println("====== Example-15 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon15.xml"));
        XmlCursor xc = xDoc.newCursor();
        String ex1Simple = "/AAA/BBB/following-sibling::*";
        XmlObject[] ex1Xml = new XmlObject[]{
            XmlObject.Factory.parse(
                "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>"),
            XmlObject.Factory.parse("<CCC><DDD/></CCC>")};
        String ex2Simple = "//CCC/following-sibling::*";
        XmlObject[] ex2Xml = new XmlObject[]{XmlObject.Factory.parse("<DDD/>"),
                                             XmlObject.Factory.parse("<FFF/>"),
                                             XmlObject.Factory.parse(
                                                 "<FFF><GGG/></FFF>")};

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

    }

    /**
     * @throws Exception
     */
    public void testZvonExample16()
        throws Exception
    {
        System.out.println("====== Example-16 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon16.xml"));
        XmlCursor xc = xDoc.newCursor();
        String ex1Simple = "/AAA/XXX/preceding-sibling::*";
        XmlObject[] ex1Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BBB><CCC/><DDD/></BBB>")};
        String ex2Simple = "//CCC/preceding-sibling::*";
        XmlObject[] ex2Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BBB><CCC/><DDD/></BBB>"),
            XmlObject.Factory.parse(
                "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>"),
            XmlObject.Factory.parse("<EEE/>"),
            XmlObject.Factory.parse("<DDD/>")};

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

    }

    /**
     * @throws Exception
     */
    public void testZvonExample17()
        throws Exception
    {
        System.out.println("====== Example-17 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon17.xml"));
        XmlCursor xc = xDoc.newCursor();
        String ex1Simple = "/AAA/XXX/following::*";
        XmlObject[] ex1Xml = new XmlObject[]{
            XmlObject.Factory.parse("<CCC><DDD/></CCC>"),
            XmlObject.Factory.parse("<DDD/>")};
        String ex2Simple = "//ZZZ/following::*";
        XmlObject[] ex2Xml = new XmlObject[]{
            XmlObject.Factory.parse("<FFF><GGG/></FFF>"),
            XmlObject.Factory.parse("<GGG/>"),
            XmlObject.Factory.parse(
                "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>"),
            XmlObject.Factory.parse(
                "<DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD>"),
            XmlObject.Factory.parse("<EEE/>"),
            XmlObject.Factory.parse("<DDD/>"),
            XmlObject.Factory.parse("<CCC/>"),
            XmlObject.Factory.parse("<FFF/>"),
            XmlObject.Factory.parse("<FFF><GGG/></FFF>"),
            XmlObject.Factory.parse("<GGG/>"),
            XmlObject.Factory.parse("<CCC><DDD/></CCC>"),
            XmlObject.Factory.parse("<DDD/>")};

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();
    }

    /**
     * the preceding axis contains all nodes that are descendants of the root
     * of the tree in which the context node is found, are not ancestors of
     * the context node, and occur before the context node in document order
     *
     * @throws Exception
     */
    public void testZvonExample18()
        throws Exception
    {
        System.out.println("====== Example-18 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon18.xml"));
        XmlCursor xc = xDoc.newCursor();
        String ex1Simple = "/AAA/XXX/preceding::*";
        XmlObject[] ex1Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BBB><CCC/><ZZZ><DDD/></ZZZ></BBB>"),
            XmlObject.Factory.parse("<CCC/>"),
            XmlObject.Factory.parse("<ZZZ><DDD/></ZZZ>"),
            XmlObject.Factory.parse("<DDD/>")
        };

        String ex2Simple = "//GGG/preceding::*";
        XmlObject[] ex2Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BBB><CCC/><ZZZ><DDD/></ZZZ></BBB>"),
            XmlObject.Factory.parse("<CCC/>"),
            XmlObject.Factory.parse("<ZZZ><DDD/></ZZZ>"),
            XmlObject.Factory.parse("<DDD/>"),

            XmlObject.Factory.parse("<EEE/>"),
            XmlObject.Factory.parse("<DDD/>"),
            XmlObject.Factory.parse("<CCC/>"),
            XmlObject.Factory.parse("<FFF/>")

            /*XmlObject.Factory.parse(
                                               "<DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD>"),
                                           XmlObject.Factory.parse(
                                               "<CCC><DDD/></CCC>"),
                                           XmlObject.Factory.parse(
                                               "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>"),
                                           XmlObject.Factory.parse("<DDD/>"),
                                           */
        };

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

    }

    /**
     * @throws Exception
     */
    public void testZvonExample19()
        throws Exception
    {
        System.out.println("====== Example-19 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon19.xml"));
        XmlCursor xc = xDoc.newCursor();
        String ex1Simple = "/AAA/XXX/descendant-or-self::*";

        XmlObject[] ex1Xml = new XmlObject[]{
            XmlObject.Factory.parse(
                "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>"),
            XmlObject.Factory.parse(
                "<DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD>"),
            XmlObject.Factory.parse("<EEE/>"),
            XmlObject.Factory.parse("<DDD/>"),
            XmlObject.Factory.parse("<CCC/>"),
            XmlObject.Factory.parse("<FFF/>"),
            XmlObject.Factory.parse("<FFF><GGG/></FFF>"),
            XmlObject.Factory.parse("<GGG/>")};
        String ex2Simple = "//CCC/descendant-or-self::*";

        XmlObject[] ex2Xml = new XmlObject[]{
            XmlObject.Factory.parse("<CCC/>"),
            XmlObject.Factory.parse("<CCC/>"),
            XmlObject.Factory.parse("<CCC><DDD/></CCC>"),
            XmlObject.Factory.parse("<DDD/>"),
        };

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

    }

    /**
     * @throws Exception
     */
    public void testZvonExample20()
        throws Exception
    {
        System.out.println("====== Example-20 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon20.xml"));
        XmlCursor xc = xDoc.newCursor();

        String ex1Simple = "/AAA/XXX/DDD/EEE/ancestor-or-self::*";
        XmlObject[] ex1Xml = new XmlObject[]{
            XmlObject.Factory.parse(
                "<AAA><BBB><CCC/><ZZZ><DDD/></ZZZ></BBB><XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX><CCC><DDD/></CCC></AAA>"),
            XmlObject.Factory.parse(
                "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>"),
            XmlObject.Factory.parse(
                "<DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD>"),
            XmlObject.Factory.parse("<EEE/>"),
        };
        String ex2Simple = "//GGG/ancestor-or-self::*";
        XmlObject[] ex2Xml = new XmlObject[]{
            XmlObject.Factory.parse(
                "<AAA><BBB><CCC/><ZZZ><DDD/></ZZZ></BBB><XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX><CCC><DDD/></CCC></AAA>"),
            XmlObject.Factory.parse(
                "<XXX><DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD></XXX>"),
            XmlObject.Factory.parse(
                "<DDD><EEE/><DDD/><CCC/><FFF/><FFF><GGG/></FFF></DDD>"),
            XmlObject.Factory.parse("<FFF><GGG/></FFF>"),
            XmlObject.Factory.parse("<GGG/>"),
        };

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

    }

    /**
     * @throws Exception
     */
    public void testZvonExample21()
        throws Exception
    {
        System.out.println("====== Example-21 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon21.xml"));
        XmlCursor xc = xDoc.newCursor();
        String ex1Simple = "//GGG/ancestor::*";
        XmlObject[] ex1Xml = new XmlObject[]{

            XmlObject.Factory.parse(
                "<AAA><BBB><CCC/><ZZZ/></BBB><XXX><DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD></XXX><CCC><DDD/></CCC></AAA>"),
            XmlObject.Factory.parse(
                "<XXX><DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD></XXX>"),
            XmlObject.Factory.parse(
                "<DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD>"),
            XmlObject.Factory.parse(
                "<FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF>"),
        };
        String ex2Simple = "//GGG/descendant::*";
        XmlObject[] ex2Xml = new XmlObject[]{
            XmlObject.Factory.parse("<JJJ><QQQ/></JJJ>"),
            XmlObject.Factory.parse("<QQQ/>"),
            XmlObject.Factory.parse("<JJJ/>")};
        String ex3Simple = "//GGG/following::*";
        XmlObject[] ex3Xml = new XmlObject[]{XmlObject.Factory.parse("<HHH/>"),
                                             XmlObject.Factory.parse(
                                                 "<CCC><DDD/></CCC>"),
                                             XmlObject.Factory.parse("<DDD/>")};
        String ex4Simple = "//GGG/preceding::*";
        XmlObject[] ex4Xml = new XmlObject[]{
            XmlObject.Factory.parse("<BBB><CCC/><ZZZ/></BBB>"),
            XmlObject.Factory.parse("<CCC/>"),
            XmlObject.Factory.parse("<ZZZ/>"),
            XmlObject.Factory.parse("<EEE/>"),
            XmlObject.Factory.parse("<HHH/>"),
        };
        String ex5Simple = "//GGG/self::*";
        XmlObject[] ex5Xml = new XmlObject[]{
            XmlObject.Factory.parse("<GGG><JJJ><QQQ/></JJJ><JJJ/></GGG>")};

        String ex6Simple = "//GGG/ancestor::* | //GGG/descendant::* | //GGG/following::* | //GGG/preceding::* | //GGG/self::*";
        XmlObject[] ex6Xml = new XmlObject[]{
            XmlObject.Factory.parse(
                "<AAA><BBB><CCC/><ZZZ/></BBB><XXX><DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD></XXX><CCC><DDD/></CCC></AAA>"),
            XmlObject.Factory.parse("<BBB><CCC/><ZZZ/></BBB>"),
            XmlObject.Factory.parse("<CCC/>"),
            XmlObject.Factory.parse("<ZZZ/>"),
            XmlObject.Factory.parse(
                "<XXX><DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD></XXX>"),
            XmlObject.Factory.parse(
                "<DDD><EEE/><FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF></DDD>"),
            XmlObject.Factory.parse("<EEE/>"),
            XmlObject.Factory.parse(
                "<FFF><HHH/><GGG><JJJ><QQQ/></JJJ><JJJ/></GGG><HHH/></FFF>"),
            XmlObject.Factory.parse("<HHH/>"),
            XmlObject.Factory.parse("<GGG><JJJ><QQQ/></JJJ><JJJ/></GGG>"),
            XmlObject.Factory.parse("<JJJ><QQQ/></JJJ>"),
            XmlObject.Factory.parse("<QQQ/>"),
            XmlObject.Factory.parse("<JJJ/>"),
            XmlObject.Factory.parse("<HHH/>"),
            XmlObject.Factory.parse("<CCC><DDD/></CCC>"),
            XmlObject.Factory.parse("<DDD/>"),
        };

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();

        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

        System.out.println("Test 3: " + ex3Simple);
        XmlCursor x3 = xDoc.newCursor();
        x3.selectPath(fixPath(ex3Simple));
        //XPathCommon.display(x3);
        XPathCommon.compare(x3, ex3Xml);
        x3.dispose();

        System.out.println("Test 4: " + ex4Simple);
        XmlCursor x4 = xDoc.newCursor();
        x4.selectPath(fixPath(ex4Simple));
        int i = 0;
        /*   System.out.println("****************HERE");

           while (i < x4.getSelectionCount())
           {
               x4.toSelection(i++);
               System.out.println(x4.getName() + ((i - 1) + ""));
           }

           System.out.println("****************SETS");
           XPathCommon.display(x4);
           //XPathCommon.display(ex4Xml);
           System.out.println("**************END SETS");
        */
        XPathCommon.compare(x4, ex4Xml);
        x4.dispose();


        System.out.println("Test 5: " + ex5Simple);
        XmlCursor x5 = xDoc.newCursor();
        x5.selectPath(fixPath(ex5Simple));
        //XPathCommon.display(x5);
        XPathCommon.compare(x5, ex5Xml);
        x5.dispose();

        System.out.println("Test 6: " + ex6Simple);
        XmlCursor x6 = xDoc.newCursor();
        x6.selectPath(fixPath(ex6Simple));
        //XPathCommon.display(x6);
        XPathCommon.compare(x6, ex6Xml);
        x6.dispose();
    }

    /**
     * @throws Exception
     */
    public void testZvonExample22()
        throws Exception
    {
        System.out.println("====== Example-22 ==========");
        XmlObject xDoc = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/xpath/zvon22.xml"));
        XmlCursor xc = xDoc.newCursor();

        String ex1Simple = "//BBB[position() mod 2 = 0 ]";
        XmlObject[] ex1Xml = new XmlObject[]{XmlObject.Factory.parse("<BBB/>"),
                                             XmlObject.Factory.parse("<BBB/>"),
                                             XmlObject.Factory.parse("<BBB/>"),
                                             XmlObject.Factory.parse("<BBB/>")};

        String ex2Simple = "//BBB[ position() = floor(last() div 2 + 0.5) or position() = ceiling(last() div 2 + 0.5) ]";
        XmlObject[] ex2Xml = new XmlObject[]{XmlObject.Factory.parse("<BBB/>"),
                                             XmlObject.Factory.parse("<BBB/>")};

        String ex3Simple = "//CCC[ position() = floor(last() div 2 + 0.5) or position() = ceiling(last() div 2 + 0.5) ]";
        XmlObject[] ex3Xml = new XmlObject[]{XmlObject.Factory.parse("<CCC/>")};

        System.out.println("Test 1: " + ex1Simple);
        XmlCursor x1 = xDoc.newCursor();
        x1.selectPath(fixPath(ex1Simple));
        //XPathCommon.display(x1);
        XPathCommon.compare(x1, ex1Xml);
        x1.dispose();
        System.out.println("Test 2: " + ex2Simple);
        XmlCursor x2 = xDoc.newCursor();
        x2.selectPath(fixPath(ex2Simple));
        //XPathCommon.display(x2);
        XPathCommon.compare(x2, ex2Xml);
        x2.dispose();

        System.out.println("Test 3: " + ex3Simple);
        XmlCursor x3 = xDoc.newCursor();
        x3.selectPath(fixPath(ex3Simple));
        //XPathCommon.display(x3);
        XPathCommon.compare(x3, ex3Xml);
        x3.dispose();
    }


   /* public static void main(String[] rgs)
    {
        try
        {
            new XPathTest("").testZvonExample21();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    */
//    public void testZvonExample() throws Exception {
//        XmlObject xDoc = XmlObject.Factory.parse(JarUtil.getResourceFromJar(Common.XMLCASES_JAR,
//                                                                            "xbean/xmlcursor/xpath/zvon.xml"));
//        XmlCursor xc = xDoc.newCursor();
//    }


}
