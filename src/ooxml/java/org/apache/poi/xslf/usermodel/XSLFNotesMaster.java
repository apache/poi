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
package org.apache.poi.xslf.usermodel;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.util.Beta;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesMaster;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideMaster;
import org.openxmlformats.schemas.presentationml.x2006.main.NotesMasterDocument;
import org.openxmlformats.schemas.presentationml.x2006.main.SldMasterDocument;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
* Notes master object associated with this layout.
* <p>
*  Within a notes master slide are contained all elements
* that describe the objects and their corresponding formatting
* for within a presentation slide.
* </p>
* <p>
* Within a nodes master slide are two main elements.
* The cSld element specifies the common slide elements such as shapes and
* their attached text bodies. Then the notesStyles element specifies the
* formatting for the text within each of these shapes.
* </p>
 *
 * @author Yegor Kozlov
*/
@Beta
 public class XSLFNotesMaster extends XSLFSheet {
	 private CTNotesMaster _slide;
    private Map<String, XSLFSlideLayout> _layouts;
    private XSLFTheme _theme;

    XSLFNotesMaster() {
        super();
        _slide = CTNotesMaster.Factory.newInstance();
    }

    protected XSLFNotesMaster(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
        super(part, rel);
        NotesMasterDocument doc =
            NotesMasterDocument.Factory.parse(getPackagePart().getInputStream());
        _slide = doc.getNotesMaster();
        setCommonSlideData(_slide.getCSld());
    }

    @Override
    public CTNotesMaster getXmlObject() {
       return _slide;
    }

    @Override
    protected String getRootElementName(){
        return "notesMaster";
    }
}