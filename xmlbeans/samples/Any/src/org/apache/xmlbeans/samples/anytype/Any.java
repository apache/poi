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

package org.apache.xmlbeans.samples.anytype;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.samples.any.ListOfStrings;
import org.apache.xmlbeans.samples.any.RootDocument;
import org.apache.xmlbeans.samples.any.StringelementDocument;
import org.apache.xmlbeans.samples.any.RootDocument.Root.Arrayofany;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;

/**
 * A sample that illustrates various ways to manipulate XML whose
 * schema defines elements as type xs:any. Unlike its treatment of
 * other schema types, XMLBeans does not generate accessors for the 
 * xs:any particle when compiling schema. Instead, your code 
 * handles instances of this type through any of several alternative 
 * means, including XPath queries, the selectChildren method, 
 * XmlCursor instances and the DOM API. This samples illustrates 
 * these alternative approaches.
 */
public class Any
{
    private static final String m_namespaceUri = "http://xmlbeans.apache.org/samples/any";

    /**
     * Receives <root> XML instance, executing methods that 
     * edit the received instance or create a new one.
     * 
     * @param args An array in which the first item is a
     * path to the XML instance file.
     */
    public static void main(String[] args)
    {
        Any thisSample = new Any();
        System.out.println("Running Any.buildDocFromScratch\n");
        thisSample.buildDocFromScratch();
        
        RootDocument rootDoc = (RootDocument)thisSample.parseXml(args[0]);

        System.out.println("Running Any.editExistingDocWithSelectChildren\n");
        thisSample.editExistingDocWithSelectChildren(rootDoc);

        System.out.println("Running Any.editExistingDocWithDOM\n");
        thisSample.editExistingDocWithDOM(rootDoc);

        System.out.println("Running Any.editExistingDocWithSelectPath\n");
        thisSample.editExistingDocWithSelectPath(rootDoc);
    }

    /**
     * Creates a new <root> document from scratch.
     * 
     * This method illustrates how you can use XmlCursor instances
     * to build XML that is defined in schema as xs:any.
     * 
     * @return <code>true</code> if the new document is valid;
     * otherwise, <code>false</code>.
     */
    public boolean buildDocFromScratch()
    {
        // Start by creating a <root> element that will contain
        // the children built by this method.
        RootDocument rootDoc = RootDocument.Factory.newInstance();
        RootDocument.Root root = rootDoc.addNewRoot();
        
        // Add the first element, <stringelement>.
        root.setStringelement("some text");

        // Create an XmlObject in which to build the second
        // element in the sequence, <anyfoo>. Here, the 
        // XmlObject instance is simply a kind of incubator
        // for the XML. Later the XML will be moved into the
        // document this code is building.
        XmlObject anyFoo = XmlObject.Factory.newInstance();

        // Add a cursor to do the work of building the XML.
        XmlCursor childCursor = anyFoo.newCursor();
        childCursor.toNextToken();

        // Add the element in the schema's namespace, then add
        // element content.
        childCursor.beginElement(new QName(m_namespaceUri, "anyfoo"));
        childCursor.insertChars("some text");

        // Move the cursor back to the new element's top, where 
        // it can grab the element's XML.
        childCursor.toStartDoc();
        childCursor.toNextToken();

        // Move the XML into the <root> document by moving it
        // from a position at one cursor to a position at
        // another.
        XmlCursor rootCursor = root.newCursor();
        rootCursor.toEndToken();
        childCursor.moveXml(rootCursor);

        // Add the fourth element, <arrayofany>, by building it
        // elsewhere, then moving the new XML into place under
        // <root>.
        Arrayofany arrayOfAny = root.addNewArrayofany();
        if (buildArrayOfAny(arrayOfAny) == null)
        {
            return false;
        }

        childCursor.dispose();
        rootCursor.dispose();

        // Print and validate the result.
        System.out.println("Output: The <root> document built from scratch.\n");
        System.out.println(rootDoc + "\n");
        return validateXml(rootDoc);
    }
    
    /**
     * Replaces the <anyfoo> element with an <anybar> element in the
     * incoming XML.
     * 
     * This method illustrates how you can use the XmlCursor.selectChildren
     * method to retrieve child elements whose type is defined as
     * xs:any in schema.
     * 
     * @param rootDoc An instance of the <root> XML document.
     * @return <code>true</code> if the editing XML is valid; 
     * otherwise, <code>false</code>.
     */
    public boolean editExistingDocWithSelectChildren(RootDocument rootDoc)
    {
        RootDocument.Root root = rootDoc.getRoot();
        
        // Select the <anyfoo> children of <root>.
        XmlObject[] stringElements =
            root.selectChildren(new QName(m_namespaceUri, "anyfoo"));

        // If the element is there, replace it with another element.
        if (stringElements.length > 0)
        {
            XmlCursor editCursor = stringElements[0].newCursor();
            editCursor.removeXml();
            editCursor.beginElement(new QName(m_namespaceUri, "anybar"));
            editCursor.insertChars("some other text");                
            editCursor.dispose();
        }
        System.out.println("Output: The <anyfoo> element has been replaced\n" +
        		"by an <anybar> element.\n");
        System.out.println(rootDoc + "\n");
        return validateXml(rootDoc);
    }

    /**
     * Adds a new <bar> element between the first and second
     * children of the <arrayofany> element.
     * 
     * This method illustrates how you can use DOM methods to 
     * retrieve and edit elements whose type is defined as
     * xs:any in schema.
     * 
     * @param rootDoc An instance of the <root> XML document.
     * @return <code>true</code> if the editing XML is valid; 
     * otherwise, <code>false</code>.
     */
    public boolean editExistingDocWithDOM(RootDocument rootDoc)
    {
        RootDocument.Root root = rootDoc.getRoot();
        
        // Get the DOM nodes for the <arrayofany> element's children.
        Node arrayOfAnyNode = root.getArrayofany().getDomNode();

        // You don't have get* accessors for any of the <arrayofany> 
        // element's children, so use DOM to identify the first
        // and second elements while looping through the child list.
        NodeList childList = arrayOfAnyNode.getChildNodes();
        Element firstElementChild = null;
        Element secondElementChild = null;

        // Find the first child element and make sure it's
        // <stringelement>.
        for (int i = 0; i < childList.getLength(); i++)
        {
            Node node = childList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                if (node.getLocalName().equals("stringelement"))
                {
                    firstElementChild = (Element)node;                
                    break;
                }
            }
        }
        if (firstElementChild == null) {return false;}

        // Find the second child element and make sure it's
        // <someelement>.
        Node node = firstElementChild.getNextSibling();
        do {
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                if (node.getLocalName().equals("someelement"))
                {
                    secondElementChild = (Element)node;
                    break;
                }
            }
            node = node.getNextSibling();
        } while (node != null);
        if (secondElementChild == null) {return false;}
        
        // Create and insert a new <bar> element.
        Element fooElement = 
            secondElementChild.getOwnerDocument().createElementNS("http://openuri.org","bar");
        Node valueNode = 
            fooElement.getOwnerDocument().createTextNode("some text");
        fooElement.appendChild(valueNode);
        arrayOfAnyNode.insertBefore(fooElement, secondElementChild);
        
        System.out.println("Output: <arrayofany> has a new <bar> child element.\n");
        System.out.println(rootDoc + "\n");
        return validateXml(rootDoc);        
    }
    
    /**
     * Edits incoming <root> XML to make the following changes: replace
     * <somelement> with its <stringlist> child; add a new <foo> 
     * element as the second child of <arrayofany>.
     * 
     * This method illustrates how you can use the selectPath method
     * to find an element defined as xs:any in schema, then use
     * XmlCursor instances to edit the XML.
     * 
     * @param rootDoc An instance of the <root> XML document.
     * @return <code>true</code> if the editing XML is valid; 
     * otherwise, <code>false</code>.
     */
    public boolean editExistingDocWithSelectPath(RootDocument rootDoc)
    {
        String namespaceDecl = "declare namespace any='" + 
        			m_namespaceUri + "'; ";
        XmlCursor selectionCursor = rootDoc.getRoot().getArrayofany().newCursor();

        // Save the cursor's position for later, then use XPath
        // and cursor movement to position the cursor at
        // the <stringlist> element.
        selectionCursor.push();
        selectionCursor.selectPath(namespaceDecl + 
                "$this//any:someelement/any:stringlist");
        selectionCursor.toNextSelection();

        // Create a new cursor and move it to the selection
        // cursor's <someelement> parent. Moving the 
        // <stringlist> element to this position, displacing
        // the <someelement> downward, then removing the
        // <someelement> XML effectively replaces <someelement>
        // with <stringlist>.
        XmlCursor editCursor = selectionCursor.newCursor();
        editCursor.toParent();
        selectionCursor.moveXml(editCursor);
        editCursor.removeXml();
        editCursor.dispose();

        // Return the cursor to the <arrayofany> element so you 
        // can do more editing. Then move the cursor to the second 
        // child and insert a new element as second child.
        selectionCursor.pop();
        selectionCursor.toFirstChild();
        selectionCursor.toNextSibling();
        selectionCursor.beginElement("foo", "http://openuri.org");
        selectionCursor.insertChars("some text");
        selectionCursor.dispose();

        System.out.println("Output: <stringlist> has been promoted to replace \n" +
                "<someelement>, and there's a new <foo> element.\n");
        System.out.println(rootDoc + "\n");
        return validateXml(rootDoc);        
    }

    /**
     * Like the code in the buildDocFromScratch method, this code
     * uses the XmlCursor to build XML piece by piece, building
     * out the Arrayofany instance it receives. 
     * 
     * @return A valid <arrayofany> element bound to an 
     * Arrrayofany instance.
     */
    private Arrayofany buildArrayOfAny(Arrayofany arrayOfAny)
    {
        // Create a simple <stringelement> and move it into place
        // under <arrayofany>.
        StringelementDocument stringElementDoc = 
            StringelementDocument.Factory.newInstance();        
        stringElementDoc.setStringelement("some text");
        XmlCursor childCursor = stringElementDoc.newCursor();
        childCursor.toFirstContentToken();

        // Add a cursor to mark the position at which the new child 
        // XML will be moved.
        XmlCursor arrayCursor = arrayOfAny.newCursor();
        arrayCursor.toNextToken();
        childCursor.moveXml(arrayCursor);        
        childCursor.dispose();
        
        // Create a <someelement> that contains a <stringlist>
        // child element, then get the XmlObject representing the new
        // <stringlist>. Note that the XmlCursor.beginElement method
        // leaves the cursor between START and END tokens -- where 
        // content can be placed.
        arrayCursor.beginElement("someelement", m_namespaceUri);
        arrayCursor.beginElement("stringlist", m_namespaceUri);
        arrayCursor.toPrevToken();
        XmlObject stringList = arrayCursor.getObject();

        // The cursor's no longer needed.
        arrayCursor.dispose();

        // Create the <stringlist> element's value and set it.
        ListOfStrings stringListValue = buildListOfStrings();
        if (stringListValue == null)
        {
            return null;
        }
        stringList.set(stringListValue);

        // Validate the new XML.
        if (!validateXml(arrayOfAny))
        {
            return null;
        }

        return arrayOfAny;
    }
    
    /**
     * Creates an instance of the ListOfStrings complex type defined
     * in the schema. The instance returned by this method can be 
     * inserted using either a set* operation or a cursor, as in 
     * {@link #buildArrayOfAny()}.
     * 
     * @return A valid instance of ListOfStrings.
     */
    private ListOfStrings buildListOfStrings()
    {
        // Create an instance of the ListOfStrings complex type.
        ListOfStrings stringList = ListOfStrings.Factory.newInstance();
        stringList.setId("001");

        // Add two children for the instance's root.
        XmlString stringElement = stringList.addNewStringelement();
        stringElement.setStringValue("string1");
        stringElement = stringList.addNewStringelement();
        stringElement.setStringValue("string2");
        
        // Validate the new XML.
        if (!validateXml(stringList))
        {
            return null;
        }

        return stringList;
    }
    
    /**
     * <p>Validates the XML, printing error messages when the XML is invalid. Note
     * that this method will properly validate any instance of a compiled schema
     * type because all of these types extend XmlObject.</p>
     *
     * <p>Note that in actual practice, you'll probably want to use an assertion
     * when validating if you want to ensure that your code doesn't pass along
     * invalid XML. This sample prints the generated XML whether or not it's
     * valid so that you can see the result in both cases.</p>
     *
     * @param xml The XML to validate.
     * @return <code>true</code> if the XML is valid; otherwise, <code>false</code>
     */
    public static boolean validateXml(XmlObject xml)
    {
        boolean isXmlValid = false;

        // A collection instance to hold validation error messages.
        ArrayList validationMessages = new ArrayList();

        // Validate the XML, collecting messages.
        isXmlValid = xml.validate(
                new XmlOptions().setErrorListener(validationMessages));

        // If the XML isn't valid, print the messages.
        if (!isXmlValid)
        {
            printErrors(validationMessages);
        }
        return isXmlValid;
    }
    
    /**
     * Receives the collection containing errors found during
     * validation and print the errors to the console.
     * 
     * @param validationErrors The validation errors.
     */
    public static void printErrors(ArrayList validationErrors)
    {
        Iterator iter = validationErrors.iterator();
        while (iter.hasNext())
        {
            System.out.println(">> " + iter.next() + "\n");
        }
    }

    /**
     * <p>Creates a File from the XML path provided in main arguments, then
     * parses the file's contents into a type generated from schema.</p>
     * <p/>
     * <p>Note that this work might have been done in main. Isolating it here
     * makes the code separately available from outside this class.</p>
     *
     * @param xmlFilePath A path to XML based on the schema in inventory.xsd.
     * @return An instance of a generated schema type that contains the parsed
     *         XML.
     */
    public XmlObject parseXml(String xmlFilePath)
    {
        File xmlFile = new File(xmlFilePath);
        XmlObject xml = null;
        try
        {
            xml = XmlObject.Factory.parse(xmlFile);
        } catch (XmlException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return xml;
    }
}
