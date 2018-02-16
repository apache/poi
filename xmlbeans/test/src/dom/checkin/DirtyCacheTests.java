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

package dom.checkin;

import junit.framework.TestCase;
import org.apache.xmlbeans.XmlCursor;
import org.w3c.dom.Node;

import xbean.dom.complexTypeTest.ElementT;
import xbean.dom.complexTypeTest.EltTypeDocument;
import xbean.dom.complexTypeTest.MixedT;
import xbean.dom.complexTypeTest.MixedTypeDocument;
import xbean.dom.dumbNS.RootDocument;

import java.math.BigInteger;

public class DirtyCacheTests extends TestCase
{

   public void testDirtyValue()throws Exception{
        EltTypeDocument o = EltTypeDocument.Factory.newInstance();
        ElementT t = o.addNewEltType();
        t.setChild1(new BigInteger("30"));
        Node n = o.getDomNode();
        n = n.getFirstChild();
        n = n.getFirstChild();
        n = n.getFirstChild();//text
        String s = n.getNodeValue();
        System.out.println(n.getNodeName()+" "+ s);
        t.setChild1(new BigInteger("5"));
        s = n.getNodeValue();
        System.out.println(n.getNodeName()+" "+ s);
    }

    public void testDirtyValue1()throws Exception{
        MixedTypeDocument o = MixedTypeDocument.Factory.newInstance();
        MixedT testElt = o.addNewMixedType();
        assertEquals(null, testElt.getChild1());
        assertEquals(null, testElt.xgetChild1());
        testElt.setChild2(new BigInteger("5"));
        testElt.setChild3(new BigInteger("1"));
        testElt.setChild1(new BigInteger("0"));

        XmlCursor cur = testElt.newCursor();
        cur.toFirstContentToken();
        cur.insertChars("Random mixed content");
        Node n = o.getDomNode();
        n = n.getFirstChild();
        n = n.getFirstChild();

        String s = n.getNodeValue();
        System.out.println(n.getNodeName()+" "+ s);
        n=n.getNextSibling();
        n = n.getFirstChild();

        s = n.getNodeValue();
        System.out.println(n.getNodeName()+" "+ s);
    }

     public void testDirtyValue2()throws Exception{
        RootDocument o = RootDocument.Factory.newInstance();
        RootDocument.Root testElt = o.addNewRoot();
        testElt.setB(new BigInteger("5"));
        Node n = o.getDomNode();
        n = n.getFirstChild();
        n = n.getAttributes().getNamedItem("b");

        System.out.println(n.getNodeName()+" "+ n.getNodeValue());
    }
}
