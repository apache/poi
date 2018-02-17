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

package misc.checkin;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.GDurationBuilder;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExt;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExtImpl;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;


import tools.util.*;



/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Nov 19, 2003
 */
public class RichParserTests extends TestCase
{
    public RichParserTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(RichParserTests.class); }

    public void testPrimitiveTypes() throws Exception
    {
        XMLStreamReader xsr = XmlObject.Factory.parse(new FileInputStream(
                JarUtil.getResourceFromJarasFile("xbean/misc/primitiveTypes.xml"))).
                    newXMLStreamReader();
        XMLStreamReaderExt xsrext = new XMLStreamReaderExtImpl(xsr);

        while (xsrext.hasNext())
        {
            switch (xsrext.next())
            {
                case XMLEvent.ATTRIBUTE:
                    processText(xsrext.getLocalName(), xsrext, -1);
                    break;
                case XMLEvent.START_ELEMENT:
                    for (int i = 0; i<xsrext.getAttributeCount(); i++)
                    {
                        processText(xsrext.getAttributeLocalName(i), xsrext, i);
                    }
                    String ln = xsrext.getLocalName();
                    processText(ln, xsrext, -1);
                    break;
            }
        }
    }

    private static final String[] strings = {"    this is a long string ... in attribute  ",
                                             "    this is a long string\n... in text  "};
    private static int stringsIdx = 0;
    private static final int[] ints = {5, -6, 15, 7, 2147483647, -2147483648, 5, -6, 15, 7, 2147483647, -2147483648};
    private static int intsIdx = 0;
    private static final boolean[] bools = {true, false, false, true, false, true, false, false, true, false};
    private static int boolsIdx = 0;
    private static final short[] shorts = {3, 3};
    private static int shortsIdx = 0;
    private static final byte[] bytes = {1, 1};
    private static int bytesIdx = 0;
    private static final long[] longs = {-500000, 1, 2, -500000, 1, 2};
    private static int longsIdx = 0;
    private static final double[] doubles = {1, -2.007, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN, 1, -2.007, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN};
    private static int doublesIdx = 0;
    private static final float[] floats = {12.325f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, 12.325f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
    private static int floatsIdx = 0;
    private static final QName[] qnames = { new QName("pre_uri", "local1"),
         new QName("local3"), new QName("pre_uri", "local1"),
         //new QName("default_uri", "local2"), new QName("default_uri", "local2"),
         new QName("local3")};
    private static int qnamesIdx = 0;

    private static void processText(String ln, XMLStreamReaderExt xs, int attIndex)
        throws XMLStreamException, IOException
    {
        if ("int".equals(ln))
        {
            int v = attIndex>-1 ? xs.getAttributeIntValue(attIndex) : xs.getIntValue();
            Assert.assertTrue("int " + v, ints[intsIdx++]==v);
        }
        else if ("boolean".equals(ln))
        {
            boolean v = attIndex>-1 ? xs.getAttributeBooleanValue(attIndex) : xs.getBooleanValue();
            Assert.assertTrue("boolean " + v, bools[boolsIdx++]==v);
        }
        else if ("short".equals(ln))
        {
            short v = attIndex>-1 ? xs.getAttributeShortValue(attIndex) : xs.getShortValue();
            Assert.assertTrue("short " + v, shorts[shortsIdx++]==v);
        }
        else if ("byte".equals(ln))
        {
            byte v = attIndex>-1 ? xs.getAttributeByteValue(attIndex) : xs.getByteValue();
            Assert.assertTrue("byte " + v, bytes[bytesIdx++]==v);
        }
        else if ("long".equals(ln))
        {
            long v = attIndex>-1 ? xs.getAttributeLongValue(attIndex) : xs.getLongValue();
            Assert.assertTrue("long " + v, longs[longsIdx++]==v);
        }
        else if ("double".equals(ln))
        {
            double v = attIndex>-1 ? xs.getAttributeDoubleValue(attIndex) : xs.getDoubleValue();
            Assert.assertTrue("double expected: " + doubles[doublesIdx] + "  actual: " + v,
                new Double(doubles[doublesIdx++]).equals(new Double(v)));
            // makeing new Doubles because Double.NaN==Double.NaN is false;
        }
        else if ("float".equals(ln))
        {
            float v = attIndex>-1 ? xs.getAttributeFloatValue(attIndex) : xs.getFloatValue();
            Assert.assertTrue("float expected: " + floats[floatsIdx] + "  actual: " + v,
                new Float(floats[floatsIdx++]).equals( new Float(v)));
            // makeing new Floats because Float.NaN==Float.NaN is false;
        }
        else if ("decimal".equals(ln))
        {
            BigDecimal v = attIndex>-1 ? xs.getAttributeBigDecimalValue(attIndex) : xs.getBigDecimalValue();
            Assert.assertTrue("BigDecimal " + v, new BigDecimal("1.001").equals(v));
        }
        else if ("integer".equals(ln))
        {
            BigInteger v = attIndex>-1 ? xs.getAttributeBigIntegerValue(attIndex) : xs.getBigIntegerValue();
            Assert.assertTrue("BigInteger " + v, new BigInteger("1000000000").equals(v));
        }
        else if ("base64Binary".equals(ln))
        {
            InputStream v = attIndex>-1 ? xs.getAttributeBase64Value(attIndex) : xs.getBase64Value();
            String a = readIS(v);
            Assert.assertTrue("Base64Binary " + a, "base64Binary".equals(a));
        }
        else if ("hexBinary".equals(ln))
        {
            InputStream v = attIndex>-1 ? xs.getAttributeHexBinaryValue(attIndex) : xs.getHexBinaryValue();
            String a = readIS(v);
            Assert.assertTrue("HexBinary " + a, "hexBinary".equals(a));
        }
        else if ("date".equals(ln))
        {
            Calendar v = attIndex>-1 ? xs.getAttributeCalendarValue(attIndex) : xs.getCalendarValue();
            Calendar c = new XmlCalendar( "2001-11-26T21:32:52Z" );
            Assert.assertTrue("Calendar expected:" + c.getTimeInMillis() + " actual:" + v.getTimeInMillis(), c.getTimeInMillis()==v.getTimeInMillis());
        }
        else if ("dateTime".equals(ln))
        {
            Date v = attIndex>-1 ? xs.getAttributeDateValue(attIndex) : xs.getDateValue();
            Date d = new XmlCalendar("2001-11-26T21:32:52").getTime();
            Assert.assertTrue("Date expected:" + d + " actual:" + v, d.equals(v));
        }
        else if ("gYearMonth".equals(ln))
        {
            GDate v = attIndex>-1 ? xs.getAttributeGDateValue(attIndex) : xs.getGDateValue();
            GDateBuilder gdb = new GDateBuilder();
            gdb.setYear(2001);
            gdb.setMonth(11);
            Assert.assertTrue("GDate expected:" + gdb + " actual:" + v, gdb.toGDate().equals(v));
        }
        else if ("duration".equals(ln))
        {
            GDuration v = attIndex>-1 ? xs.getAttributeGDurationValue(attIndex) : xs.getGDurationValue();
            GDurationBuilder gdb = new GDurationBuilder();
            gdb.setSign(-1);
            gdb.setSecond(7);
            Assert.assertTrue("GDuration expected:" + gdb + " actual:" + v, gdb.toGDuration().equals(v));
        }
        else if ("QName".equals(ln))
        {
            QName v = attIndex>-1 ? xs.getAttributeQNameValue(attIndex) : xs.getQNameValue();
            Assert.assertTrue("QName expected:" + qnames[qnamesIdx] + " actual:" + v, qnames[qnamesIdx++].equals(v));
        }
        else if ("string".equals(ln))
        {
            String v = attIndex>-1 ? xs.getAttributeStringValue(attIndex) : xs.getStringValue();
            String s = strings[stringsIdx++];


            Assert.assertTrue("String expected:\n'" + s + "'         actual:\n'" + v + "'", s.equals(v));
        }
    }

    public static String readIS(InputStream is)
        throws IOException
    {
        String res = "";
        byte[] buf = new byte[20];
        while (true)
        {
            int l = is.read(buf);
            if (l<0)
                break;
            res += new String(buf, 0, l);
        }
        return res;
    }

    public static void main(String[] args) throws IOException, XMLStreamException
    {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader xsr = factory.createXMLStreamReader(new FileInputStream(new File(args[0])));
        XMLStreamReaderExt xsrext = new XMLStreamReaderExtImpl(xsr);

        while (xsrext.hasNext())
        {
            switch (xsrext.next())
            {
                case XMLEvent.ATTRIBUTE:
                    processText(xsrext.getLocalName(), xsrext, -1);
                    break;
                case XMLEvent.START_ELEMENT:
                    for (int i = 0; i<xsrext.getAttributeCount(); i++)
                    {
                        processText(xsrext.getAttributeLocalName(i), xsrext, i);
                    }
                    String ln = xsrext.getLocalName();
                    processText(ln, xsrext, -1);
                    break;
            }
        }
    }
}
