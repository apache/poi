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
package scomp.contentType.simple.detailed;

import scomp.common.BaseCase;
import xbean.scomp.contentType.simpleType.PantSizeEltDocument;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 *
 *
 */
public class SimpleType extends BaseCase {
    public void testPattern() throws Throwable {
        PantSizeEltDocument size = PantSizeEltDocument.Factory.newInstance();
        size.setPantSizeElt(16);
        //size> max inclusive
        assertTrue(!size.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID
        };
        assertTrue(compareErrorCodes(errExpected));

        size.setPantSizeElt(-1);
        showErrors();
        clearErrors();
        assertTrue(!size.validate(validateOptions));
        errExpected = new String[]{
            XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID
        };
        assertTrue(compareErrorCodes(errExpected));


        size.setPantSizeElt(14);
        try {
            assertTrue(size.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }

    }
}
