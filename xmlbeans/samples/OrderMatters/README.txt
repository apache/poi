Sample: MixedContent
Author: Eric Vasilik (ericvas@bea.com)
Last Updated: Oct. 28, 2004

Versions:
    xmlbeans-1.0.3
    xmlbeans-v2

-----------------------------------------------------------------------------

This samples gives an quick overview of how to use XmlBeans with both the
strongly typed XmlObjects (StatementDocument, Transaction) and with the
XmlCursor.

In the sample, a instance of a statement is iterated over twice --
once using the strongly typed array approach and once with an XmlCursor.  When
walking over the array the programmer naivly adds up deposit amounts before
the withdrawal amounts.  The end result is a positive balance.  When walking
over the array using XmlCursor, the transaction amounts are processed in order
and the end result is a negative balance.

In this situation, the order of the xml elements matters!

To try out this sample:

1. Set XMLBEANS_HOME in your environment
2. Ant must be on your PATH
3. To compile the schemas and sample source, run "ant build"
4. To execute the sample, run "ant run"
