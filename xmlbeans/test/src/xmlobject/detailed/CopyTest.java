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
package xmlobject.detailed;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.xmlbeans.XmlObject;

public class CopyTest extends TestCase
{
    public CopyTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(CopyTest.class);
    }

    // Test for a Document object being copied as DocFrag if the type of the
    // source of the copy is not a document type (as is the case with NO_TYPE).
    public void testXobjTypeOnDomNodeCopy() throws Exception
    {
        XmlObject o = XmlObject.Factory.parse("<foo><a/></foo>");
        String xobjOrgClassName = "org.apache.xmlbeans.impl.store.Xobj$DocumentXobj";
        assertEquals("Invalid Type!", o.getDomNode().getClass().getName(),xobjOrgClassName);

        XmlObject o2 = o.copy();
        String xobjCopyClassName = o2.getDomNode().getClass().getName();
        System.out.println ( "DocXobj:"+ xobjCopyClassName);

        // check for the expected type
        assertEquals("Invalid Type!", "org.apache.xmlbeans.impl.store.Xobj$DocumentXobj",xobjOrgClassName);
        assertEquals("Invalid Type!", "org.apache.xmlbeans.impl.store.Xobj$DocumentXobj",xobjCopyClassName);
    }

    // Test the same for a simple untyped XmlObject copy
    public void testXobjTypeOnCopy() throws Exception
    {
        String untypedXobjClass = "org.apache.xmlbeans.impl.values.XmlAnyTypeImpl";

        XmlObject o = XmlObject.Factory.parse("<foo><a/></foo>");
        assertEquals("Invalid Type!",untypedXobjClass,o.getClass().getName());

        XmlObject o2 = o.copy();
        String xobjClass = o2.getClass().getName();
        // type should be unchanged after the copy
        assertEquals("Invalid Type!",untypedXobjClass,o2.getClass().getName());
    }


}
