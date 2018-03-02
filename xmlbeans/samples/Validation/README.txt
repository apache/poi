Sample: Validation
Author: Steven Traut (straut@bea.com)
Last Updated: May 25, 2005

Versions:
    xmlbeans-v1 1.0.3
    xmlbeans-v2

-----------------------------------------------------------------------------

This sample illustrates how you can use the XMLBeans API to validate
XML instances against schema. The API provides two validation features:

- A validate method (available from XmlOjbect and types generated from schema)
with which you can validate the bound instance and collect error messages that 
result.
- An option through which you can specify that simple schema types should
be validated by XMLBeans when your code sets their value. This feature
will simply throw an exception if setting the value renders the instance
invalid.

Because it uses invalid XML for illustration, this sample is designed to "fail"
when it runs. When you run this sample, you'll see it print two blocks of information
in the console:
- A message containing errors resulting from calling the validate method
on invalid XML.
- The stack trace of an exception resulting from setting an invalid value
when the XmlOptions.VALIDATE_ON_SET option has been specified.

Note that you can also validate at the command line using tools provided 
in the bin directory of the XMLBeans distribution.

To try out this sample:

1. Set XMLBEANS_HOME in your environment
2. Ant must be on your PATH
3. To compile the schemas and sample source, run "ant build"
4. To execute the sample, run "ant run"
