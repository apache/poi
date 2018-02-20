package xmlcursor.xpath;


import java.io.*;
import java.util.HashMap;

import org.apache.xmlbeans.*;

/**
 *
 */
public class CustomerTest
{

    // public class XMLBeanXQueryTest {

    static String sXml = "<?xml version=\"1.0\"?>\n" +
        "<!DOCTYPE book SYSTEM \"book.dtd\">\n" +
        "\n" +
        "<book>\n" +
        "  <title>Data on the Web</title>\n" +
        "  <author>Serge Abiteboul</author>\n" +
        "  <author>Peter Buneman</author>\n" +
        "  <author>Dan Suciu</author>\n" +
        "  <section id=\"intro\" difficulty=\"easy\" >\n" +
        "    <title>Introduction</title>\n" +
        "    <p>Text ... </p>\n" +
        "    <section>\n" +
        "      <title>Audience</title>\n" +
        "      <p>Text ... </p>\n" +
        "    </section>\n" +
        "    <section>\n" +
        "      <title>Web Data and the Two Cultures</title>\n" +
        "      <p>Text ... </p>\n" +
        "      <figure height=\"400\" width=\"400\">\n" +
        "        <title>Traditional client/server architecture</title>\n" +
        "        <image source=\"csarch.gif\"/>\n" +
        "      </figure>\n" +
        "      <p>Text ... </p>\n" +
        "    </section>\n" +
        "  </section>\n" +
        "  <section id=\"syntax\" difficulty=\"medium\" >\n" +
        "    <title>A Syntax For Data</title>\n" +
        "    <p>Text ... </p>\n" +
        "    <figure height=\"200\" width=\"500\">\n" +
        "      <title>Graph representations of structures</title>\n" +
        "      <image source=\"graphs.gif\"/>\n" +
        "    </figure>\n" +
        "    <p>Text ... </p>\n" +
        "    <section>\n" +
        "      <title>Base Types</title>\n" +
        "      <p>Text ... </p>\n" +
        "    </section>\n" +
        "    <section>\n" +
        "      <title>Representing Relational Databases</title>\n" +
        "      <p>Text ... </p>\n" +
        "      <figure height=\"250\" width=\"400\">\n" +
        "        <title>Examples of Relations</title>\n" +
        "        <image source=\"relations.gif\"/>\n" +
        "      </figure>\n" +
        "    </section>\n" +
        "    <section>\n" +
        "      <title>Representing Object Databases</title>\n" +
        "      <p>Text ... </p>\n" +
        "    </section>\n" +
        "  </section>\n" +
        "</book>";


    static String sXml1 = "<report>\n" +
        "  <section>\n" +
        "    <section.title>Procedure</section.title>\n" +
        "     <section.content>\n" +
        "      The patient was taken to the operating room where she was placed\n" +
        "      in supine position and\n" +
        "      </section.content> </section></report>";

    public static void test_xpath(int id, String xml, String xpath)
    {
        try
        {
            System.out.println("\n====== test" + id + ": " + xpath + " =======");
            XmlObject xmlObj = XmlObject.Factory.parse(xml);
            XmlObject[] results = xmlObj.selectPath(xpath);
            show_result(results);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    public static void test_xquery(int id, String xmlFile, String xquery)
    {
        try
        {
            System.out.println("\n====== test" + id + ": " + xquery + " =======");
            XmlObject xmlObj = XmlObject.Factory.parse(sXml);
            XmlObject[] results = xmlObj.execQuery(xquery);
            show_result(results);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    private static void show_result(Object[] results)
        throws Throwable
    {
        for (int i = 0; i < results.length; i++)
        {
            Object node = results[i];
            System.out.println("=> class: " + node.getClass() + ", obj: " + node);
        }
    }

    public static void test_xpath()
    {
        System.out.println("\n====== xpath test =======");
        /*
        test_xpath(1, "xml/data/book.xml", "./book/section[@difficulty =
       \"easy\"]");
        test_xpath(2, "xml/data/book.xml", "./book/section");
        test_xpath(3, "xml/data/report1.xml",
       "./report/section/section.title[text() = \"Procedure\"]");
        */
//  test_xpath(0, sXml, "./book/section");
//  test_xpath(1, sXml, "./book/section[@difficulty =\"easy\"]");
        test_xpath(2, sXml1,
            "./report/section/section.title[text() = \"Procedure\"]");
//  test_xpath(3, sXml1,
// "./report/section[section.title = \"Procedure\"]");

        // this is not allowed in XPath(but it is OK for XQuery).
        // test_xpath(4, "xml/data/report1.xml", "./report/section/section.title =\"Procedure\"");
    }

    public static void test_xquery()
    {
        System.out.println("\n====== xquery test =======");
        test_xquery(1, "xml/data/bib.xml", xquery1);
        test_xquery(2, "xml/data/bib.xml", xquery2);
    }

    /*
    static final String xquery1 = "for $b in ./bib/book "
 +"  where $b/publisher[text() = \"Addison-Wesley\"] and $b[@year > 1992] "
 +"return "
 +"    <book year=\"{ $b/@year }\"> "
 +"{ $b/title }"
 +"</book>";
    static final String xquery2 = "for $b in ./bib/book "
 +"  where $b/publisher = \"Addison-Wesley\" and $b/@year > 1992 "
 +"return "
 +"    <book year=\"{ $b/@year }\"> "
 +"{ $b/title }"
 +"</book>";
    */
    static final String xquery1 = "for $b in $this/bib/book "
        +
        "  where $b/publisher[text() = \"Addison-Wesley\"] and $b[@year > 1992] "
        + "return "
        + "    <book year=\"{ $b/@year }\"> "
        + "{ $b/title }"
        + "</book>";
    static final String xquery2 = "for $b in $this/bib/book "
        + "  where $b/publisher = \"Addison-Wesley\" and $b/@year > 1992 "
        + "return "
        + "    <book year=\"{ $b/@year }\"> "
        + "{ $b/title }"
        + "</book>";

    public static void main(String[] args)
    {
        test_xpath();
        //  test_xquery();
        try{
        testXMLBeans();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void testXMLBeans()  throws Exception
    {
        XmlObject doc = XmlObject.Factory.parse(" <contact xmlns=\"http://dearjohn/address-book\"/>");
        HashMap nsMap = new HashMap();
        nsMap.put("ns", "http://dearjohn/address-book");
        XmlObject[] xmlObjs = doc.execQuery("/ns:contact", new
            XmlOptions().setLoadAdditionalNamespaces(nsMap));
        System.out.println(xmlObjs);
    }


}

//}
