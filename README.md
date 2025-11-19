# Apache POI™

A Java library for reading and writing Microsoft Office binary and OOXML file formats.

The Apache POI Project's mission is to create and maintain Java APIs for manipulating various file formats based upon the Office Open XML standards (OOXML) and Microsoft's OLE 2 Compound Document format (OLE2). In short, you can read and write MS Excel files using Java. In addition, you can read and write MS Word and MS PowerPoint files using Java. Apache POI is your Java Excel solution (for Excel 97-2008). We have a complete API for porting other OOXML and OLE2 formats and welcome others to participate.

OLE2 files include most Microsoft Office files such as XLS, DOC, and PPT as well as MFC serialization API based file formats. The project provides APIs for the OLE2 Filesystem (POIFS) and OLE2 Document Properties (HPSF).

Office OpenXML Format is the new standards based XML file format found in Microsoft Office 2007 and 2008. This includes XLSX, DOCX and PPTX. The project provides a low level API to support the Open Packaging Conventions using openxml4j.

For each MS Office application there exists a component module that attempts to provide a common high level Java api to both OLE2 and OOXML document formats. This is most developed for Excel workbooks (SS=HSSF+XSSF). Work is progressing for Word documents (WP=HWPF+XWPF) and PowerPoint presentations (SL=HSLF+XSLF).

The project has some support for Outlook (HSMF). Microsoft opened the specifications to this format in October 2007. We would welcome contributions.

There are also projects for Visio (HDGF and XDGF), TNEF (HMEF), and Publisher (HPBF).

This library includes the following components, roughly in descending order of maturity:

* Excel spreadsheets (Common SS = HSSF, XSSF, and SXSSF)
* PowerPoint slideshows (Common SL = HSLF and XSLF)
* Word processing documents (Common WP = HWPF and XWPF)
* Outlook email (HSMF and HMEF)
* Visio diagrams (HDGF and XDGF)
* Publisher (HPBF)

And lower-level, supporting components:

* OLE2 Filesystem (POIFS)
* OLE2 Document Properties (HPSF)
* TNEF (HMEF) for Outlook winmail.dat files
* OpenXML4J (OOXML)

| Components named H??F are for reading or writing OLE2 binary formats.
| Components named X??F are for reading or writing OpenOffice XML (OOXML) formats.

# Getting started

Website: https://poi.apache.org/

[Mailing lists](https://poi.apache.org/mailinglists.html):

* [Developers](https://lists.apache.org/list.html?dev@poi.apache.org)
* [Users](https://lists.apache.org/list.html?user@poi.apache.org) Including Announcements 
* [General](https://lists.apache.org/list.html?general@poi.apache.org)

## Bug trackers

* [Bugzilla](https://bz.apache.org/bugzilla/buglist.cgi?product=POI)
* [GitHub](https://github.com/apache/poi/issues)

## Source code

* https://github.com/apache/poi

Requires Java 11 or later. `trunk` branch is used for 6.0.0 development. POI 4 and 5 releases require Java 8 or later.


## Jars

A good resource for finding the published jars and forming build tool dependency definitions is https://mvnrepository.com/artifact/org.apache.poi.

* poi - main jar, including shared interfaces
* poi-scratchpad - extra classes to support legacy MS file formats (`H**F`)
* poi-ooxml - support for newer OOXML file formats (`X**F`)
* poi-ooxml-lite - generated classes based on MS XSDs used bt poi-ooxml (only includes most commonly used classes)
* poi-ooxml-full - generated classes based on MS XSDs (can be used instead of poi-ooxml-lite if you need support for less commonly used features)
* poi-excelant - tools for working with Excel files in Apache Ant scripts
* poi-examples

# Contributing

* Download and install git, Java JDK 11+, and Apache Ant 1.8+ or Gradle

* Check out the code from git

* Import the project into Eclipse or your favorite IDE

* Write a unit test:

  * Binary formats and Common APIs: poi/src/test/java/org/apache/poi/
  * OOXML APIs only: poi-ooxml/src/test/java/org/apache/poi/
  * Scratchpad (Binary formats): poi-scratchpad/src/test/java/org/apache/poi/
  * Test files: test-data/

* Navigate the source, make changes, and run unit tests to verify

  * Binary formats and Common APIs: poi/src/main/java/org/apache/poi/
  * OOXML APIs only: poi-ooxml/src/main/java/org/apache/poi/
  * Scratchpad (Binary formats): poi-scratchpad/src/main/java/org/apache/poi/
  * Examples: poi-examples/src/main/java/org/apache/poi/

* More info: [How To Build page](https://poi.apache.org/devel/)

# Building jar files

To build the jar files for poi, poi-ooxml, poi-ooxml-lite, poi-ooxml-full and poi-examples::

    ./gradlew jar

    gradlew jar
