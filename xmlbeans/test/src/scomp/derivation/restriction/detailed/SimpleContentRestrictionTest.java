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

import xbean.scomp.derivation.simpleExtension.SimpleRestrictionEltDocument;
import xbean.scomp.derivation.simpleExtension.SimpleRestrictionT;
import scomp.common.BaseCase;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 */
public class SimpleContentRestrictionTest extends BaseCase {
    public void testLegalValues() throws Throwable {
        SimpleRestrictionEltDocument doc = SimpleRestrictionEltDocument.Factory.newInstance();
        SimpleRestrictionT elt = doc.addNewSimpleRestrictionElt();
        elt.setIntValue(3);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

    public void testIllegalValues() throws Throwable {
        SimpleRestrictionEltDocument doc = SimpleRestrictionEltDocument.Factory.newInstance();
        SimpleRestrictionT elt = doc.addNewSimpleRestrictionElt();
        elt.setIntValue(5);

        assertTrue(!doc.validate(validateOptions));

        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID
        };
        assertTrue(compareErrorCodes(errExpected));


    }
}
