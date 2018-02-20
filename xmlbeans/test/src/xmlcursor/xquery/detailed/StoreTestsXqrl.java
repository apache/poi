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

package xmlcursor.xquery.detailed;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;




//Used to be a checkin 
public class StoreTestsXqrl extends TestCase
{
    public StoreTestsXqrl(String name) { super(name); }
    public static Test suite() { return new TestSuite(StoreTestsXqrl.class); }

    private void doTokenTest ( String xml )
        throws Exception
    {
        XmlCursor c = XmlObject.Factory.parse( xml ).newCursor();
        //String s = c.execQuery( "$this" ).xmlText();
        String s = c.execQuery( "." ).xmlText();
        Assert.assertTrue( s.equals( xml ) );
    }
    
    private void doSaveTest ( String xml )
        throws Exception
    {
        doTokenTest( xml );
    }

    public void testSaving ( )
        throws Exception
    {
        doSaveTest( "<foo xmlns=\"foo.com\"><bar>1</bar></foo>" );
        doSaveTest( "<foo><!--comment--><?target foo?></foo>" );
        doSaveTest( "<foo>a<bar>b</bar>c<bar>d</bar>e</foo>" );
        doSaveTest( "<foo xmlns:x=\"y\"><bar xmlns:x=\"z\"/></foo>" );
        doSaveTest( "<foo x=\"y\" p=\"r\"/>" );

        String s = "<foo>aaa</foo>bbb";
        s = s + s + s + s + s + s + s + s + s + s + s + s + s + s + s;
        s = "<bar>xxxx" + s + "</bar>";
        
        doSaveTest( s );

        XmlObject x =
            XmlObject.Factory.parse( "<foo xmlns:a='a.com'><bar xmlns:a='b.com'/></foo>" );

        XmlCursor c = x.newCursor();

        c.toFirstChild();
        c.toFirstChild();

        Assert.assertTrue( c.xmlText().equals( "<bar xmlns:a=\"b.com\"/>" ) );
        
        x = XmlObject.Factory.parse( "<foo xmlns:a='a.com'><bar/></foo>" );

        c = x.newCursor();

        c.toFirstChild();
        c.toFirstChild();

        Assert.assertTrue( c.xmlText().equals( "<bar xmlns:a=\"a.com\"/>" ) );
    }
    
    
    private void testTextFrag ( String actual, String expected )
    {
        String pre = "<xml-fragment>";
        
        String post = "</xml-fragment>";
        
        Assert.assertTrue( actual.startsWith( pre ) );
        Assert.assertTrue( actual.endsWith( post ) );
        
        Assert.assertTrue(
            expected.equals(
                actual.substring(
                    pre.length(), actual.length() - post.length() ) ) );
    }

    //
    // Make sure XQuery works (tests the saver too)
    //

    public void testXQuery ( )
        throws Exception
    {
        XmlCursor c =
            XmlObject.Factory.parse(
                "<foo><bar>1</bar><bar>2</bar></foo>" ).newCursor();

        String s =
            c.execQuery( "for $b in //bar order by ($b) " +
            "descending return $b").xmlText();

        testTextFrag( s, "<bar>2</bar><bar>1</bar>" );
        
        c = XmlObject.Factory.parse( "<foo></foo>" ).newCursor();
        c.toNextToken();
        c.toNextToken();
        c.insertElement( "boo", "boo.com" );
        c.toStartDoc();
        
        Assert.assertTrue(
            //c.execQuery( "$this" ).
            c.execQuery( "." ).
                xmlText().equals(
                    "<foo><boo:boo xmlns:boo=\"boo.com\"/></foo>" ) );
    }

    
    public void testPathing ( )
        throws Exception
    {
        XmlObject x =
            XmlObject.Factory.parse(
                "<foo><bar>1</bar><bar>2</bar><bar>3</bar></foo>" );

        XmlCursor c = x.newCursor();

        c.selectPath( "//bar" );
        
        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.xmlText().equals( "<bar>1</bar>" ) );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.xmlText().equals( "<bar>2</bar>" ) );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.xmlText().equals( "<bar>3</bar>" ) );

        Assert.assertTrue( !c.toNextSelection() );

        //
        //
        //

        x =
            XmlObject.Factory.parse(
                "<foo><bar x='1'/><bar x='2'/><bar x='3'/></foo>" );

        c = x.newCursor();

        //c.selectPath( "$this//@x" );
        c.selectPath( ".//@x" );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.currentTokenType().isAttr() );
        Assert.assertTrue( c.getTextValue().equals( "1" ) );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.currentTokenType().isAttr() );
        Assert.assertTrue( c.getTextValue().equals( "2" ) );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.currentTokenType().isAttr() );
        Assert.assertTrue( c.getTextValue().equals( "3" ) );

        Assert.assertTrue( !c.toNextSelection() );

        //
        //
        //

        x =
            XmlObject.Factory.parse(
                "<foo><bar>1</bar><bar>2</bar><bar>3</bar></foo>" );

        c = x.newCursor();

        c.selectPath( "//text()" );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.currentTokenType().isText() );
        Assert.assertTrue( c.getChars().equals( "1" ) );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.currentTokenType().isText() );
        Assert.assertTrue( c.getChars().equals( "2" ) );

        Assert.assertTrue( c.toNextSelection() );
        Assert.assertTrue( c.currentTokenType().isText() );
        Assert.assertTrue( c.getChars().equals( "3" ) );

        Assert.assertTrue( !c.toNextSelection() );
    }
}
