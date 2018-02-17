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
package scomp.derivation.extension.detailed;

import xbean.scomp.derivation.emtpy.ExtendedEmptyEltDocument;
import xbean.scomp.derivation.emtpy.ExtendedEmptyT;
import scomp.common.BaseCase;

/**
 * @owner: ykadiysk
 * Date: Jul 21, 2004
 * Time: 10:18:48 AM
 */
public class EmptyContentExtension extends BaseCase{
    public void testEmptyElementContent() throws Throwable{
       ExtendedEmptyEltDocument doc=ExtendedEmptyEltDocument.Factory.newInstance();
       ExtendedEmptyT elt=doc.addNewExtendedEmptyElt();
       assertTrue( !elt.isSetExtendedAttr() );
        elt.setEmptyAttr("baseAttr");
       
        ExtendedEmptyT.ExtendedChild child=elt.addNewExtendedChild();
        child.setSubCh1("Child 1");
        child.setSubCh2(1.3f);

        try{
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
           throw t;
        }
    }
}
