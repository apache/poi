Sample: XQueryXPath
Author: Steven Traut (straut@bea.com)
Last Updated: May 14, 2008

Versions:
    xmlbeans-v1 1.0.3
    xmlbeans-v2 2.4.0

-----------------------------------------------------------------------------

This sample illustrates how you can use the XMLBeans API to execute
XPath and XQuery expressions. The sample illustrates these features:

- Using the XmlObject.selectPath and XmlCursor.selectPath methods
to execute XPath expressions. The selectPath method's results (if
any) are always chunks of the instance queried against. In other
words, changes to query results change the original instance.
However, you work with results differently depending on whether
selectPath was called from an XmlObject or XmlCursor instance. See
the SelectPath class for more information.

- Using the XmlObject.execQuery and XmlCursor.execQuery methods
to execute XQuery expressions. Results of these queries are copied
into new XML, meaning that changes to results do not change the 
original instance. Here again, you work with results differently
depending how which method you used to query. See the ExecQuery
class for more information.

A note about dependencies. Very simple XPath expressions -- e.g.,
expressions without predicates or function calls -- require only
the xbean.jar on your class path. More complex XPath expressions
and XQuery expressions require an XPath/XQuery engine, such as
Saxon. XMLBeans 2.4.0 supports the use of Saxon 9. Two Saxon jars,
saxon9.jar and saxon9-dom.jar, as well as xbean_xpath.jar, are 
required on the classpath for code in this sample to run.
These jars are created in the build/lib directory if you build
XMLBeans from Apache source.

To try out this sample:

1. Set XMLBEANS_HOME in your environment
2. Ant must be on your PATH
3. xbean_xpath.jar, saxon9.jar, and saxon9-dom.jar must be on your 
   classpath.
   These files are created in the build/lib directory when you 
   build XMLBeans from source.
4. To compile the schemas and sample source, run "ant build"
5. To execute the sample, run "ant run"
