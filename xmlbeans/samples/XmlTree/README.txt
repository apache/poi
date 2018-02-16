Sample: XmlTree
Author: Steven Traut (straut@bea.com)
Last Updated: June 14, 2005

Versions:
    xmlbeans-v1 1.0.3
    xmlbeans-v2

-----------------------------------------------------------------------------

This sample illustrates how you can use the XMLBeans API to create a Java 
tree view of an XML document. The JTree implementation in the sample supports 
binding to any XML document. By accessing bound XML using an XmlCursor 
instance and XPath rather than through accessors provided by compiling
schema, the data model behind this tree can support XML without regard
to schema. The XmlEntry contains the simple code that is XMLBeans-aware. 
The XmlModel class, with which the tree implementation interacts directly, 
in turn knows nothing of XMLBeans or the underlying XML, instead reaching 
the data through XmlEntry instances.

When you run this sample, it will display a window with the following bits of
UI:

- A box at the top with the path to the XML that the tree represents. By 
default, this is the PurchaseOrder.xml file included with the sample. You can
change this path to point to another XML file, then click the Refresh
button to update the tree.
- A pane on the left displaying the tree itself. You can click nodes in the
tree to display the XML the node represents.
- A pane on the right displaying XML for the tree node that is selected in
the left pane.

To try out this sample:

1. Set XMLBEANS_HOME in your environment
2. Ant must be on your PATH
3. To compile the sample source, run "ant build"
4. To execute the sample, run "ant run"
