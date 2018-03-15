Sample: DateTime
Author: Rashmi Banthia (rjain29@gmail.com)
Last Updated: Oct. 11, 2004

Versions:
    xmlbeans-1.0.3


-----------------------------------------------------------------------------

This sample demonstrates how you can work with XML Schema primitive types date,
dateTime, time, duration, gDay.

This sample illustrates how you can
(1) Convert org.apache.xmlbeans.XmlDate to java.util.Calendar,org.apache.xmlbeans.GDate, java.util.Date
(2) Convert org.apache.xmlbeans.XmlTime to java.util.Calendar,org.apache.xmlbeans.GDate, java.util.Date
(3) Convert org.apache.xmlbeans.XmlDuration to org.apache.xmlbeans.GDuration
(4) Convert org.apache.xmlbeans.XmlGday to java.util.Calendar,org.apache.xmlbeans.GDate, Day - primitive java int
(5) Get/Set XML Schema primitive types date, dateTime, time, duration, and gDay.


XMLBean Types provide mapping between natural Java classes and W3C Schema types.
For eg:

Schema Type             Formal Class            Natural Java Class
xs:date                 XmlDate                 java.util.Calendar (XmlCalendar)
xs:duration             XmlDuration             org.apache.xmlbeans.GDuration
xs:dateTime             XmlDateTime             java.util.Calendar (XmlCalendar)
xs:time                 XmlTime                 java.util.Calendar (XmlCalendar)

The XmlCalendar is a subclass of GregorianCalendar that modifies several key
details in the behavior of GregorianCalendar to make it more useful when
dealing with XML dates.


When you run this sample:
(1) It will print element values using different formats ie. Calendar, Date, GDate. Please
note it prints only first occurence of element's value for the purpose of simplicity.
(2) It will create a new <important-date> element and saves the same in a XML Document.


To try out this sample:

1. Set XMLBEANS_HOME in your environment
2. Ant must be on your PATH
3. To compile the schemas and sample source, run "ant build"
4. To execute the sample, run "ant run"
