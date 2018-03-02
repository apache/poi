Sample: AbstractTypes
Category: abstracttypes
Author: Radu Preotiuc
Last Updated: Feb. 7, 2005

Versions:
    xmlbeans-v1 1.0.3
    xmlbeans-v2

-----------------------------------------------------------------------------

This sample illustrates the use of abstract XmlSchema types in XmlBeans.

The scenario is simple: someone declares a generic XmlSchema containing the
basic structure of a document with abstract, generic types and delivers a .jar
file containing the compiled version of that Schema. We then define our own
customization of that schema implementing the abstract types, we compile this
XmlSchema using the .jar that was provided and create a sample instance
document using XmlBeans.
Here are the steps:

Step1. Create the schema file "abstractBase.xsd" and compile it
to abstractbase.jar.
Step2. Create a Schema that implements the abstract types defined in
"abstractBase.xsd", and compile it with "abstractbase.jar" on the classpath.
Step3. Use the jars created in steps 1 and 2 to create an instance document
using the concrete types.

To try out this sample:

1. Set XMLBEANS_HOME in your environment
2. Ant must be on your PATH
3. To compile the schemas and sample source, run "ant build"
4. To execute the sample, run "ant run"

