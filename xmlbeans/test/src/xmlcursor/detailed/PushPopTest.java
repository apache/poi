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


package xmlcursor.detailed;


import junit.framework.*;

import org.apache.xmlbeans.XmlObject;

import xmlcursor.common.*;


/**
 *
 *
 */
public class PushPopTest extends BasicCursorTestCase {

      String sDoc="<foo xmlns:edi='http://ecommerce.org/schema'><?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?><!-- the 'price' element's namespace is http://ecommerce.org/schema -->  <edi:price units='Euro' date='12-12-03'>32.18</edi:price> </foo>";

    public PushPopTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(PushPopTest.class);
    }

    public void testPopEmpty(){
	assertEquals(false,m_xc.pop());
    }

    public void testPushNTimes(){
	int nCount=100;
	for (int i=0;i<nCount;i++)
	    m_xc.push();
	boolean result=true;
	for (int i=0;i<nCount;i++)
	    result&=m_xc.pop();
	assertEquals(true,result);
	assertEquals(false,m_xc.pop());
    }

    public void setUp() throws Exception{
	m_xc=XmlObject.Factory.parse(sDoc).newCursor();
    }
}
