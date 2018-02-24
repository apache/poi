Sample: SubstitutionGroup
Author: Rashmi Banthia (rjain29@gmail.com)
Last Updated: Oct. 6th, 2004

Versions:
    xmlbeans-1.0.3


-----------------------------------------------------------------------------

This sample illustrates substitution group elements in an XML document.

When you run this sample, you'll see it print substitution group element names and values.
Also it creates a new XML document to demonstrate how to write substitution group elements.


For example, for the following schema and XML fragment:

<xs:element name="comment" type="xs:string" />
<xs:element name="ship-comment" type="xs:string" substitutionGroup="po:comment" />
<xs:element name="bill-comment" type="xs:string" substitutionGroup="po:comment" />

<fragment>
    <ship-comment>Sample ship comment</ship-comment>
</fragment>
<fragment>
    <comment>Sample comment</comment>
</fragment>


It will print:
--------------
ship-comment
Sample ship comment

comment
Sample comment


Also it will write the following fragment to a XML document:
<fragment>
    <bill-comment>Sample bill comment</bill-comment>
</fragment>



To try out this sample:

1. Set XMLBEANS_HOME in your environment
2. Ant must be on your PATH
3. To compile the schemas and sample source, run "ant build"
4. To execute the sample, run "ant run"
