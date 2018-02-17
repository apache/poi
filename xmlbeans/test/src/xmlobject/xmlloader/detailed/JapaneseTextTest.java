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
package xmlobject.xmlloader.detailed;

import junit.framework.TestCase;
import org.apache.xmlbeans.XmlObject;
import tools.util.JarUtil;

public class JapaneseTextTest extends TestCase {

    public void testEucJp() throws Exception{
        loadFile("pr-xml-euc-jp.xml");
    }
    public void testIso2022Jp()throws Exception{
           loadFile("pr-xml-iso-2022-jp.xml");
    }
     public void testLittleEndian()throws Exception{
            loadFile("pr-xml-little-endian.xml");
     }
     public void testShift_jis()throws Exception{
          loadFile("pr-xml-shift_jis.xml");
     }
     public void testUtf8()throws Exception{
          loadFile("pr-xml-utf-8.xml");
     }
     public void testUtf16()throws Exception{
          loadFile("pr-xml-utf-16.xml");
     }

    public void testWeeklyEucJp()throws Exception{
        loadFile("weekly-euc-jp.xml");
    }
    public void testWeeklyIso2022Jp()throws Exception{
        loadFile("weekly-iso-2022-jp.xml");
    }
     public void testWeeklyLittleEndian()throws Exception{
         loadFile("weekly-little-endian.xml");
     }
     public void testWeeklyShift_jis()throws Exception{
         loadFile("weekly-shift_jis.xml");
     }
     public void testWeeklyUtf8()throws Exception{
         loadFile("weekly-utf-8.xml");
     }
     public void testWeeklyUtf16()throws Exception{
         loadFile("weekly-utf-16.xml");
     }

     public void testPrefixLocalName()throws Exception{
         loadFile("prefix_test.xml");
     }


    public void loadFile(String file) throws Exception{

        XmlObject.Factory.parse(JarUtil.
                getResourceFromJarasStream("xbean/xmlobject/japanese/"+file));

    }



}
