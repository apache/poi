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

package xmlobject.checkin;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.framework.Assert;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.QNameSetBuilder;
import org.apache.xml.test.selectChldAtt.DocDocument;
import org.apache.xml.test.selectChldAtt.TypeExtendedC;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

public class SelectChldAttTests extends TestCase
{
    public SelectChldAttTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(SelectChldAttTests.class); }

    private static final String XSI_URI="http://www.w3.org/2001/XMLSchema-instance";

    public void testSelect() throws XmlException
    {
        String uri = "http://xml.apache.org/test/selectChldAtt";

        String xml = "<doc xmlns='" + uri + "'>\n" +
            "  <int>7</int>\n" +
            "  <string> ... some text ... </string>\n" +

            "  <elemA price='4.321'>\n" +
            "    <topLevelElement> this is wildcard bucket </topLevelElement>\n" +
            "  </elemA>\n" +

            "  <elemB xmlns:p='uri:other_namespace' \n" +
            "       p:att='attribute in #other namespace'>\n" +
            "    <someElement>2</someElement>\n" +
            "    <p:otherElement> element in #other namespace </p:otherElement>\n" +
            "  </elemB>\n" +

            "  <elemC xmlns:xsi='" + XSI_URI + "' \n" +
            "         xmlns:p='uri_other_namespace' \n" +
            "         xsi:type='typeExtendedC' \n" +
            "         att1='attribute from typeC' \n" +
            "         aditionalAtt='attribute added in type extension' \n" +
            "         p:validAtt='attribute in any bucket' >\n" +
            "    <someElement> element from typeC </someElement>\n" +
            "    <p:validElement> element in the 'any' bucket for typeExtendedC </p:validElement>\n" +
            "    <aditionalElement> element from typeExtendedC </aditionalElement>\n" +
            "  </elemC>\n" +
            "</doc>";

        DocDocument document = DocDocument.Factory.parse(xml);
        DocDocument.Doc doc = document.getDoc();
        Collection errors = new ArrayList();
        Assert.assertTrue("Valid instance", doc.validate(new XmlOptions().setErrorListener(errors)));
        printErrors(errors);

        XmlObject xo;
        XmlObject[] xos;

        // select a known element
        xos = doc.selectChildren(new QName(uri, "int"));
        //print("1 selectChildren 'int' : ", xos);
        Assert.assertTrue("1 selectChildren 'int' : ", verifyResult(xos, new String[]{"<xml-fragment>7</xml-fragment>"}));

        xos = doc.selectChildren(uri, "string");
        //print("2 selectChildren 'string' : ", xos);
        Assert.assertTrue("2 selectChildren 'string' : ", verifyResult(xos, new String[]{"<xml-fragment>... some text ...</xml-fragment>"}));

        // elemA
        xos = doc.selectChildren(new QName(uri, "elemA"));
        //print("3 selectChildren 'elemA' : ", xos);
        Assert.assertTrue("3 selectChildren 'elemA' : ",
            verifyResult(xos, new String[]{"<xml-fragment price=\"4.321\" xmlns:sel=\"" + uri +"\">\n" +
            "  <sel:topLevelElement>this is wildcard bucket</sel:topLevelElement>\n" +
            "</xml-fragment>"}));

        // select a known attribute
        xo = xos[0].selectAttribute(new QName("", "price"));
        //print("4     selectAttribute 'price' : ", xo);
        Assert.assertTrue("4     selectAttribute 'price' : ",
            verifyResult(xo, "<xml-fragment>4.321</xml-fragment>"));

        // select all attributes
        QNameSet qns = QNameSet.forWildcardNamespaceString("##any", uri);
        xos = xos[0].selectAttributes(qns);
        //print("5     selectAttributes set'##any' :", xos);
        Assert.assertTrue("5     selectAttributes set'##any' :",
            verifyResult(xos, new String[]{"<xml-fragment>4.321</xml-fragment>"}));

        // elemB
        xos = doc.selectChildren(new QName(uri, "elemB"));
        //print("6 selectChildren 'elemB' : ", xos);

        //print("7     selectChildren set'##other' : " , xos[0].selectChildren(QNameSet.forWildcardNamespaceString("##other", uri)));
        Assert.assertTrue("7     selectChildren set'##other' : ",
            verifyResult( xos[0].selectChildren(QNameSet.forWildcardNamespaceString("##other", uri))
            , new String[]{"<xml-fragment xmlns:p=\"uri:other_namespace\">element in #other namespace</xml-fragment>"}));
        //print("8     selectAttributes set'##other' : ", xos[0].selectAttributes(QNameSet.forWildcardNamespaceString("##other", uri)));
        Assert.assertTrue("8     selectAttributes set'##other' : ",
            verifyResult(xos[0].selectAttributes(QNameSet.forWildcardNamespaceString("##other", uri)),
            new String[]{"<xml-fragment xmlns:p=\"uri:other_namespace\">attribute in #other namespace</xml-fragment>"}));

        // elemC
        xos = doc.selectChildren(new QName(uri, "elemC"));
        //print("9 selectChildren 'elemC' : ", xos);
        //print("10    selectChildren set'##any' : " , xos[0].selectChildren(QNameSet.forWildcardNamespaceString("##any", uri)));
        Assert.assertTrue("10    selectChildren set'##any' : ",
            verifyResult(xos[0].selectChildren(QNameSet.forWildcardNamespaceString("##any", uri))
            , new String[]{"<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\">element from typeC</xml-fragment>",
            "<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\">element in the 'any' bucket for typeExtendedC</xml-fragment>",
            "<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\">element from typeExtendedC</xml-fragment>"}));

        // select elements in the any bucket by excluding the the known elements
        QNameSetBuilder qnsb = new QNameSetBuilder();
        qnsb.add(new QName(uri, "someElement"));
        qnsb.add(new QName(uri, "aditionalElement"));
        qnsb.invert();

        //print("11a    selectChildren in the any bucket for typeExtendedC: " , xos[0].selectChildren(qnsb.toQNameSet()));
        Assert.assertTrue("11a    selectChildren in the any bucket for typeExtendedC: ",
            verifyResult(xos[0].selectChildren(qnsb.toQNameSet()),
            new String[]{"<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\">element in the 'any' bucket for typeExtendedC</xml-fragment>"}));

        //print("11b    selectChildren in the any bucket for typeExtendedC: " , xos[0].selectChildren(TypeExtendedC.type.qnameSetForWildcardElements()));
        Assert.assertTrue("11b    selectChildren in the any bucket for typeExtendedC: ",
            verifyResult(xos[0].selectChildren(TypeExtendedC.type.qnameSetForWildcardElements()),
            new String[]{"<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\">element in the 'any' bucket for typeExtendedC</xml-fragment>"}));

        // select attributes in the any bucket by excluding the the known attributes
        qnsb = new QNameSetBuilder();
        qnsb.add(new QName("", "att1"));
        qnsb.add(new QName("", "aditionalAtt"));
        qnsb.add(new QName(XSI_URI, "type"));
        qnsb.invert();

        //print("12a    selectChildren in the any bucket for typeExtendedC: " , xos[0].selectAttributes(qnsb.toQNameSet()));
        Assert.assertTrue("12a    selectChildren in the any bucket for typeExtendedC: ",
            verifyResult(xos[0].selectAttributes(qnsb.toQNameSet()),
            new String[]{"<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\">attribute in any bucket</xml-fragment>"}));
        //print("12b    selectChildren in the any bucket for typeExtendedC: " , xos[0].selectAttributes(TypeExtendedC.type.qnameSetForWildcardAttributes()));
        Assert.assertTrue("12b    selectChildren in the any bucket for typeExtendedC: ",
            verifyResult(xos[0].selectAttributes(TypeExtendedC.type.qnameSetForWildcardAttributes()),
            new String[]{"<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\">typeExtendedC</xml-fragment>",
            "<xml-fragment xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"uri_other_namespace\">attribute in any bucket</xml-fragment>"}));
    }

    private static void print(String msg, XmlObject[] xos)
    {
        System.out.println(msg + "   " + xos.length);
        for (int i=0; i<xos.length; i++)
        {
            System.out.println("      " + i + " :" + xos[i]);
        }
    }

    private static void print(String msg, XmlObject xo)
    {
        System.out.println(msg + "   " + xo);
    }

    private static void printErrors(Collection errors)
    {
        for (Iterator i = errors.iterator(); i.hasNext();)
        {
            System.out.println("ERROR: " + i.next());
        }
    }

    private static boolean verifyResult(XmlObject[] xos, String[] expected)
    {
        if (xos==null && expected==null)
            return true;

        if (xos==null || expected==null)
            return false;

        if (xos.length != expected.length)
            return false;

        for (int i = 0; i < xos.length; i++)
        {
            XmlObject xo = xos[i];
            if (!equalsIgnoreNewLine(xo.toString(),expected[i]))
            {
                System.out.println("ERROR:\n    Actual:\n" + xo.toString() + "\n   Expected:\n" + expected[i]);
                return false;
            }
        }
        return true;
    }

    private static boolean verifyResult(XmlObject xo, String expected)
    {
        if (xo==null && expected==null)
            return true;

        if (xo==null || expected==null)
            return false;

        if (!xo.toString().equals(expected))
        {
            System.out.println("ERROR:\n    Actual:\n" + xo.toString() + "\n   Expected:\n" + expected);
            return false;
        }
        else
            return true;
    }

    private static boolean equalsIgnoreNewLine(String s1, String s2)
    {
        if (s1==null && s2==null)
            return true;

        if (s1==null || s2==null)
            return false;

        int i1 = 0, i2 = 0;
        while (i1<s1.length() || i2<s2.length())
        {
            if (s1.charAt(i1)=='\n' || s1.charAt(i1)=='\r')
            {
                i1++;
                continue;
            }

            if (s2.charAt(i2)=='\n' || s2.charAt(i2)=='\r')
            {
                i2++;
                continue;
            }

            if (s1.charAt(i1)!=s2.charAt(i2))
                return false;

            i1++;
            i2++;
        }

        if ( (i1 == s1.length()) && (i2 == s2.length()))
            return true;
        else
            return false;
    }
}
