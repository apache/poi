package xmlobject.checkin;

import junit.framework.TestCase;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;

/**
 * Created by Cezar Andrei (cezar dot andrei at gmail dot com)
 * Date: Apr 10, 2008
 */
public class XPathTest
        extends TestCase
{
    public XPathTest(String name)
    {
        super(name);
    }

    public void testPath()
        throws XmlException
    {
        final XmlObject obj = XmlObject.Factory.parse(
                "<a>" +
                  "<b>" +
                    "<c>val1</c>" +
                    "<d><c>val2</c></d>" +
                  "</b>" +
                  "<c>val3</c>" +
                "</a>");
        final XmlCursor c = obj.newCursor();

        c.selectPath(".//b/c");

        int selCount = c.getSelectionCount();
        assertEquals("SelectionCount", 1, selCount);

        while ( c.hasNextSelection() )
        {
            c.toNextSelection();

            assertEquals("OnStartElement", true, c.isStart());
            assertEquals("TextValue", "val1", c.getTextValue());
            System.out.println(" -> " + c.getObject() );
        }
        c.dispose();
    }


    public void testPath2()
        throws XmlException
    {
        final XmlObject obj = XmlObject.Factory.parse(
                "<a>" +
                  "<b>" +
                    "<c>val1</c>" +
                    "<d>" +
                      "<c>val2</c>" +
                      "<b><c>val3</c></b>" +
                    "</d>" +
                  "</b>" +
                  "<c>val4</c>" +
                "</a>");
        final XmlCursor c = obj.newCursor();

        c.selectPath(".//b/c");

        int selCount = c.getSelectionCount();
        assertEquals("SelectionCount", 2, selCount);

        assertEquals("hasNextSelection", true, c.hasNextSelection() );
        c.toNextSelection();

        System.out.println(" -> " + c.getObject() );
        assertEquals("OnStartElement", true, c.isStart());
        assertEquals("TextValue", "val1", c.getTextValue());


        assertEquals("hasNextSelection2", true, c.hasNextSelection() );
        c.toNextSelection();

        System.out.println(" -> " + c.getObject() );
        assertEquals("OnStartElement2", true, c.isStart());
        assertEquals("TextValue2", "val3", c.getTextValue());

        c.dispose();
    }

    public void testPath3()
        throws XmlException
    {
        final XmlObject obj = XmlObject.Factory.parse(
                "<a>" +
                  "<b>" +
                    "<c>val1</c>" +
                    "<d>" +
                      "<c>val2</c>" +
                      "<b>" +
                        "<c>val3" +
                          "<c>val5</c>" +
                        "</c>" +
                      "</b>" +
                    "</d>" +
                   "</b>" +
                   "<c>val4</c>" +
                 "</a>");
        final XmlCursor c = obj.newCursor();

        c.selectPath(".//b/c//c");

        int selCount = c.getSelectionCount();
        assertEquals("SelectionCount", 1, selCount);

        while ( c.hasNextSelection() )
        {
            c.toNextSelection();

            System.out.println(" -> " + c.getObject() );
            assertEquals("OnStartElement", true, c.isStart());
            assertEquals("TextValue", "val5", c.getTextValue());
        }
        c.dispose();
    }
}
