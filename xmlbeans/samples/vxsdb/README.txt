Sample: VelocityXmlBeans
Category: sql
Author: Philip Mark Donaghy
Last Updated: Apr. 23, 2007

Versions:
    xmlbeans-v1 1.0.3
    xmlbeans-v2

--------------------------------------------------------------------------------

Vxsdb is a XmlBeans sample application which derives a Apache DB DdlUtils data 
model from an xml schema. It was conceived from things I have learned from using
XmlBeans and talking to people at the ASF conference. Vxsdb uses Jakarta 
Velocity as its templating engine.

Features:

    - Inputs a xml schema
    - Outputs an Apache DB DdlUtils data model in xml

Building this sample requires Apache dependancies which are automatically 
downloaded to the lib directory when Ant is executed.

To try out this sample:

1. Set XMLBEANS_HOME in your environment
2. Ant must be on your PATH
3. To compile the sample source, run "ant"
4. To execute the sample, run "ant -f run.xml"
5. The files build/datamodel.xml and build/create.sql are generated

To build this sample without downloading dependencies
-----------------------------------------------------
ant -Ddeps.exist=true

By default run creates a ddl for the Derby RDBMS. To change the target 
database add a directive on the command line.

Change database
---------------
ant -f run.xml -DtargetDatabase=postgresql

Postgres quickstart
-------------------
$ su -
# su - postgres
$ initdb
$ createdb easypo
$ psql easypo
# \i xml/create.sql
