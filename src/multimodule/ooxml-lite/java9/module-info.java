/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


open module org.apache.poi.ooxml.schemas {
    // this still throws "requires transitive directive for an automatic module" in JDK 14
    // see https://bugs.openjdk.java.net/browse/JDK-8240847
    requires transitive org.apache.xmlbeans;
    requires java.xml;




    exports com.microsoft.schemas.compatibility;
    exports com.microsoft.schemas.office.excel;
    exports com.microsoft.schemas.office.office;
    exports com.microsoft.schemas.office.visio.x2012.main;
    exports com.microsoft.schemas.office.x2006.digsig;
    exports com.microsoft.schemas.vml;
    exports org.apache.poi.schemas.ooxml.system.ooxml;
    exports org.apache.poi.schemas.vmldrawing;
    exports org.etsi.uri.x01903.v13;
    exports org.openxmlformats.schemas.drawingml.x2006.chart;
    exports org.openxmlformats.schemas.drawingml.x2006.main;
    exports org.openxmlformats.schemas.drawingml.x2006.picture;
    exports org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing;
    exports org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing;
    exports org.openxmlformats.schemas.officeDocument.x2006.customProperties;
    exports org.openxmlformats.schemas.officeDocument.x2006.docPropsVTypes;
    exports org.openxmlformats.schemas.officeDocument.x2006.extendedProperties;
    exports org.openxmlformats.schemas.officeDocument.x2006.math;
    exports org.openxmlformats.schemas.officeDocument.x2006.relationships;
    exports org.openxmlformats.schemas.officeDocument.x2006.sharedTypes;
    exports org.openxmlformats.schemas.presentationml.x2006.main;
    exports org.openxmlformats.schemas.spreadsheetml.x2006.main;
    exports org.openxmlformats.schemas.wordprocessingml.x2006.main;
    exports org.openxmlformats.schemas.xpackage.x2006.digitalSignature;
    exports org.w3.x2000.x09.xmldsig;
}