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

import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlSaxHandler;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlInt;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.Comparable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Iterator;
import java.util.TreeSet;
import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

   
public class TypedSettersTests extends TestCase
{
    public TypedSettersTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(TypedSettersTests.class); }

    private static final String schemaNs ="xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
    private static final String instanceNs = "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"";

    private static final String fmt ( String s )
    {
        StringBuffer sb = new StringBuffer();

        for ( int i = 0 ; i < s.length() ; i++ )
        {
            char ch = s.charAt( i );

            if (ch != '$')
            {
                sb.append( ch );
                continue;
            }
            
            ch = s.charAt( ++i );

            String id = "";

            while ( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))
            {
                id = id + ch;
                ch = s.charAt( ++i );
            }

            String arg = "";

            if (ch == '(')
            {
                ch = s.charAt( ++i );

                while ( ch != ')' )
                {
                    arg += ch;
                    ch = s.charAt( ++i );
                }
            }
            else
                i--;

            if (id.equals( "schema" ))
                sb.append( schemaNs );
            else if (id.equals( "xsi" ))
                sb.append( instanceNs );
            else if (id.equals( "type" ))
            {
                Assert.assertTrue( arg.length() > 0 );
                sb.append( "xsi:type=\"" + arg + "\"" );
            }
            else
                Assert.assertTrue( false );
        }

        return sb.toString();
    }

    private static final String nses = schemaNs + " " + instanceNs;
    
    public void testJavaNoTypeSingletonElement ( )
        throws Exception
    {
        XmlObject x = XmlObject.Factory.parse( "<xyzzy/>" );
        XmlObject x2 = XmlObject.Factory.parse( "<bubba>moo</bubba>" );
        XmlCursor c = x.newCursor();
        XmlCursor c2 = x2.newCursor();

        c.toNextToken();
        c2.toNextToken();

        c.getObject().set( c2.getObject() );

        Assert.assertTrue( x.xmlText().equals( "<xyzzy>moo</xyzzy>" ) );
    }
    
    public void testJavaNoTypeSingletonAttribute ( )
        throws Exception
    {
        XmlObject x = XmlObject.Factory.parse( "<xyzzy a=''/>" );
        XmlObject x2 = XmlObject.Factory.parse( "<bubba b='moo'/>" );
        XmlCursor c = x.newCursor();
        XmlCursor c2 = x2.newCursor();

        c.toNextToken();
        c.toNextToken();
        c2.toNextToken();
        c2.toNextToken();

        c.getObject().set( c2.getObject() );

        Assert.assertTrue( x.xmlText().equals( "<xyzzy a=\"moo\"/>" ) );
    }
    
    public void testJavaNoTypeSingletonElementWithXsiType ( )
        throws Exception
    {
        XmlObject x = XmlObject.Factory.parse( "<xyzzy/>", new XmlOptions()
                .setDocumentType( XmlObject.type ) );
        String input= fmt( "<xml-fragment $type(xs:int) $xsi $schema>" +
                "69</xml-fragment>" );
        //String input=
        XmlObject x2 = XmlObject.Factory
                .parse( input );



        Assert.assertTrue(x2.schemaType() == XmlInt.type );

  }
    
}
