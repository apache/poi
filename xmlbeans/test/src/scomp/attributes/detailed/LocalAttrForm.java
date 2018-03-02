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
package scomp.attributes.detailed;

import scomp.common.BaseCase;
import xbean.scomp.namespace.attributeFormDefault.AttributeUnqualifiedDocument;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 *
 *
 */
public class LocalAttrForm extends BaseCase{

    /**
     * attrFormDefault is "qualified", overwrite locally to "unqualified"
     * @throws Throwable
     */
    public void testRun() throws Throwable{

        String input="<ns:AttributeUnqualified " +
                "xmlns:ns=\"http://xbean/scomp/namespace/AttributeFormDefault\"" +
                " LocalAttribute=\"foobar\"/>";

        AttributeUnqualifiedDocument doc=
               AttributeUnqualifiedDocument.Factory.parse(input);

         try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

        doc.getAttributeUnqualified().getLocalAttribute().
                newCursor()
                .setName(new QName(
                        "http://xbean/scomp/namespace/AttributeFormDefault",
                        "LocalAttribute"));
         assertTrue( !doc.validate(validateOptions) );
        System.out.println(doc.xmlText());
        showErrors();
        String[] errExpected = new String[]
        {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NO_WILDCARD};
            assertTrue(compareErrorCodes(errExpected));
        


    }
}
