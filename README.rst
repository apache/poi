
Apache POI
======================

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

Getting started
------------------

Website: https://poi.apache.org/

`Mailing lists`_:

* `Developers`_
* `Users`_
* `General`_ (release announcements)

Bug tracker:

* `Bugzilla`_
* `GitHub pull requests`_

Source code:

* Official `Apache Subversion repo`_ at apache.org
* `ViewVC repo browser`_ at apache.org
* Official `Apache git mirror`_ at apache.org
* Unofficial `GitHub git mirror`_ at github.com

Requires Java 1.6 or later.

Contributing
------------------

* Download and install svn or git, Java JDK 1.6+, and Apache Ant 1.8+ or Gradle

* Check out the code from svn or git

* Import the project into Eclipse or your favorite IDE

* Write a unit test:

  * Binary formats and Common APIs: src/testcases/org/apache/poi/
  * OOXML APIs only: src/ooxml/testcases/org/apache/poi/
  * Scratchpad (Binary formats): src/scratchpad/testcases/org/apache/poi/
  * test files: test-data/

* Navigate the source, make changes, and run unit tests to verify

  * Binary formats and Common APIs: src/java/org/apache/poi/
  * OOXML APIs only: src/ooxml/java/org/apache/poi/
  * Scratchpad (Binary formats): src/scratchpad/src/org/apache/poi/
  * Examples: src/examples/src/org/apache/poi/


Building jar files
------------------

To build the jar files for poi, poi-ooxml, poi-ooxml-schemas, and poi-examples::

    ant jar


.. _Mailing lists: https://poi.apache.org/mailinglists.html
.. _Developers: https://lists.apache.org/list.html?dev@poi.apache.org
.. _Users: https://lists.apache.org/list.html?user@poi.apache.org
.. _General: https://lists.apache.org/list.html?general@poi.apache.org
.. _Bugzilla: https://bz.apache.org/bugzilla/buglist.cgi?product=POI
.. _GitHub pull requests: https://github.com/apache/poi/pulls

.. _Apache Subversion repo: https://svn.apache.org/repos/asf/poi/trunk
.. _ViewVC repo browser: https://svn.apache.org/viewvc/poi/trunk
.. _Apache git mirror: https://git.apache.org/poi.git/
.. _GitHub git mirror: https://github.com/apache/poi


