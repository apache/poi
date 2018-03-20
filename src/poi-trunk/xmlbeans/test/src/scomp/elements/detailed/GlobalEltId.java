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
import xbean.scomp.element.globalEltDefault.IDElementDocument;

/**
 *
 *
 */
public class GlobalEltId extends BaseCase{
    public void testRun()throws Throwable{
        IDElementDocument doc=
                IDElementDocument.Factory.newInstance();
        doc.addNewIDElement().setID("IDAttr");
           try {
            assertTrue( doc.validate(validateOptions)) ;
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }
}
