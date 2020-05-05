/* ====================================================================
   Copyright 2017 Andreas Beeker (kiwiwings@apache.org)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


open module org.apache.poi.ooxml.schemas {
    requires transitive xmlbeans;
    requires java.xml;
    exports com.microsoft.schemas.compatibility;
    exports com.microsoft.schemas.office.excel;
    exports com.microsoft.schemas.office.office;
    exports com.microsoft.schemas.office.powerpoint;
    exports com.microsoft.schemas.office.visio.x2012.main;
    exports com.microsoft.schemas.office.word;
    exports com.microsoft.schemas.vml;
    exports org.openxmlformats.schemas.drawingml.x2006.chart;
    exports org.openxmlformats.schemas.drawingml.x2006.chartDrawing;
    exports org.openxmlformats.schemas.drawingml.x2006.compatibility;
    exports org.openxmlformats.schemas.drawingml.x2006.diagram;
    exports org.openxmlformats.schemas.drawingml.x2006.lockedCanvas;
    exports org.openxmlformats.schemas.drawingml.x2006.main;
    exports org.openxmlformats.schemas.drawingml.x2006.picture;
    exports org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing;
    exports org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing;
    exports org.openxmlformats.schemas.officeDocument.x2006.bibliography;
    exports org.openxmlformats.schemas.officeDocument.x2006.characteristics;
    exports org.openxmlformats.schemas.officeDocument.x2006.customProperties;
    exports org.openxmlformats.schemas.officeDocument.x2006.customXml;
    exports org.openxmlformats.schemas.officeDocument.x2006.docPropsVTypes;
    exports org.openxmlformats.schemas.officeDocument.x2006.extendedProperties;
    exports org.openxmlformats.schemas.officeDocument.x2006.math;
    exports org.openxmlformats.schemas.officeDocument.x2006.relationships;
    exports org.openxmlformats.schemas.presentationml.x2006.main;
    exports org.openxmlformats.schemas.schemaLibrary.x2006.main;
    exports org.openxmlformats.schemas.spreadsheetml.x2006.main;
    exports org.openxmlformats.schemas.wordprocessingml.x2006.main;
    // opens schemaorg_apache_xmlbeans.system.OoxmlSchemas to xmlbeans;
}