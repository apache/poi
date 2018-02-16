Sample: Any
Author: Steven Traut (straut@bea.com)
Last Updated: June 14, 2005

Versions:
    xmlbeans-v1 1.0.3
    xmlbeans-v2

-----------------------------------------------------------------------------

This sample illustrates how you can use the XMLBeans API to work with 
XML based on schema that features xs:any particles. Unlike other schema 
types, xs:any in schema does not result in accessors when you use XMLBeans
to compile schema. Instead, your code must use alternate means to access
and create these parts of the XML. These alternate means include:

- Using XmlCursor instances to "walk" the XML, retrieving and creating
elements.
- Using the selectPath method to retrieve XML via XPath. 
- Using the selectChildren method to retrieve elements that are children
by name.
- Using the DOM API to "walk" the node tree, retrieving elements by
name and creating new elements.

When you run this sample, you'll see it print four blocks of information
in the console:
- Results of the Any.buildDocFromScratch method, which builds from scratch the 
XML contents of any.xml included with this sample.
- Results of the Any.editExistingDocWithSelectChildren method, which receives
any.xml and replaces one of its elements.
- Results of the Any.editExistingDocWithDOM method, which receives
any.xml and adds an element.
- Results of the Any.editExistingDocWithSelectPath method, which receives
any.xml, promotes one of its elements, and adds a new element.

To try out this sample:

1. Set XMLBEANS_HOME in your environment
2. Ant must be on your PATH
3. To compile the schemas and sample source, run "ant build"
4. To execute the sample, run "ant run"
