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

import xbean.scomp.derivation.emtpy.RestrictedEmptyEltDocument;
import xbean.scomp.derivation.emtpy.RestrictedEmptyT;
import scomp.common.BaseCase;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 */
public class EmptyContentRestriction extends BaseCase {

    public void testRestriction() throws Throwable {
        RestrictedEmptyEltDocument doc = RestrictedEmptyEltDocument.Factory.newInstance();

        RestrictedEmptyT elt = doc.addNewRestrictedEmptyElt();
        elt.setEmptyAttr("foobar");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ATTR_LOCALLY_VALID$FIXED
        };
                     assertTrue(compareErrorCodes(errExpected));

        elt.setEmptyAttr("myval");
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        XmlCursor cur = elt.newCursor();
        cur.toFirstContentToken();
        cur.toNextToken();
        cur.beginElement("foobar");
        assertEquals("<xml-fragment>" +
                "<emt:RestrictedEmptyElt emptyAttr=\"myval\" " +
                "xmlns:emt=\"http://xbean/scomp/derivation/Emtpy\"/>" +
                "<foobar/></xml-fragment>", doc.xmlText());

        clearErrors();
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED
        };
                     assertTrue(compareErrorCodes(errExpected));


    }
}
