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

package scomp.namespace.detailed;

import scomp.common.BaseCase;
import xbean.scomp.namespace.attributeFormDefault.AttributeFormDefaultEltDocument;
import xbean.scomp.namespace.attributeFormDefault.ElementT;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 * 
 */
public class AttrFormDefault extends BaseCase {
    public void testValid() throws Throwable {
        AttributeFormDefaultEltDocument doc =
                AttributeFormDefaultEltDocument.Factory.parse("<ns:AttributeFormDefaultElt " +
                "xmlns:ns=\"http://xbean/scomp/namespace/AttributeFormDefault\"" +
                " ns:localAttr=\"foobar\"/>");

        try {
            doc.validate(validateOptions);
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

     public void testInvalid() throws Throwable {
        AttributeFormDefaultEltDocument doc =
                AttributeFormDefaultEltDocument.Factory.newInstance();
         ElementT elt=doc.addNewAttributeFormDefaultElt();
         XmlAnySimpleType val=XmlAnySimpleType.Factory.newInstance();
         val.setStringValue("345");
         elt.setLocalAttr(val);
        try {
            assertTrue( doc.validate(validateOptions) );
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

         doc =
                        AttributeFormDefaultEltDocument.Factory.parse("<ns:AttributeFormDefaultElt " +
                        "xmlns:ns=\"http://xbean/scomp/namespace/AttributeFormDefault\"" +
                        " localAttr=\"foobar\"/>");
           assertTrue( ! doc.validate(validateOptions));
          showErrors();
         String[] errExpected = new String[]{
             XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NO_WILDCARD             
         };
              assertTrue(compareErrorCodes(errExpected));

    }
}
