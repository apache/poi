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
import xbean.scomp.element.globalEltDefault.GlobalEltDefaultIntDocument;
import xbean.scomp.element.globalEltDefault.GlobalEltDefaultStrDocument;

/**
 *
 *
 */
public class GlobalEltDefault extends BaseCase {
    //empty string is OK considered , so default value is ignored
    public void testStringType()throws Throwable{
    GlobalEltDefaultStrDocument testDoc=GlobalEltDefaultStrDocument
            .Factory.newInstance();
     assertEquals(null,testDoc.getGlobalEltDefaultStr());
/*try{
    assertTrue(testDoc.validate(validateOptions));
}catch(Throwable t){
    showErrors();
    throw t;
} */
        testDoc.setGlobalEltDefaultStr("foo");
        try{
            assertTrue(testDoc.validate(validateOptions));
        }catch(Throwable t){
            showErrors();
            throw t;
        }

        }

    //default value is used
    public void testIntType()throws Throwable{
       GlobalEltDefaultIntDocument testDoc
                =GlobalEltDefaultIntDocument.Factory
               .newInstance();
     assertEquals(0,testDoc.getGlobalEltDefaultInt());
    try{
    assertTrue(testDoc.validate(validateOptions));
}catch(Throwable t){
    showErrors();
    throw t;
}
    }
}
