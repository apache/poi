/*
 *   Copyright 2009 The Apache Software Foundation
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
package misc.detailed;

import misc.common.JiraTestBase;
import com.easypo.*;

public class JiraRegression251_300Test extends JiraTestBase
{
    public JiraRegression251_300Test(String name)
    {
        super(name);
    }

    /*
    * [XMLBEANS-260]: SchemaType#isSkippedAnonymousType() throws an NPE
    * if _outerSchemaTypeRef is not set
    */
    public void test_jira_xmlbeans260()
    {
        // construct an instance of a non-anonymous type
        XmlShipperBean xbean = XmlShipperBean.Factory.newInstance();
        // the following call should not throw an NPE;
        // it should return false instead
        boolean isSkipped = xbean.schemaType().isSkippedAnonymousType();
        assertFalse(isSkipped);
    }
}
