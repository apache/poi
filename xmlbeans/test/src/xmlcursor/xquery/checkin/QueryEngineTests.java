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
package xmlcursor.xquery.checkin;

import junit.framework.TestCase;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlInteger;

public class QueryEngineTests extends TestCase
{
    // Execute repeated queries to test picking up of the query engine from classpath
    // This test is following rev 292446 
    public void testQueryEngineSelection()throws Exception{
            XmlObject o = XmlObject.Factory.parse("<foo><a><b/></a></foo>");
            XmlObject[] res = o.execQuery("(//a/b)");
            res= o.execQuery("(//a/b)");
            String expectedRes = "<b/>";
            assertEquals(expectedRes,res[0].xmlText());

        }

}
