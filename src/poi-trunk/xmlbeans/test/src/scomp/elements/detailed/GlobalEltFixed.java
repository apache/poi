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

package scomp.elements.detailed;

import scomp.common.BaseCase;
import xbean.scomp.element.globalEltFixed.GlobalEltFixedIntDocument;
import xbean.scomp.element.globalEltFixed.GlobalEltFixedStrDocument;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 *
 */
public class GlobalEltFixed extends BaseCase {

    public void testValidPresent() throws Exception {
        GlobalEltFixedIntDocument testEltInt = GlobalEltFixedIntDocument.Factory.parse("<GlobalEltFixedInt " +
                "xmlns=\"http://xbean/scomp/element/GlobalEltFixed\"" +
                "> +01 </GlobalEltFixedInt>");
        GlobalEltFixedStrDocument testEltStr = GlobalEltFixedStrDocument.Factory.parse("<GlobalEltFixedStr " +
                "xmlns=\"http://xbean/scomp/element/GlobalEltFixed\"" +
                ">XBean</GlobalEltFixedStr>");
        assertTrue(testEltInt.validate());
        assertTrue(testEltStr.validate());
    }

    //document should be valid even if the values
    // are missing
    public void testValidMissing() throws Exception {
        GlobalEltFixedIntDocument testEltInt = GlobalEltFixedIntDocument
                .Factory.parse("<GlobalEltFixedInt " +
                "xmlns=\"http://xbean/scomp/element/GlobalEltFixed\"" +
                "/>");
        assertTrue(testEltInt.validate());
    }

    public void testIntTypeInvalid() throws Exception {
        GlobalEltFixedIntDocument testEltInt = GlobalEltFixedIntDocument
                .Factory.parse("<GlobalEltFixedInt " +
                "xmlns=\"http://xbean/scomp/element/GlobalEltFixed\"" +
                "> foobar </GlobalEltFixedInt>");
        assertTrue(!testEltInt.validate(validateOptions));
        String[] errExpected = new String[]{
            XmlErrorCodes.DECIMAL
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testIntValueInvalid() throws Exception {
        GlobalEltFixedIntDocument testEltInt = GlobalEltFixedIntDocument.Factory.parse("<GlobalEltFixedInt " +
                "xmlns=\"http://xbean/scomp/element/GlobalEltFixed\"" +
                "> -1 </GlobalEltFixedInt>");
        assertTrue(!testEltInt.validate(validateOptions));
        assertEquals(1, errorList.size());
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_LOCALLY_VALID$FIXED_VALID_SIMPLE_TYPE};
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testStrValueInvalid() throws Exception {
        GlobalEltFixedStrDocument testEltStr = GlobalEltFixedStrDocument.Factory.parse("<GlobalEltFixedStr " +
                "xmlns=\"http://xbean/scomp/element/GlobalEltFixed\"" +
                "> XBean </GlobalEltFixedStr>");
        assertTrue(!testEltStr.validate(validateOptions));
        assertEquals(1, errorList.size());
        String[] errExpected = new String[]{
             XmlErrorCodes.ELEM_LOCALLY_VALID$FIXED_VALID_SIMPLE_TYPE};
        assertTrue(compareErrorCodes(errExpected));

    }

   
}
