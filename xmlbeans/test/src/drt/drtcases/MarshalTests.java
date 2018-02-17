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

package drt.drtcases;

import com.mytest.IntEnum;
import com.mytest.IntegerEnum;
import com.mytest.ModeEnum;
import com.mytest.MyClass;
import com.mytest.MySubClass;
import com.mytest.MySubSubClass;
import com.mytest.SimpleContentExample;
import com.mytest.YourClass;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.xmlbeans.BindingContext;
import org.apache.xmlbeans.BindingContextFactory;
import org.apache.xmlbeans.EncodingStyle;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.Marshaller;
import org.apache.xmlbeans.ObjectFactory;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SoapMarshaller;
import org.apache.xmlbeans.SoapUnmarshaller;
import org.apache.xmlbeans.Unmarshaller;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.binding.compile.Schema2Java;
import org.apache.xmlbeans.impl.common.XmlReaderToWriter;
import org.apache.xmlbeans.impl.common.XmlStreamUtils;
import org.apache.xmlbeans.impl.marshal.BindingContextFactoryImpl;
import org.apache.xmlbeans.impl.marshal.util.ArrayUtils;
import org.apache.xmlbeans.impl.tool.PrettyPrinter;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class MarshalTests extends TestCase
{
    private static final boolean VERBOSE = false;

    //must be in sync with binding config file
    private static final BigInteger DEFAULT_BIG_INT =
        new BigInteger("876587658765876587658765876587658765");
    private static final String XSD_URI = "http://www.w3.org/2001/XMLSchema";
    private static final String SOAPENC_URI = "http://schemas.xmlsoap.org/soap/encoding/";
    private static final QName DFLT_ELEM_NAME = new QName("java:com.mytest", "load");
    private static final QName MYCLASS_NAME = new QName("java:com.mytest", "MyClass");
    private static final QName DUMMY_QNAME = new QName("foo", "bar");
    private static final QName ANY_TYPE_NAME = new QName(XSD_URI, "anyType");


    public MarshalTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(MarshalTests.class);
    }

    //does not test any xmlbeans code, but rather a quick sanity check
    //of the current jsr 173 impl
    public void testAStream()
        throws Exception
    {
        String doc = "<a x='y'>food</a>";
        StringReader sr = new StringReader(doc);
        final XMLStreamReader reader =
            XMLInputFactory.newInstance().createXMLStreamReader(sr);

        dumpReader(reader);
    }

    //does not test any xmlbeans code, but rather a quick sanity check
    //of the current jsr 173 impl
    public void testAStreamWriter()
        throws Exception
    {
        StringWriter sw = new StringWriter();
        final XMLStreamWriter writer =
            XMLOutputFactory.newInstance().createXMLStreamWriter(sw);

        writer.writeStartDocument();
        writer.writeStartElement("dummy");
        final String uri = "uri";
        final String prefix = "prefix";
        final String lname = "lname";

        writer.writeStartElement(prefix, lname, uri);
        writer.writeNamespace(prefix, uri);
        writer.writeEndElement();

        boolean writeit = false;
        if (writer.getPrefix(uri) == null) {
            writeit = true;
        }
        writer.writeStartElement(prefix, lname, uri);
        if (writeit) {
            writer.writeNamespace(prefix, uri);
        }
        writer.writeEndElement();

        writer.writeEndElement(); //dummy

        writer.writeEndDocument();
        writer.close();
        sw.close();

        final String DOC = sw.getBuffer().toString();
        inform("DOC W: " + DOC);


        StringReader sr = new StringReader(DOC);
        final XMLStreamReader reader =
            XMLInputFactory.newInstance().createXMLStreamReader(sr);

        //uncomment when stax bug is fixed
        //dumpReader(reader, true);
    }

    public void testManySimpleTypesUnmarshall()
        throws Exception
    {
        testSimpleTypeUnmarshal(Boolean.TRUE, "boolean");
        testSimpleTypeUnmarshal(new Byte((byte)125), "byte");
        testSimpleTypeUnmarshal(new Short((short)5543), "short");
        testSimpleTypeUnmarshal(new Integer(55434535), "int");
        testSimpleTypeUnmarshal(new Long(554345354445555555L), "long");
        testSimpleTypeUnmarshal(new BigInteger("55434535443332323245555555"), "integer");
        testSimpleTypeUnmarshal(new BigInteger("55434535443332323245555555"), "positiveInteger");
        testSimpleTypeUnmarshal(new BigInteger("55434535443332323245555555"), "nonNegativeInteger");
        testSimpleTypeUnmarshal(new BigInteger("-55434535443332323245555555"), "negativeInteger");
        testSimpleTypeUnmarshal(new BigInteger("-55434535443332323245555555"), "nonPositiveInteger");
        testSimpleTypeUnmarshal(new BigInteger("5543453555"), "unsignedLong");
        testSimpleTypeUnmarshal(new Long("5543453555"), "unsignedInt");
        testSimpleTypeUnmarshal(new Integer("62121"), "unsignedShort");
        testSimpleTypeUnmarshal(new Short("254"), "unsignedByte");
        testSimpleTypeUnmarshal(new BigDecimal("43434343342.233434342"), "decimal");
        testSimpleTypeUnmarshal(new Float(54.5423f), "float");
        testSimpleTypeUnmarshal(new Double(23432.43234), "double");

        testSimpleTypeUnmarshal(new GDuration("P1Y2M3DT10H30M"), "duration");

        testStringTypeUnmarshal("anySimpleType");
        testStringTypeUnmarshal("string");
        testStringTypeUnmarshal("normalizedString");
        testStringTypeUnmarshal("token");
        testStringTypeUnmarshal("language");
        testStringTypeUnmarshal("Name");
        testStringTypeUnmarshal("NCName");
        testStringTypeUnmarshal("NMTOKEN");
        testStringTypeUnmarshal("ID");
        testStringTypeUnmarshal("IDREF");
        testStringTypeUnmarshal("ENTITY");
        testStringTypeUnmarshal("anyURI");
        testStringTypeUnmarshal("NOTATION");


        Calendar c = Calendar.getInstance();

        testSimpleTypeUnmarshal(c, "2002-03-06T08:04:39.265Z", "dateTime");


        final byte[] bytes = new byte[]{1, 2, 3, 4, 5, 6};
        testSimpleTypeUnmarshal(bytes, "AQIDBAUG", "base64Binary");
        testSimpleTypeUnmarshal(bytes, "010203040506", "hexBinary");

        final String[] strs = new String[]{"foo", "bar"};
        testSimpleTypeUnmarshal(strs, "foo bar", "ENTITIES");
        testSimpleTypeUnmarshal(strs, "foo bar", "IDREFS");
        testSimpleTypeUnmarshal(strs, "foo bar", "NMTOKENS");

        testSimpleTypeUnmarshal("basic", "basic", SOAPENC_URI, "string");
        testSimpleTypeUnmarshal(new Integer("123"), "123", SOAPENC_URI, "int");
    }

    private void testStringTypeUnmarshal(String xsd_type)
        throws Exception
    {
        testSimpleTypeUnmarshal("test_" + xsd_type, xsd_type);
    }


    public void testManySimpleTypesMarshall()
        throws Exception
    {
        testSimpleTypeMarshal(Boolean.TRUE, "boolean");
        testSimpleTypeMarshal(new Byte((byte)125), "byte");
        testSimpleTypeMarshal(new Short((short)5543), "short");
        testSimpleTypeMarshal(new Integer(55434535), "int");
        testSimpleTypeMarshal(new Integer(75434535), "int", "int");
        testSimpleTypeMarshal(new Long(554345354445555555L), "long");
        testSimpleTypeMarshal(new BigInteger("55434535443332323245555555"), "integer");
        testSimpleTypeMarshal(new BigDecimal("43434343342.233434342"), "decimal");
        testSimpleTypeMarshal(new Float(5555.5555f), "float");
        testSimpleTypeMarshal(new Double(1231.444), "double");
        testSimpleTypeMarshal(new URI("http://www.apache.org/"), "anyURI");

        testSimpleTypeMarshal(new GDuration("P1Y2M3DT10H30M"), "duration");

        testSimpleTypeMarshal("some text here", "string");
        testSimpleTypeMarshal("  ", "string");
        testSimpleTypeMarshal("", "string");
        testSimpleTypeMarshal("aToken", "token");
        testSimpleTypeMarshal("       ", "string");

        testSimpleTypeMarshal(new QName("someuri", "somelname"), "QName");
        testSimpleTypeMarshal(new QName("nakedlname"), "QName");

        final byte[] bytes = new byte[]{1, 2, 3, 4, 5, 6};
        testSimpleTypeMarshal(bytes, "base64Binary");
        testSimpleTypeMarshal(bytes, "hexBinary");


    }


    //only works for values where .toString() is equivalent to marshalling
    public void testSimpleTypeUnmarshal(Object expected, String xsd_type)
        throws Exception
    {
        testSimpleTypeUnmarshal(expected, expected.toString(), xsd_type);
    }

    public void testSimpleTypeUnmarshal(Object expected,
                                        String lexval,
                                        String xsd_type)
        throws Exception
    {
        testSimpleTypeUnmarshal(expected, lexval, XSD_URI, xsd_type);
    }

    public void testSimpleTypeUnmarshal(Object expected,
                                        String lexval,
                                        String type_uri,
                                        String xsd_type)
        throws Exception
    {
        BindingContext bindingContext = getBuiltinBindingContext();

        String xmldoc = "<a" +
            " xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'" +
            " xmlns:xs='" +
            type_uri +
            "' xsi:type='xs:" +
            xsd_type + "' >" + lexval + "</a>";

        StringReader stringReader = new StringReader(xmldoc);
        XMLStreamReader xrdr =
            XMLInputFactory.newInstance().createXMLStreamReader(stringReader);

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);

        Unmarshaller umctx =
            bindingContext.createUnmarshaller();

        Object obj = umctx.unmarshal(xrdr, options);
        reportErrors(errors, "SimpleType error. lexical=" + lexval);
        Assert.assertTrue(errors.isEmpty());


        //special case date/time tests.
        //we really need more robust testing here.
        if (expected instanceof Calendar) {
            XmlCalendar got = (XmlCalendar)obj;
            String got_lex = got.toString();
            Assert.assertEquals(lexval, got_lex);
        } else if (expected.getClass().isArray()) {
            final boolean eq = ArrayUtils.arrayEquals(expected, obj);
            final String s = "arrays not equal.  " +
                "expected " + ArrayUtils.arrayToString(expected) +
                " got " + ArrayUtils.arrayToString(obj);
            Assert.assertTrue(s, eq);
        } else {
            Assert.assertEquals(expected, obj);
        }

//        inform("OK for " + expected);
    }

    private void reportErrors(Collection errors, final String prefix)
    {
        for (Iterator itr = errors.iterator(); itr.hasNext();) {
            XmlError xmlError = (XmlError)itr.next();
            error(prefix + " " + xmlError);
        }
    }


    public void testSimpleTypeMarshal(Object orig, String xsd_type)
        throws Exception
    {
        final String java_type = orig.getClass().getName();
        testSimpleTypeMarshal(orig, xsd_type, java_type);
    }

    public void testSimpleTypeMarshal(Object orig, String xsd_type,
                                      final String java_type)
        throws Exception
    {
        BindingContext bindingContext =
            BindingContextFactory.newInstance().createBindingContext();

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);

        Marshaller ctx =
            bindingContext.createMarshaller();
        Assert.assertNotNull(ctx);


        final XMLStreamReader reader =
            ctx.marshalType(orig,
//                           new QName("uri", "lname"),
                            new QName("lname"),
                            new QName(XSD_URI, xsd_type),
                            java_type, options);


        inform("==================OBJ: " + orig);
        dumpReader(reader);

        reportErrors(errors, "simpleTypeMarshal");

        Assert.assertTrue(errors.isEmpty());
    }

    public void testSimplePolymorphicTypeMarshal()
        throws Exception
    {
        BindingContext bindingContext =
            BindingContextFactory.newInstance().createBindingContext();

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);

        Marshaller ctx =
            bindingContext.createMarshaller();
        Assert.assertNotNull(ctx);

        String our_obj = "hello";

        final QName schemaType = new QName(XSD_URI, "anyType");
        final String javaType = Object.class.getName();
        final XMLStreamReader reader =
            ctx.marshalType(our_obj,
                            new QName("lname"),
                            schemaType,
                            javaType, options);


        inform("==================POLYOBJ: " + our_obj);

        final boolean dump = false;
        if (dump) {
            dumpReader(reader);
        } else {
            Unmarshaller um =
                bindingContext.createUnmarshaller();
            Assert.assertNotNull(um);

            Object out = um.unmarshalType(reader, schemaType, javaType, options);
            Assert.assertEquals(our_obj, out);

            reportErrors(errors, "poly-marshal");
            Assert.assertTrue(errors.isEmpty());
        }
    }


    public void testByNameMarshal()
        throws Exception
    {
        com.mytest.MyClass mc = new com.mytest.MyClass();
        mc.setMyatt("attval");
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(99999.777f);
        myelt.setMyFloat(5555.4444f);
//        myelt.setMyClass(new com.mytest.MyClass());

        myelt.setWrappedArrayOne(new String[]{"a", null, "b"});

        MySubClass sub = new MySubClass();
        sub.setBigInt(new BigInteger("23522352235223522352"));
        myelt.setMySubClass(sub);
//        sub.setIsSetBigInt(false); //TESTING;
//        sub.setBigInt(null);
//        sub.setIsSetBigInt(true); //TESTING;


        myelt.setMyClass(sub);

        SimpleContentExample se = new SimpleContentExample();
        se.setFloatAttOne(44.33f);
        se.setSimpleContent("someSimpleContentOkay");
        myelt.setSimpleContentExample(se);

        myelt.setModeEnum(ModeEnum.On);

        mc.setMyelt(myelt);

        myelt.setStringArray(new String[]{"one", "two", "three"});

        myelt.setMyClassArray(new MyClass[]{sub, new MyClass(),
                                            //this type is not in our binding file,
                                            //but we should then treat is as its the parent type
                                            new MySubSubClass(),
                                            sub});


        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);

        Marshaller ctx =
            bindingContext.createMarshaller();

        Assert.assertNotNull(ctx);


        final XMLStreamReader reader =
            ctx.marshalType(mc, DFLT_ELEM_NAME,
                            MYCLASS_NAME,
                            mc.getClass().getName(), options);

//
//        final XMLStreamReader reader =
//            ctx.marshalType(sub, new QName("java:com.mytest", "sub-test"),
//                            new QName("java:com.mytest", "MySubClass"),
//                            "MyClass", null);

        inform("=======IN-OBJA: " + mc);

        dumpReader(reader);
        reportErrors(errors, "byname-marshal");
        Assert.assertTrue(errors.isEmpty());
    }


    public void testByNameMarshalSoap()
        throws Exception
    {
        final boolean verbose = false;

        com.mytest.MyClass mc = new com.mytest.MyClass();
        mc.setMyatt("attval");
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(99999.777f);
        myelt.setMyFloat(5555.4444f);

        QName qn = new QName("foo", "bar");
        myelt.setQn(qn);
        myelt.setQn2(qn);

        myelt.setWrappedArrayOne(new String[]{"a", "a", "b"});

        MySubClass sub = new MySubClass();
        sub.setBigInt(new BigInteger("23522352235223522352"));

        myelt.setMySubClass(sub);
        myelt.setMyClass(sub);
        sub.setMyelt(myelt);  //cycle

        myelt.setMyBoss(myelt); //cycle: self reference

        SimpleContentExample se = new SimpleContentExample();
        se.setFloatAttOne(44.33f);
        se.setSimpleContent("someSimpleContentOkay");
        myelt.setSimpleContentExample(se);

        myelt.setModeEnum(ModeEnum.On);

        mc.setMyelt(myelt);

        myelt.setStringArray(new String[]{"a", "b", "c"});

        myelt.setMyClassArray(new MyClass[]{sub, new MyClass(),
                                            //this type is not in our binding file,
                                            //but we should then treat is as its the parent type
                                            new MySubSubClass(),
                                            sub});


        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);

        final SoapMarshaller ctx =
            bindingContext.createSoapMarshaller(EncodingStyle.SOAP11);

        Assert.assertNotNull(ctx);


        final XMLStreamReader reader =
            ctx.marshalType(mc, DFLT_ELEM_NAME,
                            MYCLASS_NAME,
                            mc.getClass().getName(), options);

        dumpReader(reader, verbose);
        reportErrors(errors, "byname-marshal-soap");
        Assert.assertTrue(errors.isEmpty());

        inform("===========final id objs coming next===========", verbose);
        final Iterator itr = ctx.marshalReferenced(options);
        while (itr.hasNext()) {
            final XMLStreamReader rdr = (XMLStreamReader)itr.next();
            inform("got rdr: " + System.identityHashCode(rdr), verbose);
            dumpReader(rdr, verbose);
            rdr.close();
        }
    }


    public void testByNameMarshalSoapViaWriter()
        throws Exception
    {
        final Collection errors = new LinkedList();

        final String xmldoc = createSoapExampleXmlString(new MyClass(), errors);

        inform("=======SOAPOUT-XML:\n" +
               PrettyPrinter.indent(xmldoc));
        reportErrors(errors, "byname-marshal-soap-writer");
        Assert.assertTrue(errors.isEmpty());
    }


    public void testByNameSoapUnmarshal()
        throws Exception
    {
        final boolean verbose = false;

        final Collection errors = new LinkedList();
        final MyClass source_mc = new MyClass();
        final String xmldoc = createSoapExampleXmlString(source_mc, errors);
        reportErrors(errors, "byname-marshal-soap-writer");
        Assert.assertTrue(errors.isEmpty());

        BindingContext bindingContext =
            getBindingContext(getBindingConfigDocument());

        XmlOptions opts = new XmlOptions();
        opts.setLoadLineNumbers();
        final Document document = (Document) XmlObject.Factory.parse( sXml, opts ).getDomNode();

        if (verbose) {
            final XMLStreamReader tmp_stream = XmlBeans.nodeToXmlStreamReader(document);
            dumpReader(tmp_stream, true);
            tmp_stream.close();
        }

        final SoapUnmarshaller um =
            bindingContext.createSoapUnmarshaller(EncodingStyle.SOAP11, document);

        final XMLStreamReader xrdr = XmlBeans.nodeToXmlStreamReader(document);
//        {
//            while(xrdr.hasNext()) {
//                System.out.println("## AT " + XmlStreamUtils.printEvent(xrdr));
//                final int e = xrdr.next();
//            }
//            if (System.currentTimeMillis() > 1) {
//                throw new AssertionError("STOP!");
//            }
//        }

        while (!xrdr.isStartElement()) {
            xrdr.next();
        }
        xrdr.next();
        //now at Dummy node
        while (!xrdr.isStartElement()) {
            xrdr.next();
        }
        //now at actual type
        xrdr.require(XMLStreamReader.START_ELEMENT,
                     DFLT_ELEM_NAME.getNamespaceURI(),
                     DFLT_ELEM_NAME.getLocalPart());


        final Object obj = um.unmarshalType(xrdr, MYCLASS_NAME,
                                            MyClass.class.getName(), null);

        inform("GOT OBJ: " + obj.getClass(), verbose);
        MyClass mc = (MyClass)obj;

        final YourClass myelt = mc.getMyelt();
        Assert.assertNotNull(myelt);
        Assert.assertEquals(myelt.getBools(), source_mc.getMyelt().getBools());
        Assert.assertTrue(Arrays.equals(myelt.getWrappedArrayOne(),
                                        source_mc.getMyelt().getWrappedArrayOne()));
        Assert.assertEquals(DUMMY_QNAME, myelt.getQn2());
        Assert.assertEquals(myelt.getQn(), myelt.getQn2());
        Assert.assertTrue(myelt.getMySubClass() == myelt.getMyClass());
        Assert.assertTrue(myelt.getMyBoss() == myelt);
        Assert.assertTrue(myelt.objectArray[3] == myelt.objectArray[4]);
        Assert.assertTrue(myelt.objectArray[3] == myelt.objectArray);
        Assert.assertSame(myelt.objectArray[0], myelt.objectArray[1]);
        Assert.assertNull(myelt.objectArray[5]);

        MySubClass sub = (MySubClass)myelt.objectArray[8];
        Assert.assertEquals(BigInteger.ONE, sub.getBigInt());

        Assert.assertTrue(myelt.objectArrayTwo[1] == myelt.objectArray);
        Assert.assertTrue(myelt.objectArrayTwo[2] == myelt.objectArrayTwo);


        xrdr.close();
    }


    public void testAnyTypeSoapUnmarshal()
        throws Exception
    {
        final boolean verbose = false;

        final Collection errors = new LinkedList();
        final MyClass source_mc = new MyClass();


        BindingContext bindingContext =
            getBindingContext(getBindingConfigDocument());

        //////////////////////////////////////////////

        final XmlOptions options = new XmlOptions();
        options.setErrorListener(errors);

        final SoapMarshaller ctx =
            bindingContext.createSoapMarshaller(EncodingStyle.SOAP11);

        Assert.assertNotNull(ctx);

        StringWriter sw = new StringWriter();
        XMLStreamWriter xml_out =
            XMLOutputFactory.newInstance().createXMLStreamWriter(sw);

        xml_out.writeStartDocument();
        xml_out.writeStartElement("DUMMY_ROOT");
        xml_out.writeNamespace("xs", XSD_URI);
        xml_out.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        xml_out.writeNamespace("jt", "java:com.mytest");

        ctx.marshalType(xml_out, source_mc, DFLT_ELEM_NAME,
                        ANY_TYPE_NAME,
                        Object.class.getName(), options);


        xml_out.writeComment("ids are coming next");

        ctx.marshalReferenced(xml_out, options);
        xml_out.writeEndElement();
        xml_out.writeEndDocument();
        xml_out.close();
        sw.close();
        final String xmldoc = sw.getBuffer().toString();

        //////////////////////////////////////////////

        reportErrors(errors, "byname-marshal-soap-writer");
        Assert.assertTrue(errors.isEmpty());


        XmlOptions opts = new XmlOptions();
        opts.setLoadLineNumbers();
        final Document document = (Document) XmlObject.Factory.parse(xmldoc, opts).getDomNode();

        if (verbose) {
            final XMLStreamReader tmp_stream = XmlBeans.nodeToXmlStreamReader(document);
            dumpReader(tmp_stream, true);
            tmp_stream.close();
        }

        final SoapUnmarshaller um =
            bindingContext.createSoapUnmarshaller(EncodingStyle.SOAP11, document);

        final XMLStreamReader xrdr = XmlBeans.nodeToXmlStreamReader(document);

        while (!xrdr.isStartElement()) {
            xrdr.next();
        }
        xrdr.next();
        //now at Dummy node
        while (!xrdr.isStartElement()) {
            xrdr.next();
        }
        //now at actual type
        xrdr.require(XMLStreamReader.START_ELEMENT,
                     DFLT_ELEM_NAME.getNamespaceURI(),
                     DFLT_ELEM_NAME.getLocalPart());


        final Object obj = um.unmarshalType(xrdr, ANY_TYPE_NAME,
                                            Object.class.getName(), null);

        inform("GOT OBJ: " + obj.getClass(), verbose);
        Assert.assertTrue(obj instanceof MyClass);
        xrdr.close();
    }

    //note that mc obj will be changed
    private String createSoapExampleXmlString(MyClass mc,
                                              final Collection errors)
        throws Exception
    {
        mc.setMyatt("attval");
        YourClass myelt = new YourClass();
        myelt.setAttrib(99999.777f);
        myelt.setMyFloat(5555.4444f);

        myelt.setQn(DUMMY_QNAME);
        myelt.setQn2(DUMMY_QNAME);

        myelt.setWrappedArrayOne(new String[]{"a", "a", "b4", "b4", "a"});

        MySubClass sub = new MySubClass();
        sub.setBigInt(new BigInteger("23522352235223522352"));
        sub.quadStringArray = MySubClass.newQuadStringArray();

        myelt.setMySubClass(sub);
        myelt.setMyClass(sub);
        sub.setMyelt(myelt);  //cycle

        myelt.setMyBoss(myelt); //cycle: self reference

        SimpleContentExample se = new SimpleContentExample();
        se.setFloatAttOne(44.33f);
        se.setSimpleContent("someSimpleContentOkay");
        myelt.setSimpleContentExample(se);

        myelt.setModeEnum(ModeEnum.On);

        mc.setMyelt(myelt);

        myelt.setStringArray(new String[]{"a", "b", "c", "a", "a", "b"});

        myelt.setMyClassArray(new MyClass[]{sub,
                                            new MyClass(),
                                            mc,
                                            sub});

        final MySubSubClass subsub = new MySubSubClass();
        subsub.setBigInt(BigInteger.ONE);

        final Object[] obj_array = new Object[]{"a",
                                                "a",
                                                myelt,
                                                null,
                                                null,
                                                null,
                                                new Integer(5),
                                                null,
                                                subsub,
        };
        obj_array[3] = obj_array;
        obj_array[4] = obj_array;
        myelt.objectArray = obj_array;

        final Object[] obj_array2 = new Object[4];
        obj_array2[0] = "a";
        obj_array2[1] = obj_array;
        obj_array2[2] = obj_array2;
        obj_array2[3] = null;

        myelt.objectArrayTwo = obj_array2;
        obj_array[7] = obj_array2;

        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        final XmlOptions options = new XmlOptions();
        options.setErrorListener(errors);

        final SoapMarshaller ctx =
            bindingContext.createSoapMarshaller(EncodingStyle.SOAP11);

        Assert.assertNotNull(ctx);

        StringWriter sw = new StringWriter();
        XMLStreamWriter xml_out =
            XMLOutputFactory.newInstance().createXMLStreamWriter(sw);

        xml_out.writeStartDocument();
        xml_out.writeStartElement("DUMMY_ROOT");
//        xml_out.writeDefaultNamespace("java:com.mytest");
        xml_out.writeNamespace("xs", XSD_URI);
        xml_out.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        xml_out.writeNamespace("jt", "java:com.mytest");


        ctx.marshalType(xml_out, mc, DFLT_ELEM_NAME,
                        MYCLASS_NAME,
                        mc.getClass().getName(), options);

//            xml_out.writeComment("simple string");
//            ctx.marshalType(xml_out, "TEST1", new QName("someuri", "str"),
//                            new QName(XSD_URI, "string"),
//                            String.class.getName(), options);
//


        xml_out.writeComment("ids are coming next");


        ctx.marshalReferenced(xml_out, options);
        xml_out.writeEndElement();


        xml_out.writeEndDocument();


        xml_out.close();
        sw.close();
        return sw.getBuffer().toString();
    }


    public void testWrappedArray()
        throws Exception
    {
        String[] strs = new String[]{"aa", null, "bb", "cc"};
        final String java_type = strs.getClass().getName();
        strs = null;  // testing...

        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);

        Marshaller ctx =
            bindingContext.createMarshaller();

        Assert.assertNotNull(ctx);

        final QName element_name = new QName("java:com.mytest", "string-array");
        final QName schema_type = new QName("java:com.mytest", "ArrayOfString");
        final XMLStreamReader reader =
            ctx.marshalType(strs, element_name, schema_type, java_type, options);

        inform("=======WRAPPED-ARRAY-OBJ: " + strs);

//        dumpReader(reader);
        reportErrors(errors, "wrapped-array");
        Assert.assertTrue(errors.isEmpty());

        final Unmarshaller um = bindingContext.createUnmarshaller();
        Object retval = um.unmarshalType(reader, schema_type, java_type, options);
        reportErrors(errors, "wrapped-array2");
        Assert.assertTrue(errors.isEmpty());

        Assert.assertTrue("expected " + ArrayUtils.arrayToString(strs) +
                          " got " + ArrayUtils.arrayToString(retval),
                          ArrayUtils.arrayEquals(strs, retval));
    }


    public void DISABLED_testInitPerf()
        throws Exception
    {
        final File conf = getBindingConfigDocument();

        final int trials = 2;

        final BindingContextFactory context_factory = BindingContextFactory.newInstance();

        BindingContext bindingContext = null;

        final long before_millis = System.currentTimeMillis();
        for (int i = 0; i < trials; i++) {

            bindingContext =
                ((BindingContextFactoryImpl)context_factory).createBindingContextFromConfig(conf);

            if (bindingContext == null) {
                throw new Exception("bad news");
            }
        }
        final long after_millis = System.currentTimeMillis();
        final long diff = (after_millis - before_millis);
        boolean verbose = true;
        inform("INIT-milliseconds: " + diff + " trials: " + trials, verbose);
        inform("INIT-milliseconds PER trial: " + (diff / (double)trials), verbose);
    }


    public void testByNameMarshalViaWriter()
        throws Exception
    {
        com.mytest.MyClass mc = new com.mytest.MyClass();
        mc.setMyatt("attval");
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(99999.777f);
        myelt.setMyFloat(5555.4444f);
//        myelt.setMyClass(new com.mytest.MyClass());
//        myelt.setMyClass(new com.mytest.MySubSubClass());
        myelt.setMyClass(new com.mytest.MySubClass());
        mc.setMyelt(myelt);

        myelt.setModeEnum(ModeEnum.Off);
//        myelt.setQn(new QName("someURI", "somePart"));
        myelt.setQn(new QName("java:com.mytest2", "somePart"));
        final SimpleContentExample sce = new SimpleContentExample();
        sce.setFloatAttOne(5.43234f);
        sce.setSimpleContent("SIMPLE_CONTENT");
        myelt.setSimpleContentExample(sce);

        myelt.setStringArray(new String[]{"one", "two", "three"});

        myelt.setSimpleStringArray(myelt.getStringArray());

        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        StringWriter sw = new StringWriter();
        XMLStreamWriter w = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);


        Marshaller ctx =
            bindingContext.createMarshaller();
        Assert.assertNotNull(ctx);


        final QName elem_name = DFLT_ELEM_NAME;
        final QName type_name = MYCLASS_NAME;
        ctx.marshalType(w, mc,
                        elem_name,
                        type_name,
                        mc.getClass().getName(), options);
        w.close();
        sw.close();

        inform("=======IN-OBJ: " + mc);
        inform("=======OUT-XML:\n" + PrettyPrinter.indent(sw.getBuffer().toString()));
        reportErrors(errors, "byname-writer");
        Assert.assertTrue(errors.isEmpty());


        StringReader sr = new StringReader(sw.getBuffer().toString());
        XMLStreamReader rdr =
            XMLInputFactory.newInstance().createXMLStreamReader(sr);
        while (!rdr.isStartElement()) {
            rdr.next();
        }
        Unmarshaller umctx = bindingContext.createUnmarshaller();
        Object out_obj = umctx.unmarshalType(rdr, type_name, mc.getClass().getName(), options);
        reportErrors(errors, "byname-doc-writer");
        Assert.assertTrue(errors.isEmpty());
        if (!mc.equals(out_obj)) {
            inform("IN : " + mc, true);
            inform("OUT: " + out_obj, true);
        }
        Assert.assertEquals(mc, out_obj);
    }

    public void testByNameDocMarshalViaWriter()
        throws Exception
    {
        com.mytest.MyClass mc = new com.mytest.MyClass();
        mc.setMyatt("attval");
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(99999.777f);
        myelt.setMyFloat(5555.4444f);
//        myelt.setMyClass(new com.mytest.MyClass());
        myelt.setMyClass(null);
        mc.setMyelt(myelt);

        myelt.setStringArray(new String[]{"one", "two", "three"});


        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        StringWriter sw = new StringWriter();
        XMLStreamWriter w = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);
        Marshaller ctx =
            bindingContext.createMarshaller();
        Assert.assertNotNull(ctx);
        ctx.marshal(w, mc, options);

        //now unmarshall from String and compare objects...
        StringReader sr = new StringReader(sw.getBuffer().toString());
        XMLStreamReader rdr =
            XMLInputFactory.newInstance().createXMLStreamReader(sr);
        Unmarshaller umctx = bindingContext.createUnmarshaller();
        Object out_obj = umctx.unmarshal(rdr, options);
        reportErrors(errors, "byname-doc-writer");
        Assert.assertTrue(errors.isEmpty());
        Assert.assertEquals(mc, out_obj);

    }

    public void testByNameMarshalElementViaWriter()
        throws Exception
    {
        com.mytest.MyClass mc = new com.mytest.MyClass();
        mc.setMyatt("attval");
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(99999.777f);
        myelt.setMyFloat(5555.4444f);
//        myelt.setMyClass(new com.mytest.MyClass());
//        myelt.setMyClass(new com.mytest.MySubSubClass());
        mc.setMyelt(myelt);

        myelt.setStringArray(new String[]{"one", "two", "three"});


        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        StringWriter sw = new StringWriter();
        XMLStreamWriter w = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);
        Marshaller ctx =
            bindingContext.createMarshaller();
        Assert.assertNotNull(ctx);

        final QName elem_name = DFLT_ELEM_NAME;
        ctx.marshalElement(w,
                           mc,
                           elem_name,
                           mc.getClass().getName(),
                           options);

        //now unmarshall from String and compare objects...
        StringReader sr = new StringReader(sw.getBuffer().toString());
        XMLStreamReader rdr =
            XMLInputFactory.newInstance().createXMLStreamReader(sr);
        Unmarshaller umctx = bindingContext.createUnmarshaller();
        while (!rdr.isStartElement()) {
            rdr.next();
        }
        Object out_obj = umctx.unmarshalElement(rdr, elem_name,
                                                mc.getClass().getName(),
                                                options);
        reportErrors(errors, "marsh-elem");
        Assert.assertEquals(mc, out_obj);
        Assert.assertTrue(errors.isEmpty());
    }


    public void testByNameDocMarshalViaOutputStream()
        throws Exception
    {
        com.mytest.MyClass mc = new com.mytest.MyClass();
        mc.setMyatt("attval");
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(99999.777f);
        myelt.setMyFloat(5555.4444f);
//        myelt.setMyClass(new com.mytest.MyClass());
//        myelt.setMyClass(null);
//        myelt.setMyClass(new com.mytest.MySubSubClass());
        myelt.setMyClass(new com.mytest.MySubClass());

        mc.setMyelt(myelt);

        myelt.setStringArray(new String[]{"one", "two", "three"});

        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final String encoding = "UTF-16";

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);
        options.setCharacterEncoding(encoding);
        options.setSavePrettyPrint();
        options.setSavePrettyPrintIndent(2);
        options.setValidateOnSet();
        Marshaller ctx =
            bindingContext.createMarshaller();
        Assert.assertNotNull(ctx);

        ctx.marshal(baos, mc, options);
        baos.close();
        final byte[] buf = baos.toByteArray();
        inform("16Doc=" + new String(buf, encoding));

        //now unmarshall from String and compare objects...
        Unmarshaller umctx = bindingContext.createUnmarshaller();
        final ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        Object out_obj = umctx.unmarshal(bais, options);
        reportErrors(errors, "marsh-outstream");
        if (!mc.equals(out_obj)) {
            inform("\nIN : " + mc, true);
            inform("OUT: " + out_obj, true);
        }
        Assert.assertEquals(mc, out_obj);
        Assert.assertTrue(errors.isEmpty());
    }


    public void testByNameDocMarshalViaOutputStreamToFile()
        throws Exception
    {
        com.mytest.MyClass mc = new com.mytest.MyClass();
        mc.setMyatt("attval");
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(99999.777f);
        myelt.setMyFloat(5555.4444f);
//        myelt.setMyClass(new com.mytest.MyClass());
//        myelt.setMyClass(null);
//        myelt.setMyClass(new com.mytest.MySubSubClass());
        myelt.setMyClass(new com.mytest.MySubClass());

        mc.setMyelt(myelt);

        myelt.setStringArray(new String[]{"one", "two", "three"});

        myelt.objectArray = new Object[]{"hi", new Integer(5)};

        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());


        final File tmpfile = File.createTempFile("xbeans-marshal-test", ".xml");
        FileOutputStream fos = new FileOutputStream(tmpfile);


        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);
        options.setSavePrettyPrint();
        options.setSavePrettyPrintIndent(2);
        options.setValidateOnSet();
        Marshaller ctx =
            bindingContext.createMarshaller();
        Assert.assertNotNull(ctx);

//        ctx.marshal(fos, mc);
        ctx.marshal(fos, mc, options);
        fos.close();

        //now unmarshall from file and compare objects...
        Unmarshaller umctx = bindingContext.createUnmarshaller();
        final FileInputStream output = new FileInputStream(tmpfile);
        Object out_obj = umctx.unmarshal(output, options);
        reportErrors(errors, "marsh-outstream");
        if (!mc.equals(out_obj)) {
            inform("\nIN : " + mc, true);
            inform("OUT: " + out_obj, true);
        }
        Assert.assertEquals(mc, out_obj);
        Assert.assertTrue(errors.isEmpty());
        output.close();
    }


    public void testRoundtripPerf()
        throws Exception
    {
        //crank up these numbers to see real perf testing
        //the test still has some value aside from perf
        //in that it can test large stack depths.
        final int trials = 3;
//        final int trials = 10000;
        final int depth = 7;
        final int boolean_array_size = 5;

        Random rnd = new Random();

        com.mytest.MyClass top_obj = new com.mytest.MyClass();

        com.mytest.MyClass curr = top_obj;

        boolean[] bools = createRandomBooleanArray(rnd, boolean_array_size);
        SimpleContentExample sce = new SimpleContentExample();
        sce.setFloatAttOne(-4.234f);
        sce.setSimpleContent("simple simple simple");

        for (int i = 0; i < depth; i++) {
            com.mytest.YourClass myelt = new com.mytest.YourClass();
            myelt.setSimpleContentExample(sce);
            myelt.setAttrib(rnd.nextFloat());
            myelt.setMyFloat(rnd.nextFloat());
            myelt.setBooleanArray(bools);
            myelt.setWrappedArrayOne(new String[]{"W1" + rnd.nextInt(), null, "W2" + rnd.nextInt()});
            myelt.setWrappedArrayTwo(null);
            myelt.setModeEnum(ModeEnum.Off);
            myelt.setIntegerEnum(IntegerEnum.value2);
            myelt.setIntEnum(IntEnum.value3);
            final com.mytest.MyClass my_c = new com.mytest.MyClass();
            myelt.setMyClass(my_c);
            curr.setMyelt(myelt);
            curr.setMyatt("STR" + rnd.nextInt());
            curr = my_c;
        }

        inform("top_perf_obj = " + top_obj);

        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        final String javaType = "com.mytest.MyClass";
        final QName schemaType = MYCLASS_NAME;
        final QName elem_name = DFLT_ELEM_NAME;
        final String class_name = top_obj.getClass().getName();

        Object out_obj = null;
        final long before_millis = System.currentTimeMillis();
        final XmlOptions options = new XmlOptions();
        final LinkedList errors = new LinkedList();
        options.setErrorListener(errors);


        final Marshaller ctx =
            bindingContext.createMarshaller();
        final Unmarshaller umctx = bindingContext.createUnmarshaller();

        for (int i = 0; i < trials; i++) {
            errors.clear();

            Assert.assertNotNull(ctx);


            final XMLStreamReader reader =
                ctx.marshalType(top_obj, elem_name,
                                schemaType,
                                class_name, options);


//            //DEBUG!!!
//            if (System.currentTimeMillis() > 1) {
//                dumpReader(reader);
//                return;
//            }

            out_obj = umctx.unmarshalType(reader, schemaType, javaType, options);
        }
        final long after_millis = System.currentTimeMillis();
        final long diff = (after_millis - before_millis);
        inform(" perf_out_obj = " + top_obj);
        reportErrors(errors, "perf");
        Assert.assertTrue(errors.isEmpty());
        Assert.assertEquals(top_obj, out_obj);
        inform("milliseconds: " + diff + " trials: " + trials);
        inform("milliseconds PER trial: " + (diff / (double)trials));
    }


    public void testThreadedRoundtripPerf()
        throws Exception
    {
        //crank up these numbers to see real perf testing
        //the test still has some value aside from perf
        //in that it can test large stack depths.
        final int trials = getTrials(30);
        final int depth = 12;
        final int thread_cnt = 3;
        final int boolean_array_size = 30;

        Random rnd = new Random();

        com.mytest.MyClass top_obj = new com.mytest.MyClass();

        com.mytest.MyClass curr = top_obj;

        boolean[] bools = createRandomBooleanArray(rnd, boolean_array_size);

        for (int i = 0; i < depth; i++) {
            com.mytest.YourClass myelt = new com.mytest.YourClass();
            myelt.setAttrib(rnd.nextFloat());
            myelt.setMyFloat(rnd.nextFloat());
            myelt.setBooleanArray(bools);
            final com.mytest.MyClass my_c = new com.mytest.MyClass();
            myelt.setMyClass(my_c);
            curr.setMyelt(myelt);
            curr.setMyatt("STR" + rnd.nextInt());
            curr = my_c;
        }

        //inform("top_obj = " + top_obj);

        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        final String javaType = "com.mytest.MyClass";
        final QName schemaType = MYCLASS_NAME;
        final QName elem_name = DFLT_ELEM_NAME;
        final String class_name = top_obj.getClass().getName();

        final Marshaller msh = bindingContext.createMarshaller();
        Assert.assertNotNull(msh);
        final Unmarshaller umsh = bindingContext.createUnmarshaller();
        Assert.assertNotNull(umsh);

        Object out_obj = null;
        final XmlOptions options = new XmlOptions();
        final LinkedList errors = new LinkedList();
        options.setErrorListener(errors);

        final long before_millis = System.currentTimeMillis();

        RoundTripRunner[] runners = new RoundTripRunner[thread_cnt];
        for (int i = 0; i < thread_cnt; i++) {
            runners[i] = new RoundTripRunner(top_obj, msh, umsh, elem_name,
                                             schemaType, class_name, javaType, options, trials);
        }

        inform("starting " + thread_cnt + " threads...");

        for (int i = 0; i < thread_cnt; i++) {
            runners[i].start();
        }

        inform("trials=" + trials + "\tjoining " + thread_cnt + " threads...");

        for (int i = 0; i < thread_cnt; i++) {
            runners[i].join();
        }

        inform("joined " + thread_cnt + " threads.");


        final long after_millis = System.currentTimeMillis();
        final long diff = (after_millis - before_millis);
//        inform(" perf_out_obj = " + top_obj);


        reportErrors(errors, "thread-perf");
        Assert.assertTrue(errors.isEmpty());
        //Assert.assertEquals(top_obj, out_obj);
        inform("milliseconds: " + diff + " trials: " + trials +
               " threads=" + thread_cnt);
        inform("milliseconds PER trial: " + (diff / (double)trials));
        inform("milliseconds PER roundtrip: " + (diff / ((double)trials * thread_cnt)));
    }

    private static int getTrials(int default_val)
    {
        String prop = "drtcases.MarshalTests.trials";
        String val = System.getProperty(prop);
        if (val == null) return default_val;

        int t = Integer.parseInt(val);
        assert t > 0;
        return t;
    }

    private static Object doRoundTrip(MyClass top_obj,
                                      final Marshaller msh,
                                      final Unmarshaller umsh,
                                      final QName elem_name,
                                      final QName schemaType,
                                      final String class_name,
                                      final String javaType,
                                      final XmlOptions options)
        throws XmlException
    {
        Object out_obj;
        final XMLStreamReader reader =
            msh.marshalType(top_obj, elem_name,
                            schemaType,
                            class_name, options);

        out_obj = umsh.unmarshalType(reader, schemaType, javaType);
        return out_obj;
    }

    private static class RoundTripRunner extends Thread
    {
        private final MyClass top_obj;
        private final Marshaller msh;
        private final Unmarshaller umsh;
        private final QName elem_name;
        private final QName schemaType;
        private final String class_name;
        private final String javaType;
        private final XmlOptions options;
        private final int trials;


        public RoundTripRunner(MyClass top_obj,
                               Marshaller msh,
                               Unmarshaller umsh,
                               QName elem_name,
                               QName schemaType,
                               String class_name,
                               String javaType,
                               XmlOptions options,
                               int trials)
        {
            this.top_obj = top_obj;
            this.msh = msh;
            this.umsh = umsh;
            this.elem_name = elem_name;
            this.schemaType = schemaType;
            this.class_name = class_name;
            this.javaType = javaType;
            this.options = options;
            this.trials = trials;
        }

        public void run()
        {
            final int t = trials;
            try {
                Object out_obj = null;
                for (int i = 0; i < t; i++) {
                    out_obj = doRoundTrip(top_obj, msh,
                                          umsh, elem_name,
                                          schemaType, class_name,
                                          javaType, options);
                }
                Assert.assertEquals(top_obj, out_obj);
            }
            catch (XmlException xe) {
                throw new AssertionError(xe);
            }
        }
    }

    private boolean[] createRandomBooleanArray(Random rnd, int size)
    {
        boolean[] a = new boolean[size];
        for (int i = 0; i < size; i++) {
            a[i] = rnd.nextBoolean();
        }
        return a;
    }


    public void testJavaToSchemaToJava()
        throws Exception
    {
        Random rnd = new Random();

        com.mytest.MyClass top_obj = new com.mytest.MyClass();
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(rnd.nextFloat());
        myelt.setMyFloat(rnd.nextFloat());
        final com.mytest.MyClass my_c = new com.mytest.MyClass();
//        myelt.setMyClass(my_c);
        myelt.setMyClass(null);
        top_obj.setMyelt(myelt);
//        curr.setMyatt("STR" + rnd.nextInt());
        top_obj.setMyatt(null);
//        top_obj.setMyatt("someVALUE");


        inform("top_obj = " + top_obj);

        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());


        //TODO: remove hard coded values
        final String javaType = "com.mytest.MyClass";
        final QName schemaType = MYCLASS_NAME;
        final QName elem_name = DFLT_ELEM_NAME;
        final String class_name = top_obj.getClass().getName();

        Object out_obj = null;

        final XmlOptions options = new XmlOptions();
        final ArrayList errors = new ArrayList();
        options.setErrorListener(errors);

        Marshaller ctx =
            bindingContext.createMarshaller();
        Assert.assertNotNull(ctx);


        final XMLStreamReader reader =
            ctx.marshalType(top_obj, elem_name,
                            schemaType,
                            class_name, options);

        Unmarshaller umctx =
            bindingContext.createUnmarshaller();
        out_obj = umctx.unmarshalType(reader, schemaType, javaType, options);

        inform(" out_obj = " + top_obj);
        reportErrors(errors, "j2s2j");
        Assert.assertEquals(top_obj, out_obj);
        Assert.assertTrue(errors.isEmpty());
    }

    private static void dumpReader(final XMLStreamReader reader)
        throws XMLStreamException, XmlException, IOException
    {
        dumpReader(reader, VERBOSE);
    }

    private static void dumpReader(final XMLStreamReader reader, boolean verbose)
        throws XMLStreamException, XmlException, IOException
    {
        final boolean write_doc = true;
        if (write_doc) {
            StringWriter sw = new StringWriter();

            XMLStreamWriter xsw =
                XMLOutputFactory.newInstance().createXMLStreamWriter(sw);


            XmlReaderToWriter.writeAll(reader, xsw);

            xsw.close();

            if (verbose) {
                final String xmldoc = sw.getBuffer().toString();
                inform("DOC:", verbose);
                inform(PrettyPrinter.indent(xmldoc), verbose);
            }
        } else {
            int i = 0;
            if (verbose)
                inform((i++) + "\tSTATE: " +
                       XmlStreamUtils.printEvent(reader), verbose);
            while (reader.hasNext()) {
                final int state = reader.next();
                if (verbose)
                    inform((i++) + "\tSTATE: " +
                           XmlStreamUtils.printEvent(reader), verbose);
            }
        }
    }

    public void testByNameBeanUnmarshal()
        throws Exception
    {
        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        File doc = TestEnv.xbeanCase("marshal/doc2.xml");

        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xrdr =
            xmlInputFactory.createXMLStreamReader(doc.toURL().toString(),
                                                  new FileInputStream(doc));


        final XmlOptions options = new XmlOptions();
        ObjectFactory of = new YourClass();
        options.setUnmarshalInitialObjectFactory(of);
        final LinkedList errors = new LinkedList();
        options.setErrorListener(errors);

        Unmarshaller um_ctx =
            bindingContext.createUnmarshaller();
        Object obj = um_ctx.unmarshal(xrdr, options);

        inform("doc2-obj = " + obj);
        reportErrors(errors, "byname-um");
        Assert.assertTrue(errors.isEmpty());

    }

    public void testByNameBeanUnmarshalErrors()
        throws Exception
    {
        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        File doc = TestEnv.xbeanCase("marshal/doc3.xml");

        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xrdr =
            xmlInputFactory.createXMLStreamReader(new FileReader(doc));

        final XmlOptions options = new XmlOptions();
        final LinkedList errors = new LinkedList();
        options.setErrorListener(errors);

        Unmarshaller um_ctx =
            bindingContext.createUnmarshaller();
        Object obj = um_ctx.unmarshal(xrdr, options);

        //even with some errors, we should get an object
        Assert.assertTrue(obj != null);

        inform("doc3-obj = " + obj);

        for (Iterator itr = errors.iterator(); itr.hasNext();) {
            XmlError xmlError = (XmlError)itr.next();
            inform("doc3-ERROR: " + xmlError);
        }

        Assert.assertTrue(errors.size() > 0);

    }

    public void testByNameBeanUnmarshalFromInputStream()
        throws Exception
    {
        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        File doc = TestEnv.xbeanCase("marshal/doc2.xml");

        final XmlOptions options = new XmlOptions();
        final LinkedList errors = new LinkedList();
        options.setErrorListener(errors);

        Unmarshaller um_ctx =
            bindingContext.createUnmarshaller();
        Object obj = um_ctx.unmarshal(new FileInputStream(doc), options);

        inform("doc2-obj = " + obj);
        reportErrors(errors, "doc2-err");
        Assert.assertTrue(errors.isEmpty());

    }


    public void testByNameBeanUnmarshalType()
        throws Exception
    {
        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        final File doc = TestEnv.xbeanCase("marshal/doc.xml");
        final String javaType = "com.mytest.MyClass";
        final QName schemaType = MYCLASS_NAME;

        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xrdr =
            xmlInputFactory.createXMLStreamReader(new FileReader(doc));

        final XmlOptions xmlOptions = new XmlOptions();
        Collection errors = new LinkedList();
        xmlOptions.setErrorListener(errors);

        Unmarshaller ctx = bindingContext.createUnmarshaller();

        //this is not very safe but it should work...
        while (!xrdr.isStartElement()) {
            xrdr.next();
        }

        Object obj = ctx.unmarshalType(xrdr, schemaType, javaType, xmlOptions);
        for (Iterator itr = errors.iterator(); itr.hasNext();) {
            inform("ERROR: " + itr.next());
        }
        inform("+++++TYPE obj = " + obj);

        MyClass mc = (MyClass)obj;
        MySubClass first = (MySubClass)mc.getMyelt().getMyClassArray()[0];
        Assert.assertEquals(DEFAULT_BIG_INT, first.getBigInt());
        reportErrors(errors, "dco-err");
        Assert.assertTrue(errors.isEmpty());
    }

    public void testPerfByNameBeanUnmarshall()
        throws Exception
    {
        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        //File doc = TestEnv.xbeanCase("marshal/doc2.xml");
        File doc = TestEnv.xbeanCase("marshal/bigdoc.xml");
        final FileReader fileReader = new FileReader(doc);
        CharArrayWriter cw = new CharArrayWriter();

        bufferedStreamCopy(fileReader, cw);
        final char[] chars = cw.toCharArray();
        final CharArrayReader cr = new CharArrayReader(chars);

        final int trials = 5;

        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        final XmlOptions xmlOptions = new XmlOptions();

        final long before_millis = System.currentTimeMillis();
        for (int i = 0; i < trials; i++) {
            cr.reset();
            XMLStreamReader xrdr =
                xmlInputFactory.createXMLStreamReader(cr);
            Unmarshaller umctx =
                bindingContext.createUnmarshaller();

            Object obj = umctx.unmarshal(xrdr, xmlOptions);

            if ((i % 1000) == 0) {
                String s = obj.toString().substring(0, 70);
                inform("i=" + i + "\tobj = " + s + "...");
            }
        }
        final long after_millis = System.currentTimeMillis();
        final long diff = (after_millis - before_millis);
        inform("milliseconds: " + diff + " trials: " + trials);
        inform("milliseconds PER trial: " + (diff / (double)trials));
    }

    public void testUnmarshalValidation()
        throws Exception
    {
        final File schema = TestEnv.xbeanCase("marshal/example.xsd");
        final File instance = TestEnv.xbeanCase("marshal/example_instance.xml");

        Assert.assertTrue(schema.exists());
        Assert.assertTrue(instance.exists());

        final SchemaDocument xsd_obj =
            (SchemaDocument)XmlObject.Factory.parse(schema);
        final XmlObject[] schemas = new XmlObject[]{xsd_obj};
        final XmlOptions opts = new XmlOptions();
        opts.setCompileDownloadUrls();
        SchemaTypeSystem sts = XmlBeans.compileXsd(schemas,
                                                   XmlBeans.getBuiltinTypeSystem(),
                                                   opts);
        Schema2Java s2j = new Schema2Java(sts);

        s2j.includeSchema(xsd_obj, schema.getName());
        final File tmpfile = File.createTempFile("marshalTests", "-tylar");
        if (!tmpfile.delete()) {
            throw new AssertionError("delete failed on " + tmpfile);
        }
        final boolean ok = tmpfile.mkdirs();
        Assert.assertTrue("mkdir" + tmpfile + " failed", ok);

        s2j.bindAsExplodedTylar(tmpfile);


        final URL tylar_url = tmpfile.toURL();

        //add tylar to classpath so we can load classes out of it
        final Thread thread = Thread.currentThread();
        final ClassLoader curr_cl = thread.getContextClassLoader();
        final URLClassLoader cl =
            new URLClassLoader(new URL[]{tylar_url}, curr_cl);
        thread.setContextClassLoader(cl);

        try {
            final BindingContextFactory bcf = BindingContextFactory.newInstance();
            final BindingContext binding_context =
                bcf.createBindingContext(cl);
            final Unmarshaller um = binding_context.createUnmarshaller();
            InputStream is = new FileInputStream(instance);
            XmlOptions opts_validation_on = new XmlOptions();
            opts_validation_on.setUnmarshalValidate();
            final List errors = new ArrayList();
            opts_validation_on.setErrorListener(errors);
            final Object obj = um.unmarshal(is, opts_validation_on);
            Assert.assertNotNull(obj);
            inform("address=" + obj);
            is.close();

            reportErrors(errors);
            Assert.assertTrue(errors.isEmpty());

            //now try unmarshalType...
            final FileInputStream fis = new FileInputStream(instance);
            final XMLStreamReader rdr =
                XMLInputFactory.newInstance().createXMLStreamReader(fis);
            QName schema_type = new QName("http://nosuch.domain.name", "USAddress");
            String java_type = obj.getClass().getName();

            //not super robust but this should work for valid xml
            while (!rdr.isStartElement()) {
                rdr.next();
            }

            um.unmarshalType(rdr, schema_type, java_type, opts_validation_on);
            rdr.close();
            fis.close();

            reportErrors(errors);
            Assert.assertTrue(errors.isEmpty());


            // -- this is currently broken --
            //now lets try validating our stream over objects
            final Marshaller marshaller = binding_context.createMarshaller();
            final XmlOptions empty_opts = new XmlOptions();
            final XMLStreamReader obj_rdr =
                marshaller.marshal(obj, empty_opts);
            inform("VALIDATION-OBJ: " + obj);

            final Object obj2 = um.unmarshal(obj_rdr, opts_validation_on);
            inform("obj2=" + obj2);
            obj_rdr.close();
            reportErrors(errors);

            //TODO: fix this use case
            //Assert.assertTrue(errors.isEmpty());

            // depends on reasonable equals methods which we do not have yet
            //Assert.assertEquals(obj, obj2);

        }
        finally {
            thread.setContextClassLoader(curr_cl);
        }
    }

    private static void reportErrors(List errors)
    {
        if (!errors.isEmpty()) {
            for (Iterator itr = errors.iterator(); itr.hasNext();) {
                Object err = itr.next();
                inform("validation-error: " + err);
            }
        }
    }

    protected static void bufferedStreamCopy(Reader in, Writer out)
        throws IOException
    {
        int charsRead;
        char[] buf = new char[1024];

        while ((charsRead = in.read(buf)) != -1) {
            out.write(buf, 0, charsRead);
        }
    }

    private File getBindingConfigDocument()
    {
        File loc = TestEnv.xbeanCase("marshal/example_config.xml");
        return loc;
    }

    public static void main(String args[])
    {
        junit.textui.TestRunner.run(suite());
    }

    private static BindingContext getBuiltinBindingContext()
        throws XmlException, IOException
    {
        return BindingContextFactory.newInstance().createBindingContext();
    }

    private static BindingContext getBindingContext(File bcdoc)
        throws XmlException, IOException
    {
        return ((BindingContextFactoryImpl)BindingContextFactory.newInstance()).
            createBindingContextFromConfig(bcdoc);
    }

    private static void inform(String msg)
    {
        inform(msg, VERBOSE);
    }

    private static void inform(String msg, boolean verbose)
    {
        if (verbose) System.out.println(msg);
    }

    private static void say(String msg)
    {
        System.out.println(msg);
    }

    private static void error(String msg)
    {
        System.out.println(msg);
    }
}
