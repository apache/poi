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

package scomp.derivation.restriction.detailed;

import scomp.common.BaseCase;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 */
public class EnumTest extends BaseCase
 {
    /*
    * Refer CR199528, CR191369.
    * This fails in V1 and the case is added here to ensure compliance in v2.
    */
    public void testEnumRestrictionScomp() throws Exception {
        String xsdAsString = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "\n" +
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "\n" +
                " <xs:element name=\"ActionTypeElem\" type=\"ACTION_TYPE\" />\n" +
                " <xs:element name=\"VIPActionTypeElem\" type=\"VIP_ACTION_TYPE\" />\n" +
                "\n" +
                " <xs:simpleType name=\"ACTION_TYPE\">\n" +
                "  <xs:restriction base=\"xs:string\">\n" +
                "   <xs:enumeration value=\"PROVIDE\"/>\n" +
                "   <xs:enumeration value=\"MODIFY\"/>\n" +
                "   <xs:enumeration value=\"CEASE\"/>\n" +
                "  </xs:restriction>\n" +
                " </xs:simpleType>\n" +
                "\n" +
                " <xs:simpleType name=\"VIP_ACTION_TYPE\">\n" +
                "  <xs:restriction base=\"ACTION_TYPE\">\n" +
                "   <xs:enumeration value=\"QUERY\"/>\n" +
                "  </xs:restriction>\n" +
                " </xs:simpleType>\n" +
                "\n" +
                "</xs:schema>";

        // load schema file as SchemaDocument XmlObject
        SchemaDocument sd = SchemaDocument.Factory.parse(xsdAsString.toString());

        // compile loaded XmlObject
        try
        {
        XmlOptions options = new XmlOptions();
        List errors = new ArrayList();
        options.setErrorListener(errors);
        SchemaTypeSystem sts = XmlBeans.compileXsd((XmlObject[]) Collections.singletonList(sd).toArray(new XmlObject[]{}),
                XmlBeans.getContextTypeLoader(),
                options);
        }

        catch(XmlException xme)
        {
            System.out.println(xme.getError().getMessage());
            assertEquals(XmlErrorCodes.DATATYPE_ENUM_RESTRICTION,xme.getError().getErrorCode());
        }
        catch (NullPointerException npe)
        {
            fail("Null Pointer Exception thrown for invalid tyoe restriction attempt for Enum values");
        }

    }
}
