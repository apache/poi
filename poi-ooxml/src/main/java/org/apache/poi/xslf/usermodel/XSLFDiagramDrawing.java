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

import com.microsoft.schemas.office.drawing.x2008.diagram.DrawingDocument;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Beta;
import org.apache.xmlbeans.XmlException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Drawing representation of a SmartArt diagram.
 */
@Beta
public class XSLFDiagramDrawing extends POIXMLDocumentPart {

    private final DrawingDocument _drawingDoc;

    /* package protected */ XSLFDiagramDrawing() {
        super();
        _drawingDoc = DrawingDocument.Factory.newInstance();
    }

    /* package protected */ XSLFDiagramDrawing(PackagePart part) throws XmlException, IOException {
        super(part);
        _drawingDoc = readPackagePart(part);
    }

    private static DrawingDocument readPackagePart(PackagePart part) throws IOException, XmlException {
        try (InputStream is = part.getInputStream()) {
            return DrawingDocument.Factory.parse(is);
        }
    }

    public DrawingDocument getDrawingDocument() {
        return _drawingDoc;
    }
}
