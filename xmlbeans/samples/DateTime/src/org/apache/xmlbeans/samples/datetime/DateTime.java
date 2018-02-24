/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.samples.datetime;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.ArrayList;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;

import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.XmlDate;
import org.apache.xmlbeans.XmlCalendar;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.xmlbeans.samples.datetime.ImportantDate;
import org.apache.xmlbeans.samples.datetime.DatetimeDocument;

/**
 * The sample illustrates how you can work with XML Schema types date,
 * dateTime, time, duration, gDay.
 * It parses the XML Document, prints first occurence of <important-date>
 * value, creates a new <important-date> element and saves it in a new XML Document.
 * This sample illustrates how you can convert XMLBean types to Java types
 * (java.util.Date, java.util.Calendar).
 * It uses the schema defined in datetime.xsd.
 */
public class DateTime {

   /**
    * Receives an XML Instance and prints the element values,
    * Also creates a new XML Instance.
    *
    * @param args An array containing
    *             (a)Path to the XML Instance conforming to the XML schema in datetime.xsd.
    *             (b)Path for creating a new XML Instance.
    */
   public static void main(String args[]){
       // Create an instance of this class to work with.
       DateTime dt = new DateTime();

       // Create an instance of a Datetime type based on the received XML's schema
       DatetimeDocument doc = dt.parseXml(args[0]);

       // Prints the element values from the XML
       dt.printInstance(doc);

       // Creates a new XML and saves the file
       dt.createDocument(doc,args[1]);

   }

   /**
    * Creates a File from the XML path provided in main arguments, then
    * parses the file's contents into a type generated from schema.
    */
   public DatetimeDocument parseXml(String file){
       // Get the XML instance into a file using the path provided.
       File xmlfile = new File(file);

       // Create an instance of a type generated from schema to hold the XML.
       DatetimeDocument doc = null;

       try {
           // Parse the instance into the type generated from the schema.
           doc = DatetimeDocument.Factory.parse(xmlfile);
       }
       catch(XmlException e){
           e.printStackTrace();
       }
       catch(IOException e){
           e.printStackTrace();
       }
       return doc;
   }

   /**
    * This method prints first occurence of <important-date>
    * value. It also prints converted values from  XMLBean types to Java types
    * (java.util.Date, java.util.Calendar) and org.apache.xmlbeans.GDate.
    */
   public void printInstance(DatetimeDocument doc){
       // Retrieve the <datetime> element and get an array of
       // the <important-date> elements it contains.
       DatetimeDocument.Datetime dtelement = doc.getDatetime();
       ImportantDate[] impdate = dtelement.getImportantDateArray();

       // Loop through the <important-date> elements, printing the
       // values for each.
       for (int i=0;i<impdate.length;i++){

           //For purpose of simplicity in output, only first occurrence is printed.
           if (i==0){

               //Retrieving all <holiday> elements within  <important-date> element
               XmlDate[] holiday = impdate[i].xgetHolidayArray();
               System.out.println("Holiday(xs:date): ");

               for (int j=0;j<holiday.length;j++){
                   if (j==0){
                       //XmlDate to java.util.Calendar,org.apache.xmlbeans.GDate, java.util.Date
                       System.out.println("Calendar:" + holiday[j].getCalendarValue() );
                       System.out.println("Date:"+holiday[j].getDateValue() );
                       System.out.println("GDate:"+holiday[j].getGDateValue() +"\n");
                   }
               }

               //XmlTime to java.util.Calendar, org.apache.xmlbeans.GDate, java.util.Date
               System.out.println("Fun Begin Time(xs:time): ");
               System.out.println("Calendar:"+impdate[i].getFunBeginTime());
               System.out.println("GDate:"+impdate[i].xgetFunBeginTime().getGDateValue()  );

               //To convert java.util.Calendar to java.util.Date
               SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
               Date dt = impdate[i].getFunBeginTime().getTime();
               System.out.println("Date:"+ sdf.format(dt) +"\n" );


               //XmlDuration to org.apache.xmlbeans.GDuration
               System.out.println("Job Duration(xs:duration): ");
               System.out.println("GDuration:"+impdate[i].getJobDuration() +"\n" );

               //XmlDate to Calendar,GDate, Date
               System.out.println("Birth DateTime(xs:dateTime): ");
               System.out.println("Calendar:"+impdate[i].getBirthdatetime());
               System.out.println("Date:"+impdate[i].xgetBirthdatetime().getDateValue());
               System.out.println("GDate:"+impdate[i].xgetBirthdatetime().getGDateValue() +"\n" );


               //XmlGday to Calendar,GDate, Day - primitive java int
               System.out.println("Pay Day(xs:gDay): ");
               System.out.println("Calendar:"+impdate[i].getPayday());
               System.out.println("GDate:"+impdate[i].xgetPayday().getGDateValue());
               System.out.println("Day:"+ impdate[i].xgetPayday().getGDateValue().getDay() +"\n" );

               System.out.println("\n\n");
           }
       }

   }

   /**
    * This method creates an new <important-date> element and attaches to the existing XML Instance, and saves the
    * new Instance to a file(args[1]).
    */
   public void createDocument(DatetimeDocument doc , String file){
       // Retrieve the <datetime> element and add a new <important-date> element.
       DatetimeDocument.Datetime dtelement = doc.getDatetime();

       //
       // add an important date using XmlCalendar
       //

       ImportantDate impdate = dtelement.addNewImportantDate();

       //Creating value for <holiday> element
       Calendar  holiday = new XmlCalendar("2004-07-04");

       //Creating value for <fun-begin-time> element
       Calendar  funbegintime = new XmlCalendar("10:30:33");

       //Creating value for <fun-end-time> element
       Calendar  funendtime = new XmlCalendar("12:40:12");

       //Creating value for <birthdatetime> element
       Calendar  birthdatetime = new XmlCalendar("1977-11-29T10:10:12");

       //Creating value for <job-duration> element
       GDuration jobduration =  new GDuration(1,2,4,5,10,12,15,null);

       //Creating value for <payday> element
       Calendar payday = new XmlCalendar("---12");

       //Setting all the elements
       impdate.addHoliday(holiday);
       impdate.setFunBeginTime(funbegintime);
       impdate.setFunEndTime(funendtime);
       impdate.setJobDuration(jobduration);
       impdate.setBirthdatetime(birthdatetime);
       impdate.setPayday(payday);
       impdate.setDescription("Using XmlCalendar");


       //
       // add another important date using Calendar
       //

       impdate = dtelement.addNewImportantDate();

       //Creating value for <holiday> element using XmlCalendar
       holiday = new XmlCalendar("2004-07-04");

       //Creating value for <fun-begin-time> element using GregorianCalendar
       funbegintime = Calendar.getInstance();
       funbegintime.clear();
       funbegintime.set(Calendar.AM_PM , Calendar.AM);
       funbegintime.set(Calendar.HOUR, 10);
       funbegintime.set(Calendar.MINUTE, 30 );
       funbegintime.set(Calendar.SECOND, 35 );

       //Creating value for <fun-end-time> element
       funendtime = Calendar.getInstance();
       funendtime.clear();
       funendtime.set(Calendar.AM_PM , Calendar.AM);
       funendtime.set(Calendar.HOUR, 12);
       funendtime.set(Calendar.MINUTE, 40 );
       funendtime.set(Calendar.SECOND, 12 );

       //Creating value for <birthdatetime> element
       birthdatetime = Calendar.getInstance();
       birthdatetime.clear();
       birthdatetime.set(1977,10,29,10,10,12);

       //Creating value for <job-duration> element
       jobduration =  new GDuration(1,2,4,5,10,12,15,null);

       //Creating value for <payday> element
       payday = Calendar.getInstance();
       payday.clear();
       payday.set(Calendar.DAY_OF_MONTH,12);

       //Setting all the elements
       impdate.addHoliday(holiday);
       impdate.setFunBeginTime(funbegintime);
       impdate.setFunEndTime(funendtime);
       impdate.setJobDuration(jobduration);
       impdate.setBirthdatetime(birthdatetime);
       impdate.setPayday(payday);
       impdate.setDescription("Using Calendar");

       XmlOptions xmlOptions = new XmlOptions();
       xmlOptions.setSavePrettyPrint();

       // Validate the new XML
       boolean isXmlValid = validateXml(doc);
       if (isXmlValid) {
           File f = new File(file);

           try{
               //Writing the XML Instance to a file.
               doc.save(f,xmlOptions);
           }
           catch(IOException e){
               e.printStackTrace();
           }
           System.out.println("\nXML Instance Document saved at : " + f.getPath());
       }
   }

   /**
    * <p>Validates the XML, printing error messages when the XML is invalid. Note
    * that this method will properly validate any instance of a compiled schema
    * type because all of these types extend XmlObject.</p>
    *
    * <p>Note that in actual practice, you'll probably want to use an assertion
    * when validating if you want to ensure that your code doesn't pass along
    * invalid XML. This sample prints the generated XML whether or not it's
    * valid so that you can see the result in both cases.</p>
    *
    * @param xml The XML to validate.
    * @return <code>true</code> if the XML is valid; otherwise, <code>false</code>
    */
   public boolean validateXml(XmlObject xml)
   {
       boolean isXmlValid = false;

       // A collection instance to hold validation error messages.
       ArrayList validationMessages = new ArrayList();

       // Validate the XML, collecting messages.
       isXmlValid = xml.validate(new XmlOptions().setErrorListener(validationMessages));

       if (!isXmlValid)
       {
           System.out.println("Invalid XML: ");
           for (int i = 0; i < validationMessages.size(); i++)
           {
               XmlError error = (XmlError) validationMessages.get(i);
               System.out.println(error.getMessage());
               System.out.println(error.getObjectLocation());
           }
       }
       return isXmlValid;
   }

}


