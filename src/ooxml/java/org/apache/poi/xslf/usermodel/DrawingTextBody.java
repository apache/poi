package org.apache.poi.xslf.usermodel;

import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;

public class DrawingTextBody {
    private final CTTextBody textBody;

    public DrawingTextBody(CTTextBody textBody) {
        this.textBody = textBody;
    }

    public DrawingParagraph[] getParagraphs() {
        CTTextParagraph[] pArray = textBody.getPArray();
        DrawingParagraph[] o = new DrawingParagraph[pArray.length];

        for (int i=0; i<o.length; i++) {
            o[i] = new DrawingParagraph(pArray[i]);
        }

        return o;
    }
}
