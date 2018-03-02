Sample: XSDConfig
Author: Rashmi Banthia (rjain29@gmail.com)
Last Updated: Oct. 18th, 2004

Versions:
    xmlbeans-1.0.3
    
-----------------------------------------------------------------------------

This sample illustrates how you can specify package names to be used for xml 
namespaces. You can also specify class names to be used for individual qualified names. 

To customize the package names and the class names, you'll need to create .xsdconfig file. 
For eg: "filename.xsdconfig"

Schema for xsdconfig can be obtained from XMLBeans source. 

For this sample: 
(1) The java class names generated without the XsdConfig are: 
	* org.apache.xmlbeans.samples.catalog.ArticleDocument
	* org.apache.xmlbeans.samples.catalog.AVeryLongDescriptionElementDocument
	* org.apache.xmlbeans.samples.catalog.CatalogDocument
	* org.apache.xmlbeans.samples.catalog.JournalDocument
(CatalogXsd.java uses above mentioned classes)
	
	
(2) The java class names generated with XsdConfig are: 
	* com.catalog.XmlArticleDocumentBean
	* com.catalog.XmlShortItemBean
	* com.catalog.XmlCatalogDocumentBean
	* com.catalog.XmlJournalDocumentBean
(CatalogXsdConfig.java uses above mentioned classes)	
 
When you run this sample, you will see it print all the element values from XML document 
instance (with and without XsdConfig file).

To try out this sample:

1. Set XMLBEANS_HOME in your environment
2. Ant must be on your PATH
3. To compile the schemas and sample source, run "ant build"
