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

package scomp.derivation.restriction.facets.detailed;

import scomp.common.BaseCase;
import xbean.scomp.derivation.facets.union.UnionPatternEltDocument;
import xbean.scomp.derivation.facets.union.SmallPatternUnion;
import xbean.scomp.derivation.facets.union.UnionEnumEltDocument;
import xbean.scomp.derivation.facets.union.SmallEnumUnion;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 * Only pattern and enumeration restrictions possible
 * Compile time tests for the rest
 */
public class UnionRestriction extends BaseCase {

    public void testPatternRestriction() throws Throwable {
        UnionPatternEltDocument doc =
                UnionPatternEltDocument.Factory.newInstance();
        doc.setUnionPatternElt("small");
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionPatternElt(new Integer(1));
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        SmallPatternUnion elt = SmallPatternUnion.Factory.newInstance();
        elt.setObjectValue(new Integer(2));
        doc.xsetUnionPatternElt(elt);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionPatternElt(new Integer(-1));
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID
        };
        assertTrue(compareErrorCodes(errExpected));


    }

    public void testEnumRestriction() throws Throwable {
        UnionEnumEltDocument doc = UnionEnumEltDocument.Factory.newInstance();
        doc.setUnionEnumElt("small");
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionEnumElt(new Integer(1));
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        SmallEnumUnion elt = SmallEnumUnion.Factory.newInstance();
        elt.setObjectValue(new Integer(-1));
        doc.xsetUnionEnumElt(elt);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionEnumElt(new Integer(2));
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_ENUM_VALID
        };
        assertTrue(compareErrorCodes(errExpected));


    }
}
