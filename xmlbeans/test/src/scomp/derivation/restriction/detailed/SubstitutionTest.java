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

import xbean.scomp.contentType.modelGroup.ChoiceEltDocument;
import xbean.scomp.contentType.simpleType.PantSizeEltDocument;
import scomp.common.BaseCase;

/**
 *
 */
public class SubstitutionTest extends BaseCase
 {
    public void testSubstitution() throws Throwable{
         ChoiceEltDocument doc=
                ChoiceEltDocument.Factory.parse(
                        "<foo:ChoiceElt " +
                 "xmlns:foo=\"http://xbean/scomp/contentType/ModelGroup\"" +
                 " xmlns:sub=\"http://xbean/scomp/derivation/GroupRestriction\""+
                 " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                 " xsi:type=\"sub:restrictedChoiceT\">" +
                 "<child3>50</child3>" +
                 "</foo:ChoiceElt>"
                );
                try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

     public void testSimpleTypeSubstitution() throws Throwable{
                PantSizeEltDocument doc=
                PantSizeEltDocument.Factory.parse(
                        "<foo:PantSizeElt " +
                 "xmlns:foo=\"http://xbean/scomp/contentType/SimpleType\"" +
                 " xmlns:sub=\"http://xbean/scomp/derivation/SimpleTypeRestriction\""+
                 " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                 " xsi:type=\"sub:SmallPantSize\">"+
                  "8" +
                 "</foo:PantSizeElt>"
                );
                try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
     }
}
