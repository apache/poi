/*  Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

XMLBeans Development Kit Version 2.5.0


Welcome to XMLBeans!


Kit contents:

(1) One copy of xbean.jar, which contains XMLBeans.
    Should work on any JDK 1.4.x or 1.5.x.
    ./lib/xbean.jar

(2) One copy of jsr173_1.0_api.jar, which contains classes
    needed to parse XML files for use with XMLBeans.
    ./lib/jsr173_1.0_api.jar

(3) One copy of resolver.jar from Apache xml-commons. See scomp
    documentation for when it is needed.
    ./lib/resolver.jar

(4) One copy of xbean_xpath jar. Contains the XPath and XQuery
    "glue" code, only needed if XPath-XQuery functionality is
    required.
    ./lib/xbean_xpath.jar

(5) One copy of xmlpublic.jar, containing all the public interfaces of
    XMLBeans. (Classes included here are a subset of those included
    in xbean.jar.)
    ./lib/xmlpublic.jar

(6) One copy of xmlbeans-qname.jar, contains javax.xml.namespace
    QName.class and NamespaceContext.class. These classes were
    introduced in the jdk since 1.5. This jar is necesary on classpath
    only if using jdk 1.4.   
    ./lib/xmlbeans-qname.jar

(7) License information for XML Beans and included libraries
    ./LICENSE.txt
    ./NOTICE.txt

(8) One folder full of command-line scripts, pointing to the
    useful main() functions in the JAR.
    ./bin

(9) A copy of the plain javadoc tree for org.apache.xmlbeans.*
    ./docs/reference

(10) A preliminary collection of nicely formatted user-level
    documentation HTML (includes reformatted and nicely
    organized javadoc as well)
    ./docs/guide

(11) A few sample schemas
    ./schemas

(12) Samples that show the use of the XMLBeans API. (You'll
    also find more samples at the XMLBeans web site.)
    ./samples

Where to start?

(1) Setup.

    1. Make sure you have a JDK 1.4.x installed (or 1.5.x); that
       java[.exe] is on your path and that JAVA_HOME/bin contains
       java[.exe], javac[.exe], and jar[.exe].

    2. Set your XMLBEANS_HOME env variable to point to the directory
       in which you installed XmlBeans (i.e., /home/user/xmlbeans).

    3. Put the scripts in ./bin on your path.

    4. To test your setup, run "scomp" with no arguments.  You should 
       get a "usage" message. 


(2) Get to know XMLBeans basics.

    1. Use the tutorial located at the XMLBeans web site: 
       http://xmlbeans.apache.org/documentation/tutorial_getstarted.html.
       This provides a hands-on introduction to the most commonly
       used technologies in XMLBeans.

    2. For an even shorter introduction, see the Getting Started topic
       included with the release (./docs/guide/conGettingStartedwithXMLBeans.html)
       or at the web site
       (http://xmlbeans.apache.org/docs/2.2.0/guide/conGettingStartedwithXMLBeans.html).

    3. Explore the samples provided with the release (./samples) or 
       at the XMLBeans web site (http://xmlbeans.apache.org/samples/index.html).


(3) Get more XMLBeans depth by compiling other schemas to understand
    and use generate Java types.

     * In the ./schemas directory you'll find some collections of
       schemas you can try out.

         - easypo: a contrived simple starter "purchase order"
         - nameworld: another simple schema
         - numerals: schema illustrating the use of various flavors
                     of XmlSchema simple types
         - s4s: the Schema for Schema and Schema for XML

       To compile them, you can just send the whole directory to
       scomp, "scomp samples", or compile each file individually,
       "cd samples"; then "scomp easypo.xsd".
       You will get an "xmltypes.jar" out that contains all the
       compiled XMLBeans.  To pick your own JAR filename just say

       scomp -out myeasypo.jar easypo.xsd


     * Especially as you get started, you will want to see the
       .java source code for the generated code.  To get that,
       use a command-line like

       scomp -src mysrcdir -out myeasypo.jar easypo.xsd

       The "mysrcdir" will contain all the .java source code
       for the generated XMLBeans.


     * You can also use the XMLBean Ant task to compile your schemas
       during your build process:

       <taskdef name="xmlbean"
           classname="org.apache.xmlbeans.impl.tool.XMLBean"
           classpath="path/to/xbean.jar:path/to/jsr173_1.0_api.jar" />

       <xmlbean schemas="easypo.xsd" destfile="myeasypo.jar"
           classpath="path/to/xbean.jar:path/to/jsr173_1.0_api.jar" />

       For more information, see docs/guide/antXmlbean.html.


(4) Learn more about code generated from your schema and about
    the XMLBeans API.

    Armed with the XMLBeans source code and the basic
    docs, you're ready to program.  Things you need to know:

    * The org.apache.xmlbeans package has all the public classes
      for XMLBeans.  Programs should not need to call anything
      else in xbean.jar directly.

    * XmlObject is the base class for all XMLBeans.  It
      corresponds to xs:anyType.

    * Every schema type corresponds to an XMLBean interface,
      e.g., XmlAnySimpleType corresponds to xs:anySimpleType, and
      XmlInt corresponds to xs:int, etc.. And of course this
      extends to the XMLBean classes compiled from user-defined
      schemas.

    * Every XMLBean interface has an inner Factory class for
      creating or parsing instances, e.g., to load a file of
      generic type, use XmlObject.Factory.parse(myfile); to
      parse a string you expect to be a purchase-order, use
      PurchaseOrderDocument.Factory.parse("<ep:purchase-o...");

    * XmlCursor is the API for full XML infoset treewalking.
      It is obtained via xmlobject.newCursor(). Using it is
      less convenient, but faster than using XML Objects,
      because it does not create objects as it traverses
      the XML tree.

    * SchemaType is the basic "schema reflection" API (just like
      Class, but for Schema).  Get the actual schema type of any
      instance by saying "xobj.schemaType();" get the static
      constant schema type corresponding to any XMLBean class
      by saying "MyPurchaseOrder.type" or "XmlInt.type".
      (Analogous to "obj.getClass()" and "Object.class".)

    * A number of utility methods are available on
      org.apache.xmlbeans.XmlBeans, including a function that can be
      used to determine whether a Java class is an XmlBean and
      functions to manage runtime-loading of schema type
      systems or programmatically compiling Schema files.

    With that, you're ready to navigate the Javadoc and play
    with the code.  Also, try reading some of our 
    docs that are included in ./docs, as well as samples included
    in ./samples

(5) Try some of the other utilities included in the ./bin directory; 
    you can also see a few examples of XMLBean techniques in their 
    source code.

     * "xpretty instance.xml" pretty-prints an XML instance
       document.

       The code is in (available via source SVN access)
       org.apache.xmlbeans.impl.tool.PrettyPrinter and is
       a reasonable example of how to load and save out an
       arbitrary XML document.  XmlOptions are used to produce
       the pretty-printing.

     * "validate instance.xml schema.xsd" will validate the
       instance against the schema.  XMLBeans is intended to
       be a very accurate XML schema validator.

       The code is in (available via source SVN access)
       org.apache.xmlbeans.impl.tool.InstanceValidator.
       It is an excellent example of how to load a schema
       type system dynamically at runtime, load and validate
       an instance within that type system, and how to obtain
       lists of and locations for validation errors.

     * "xsdtree easypo" will show the inheritance hierarchy
       of the schema types in that directory.

       The code is in xml.apache.org.tool.TypeHierarchyPrinter
       and is a good introduction to how to traverse the
       metadata in a schema type system.

     * "dumpxsb xbean.jar" or "dumpxsb myfile.xsb" will dump
       the contents of "xsb" (binary schema metadata) files
       in a human-readable form.  These .xsb files contain
       the compiled metadata resulting from the .xsd files
       of a type system.  They are analogous to .class files
       for .java.

     * "inst2xsd mydoc.xml" will generate a [set of] XmlSchema
       file based on the instance document provided. This is
       useful as a starting point in authoring an XmlSchema
       document. 

     * "xsd2inst schema.xsd -name root" will generate a
       sample xml document with root "root", based on the
       schema definitions from the provided file.

       The code is in (available via source SVN access)
       org.apache.xmlbeans.impl.xsd2inst.SampleXmlUtil and is
       a great example of how to combine the XmlCursor and
       SchemaType APIs to create a full [sub]document
       that includes required children, default values, etc.

