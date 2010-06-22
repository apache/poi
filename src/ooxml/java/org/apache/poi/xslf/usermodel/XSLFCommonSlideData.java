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

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObjectData;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTable;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommonSlideData;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XSLFCommonSlideData {
    private final CTCommonSlideData data;

    public XSLFCommonSlideData(CTCommonSlideData data) {
        this.data = data;
    }

    public List<DrawingParagraph> getText() {
        CTGroupShape gs = data.getSpTree();

        List<DrawingParagraph> out = new ArrayList<DrawingParagraph>();

        processShape(gs, out);

        for (CTGroupShape shape : gs.getGrpSpList()) {
            processShape(shape, out);
        }

        for (CTGraphicalObjectFrame frame: gs.getGraphicFrameList()) {
            CTGraphicalObjectData data = frame.getGraphic().getGraphicData();
            XmlCursor c = data.newCursor();
            c.selectPath("./*");

            while (c.toNextSelection()) {
                XmlObject o = c.getObject();

                if (o instanceof CTTable) {
                    DrawingTable table = new DrawingTable((CTTable) o);

                    for (DrawingTableRow row : table.getRows()) {
                        for (DrawingTableCell cell : row.getCells()) {
                            DrawingTextBody textBody = cell.getTextBody();

                            out.addAll(Arrays.asList(textBody.getParagraphs()));
                        }
                    }
                }
            }
        }

        return out;
    }

    private void processShape(CTGroupShape gs, List<DrawingParagraph> out) {
        List<CTShape> shapes = gs.getSpList();
        for (int i = 0; i < shapes.size(); i++) {
            CTTextBody ctTextBody = shapes.get(i).getTxBody();
            if (ctTextBody==null) {
                continue;
            }

            DrawingTextBody textBody = new DrawingTextBody(ctTextBody);

            out.addAll(Arrays.asList(textBody.getParagraphs()));
        }
    }

}
