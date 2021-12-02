/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.util;

import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;

import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D;
import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2DFontTextDrawer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.poi.util.Internal;

@Internal
public class PDFFormat implements OutputFormat {
    private final PDDocument document;
    private PDPageContentStream contentStream;
    private PdfBoxGraphics2D pdfBoxGraphics2D;
    private PdfBoxGraphics2DFontTextDrawer fontTextDrawer;

    public PDFFormat(boolean textAsShapes, String fontDir, String fontTtf) {
        if (!textAsShapes) {
            fontTextDrawer = new PDFFontMapper(fontDir, fontTtf);
        }

        document = new PDDocument();
    }

    @Override
    public Graphics2D addSlide(double width, double height) throws IOException {
        PDPage page = new PDPage(new PDRectangle((float) width, (float) height));
        document.addPage(page);
        contentStream = new PDPageContentStream(document, page);
        pdfBoxGraphics2D = new PdfBoxGraphics2D(document, (float) width, (float) height);
        if (fontTextDrawer != null) {
            pdfBoxGraphics2D.setFontTextDrawer(fontTextDrawer);
        }
        return pdfBoxGraphics2D;
    }

    @Override
    public void writeSlide(MFProxy proxy, File outFile) throws IOException {
        try {
            pdfBoxGraphics2D.dispose();

            PDFormXObject appearanceStream = pdfBoxGraphics2D.getXFormObject();
            contentStream.drawForm(appearanceStream);
        } finally {
            contentStream.close();
        }
    }

    @Override
    public void writeDocument(MFProxy proxy, File outFile) throws IOException {
        document.save(new File(outFile.getCanonicalPath()));
    }

    @Override
    public void close() throws IOException {
        try {
            document.close();
        } finally {
            if (fontTextDrawer != null) {
                fontTextDrawer.close();
            }
        }
    }

}
