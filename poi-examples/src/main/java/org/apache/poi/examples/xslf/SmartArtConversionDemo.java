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
package org.apache.poi.examples.xslf;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xslf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Converts SmartArt to openxml shapes and saves the result to the specified output path.
 */
public class SmartArtConversionDemo {

    private final XMLSlideShow inputPptx;
    private final XMLSlideShow outputPptx;

    SmartArtConversionDemo(XMLSlideShow inputPptx, XMLSlideShow outputPptx) {
        this.inputPptx = inputPptx;
        this.outputPptx = outputPptx;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Expected arguments: <inputPath> <outputPath>");
            System.exit(1);
        }

        File inputFile = new File(args[0]);
        if (!inputFile.exists()) {
            System.out.printf(LocaleUtil.getUserLocale(), "Unable to find input file at path: %s", args[0]);
            System.exit(1);
        }

        try (
                FileInputStream inputPptxStream = new FileInputStream(inputFile);
                FileOutputStream outputPptxStream = new FileOutputStream(args[1])
        ) {
            XMLSlideShow inputPptx = new XMLSlideShow(inputPptxStream);
            XMLSlideShow outputPptx = new XMLSlideShow();
            SmartArtConversionDemo demo = new SmartArtConversionDemo(inputPptx, outputPptx);
            demo.convertSmartArt();
            outputPptx.write(outputPptxStream);
        }
    }

    private static void copyAndUpdateImageRelations(XSLFDiagram diagram, XSLFSlide outputSlide) throws IOException {
        XSLFGroupShape inputGroupShape = diagram.getGroupShape();
        for (XSLFShape shape : inputGroupShape.getShapes()) {
            org.openxmlformats.schemas.presentationml.x2006.main.CTShape ctShape
                    = (org.openxmlformats.schemas.presentationml.x2006.main.CTShape) shape.getXmlObject();

            if (ctShape.getSpPr().getBlipFill() == null) {
                continue;
            }

            CTBlipFillProperties blipFillProps = ctShape.getSpPr().getBlipFill();
            CTBlip blip = blipFillProps.getBlip();
            // Relationships for SmartArt diagrams are stored in `drawing#.xml.rels`, not `slide#.xml.rels`.
            POIXMLDocumentPart inputPicturePart = diagram.getDiagramDrawing().getRelationById(blip.getEmbed());

            if (inputPicturePart == null || inputPicturePart.getPackagePart() == null) {
                continue;
            }

            XSLFPictureData inputPictureData = new XSLFPictureData(inputPicturePart.getPackagePart());

            // Copy the input image to the output slides and update the shape to reference the copied image
            XMLSlideShow outputPptx = outputSlide.getSlideShow();
            XSLFPictureData outputPictureData = outputPptx.addPicture(
                    inputPicturePart.getPackagePart().getInputStream(), inputPictureData.getType());
            POIXMLDocumentPart.RelationPart outputRelation = outputSlide.addRelation(null, XSLFRelation.IMAGES, outputPictureData);
            ctShape.getSpPr().getBlipFill().getBlip().setEmbed(outputRelation.getRelationship().getId());
        }
    }

    private static XSLFTheme extractTheme(XMLSlideShow slideShow) {
        if (!slideShow.getSlideMasters().isEmpty()) {
            return slideShow.getSlideMasters().get(0).getTheme();
        }
        return null;
    }

    private void convertSmartArt() throws IOException {
        // Copy page size and theme
        outputPptx.setPageSize(inputPptx.getPageSize());
        XSLFTheme theme = extractTheme(inputPptx);
        if (theme != null) {
            outputPptx.getSlideMasters().get(0).getTheme().getXmlObject().set(theme.getXmlObject());
        }

        for (XSLFSlide inputSlide : inputPptx.getSlides()) {
            XSLFSlide outputSlide = outputPptx.createSlide();

            List<XSLFShape> inputShapes = inputSlide.getShapes();
            for (XSLFShape shape : inputShapes) {
                if (shape instanceof XSLFDiagram) {
                    copyDiagramToOutput((XSLFDiagram) shape, outputSlide);
                } else {
                    XSLFAutoShape autoShape = outputSlide.createAutoShape();
                    // Hacky hack. Reassign xml to copy the content over.
                    autoShape.getXmlObject().set(shape.getXmlObject());
                }
            }
        }
    }

    private void copyDiagramToOutput(XSLFDiagram inputDiagram, XSLFSlide outputSlide) throws IOException {
        // This method modifies the underlying xml of the input shapes. We modify the xml structure first, then
        // assign that to our output shape.
        copyAndUpdateImageRelations(inputDiagram, outputSlide);
        XSLFGroupShape inputGroupShape = inputDiagram.getGroupShape();
        XSLFGroupShape outputGroupShape = outputSlide.createGroup();
        outputGroupShape.getXmlObject().set(inputGroupShape.getXmlObject());
    }
}
