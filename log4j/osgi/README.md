# Apache POI OSGi Bundle

The POI bundle is an Uber jar which exports all the POI classes, XML Beans, OOXML Schemas and required  dependencies. The current size is around 21 MB. 
The bundle is self-contained and can be used out of the box in a bare OSGi container.

## Embedded Dependencies
The bundle embeds all the jars from lib/main:

- SparseBitSet
- curvesapi
- commons-math3
- commons-compress
- commons-collections4
- commons-codec

## Optional Dependencies

1. Apache Batik
Required to render WMF/EMF images. The OSGi bundle is provided by ServiceMix and available in Maven Central: https://mvnrepository.com/artifact/org.apache.servicemix.bundles/org.apache.servicemix.bundles.batik/1.13_1
2. Saxon
Required if using as the XSLT and XQuery Processor engine in XML Beans.
Available in Maven Central: https://mvnrepository.com/artifact/net.sf.saxon/saxon/8.9.0.4-osgi
3. Apache XML Security for Java, Bouncy Castle and XML Commons Resolver 
These are required to sign or validate signed Office documents. The OSGi bundles are available in Maven Central:

    - Apache XML Security for Java: https://mvnrepository.com/artifact/org.apache.santuario/xmlsec/2.2.0
    
    - XML Commons Resolver: https://mvnrepository.com/artifact/xml-resolver/xml-resolver/1.2-osgi
    
    - Bouncy Castle: https://mvnrepository.com/artifact/org.bouncycastle/bcprov-ext-jdk15on/1.66, https://mvnrepository.com/artifact/org.bouncycastle/bcpkix-jdk15on/1.66
4. PDFBox and PDFBox Graphics2D  
Required to render to PDF documents.
The required jars can be downloaded from:

    - PDFBox:  
      https://mvnrepository.com/artifact/org.apache.pdfbox/pdfbox
      https://mvnrepository.com/artifact/org.apache.pdfbox/fontbox
    - PDFBox Graphics2D:  
      https://mvnrepository.com/artifact/de.rototor.pdfbox/graphics2d

## Blocked Imports

The Bundle Maven Plugin performs a thorough inspection of the dependencies on external packages and by default  includes them all in the <Import-Package> section. 

Transitive dependencies from XML Beans not required by POI:

    - !com.github.javaparser.*,
    - !org.apache.tools.ant.*

Optional codecs pulled by  Commons-Compress. Not used by POI

    - !com.github.luben.zstd.*,
    - !org.tukaani.xz.*,
    - !org.brotli.dec.*,
    
Internal APIs which are no more in JPMS

    - !sun.misc.*

## Integration Testing

The project tests the bundle using the Pax Exam framework which executes junit tests within an OSGi container started by Maven. The current version uses the Apache Felix driver but the framework should not matter, same tests will pass with the Karaf or Equinox drivers.

When running integration tests Maven starts a bare Apache Felix OSGi container, deploys the POI bundle and runs a few simple tests to validate the code is working fine, e.g. create a spreadsheet, serialize it to a byte array and read back.

 

