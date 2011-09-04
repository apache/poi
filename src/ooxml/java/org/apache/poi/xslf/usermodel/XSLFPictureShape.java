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

package org.apache.poi.xslf.usermodel;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.util.Beta;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPicture;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPictureNonVisual;

import javax.imageio.ImageIO;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * @author Yegor Kozlov
 */
@Beta
public class XSLFPictureShape extends XSLFSimpleShape {
    private XSLFPictureData _data;

    /*package*/ XSLFPictureShape(CTPicture shape, XSLFSheet sheet) {
        super(shape, sheet);
    }


    /**
     * @param shapeId 1-based shapeId
     * @param rel     relationship to the picture data in the ooxml package
     */
    static CTPicture prototype(int shapeId, String rel) {
        CTPicture ct = CTPicture.Factory.newInstance();
        CTPictureNonVisual nvSpPr = ct.addNewNvPicPr();
        CTNonVisualDrawingProps cnv = nvSpPr.addNewCNvPr();
        cnv.setName("Picture " + shapeId);
        cnv.setId(shapeId + 1);
        nvSpPr.addNewCNvPicPr().addNewPicLocks().setNoChangeAspect(true);
        nvSpPr.addNewNvPr();

        CTBlipFillProperties blipFill = ct.addNewBlipFill();
        CTBlip blip = blipFill.addNewBlip();
        blip.setEmbed(rel);
        blipFill.addNewStretch().addNewFillRect();

        CTShapeProperties spPr = ct.addNewSpPr();
        CTPresetGeometry2D prst = spPr.addNewPrstGeom();
        prst.setPrst(STShapeType.RECT);
        prst.addNewAvLst();
        return ct;
    }

    /**
     * Resize this picture to the default size.
     * For PNG and JPEG resizes the image to 100%,
     * for other types sets the default size of 200x200 pixels.
     */
    public void resize() {
        XSLFPictureData pict = getPictureData();

        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(pict.getData()));
            setAnchor(new Rectangle2D.Double(0, 0, img.getWidth(), img.getHeight()));
        }
        catch (Exception e) {
            //default size is 200x200
            setAnchor(new java.awt.Rectangle(50, 50, 200, 200));
        }
    }

    public XSLFPictureData getPictureData() {
        if(_data == null){
            CTPicture ct = (CTPicture)getXmlObject();
            String blipId = ct.getBlipFill().getBlip().getEmbed();

            for (POIXMLDocumentPart part : getSheet().getRelations()) {
                if(part.getPackageRelationship().getId().equals(blipId)){
                    _data = (XSLFPictureData)part;
                }
            }
        }
        return _data;
    }
}