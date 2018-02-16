
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
import xbean.scomp.namespace.elementFormDefault.ElementFormDefaultEltDocument;
import xbean.scomp.namespace.elementFormDefault.ElementT;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 */
public class ElementFormDefault extends BaseCase {

    public void testValid() throws Throwable {
        ElementFormDefaultEltDocument doc =
                ElementFormDefaultEltDocument.Factory.parse("<ns:ElementFormDefaultElt " +
                "xmlns:ns=\"http://xbean/scomp/namespace/ElementFormDefault\">" +
                "<ns:childElt>foobar</ns:childElt>" +
                " </ns:ElementFormDefaultElt>");

        try {
            doc.validate(validateOptions);
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    public void testInvalid() throws Throwable {
        ElementFormDefaultEltDocument doc =
                ElementFormDefaultEltDocument.Factory.parse("<ns:ElementFormDefaultElt " +
                "xmlns:ns=\"http://xbean/scomp/namespace/ElementFormDefault\">" +
                "<childElt>foobar</childElt>" +
                " </ns:ElementFormDefaultElt>");

        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_ELEMENT
        };
             assertTrue(compareErrorCodes(errExpected));
                
    }
}
