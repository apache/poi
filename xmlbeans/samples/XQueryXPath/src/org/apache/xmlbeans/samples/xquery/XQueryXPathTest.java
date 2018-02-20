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
package org.apache.xmlbeans.samples.xquery;

/**
 * A class with which to test the XQueryXPath sample.
 */
public class XQueryXPathTest
{
    /**
     * Tests the XQueryXPath sample.
     */
    public static void main(String[] args)
            throws org.apache.xmlbeans.XmlException, java.io.IOException
    {
        XQueryXPath sample = new XQueryXPath();
        boolean queriesSuccessful = sample.executeQueries(args);
        assert queriesSuccessful;
    }
}
