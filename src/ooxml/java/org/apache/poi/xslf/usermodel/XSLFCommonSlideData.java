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

        for (CTGroupShape shape : gs.getGrpSpArray()) {
            processShape(shape, out);
        }

        CTGraphicalObjectFrame[] graphicFrames = gs.getGraphicFrameArray();
        for (CTGraphicalObjectFrame frame: graphicFrames) {
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
        CTShape[] shapes = gs.getSpArray();
        for (int i = 0; i < shapes.length; i++) {
            CTTextBody ctTextBody = shapes[i].getTxBody();
            if (ctTextBody==null) {
                continue;
            }

            DrawingTextBody textBody = new DrawingTextBody(ctTextBody);

            out.addAll(Arrays.asList(textBody.getParagraphs()));
        }
    }

}
