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
package org.apache.poi.xwpf.usermodel;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.Units;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture;


/**
 * @author Philipp Epp
 */
public class XWPFPicture {

    private CTPicture ctPic;
    private String description;
    private XWPFRun run;

    public XWPFPicture(CTPicture ctPic, XWPFRun run) {
        this.run = run;
        this.ctPic = ctPic;
        description = ctPic.getNvPicPr().getCNvPr().getDescr();
    }

    /**
     * Link Picture with PictureData
     *
     * @param rel
     */
    public void setPictureReference(PackageRelationship rel) {
        ctPic.getBlipFill().getBlip().setEmbed(rel.getId());
    }

    /**
     * Return the underlying CTPicture bean that holds all properties for this picture
     *
     * @return the underlying CTPicture bean
     */
    public CTPicture getCTPicture() {
        return ctPic;
    }

    /**
     * Get the PictureData of the Picture, if present.
     * Note - not all kinds of picture have data
     */
    public XWPFPictureData getPictureData() {
        CTBlipFillProperties blipProps = ctPic.getBlipFill();

        if (blipProps == null || !blipProps.isSetBlip()) {
            // return null if Blip data is missing
            return null;
        }

        String blipId = blipProps.getBlip().getEmbed();
        POIXMLDocumentPart part = run.getParent().getPart();
        if (part != null) {
            POIXMLDocumentPart relatedPart = part.getRelationById(blipId);
            if (relatedPart instanceof XWPFPictureData) {
                return (XWPFPictureData) relatedPart;
            }
        }
        return null;
    }
   
    /**
     * Returns the width of the picture (in points).
     *
     * @since POI 4.1.1
     */
    public double getWidth() {
        return Units.toPoints(ctPic.getSpPr().getXfrm().getExt().getCx());
    }
   
    /**
     * Returns the depth of the picture (in points). 
     *
     * @since POI 4.1.1
     */
    public double getDepth() {
        return Units.toPoints(ctPic.getSpPr().getXfrm().getExt().getCy());
    }

    public String getDescription() {
        return description;
    }
}
