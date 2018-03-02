
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
package ValidatingXSRTests.detailed;

import org.apache.xmlbeans.impl.validator.ValidatingXMLStreamReader;
import org.apache.xmlbeans.*;
import junit.framework.Assert;
import junit.framework.TestCase;
import tools.util.JarUtil;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

// Schema Imports
import org.openuri.test.numerals.*;
import org.openuri.test.location.*;
import org.openuri.test.person.*;
import org.openuri.test.mixedContent.LetterDocument;
import org.openuri.test.mixedContent.NoMixedDocument;
import com.foo.sample.HeadingDocument;


public class ValidatingXmlStreamReaderTests
        extends TestCase
{

    public ValidatingXmlStreamReaderTests(String name)
    {
        super(name);
    }

    // Base variable
    static String casesLoc = "xbean/ValidatingStream/";

    //////////////////////////////////////////////////////////////////////
    // Tests

    // NOTE: Tests that use getCasesFile are reading files
    //       from cases/qatest/files/xbean
    //       Tests that use getResourceFromJar are getting the contents of
    //       the file in the same location, but packaged into xmlcases.jar
    //       SO, any change to the xml files for these tests will not be
    //       reflected till they make it into xmlcases.jar. (ant build.xmlcases)

    public void testDocWithNoSchema()
        throws Exception
    {
        checkDocIsInvalid(getCasesFile(casesLoc + "po.xml"),
                          null);
    }

    public void testValidLocationDoc()
        throws Exception
    {
        checkDocIsValid(getCasesFile(casesLoc + "location.xml"),
                        null);
    }

     public void testInvalidLocationDoc()
        throws Exception
    {
        checkDocIsInvalid(getCasesFile(casesLoc + "location-inv.xml"),
                          LocationDocument.type);
    }

    public void testValidPersonDoc()
        throws Exception
    {
        checkDocIsValid(getCasesFile(casesLoc + "person.xml"),
                        PersonDocument.type);
    }

    public void testInvalidPersonDoc()
        throws Exception
    {
        checkDocIsInvalid(getCasesFile(casesLoc + "person-inv.xml"),
                          PersonDocument.type);
    }


    public void testValidMixedContentDoc()
        throws Exception
    {
        checkDocIsValid(getCasesFile(casesLoc + "mixed-content.xml"),
                        LetterDocument.type);
    }

    public void testInvalidNomixedContentDoc()
        throws Exception
    {
        checkDocIsInvalid(getCasesFile(casesLoc + "nomixed-content-inv.xml"),
                          NoMixedDocument.type);
    }

    public void testInvalidMissingAttributeDoc()
        throws Exception
    {
        checkDocIsInvalid(getCasesFile(casesLoc + "foo-inv.xml"),
                          HeadingDocument.type);
    }


    public void testContentName()
        throws Exception
    {
        String sXml = JarUtil.getResourceFromJar(casesLoc + "person-frag.xml");
        SchemaType type = Name.type;

        assertTrue("Xml-fragment is not valid:\n" + sXml,
                   checkContent(sXml, type, true));
    }


    // Same as testContentName.. expect the xml has no chars before the first
    // start element
    public void testContentName2()
        throws Exception
    {
        String sXml = JarUtil.getResourceFromJar(casesLoc + "person-frag2.xml");
        SchemaType type = Name.type;

        assertTrue("Xml-fragment is not valid:\n" + sXml,
                   checkContent(sXml, type, true));
    }

    public void testContentSibling()
        throws Exception
    {
        String sXml = JarUtil.getResourceFromJar(casesLoc + "person-sibling.xml");
        SchemaType type = PersonType.type;
        assertTrue("Xml-fragment is not valid:\n" + sXml,
                   checkContent(sXml, type, true));
    }

    public void testInvalidContentSibling()
        throws Exception
    {
        String sXml = JarUtil.getResourceFromJar(casesLoc + "person-sibling-inv.xml");
        SchemaType type = PersonType.type;
        assertTrue("Invalid Xml-fragment is getting validated:\n" + sXml,
                   !checkContent(sXml, type, true));
    }

    public void testValidXsiType()
        throws Exception
    {
        String sXml = JarUtil.getResourceFromJar(casesLoc + "person-justname.xml");
        SchemaType type = Name.type;
        assertTrue("Xml-fragment is not valid:\n" + sXml,
                   checkContent(sXml, type, true));
    }

    public void testInvalidXsiType()
        throws Exception
    {
        String sXml = JarUtil.getResourceFromJar(casesLoc + "person-justname-inv.xml");
        SchemaType type = Name.type;
        assertTrue("Invalid Xml-fragment is getting validated:\n" + sXml,
                   !checkContent(sXml, type, true));
    }

    public void testIncompatibleXsiType()
        throws Exception
    {
        String sXml = JarUtil.getResourceFromJar(casesLoc + "person-xsi-inv.xml");
        SchemaType type = Name.type;
        assertTrue("Invalid Xml-fragment is getting validated:\n" + sXml,
                   !checkContent(sXml, type, true));
    }

    public void testValidMixedContent()
        throws Exception
    {
        String sXml = JarUtil.getResourceFromJar(casesLoc + "mixed-content.xml");
        SchemaType type = org.openuri.test.mixedContent.LetterType.type;
        assertTrue("Xml-fragment is not valid:\n" + sXml,
                   checkContent(sXml, type, true));
    }
    /*
    public void testGlobalAttribute()
        throws Exception
    {
        String sXml = JarUtil.getResourceFromJar("xmlcases.jar",
                                                 casesLoc + "global-attr.xml");

        assertTrue("Global Attribute test failed:\n",
                   checkContent(sXml, null, true));

    }
    */
    // Tests for increasing code-coverage metrics
    public void testValXsrReuse()
        throws Exception
    {
        Collection errors = new ArrayList();
        File[] xmls = new File[2];
        xmls[0] = getCasesFile(casesLoc + "person.xml");
        xmls[1] = getCasesFile(casesLoc + "person-inv.xml");
        SchemaType type = PersonDocument.type;

        boolean[] ret = runValidator(xmls, type, errors);

        String common = "Test for ValidatingXmlStreamReader reuse failed";
        assertTrue(common + "\nReturn value has more than 2 elements",
                   ret.length == 2);
        assertTrue(common + "\nExpected: true & false. Actual: "
                                                + ret[0] + " & " + ret[1],
                   ret[0] && !ret[1]);
    }


    public void testIllegalEvent()
        throws Exception
    {
        // Will require writing another XSR wrapper.. albeit simple
    }

    /*/
    public void testWalk()
        throws Exception
    {
        walkXml(getCasesFile(casesLoc + "global-attr.xml"));
        System.out.println();
        walkXml(getCasesFile(casesLoc + "person-sibling.xml"));
    }
    // */
    //////////////////////////////////////////////////////////////////////
    // Utility Methods
    private void walkXml(File xml)
        throws Exception
    {
        XMLStreamReader xr = XMLInputFactory.newInstance().
                              createXMLStreamReader(new FileInputStream(xml));

        //xsr.nextTag();
        XmlContentTestXSR xsr = new XmlContentTestXSR(xr);

        while(xsr.hasNext())
        {
            int type = xsr.next();
            System.out.print(type);
            //*
            if (type == XMLEvent.START_ELEMENT)
            {
                System.out.print("\n" + xsr.getLocalName() + " ");
            }
            if (type == XMLEvent.END_ELEMENT)
            {
                System.out.println("/" + xsr.getLocalName());
            }
            if (type == XMLEvent.CHARACTERS)
            {
                char[] arr = xsr.getTextCharacters();
                String str = new String(arr);
                System.out.print("Char:" + str + " ");
            }
            //*/
        }
    }

    private boolean runValidator(File xml,
                                 SchemaType type,
                                 Collection errors)
            throws IllegalArgumentException, Exception
    {
        if (errors == null)
            throw new IllegalArgumentException(
                    "Collection object cannot be null");

        XMLStreamReader xsr = XMLInputFactory.newInstance().
                              createXMLStreamReader(new FileInputStream(xml));

        ValidatingXMLStreamReader valXsr = new ValidatingXMLStreamReader();
        valXsr.init(xsr,
                    false,
                    type,
                    XmlBeans.typeLoaderForClassLoader(ValidatingXMLStreamReader.class.getClassLoader()),
                    null,
                    errors);

        // Walk through the xml
        while (valXsr.hasNext())
            valXsr.next();

        return valXsr.isValid();
        //return true;
    }

    // This method is primarily for testing re-use of the ValXSR object.
    // but could come in handy later..
    private boolean[] runValidator(File[] xml,
                                 SchemaType type,
                                 Collection errors)
            throws IllegalArgumentException, Exception
    {
        if (errors == null)
            throw new IllegalArgumentException(
                    "Collection object cannot be null");
        ValidatingXMLStreamReader valXsr = new ValidatingXMLStreamReader();
        boolean[] retArray = new boolean[xml.length];

        for (int i = 0; i < xml.length; i++)
        {
            XMLStreamReader xsr = XMLInputFactory.newInstance().
                                  createXMLStreamReader(new FileInputStream(xml[i]));

            valXsr.init(xsr,
                        false,
                        type,
                        XmlBeans.typeLoaderForClassLoader(ValidatingXMLStreamReader.class.getClassLoader()),
                        null,
                        errors);

            // Walk through the xml
            while (valXsr.hasNext())
                valXsr.next();

            retArray[i] = valXsr.isValid();
        }

        return retArray;
    }

    protected void checkDocIsValid(File file, SchemaType type)
            throws Exception
    {
        Collection errors = new ArrayList();
        boolean isValid = runValidator(file, type, errors);

        tools.xml.Utils.printXMLErrors(errors);
        Assert.assertTrue("File '" + file.getName() + "' is invalid.", isValid);
    }


    protected void checkDocIsInvalid(File file, SchemaType type)
            throws Exception
    {
        Collection errors = new ArrayList();

        boolean isValid = runValidator(file, type, errors);
        Assert.assertTrue("File '" + file.getName() + "' is valid, but was expecting invalid.",
                          !isValid);
    }


    public boolean checkContent(String fragment,
                                 SchemaType type,
                                 boolean printErrors)
            throws Exception
    {
        XMLStreamReader xsr = XMLInputFactory.newInstance().
                              createXMLStreamReader(new StringReader(fragment));

        XmlContentTestXSR cxsr = new XmlContentTestXSR(xsr);
        Collection errors = new ArrayList();

        ValidatingXMLStreamReader valXsr = new ValidatingXMLStreamReader();
        valXsr.init(cxsr,
                    false,
                    type,
                    XmlBeans.typeLoaderForClassLoader(ValidatingXMLStreamReader.class.getClassLoader()),
                    null,
                    errors);

        // Walk through the xml
        while (valXsr.hasNext())
            valXsr.next();

        if (!valXsr.isValid())
        {
            if (printErrors)
                tools.xml.Utils.printXMLErrors(errors);
            return false;
        }
        return true;
    }


    private static File getCasesFile(String path)
        throws java.io.IOException
    {
        if (path.length()==0)
            throw new IOException("getCasesFile was called with path of len 0");
        return JarUtil.getResourceFromJarasFile(path);
        //return new File(casesRoot + path);
    }

    /////////////////////////////////////////////////////////////////////////
    // XmlStreamReader extension for content Validation
    //     will not work for Global Attribute
    public class XmlContentTestXSR
            extends StreamReaderDelegate
            implements XMLStreamReader
    {
        private static final int TAGOPEN    = 100;
        private static final int TAGCLOSE   = 101;
        private static final int UNDEFINED  =  99;
        private static final int ATTRIBUTE  = 102;
        private static final int ENDCONTENT = 103;

        int state = -1;
        int depth = -1;
        boolean initialized = false;
        int attributeCount = -1;
        boolean hasAttributes = false;

        // Constructor Wrappers
        public XmlContentTestXSR(XMLStreamReader xsr)
            throws XMLStreamException
        {
            super(xsr);
        }


        public boolean hasNext()
        {
            if (state == UNDEFINED || state == ENDCONTENT)
                return false;

            if (!initialized)       // next() has not been called yet
                return true;



            return true;
        }

        public int next()
            throws XMLStreamException
        {
            int _next;
            if (!initialized)
            {
                // First time next() is called..
                // Scan for the first XMLEvent.START_ELEMENT
                _next = UNDEFINED;
                while ((super.hasNext()) && (_next != XMLEvent.START_ELEMENT))
                     _next = super.next();

                if (_next != XMLEvent.START_ELEMENT)
                    throw new XMLStreamException(
                                       "Could not find a start element");
                initialized = true;

                // Now move past the first tag
                state = TAGOPEN;
                depth = 1;

                if ((attributeCount = super.getAttributeCount()) > 0)
                {
                    // The first element has attributes.. this is part of
                    // the content. So the first event should XMLEvent.ATTRIBUTE
                    _next = XMLEvent.ATTRIBUTE;
                }
                else
                {
                    // return super.next();
                    /*
                    If content is <xml-fragment/> then we will have returned
                    END_ELEMENT above, without ever generating a START_ELEMENT
                    In this case probably we should detect this and return a
                    END_DOCUMENT
                    */
                    _next = super.next();
                    if (_next == XMLEvent.END_ELEMENT)
                    {
                        _next = XMLEvent.END_DOCUMENT;
                        state = ENDCONTENT;
                    }
                }
                return _next;
            }

            _next = super.next();
            switch (_next)
            {
                case XMLEvent.START_ELEMENT:
                    state = TAGOPEN;
                    depth++;
                    break;

                case XMLEvent.END_ELEMENT:
                    --depth;
                    if (depth < 0 && state == TAGOPEN)
                    {
                        throw new XMLStreamException(
                                "Illegal XML Stream state");
                    }
                    else if (depth == 0 && state == TAGOPEN)
                    {
                        state = ENDCONTENT;
                        // at this point we will return ENDDOCUMENT
                        _next = XMLEvent.END_DOCUMENT;
                    }
                    break;
            }

            return _next;
        }

    }


}
