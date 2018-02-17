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
package scomp.derivation.detailed;

import scomp.common.BaseCase;
import xbean.scomp.derivation.xabstract.EltAbstractDocument;
import xbean.scomp.derivation.xabstract.AbstractT;
import xbean.scomp.derivation.xabstract.EltConcreteDocument;

import java.math.BigInteger;

/**
 *
 *
 *
 */
public class AbstractTest extends BaseCase{

   /**
    * This is an abstract element...no instance should ever be valid
    * @throws Throwable
    */

    public void testElementAbstract() throws Throwable{
        EltAbstractDocument doc=EltAbstractDocument.Factory.newInstance();
        AbstractT elt=doc.addNewEltAbstract();
        elt.setAge(new BigInteger("15"));
        elt.setName("Ben");
        assertTrue(elt!=null);
        assertTrue (! elt.validate() );
    }

     public void testElementAbstractParse() throws Throwable{
        EltAbstractDocument doc=EltAbstractDocument.Factory.parse(
                  "<foo:EltAbstract " +
                   "xmlns:foo=\"http://xbean/scomp/derivation/Abstract\">"+
                   " <name>Bob</name><age>25</age><gender>G</gender>" +
                           "</foo:EltAbstract>");

        assertTrue (! doc.validate(validateOptions) );
        showErrors();
    }

       public void testElementConcrete() throws Throwable{
           EltConcreteDocument doc=EltConcreteDocument.Factory.parse(
                   "<foo:EltConcrete " +
                   "xmlns:foo=\"http://xbean/scomp/derivation/Abstract\">"+
                   " <name>Bob</name><age>25</age><gender>G</gender>" +
                           "</foo:EltConcrete>");
           assertTrue (! doc.validate(validateOptions));
           showErrors();
       }

}
