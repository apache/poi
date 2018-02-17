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

package xmlobject.schematypes.checkin;

import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlAnySimpleType;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class QNameTests extends TestCase
{
    public QNameTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(QNameTests.class); }

    static String[] _args;
    static String _test;

    public void testQName ( )
        throws Exception
    {
        String schema =
            "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n" +
            "" +
            "  <xs:element name='any'>\n" +
            "  </xs:element>\n" +
            "" +
            "</xs:schema>\n" +
            "";

        SchemaTypeLoader stl =
            XmlBeans.loadXsd( new XmlObject[] {
                XmlObject.Factory.parse(schema) } );

        
        //
        // Test the set_XMLName function on XmlQNameImpl
        //

        String ns =
            "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
                "xmlns:xs='http://www.w3.org/2001/XMLSchema'";
        
        XmlObject sourceDocument =
            stl.parse(
                "<any " + ns + " xsi:type='xs:QName' xmlns:xxx='xxx.com'>" +
                    "xxx:abc</any>", null, null );

        XmlCursor sourceCursor = sourceDocument.newCursor();

        sourceCursor.toFirstChild();

        XmlQName sourceQName = (XmlQName) sourceCursor.getObject();

        XmlObject targetDocument =
            stl.parse(
                "<any " + ns + " xsi:type='xs:QName'>" +
                    "</any>", null, null );

        XmlCursor targetCursor = targetDocument.newCursor();

        targetCursor.toFirstChild();

        XmlQName targetQName = (XmlQName) targetCursor.getObject();

        targetQName.set( sourceQName );

        Assert.assertTrue(
            targetQName.getQNameValue().getNamespaceURI().equals( "xxx.com" ) );

        //
        // Test the set_text function on XmlQNameImpl
        //
        
        targetDocument =
            stl.parse(
                "<any " + ns + " xsi:type='xs:QName' xmlns:xxx='xxx.com'>" +
                    "</any>", null, null );

        targetCursor = targetDocument.newCursor();

        targetCursor.toFirstChild();

        targetQName = (XmlQName) targetCursor.getObject();

        try
        {
            targetQName.setStringValue( "zzz:abc" );
            
            Assert.assertTrue( false ); // Must fail
        }
        catch ( Throwable t )
        {
        }
        
        targetQName.setStringValue( "xxx:abc" );
        
        Assert.assertTrue(
            targetQName.getQNameValue().getNamespaceURI().equals( "xxx.com" ) );
    }
}
