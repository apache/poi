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
package scomp.redefine.detailed;

import xbean.scomp.redefine.simpleRedefined.NewSizeEltDocument;
import xbean.scomp.redefine.simpleRedefined.OldColorEltDocument;
import xbean.scomp.redefine.simpleRedefined.OldSizeEltDocument;
import scomp.common.BaseCase;

/**
 */
public class SimpleRedefine extends BaseCase
 {

     /**
     * test that fields from the old type def are not
     * visible anymore: only valid range for sizeT should be 3-20
     */
    public void testCodeGeneration() throws Throwable{
       NewSizeEltDocument doc=NewSizeEltDocument.Factory.newInstance();
       OldColorEltDocument doc1=OldColorEltDocument.Factory.newInstance();
       OldSizeEltDocument doc2=OldSizeEltDocument.Factory.newInstance();

         doc.setNewSizeElt(3);

       try{
           assertTrue(doc.validate(validateOptions));
       }catch(Throwable t){
           showErrors();
           throw t;
       }
       doc.setNewSizeElt(21);
          assertTrue( !doc.validate(validateOptions));
         showErrors();
         clearErrors();

         doc2.setOldSizeElt(21);
            assertTrue( !doc.validate(validateOptions));
         showErrors();
         clearErrors();

         doc1.setOldColorElt("white");

        try{
           assertTrue(doc1.validate(validateOptions));
       }catch(Throwable t){
           showErrors();
           throw t;
       }
    }
}
