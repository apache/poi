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

package scomp.substGroup.detailed;

import scomp.common.BaseCase;
import xbean.scomp.substGroup.userReported.RootDocument;
import xbean.scomp.substGroup.userReported.T;
import xbean.scomp.substGroup.userReported.ADocument;
import xbean.scomp.substGroup.userReported.BDocument;
import org.apache.xmlbeans.XmlObject;

import javax.xml.namespace.QName;

/**
 */
public class UserReportedTest extends BaseCase{
       String input="<Root xmlns=\"http://xbean/scomp/substGroup/UserReported\">" +
                "   <a/>" +
                "   <a/>" +
                "   <b/>" +
                "   <a/>" +
                "   <b/>" +
                "</Root>";
    public  void testGoal()throws Throwable{

        RootDocument doc=RootDocument.Factory.parse(input);
        try{
            assertTrue( doc.validate(validateOptions ));
        } catch(Throwable t){
            showErrors();
            throw t;
        }
    }

    public void testBuild()throws Throwable{
        T[] arr = new T[5];

        arr[0] = ADocument.Factory.newInstance().addNewA();
        arr[1] = ADocument.Factory.newInstance().addNewA();
        arr[2] = BDocument.Factory.newInstance().addNewB();

        arr[3] = ADocument.Factory.newInstance().addNewA();
        arr[4] = BDocument.Factory.newInstance().addNewB();


        RootDocument mdoc = RootDocument.Factory.newInstance();
        RootDocument.Root m = mdoc.addNewRoot();
        m.setAArray(arr);
        T[] arr1=m.getAArray();
        arr1[2].newCursor().setName(new QName("http://xbean/scomp/substGroup/UserReported","b"));
        arr1[4].newCursor().setName(new QName("http://xbean/scomp/substGroup/UserReported","b"));

       /* if (! mdoc.toString().equals(input))
           throw new Exception(mdoc.toString());
        */
        //assertEquals(input, mdoc.toString() );
         try{
            assertTrue( mdoc.validate(validateOptions ));
        } catch(Throwable t){
            showErrors();
            throw t;
        }

    }
}
