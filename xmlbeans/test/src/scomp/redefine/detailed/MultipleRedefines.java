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
package scomp.redefine.detailed;

import scomp.common.BaseCase;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.*;
import junit.framework.Assert;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class MultipleRedefines extends BaseCase
{
    private static final String[] MULTIPLE_SCHEMAS = {
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
        "    <xs:complexType name=\"T\">" +
        "        <xs:sequence>" +
        "            <xs:element name =\"A\" type=\"xs:string\"/>" +
        "        </xs:sequence>" +
        "    </xs:complexType>" +
        "</xs:schema>",
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
        "    <xs:redefine schemaLocation=\"A.xsd\">" +
        "        <xs:complexType name=\"T\">" +
        "            <xs:complexContent>" +
        "                <xs:extension base=\"T\">" +
        "                    <xs:sequence>" +
        "                        <xs:element name=\"B\" type=\"xs:string\"/>" +
        "                    </xs:sequence>" +
        "                </xs:extension>" +
        "            </xs:complexContent>" +
        "        </xs:complexType>" +
        "    </xs:redefine>" +
        " </xs:schema>",
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
        "    <xs:complexType name=\"S\">" +
        "        <xs:sequence>" +
        "            <xs:element name=\"C\" type=\"xs:string\"/>" +
        "        </xs:sequence>" +
        "    </xs:complexType>" +
        "</xs:schema>",
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
        "    <xs:redefine schemaLocation=\"B.xsd\">" +
        "        <xs:complexType name=\"T\">" +
        "            <xs:complexContent>" +
        "                <xs:extension base=\"T\">" +
        "                    <xs:sequence>" +
        "                        <xs:element name=\"D\" type=\"xs:string\"/>" +
        "                    </xs:sequence>" +
        "                </xs:extension>" +
        "            </xs:complexContent>" +
        "        </xs:complexType>" +
        "    </xs:redefine>" +
        "    <xs:redefine schemaLocation=\"C.xsd\">" +
        "        <xs:complexType name=\"S\">" +
        "            <xs:complexContent>" +
        "                <xs:extension base=\"S\">" +
        "                    <xs:sequence>" +
        "                        <xs:element name=\"D\" type=\"xs:string\"/>" +
        "                    </xs:sequence>" +
        "                </xs:extension>" +
        "            </xs:complexContent>" +
        "        </xs:complexType>" +
        "    </xs:redefine>" +
        "</xs:schema>",
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
        "    <xs:redefine schemaLocation=\"D.xsd\">" +
        "        <xs:complexType name=\"S\">" +
        "            <xs:complexContent>" +
        "                <xs:extension base=\"S\">" +
        "                    <xs:sequence>" +
        "                        <xs:element name=\"E\" type=\"xs:string\"/>" +
        "                    </xs:sequence>" +
        "                </xs:extension>" +
        "            </xs:complexContent>" +
        "        </xs:complexType>" +
        "        <xs:complexType name=\"T\">" +
        "            <xs:complexContent>" +
        "                <xs:extension base=\"T\">" +
        "                    <xs:sequence>" +
        "                        <xs:element name=\"E\" type=\"xs:string\"/>" +
        "                    </xs:sequence>" +
        "                </xs:extension>" +
        "            </xs:complexContent>" +
        "        </xs:complexType>" +
        "    </xs:redefine>" +
        "</xs:schema>"};
    private static final String[] MULTIPLE_SCHEMAS_NAME = {
        "A.xsd", "B.xsd", "C.xsd", "D.xsd", "E.xsd"};

    private static final String[] CIRCULAR_SCHEMAS = {
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
        "    <xs:redefine schemaLocation=\"D.xsd\">" +
        "        <xs:complexType name=\"T\">" +
        "            <xs:complexContent>" +
        "                <xs:extension base=\"T\">" +
        "                    <xs:sequence>" +
        "                        <xs:element name=\"A\" type=\"xs:string\"/>" +
        "                    </xs:sequence>" +
        "                </xs:extension>" +
        "            </xs:complexContent>" +
        "        </xs:complexType>" +
        "    </xs:redefine>" +
        "    <xs:complexType name=\"T\">" +
        "        <xs:sequence>" +
        "            <xs:element name=\"A\" type=\"xs:string\"/>" +
        "        </xs:sequence>" +
        "    </xs:complexType>" +
        "</xs:schema>",
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
        "    <xs:redefine schemaLocation=\"A.xsd\">" +
        "        <xs:complexType name=\"T\">" +
        "            <xs:complexContent>" +
        "                <xs:extension base=\"T\">" +
        "                    <xs:sequence>" +
        "                        <xs:element name=\"B\" type=\"xs:string\"/>" +
        "                    </xs:sequence>" +
        "                </xs:extension>" +
        "            </xs:complexContent>" +
        "        </xs:complexType>" +
        "    </xs:redefine>" +
        "</xs:schema>",
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
        "    <xs:redefine schemaLocation=\"B.xsd\">" +
        "        <xs:complexType name=\"T\">" +
        "            <xs:complexContent>" +
        "                <xs:extension base=\"T\">" +
        "                    <xs:sequence>" +
        "                        <xs:element name=\"C\" type=\"xs:string\"/>" +
        "                    </xs:sequence>" +
        "                </xs:extension>" +
        "            </xs:complexContent>" +
        "        </xs:complexType>" +
        "    </xs:redefine>" +
        "</xs:schema>",
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
        "    <xs:redefine schemaLocation=\"C.xsd\">" +
        "        <xs:complexType name=\"T\">" +
        "            <xs:complexContent>" +
        "                <xs:extension base=\"T\">" +
        "                    <xs:sequence>" +
        "                        <xs:element name=\"D\" type=\"xs:string\"/>" +
        "                    </xs:sequence>" +
        "                </xs:extension>" +
        "            </xs:complexContent>" +
        "        </xs:complexType>" +
        "    </xs:redefine>" +
        "</xs:schema>"};

    private static final String[] CIRCULAR_SCHEMAS_NAME = {
        "A.xsd", "B.xsd", "C.xsd", "D.xsd"};

    public void testMultipleRedefines() throws Exception
    {
        int N = MULTIPLE_SCHEMAS.length;
        SchemaDocument[] sdocs = new SchemaDocument[N];
        for (int i = 0; i < N; i++)
        {
            sdocs[i] = SchemaDocument.Factory.parse(MULTIPLE_SCHEMAS[i]);
            sdocs[i].documentProperties().setSourceName(MULTIPLE_SCHEMAS_NAME[i]);
        }

        SchemaTypeSystem ts = XmlBeans.compileXsd(sdocs,
            XmlBeans.getBuiltinTypeSystem(), validateOptions);
        Assert.assertNotNull(ts);

        SchemaType t = ts.findType(new QName("", "T"));
        Assert.assertNotNull(t);

        SchemaParticle p = t.getContentModel();
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getParticleType(), SchemaParticle.SEQUENCE);
        SchemaParticle[] elts = p.getParticleChildren();
        Assert.assertEquals(elts.length, 4);
        for (int i = 0; i < elts.length; i++)
            Assert.assertEquals(elts[i].getParticleType(), SchemaParticle.ELEMENT);

        Assert.assertEquals("A", elts[0].getName().getLocalPart());
        Assert.assertEquals("B", elts[1].getName().getLocalPart());
        Assert.assertEquals("D", elts[2].getName().getLocalPart());
        Assert.assertEquals("E", elts[3].getName().getLocalPart());
    }

    public void testCircularRedefines() throws Exception
    {
        int N =CIRCULAR_SCHEMAS.length;
        SchemaDocument[] sdocs = new SchemaDocument[N];
        for (int i = 0; i < N; i++)
        {
            sdocs[i] = SchemaDocument.Factory.parse(CIRCULAR_SCHEMAS[i]);
            sdocs[i].documentProperties().setSourceName(CIRCULAR_SCHEMAS_NAME[i]);
        }

        setUp();
        boolean caught = false;
        try
        {
            SchemaTypeSystem ts = XmlBeans.compileXsd(sdocs,
                XmlBeans.getBuiltinTypeSystem(), validateOptions);
        }
        catch (XmlException e)
        {
            caught = true;
            Iterator it = errorList.iterator();
            XmlError err = (XmlError) it.next();
            Assert.assertFalse(it.hasNext());
            String message = err.getMessage();
            // TODO check an error code instead
            Assert.assertTrue(message.toLowerCase().indexOf("circular") >= 0);
        }
        clearErrors();
        Assert.assertTrue("Compilation should fail", caught);
    }
}
