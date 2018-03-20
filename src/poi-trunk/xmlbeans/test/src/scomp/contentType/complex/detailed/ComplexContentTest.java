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

package scomp.contentType.complex.detailed;

import scomp.common.BaseCase;
import org.apache.xmlbeans.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;

import junit.framework.Assert;

/**
 *
 *
 *
 */
public class ComplexContentTest extends BaseCase {
    //TODO: this should be a detailed
    //  test on a complex elt w/ annotations and other such
    public void testRun() {
        fail("Implement me");
    }

    // complex types with simple content whose content is declared via an inline <simpleType>
    // Issue fixed with Svn revision 165352
    public void testSimpleContentDerivation() {
        String sInputXsd = "<?xml version=\"1.0\"?>\n" +
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "    <xs:complexType name=\"myType\">\n" +
                "        <xs:simpleContent>\n" +
                "            <xs:extension base=\"xs:string\"/>\n" +
                "        </xs:simpleContent>\n" +
                "    </xs:complexType>\n" +
                "    <xs:complexType name=\"fooType\">\n" +
                "        <xs:simpleContent>\n" +
                "            <xs:restriction base=\"myType\">\n" +
                "                <xs:simpleType>\n" +
                "                    <xs:restriction base=\"xs:string\"/>\n" +
                "                </xs:simpleType>\n" +
                "            </xs:restriction>\n" +
                "        </xs:simpleContent>\n" +
                "    </xs:complexType>\n" +
                "    <xs:element name=\"root\" type=\"fooType\"/>\n" +
                "</xs:schema>";

        XmlOptions options = new XmlOptions();
        List errors = new ArrayList();
        options.setErrorListener(errors);

        try {
            XmlObject xobj = XmlObject.Factory.parse(sInputXsd);
            assertTrue("Compiled XmlObject Validation Failed!",xobj.validate());
        }
        catch (XmlException xme) {
            xme.printStackTrace();
            Assert.fail("XmlException thrown when compiling schema");
        }

        // check for errors
        for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
            System.out.println("Xsd Compilation Errors : " + iterator.next());
        }
        if (!errors.isEmpty()) {
            Assert.fail("Errors found when compiling schema");
        }
    }
}
