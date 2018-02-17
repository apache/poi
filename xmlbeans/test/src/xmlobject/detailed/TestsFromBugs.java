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

package xmlobject.detailed;

import org.apache.xmlbeans.*;
import com.mytest.Bar;
import com.mytest.Foo;
import com.mytest.Info;
import com.mytest.TestDocument;
import test.xmlobject.test36510.Test36510AppDocument;
import junit.framework.TestCase;

import java.io.File;

/**
 * Test file that implements test cases that come from closing bugs.
 */
public class TestsFromBugs extends TestCase
{
    File instance;

    public TestsFromBugs(String name)
    {
        super(name);
    }

    /**
     * Radar Bug: 36156
     * Problem with Namespace leaking into siblings
     */
    public void test36156()
            throws Exception
    {
        String str = "<x><y xmlns=\"bar\"><z xmlns=\"foo\"/></y><a/></x>";
        XmlObject x = XmlObject.Factory.parse(str);

        assertTrue("Test 36156 failed: ", x.xmlText().equals(str));
    }

    /*
     * Radar Bug: 36510
     */
    public void test36510()
            throws Exception
    {
        String str = "<test36510-app version='1.0' " +
                "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'" +
                " xsi:schemaLocation='http://test/xmlobject/test36510' " +
                "xmlns='http://test/xmlobject/test36510'>" +
                "<testConstraint>" +
                "<customConstraint>" +
                "<description>These portlets don't" +
                " require any guarantee</description>" +
                "<options>BEST</options>" +
                "</customConstraint></testConstraint>" +
                "</test36510-app>";

        Test36510AppDocument doc = Test36510AppDocument.Factory.parse(str);
        str = doc.getTest36510App().getTestConstraintArray()[0].
                getCustomConstraint().getOptions().toString();
        assertTrue("Test 36510 failed: ", str.equals("BEST"));
    }


    /*
     * Radar Bug: 40907
     */
    public void test40907()
            throws Exception
    {
        String str = "<myt:Test xmlns:myt=\"http://www.mytest.com\">" +
                "<myt:foo>" +
                "<myt:fooMember>this is foo member</myt:fooMember>" +
                "</myt:foo>" +
                "</myt:Test>";
        TestDocument doc = TestDocument.Factory.parse(str);

        assertTrue("XML Instance did not validate.", doc.validate());

        Bar bar = Bar.Factory.newInstance();
        bar.setFooMember("new foo member");
        bar.setBarMember("new bar member");

        Info info = doc.getTest();

        Foo foo = info.addNewFoo();
        foo.set(bar);

        assertTrue("Modified XML instance did not validate.", doc.validate());
        str = "<myt:Test xmlns:myt=\"http://www.mytest.com\">" +
                "<myt:foo>" +
                "<myt:fooMember>this is foo member</myt:fooMember>" +
                "</myt:foo>" +
                "<myt:foo xsi:type=\"myt:bar\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "<myt:fooMember>new foo member</myt:fooMember>" +
                "<myt:barMember>new bar member</myt:barMember>" +
                "</myt:foo>" +
                "</myt:Test>";
        assertEquals("XML instance is not as expected", doc.xmlText(), str);

    }

    /**
     * Simple Compilation Tests - If the methods are not present,
     *                          - this class won't compile
     * Ensures method getSourceName is on SchemaComponent and
     * can be called from SchemaGlobalElement and SchemaGlobalAttribute
     * @throws Exception
     */
    public void test199585() throws Exception
    {
        String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "    targetNamespace=\"urn:lax.Doc.Compilation\"\n" +
                "    xmlns:tns=\"urn:lax.Doc.Compilation\"\n" +
                "    xmlns:pre=\"noResolutionNamespace\"\n" +
                "    elementFormDefault=\"qualified\"\n" +
                "    attributeFormDefault=\"unqualified\">\n" +
                "   <xs:element name=\"QuantityElement\" type=\"tns:quantity\" />"+
                "   <xs:simpleType name=\"quantity\">\n" +
                "    <xs:restriction base=\"xs:NMTOKEN\">\n" +
                "      <xs:enumeration value=\"all\"/>\n" +
                "      <xs:enumeration value=\"most\"/>\n" +
                "      <xs:enumeration value=\"some\"/>\n" +
                "      <xs:enumeration value=\"few\"/>\n" +
                "      <xs:enumeration value=\"none\"/>\n" +
                "    </xs:restriction>\n" +
                "  </xs:simpleType>" +
                "</xs:schema>";

        XmlObject[] schemas = new XmlObject[]{
            XmlObject.Factory.parse(str)};
        XmlOptions xOpt = new XmlOptions().setValidateTreatLaxAsSkip();

        SchemaTypeSystem sts = XmlBeans.compileXmlBeans(null, null, schemas,
                null, XmlBeans.getBuiltinTypeSystem(), null, xOpt);

        //ensure SchemaGlobalElement has getSourceName Method
        SchemaGlobalElement[] sge = sts.globalElements();
        for (int i = 0; i < sge.length; i++) {
            System.out.println("SGE SourceName: "+sge[i].getSourceName());

        }
        //ensure SchemaGlobalAttribute has getSourceName Method
        SchemaGlobalAttribute[] sga = sts.globalAttributes();
        for (int i = 0; i < sga.length; i++) {
            System.out.println("SGE SourceName: " + sga[i].getSourceName());
        }

        //ensure SchemaGlobalElement is a subType of SchemaComponent
        SchemaComponent[] sce = sts.globalElements();
        for (int i = 0; i < sce.length; i++) {
            System.out.println("SCE SourceName: " + sce[i].getSourceName());

        }

        //ensure SchemaGlobalAttribute is a subType of SchemaComponent
        SchemaComponent[] sca = sts.globalElements();
        for (int i = 0; i < sca.length; i++) {
            System.out.println("SCA SourceName: " + sca[i].getSourceName());
        }



    }

}
