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

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;

import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.Beta;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOfficeArtExtension;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOfficeArtExtensionList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRelativeRect;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTApplicationNonVisualDrawingProps;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPicture;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPictureNonVisual;

/**
 * Represents a picture shape
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
    
    /**
     * Is this an internal picture (image data included within
     *  the PowerPoint file), or an external linked picture
     *  (image lives outside)?
     */
    public boolean isExternalLinkedPicture() {
        if (getBlipId() == null && getBlipLink() != null) {
            return true;
        }
        return false;
    }

    /**
     * Return the data on the (internal) picture.
     * For an external linked picture, will return null
     */
    public XSLFPictureData getPictureData() {
        if(_data == null){
            String blipId = getBlipId();
            if (blipId == null) return null;

            PackagePart p = getSheet().getPackagePart();
            PackageRelationship rel = p.getRelationship(blipId);
            if (rel != null) {
                try {
                    PackagePart imgPart = p.getRelatedPart(rel);
                    _data = new XSLFPictureData(imgPart, rel);
                }
                catch (Exception e) {
                    throw new POIXMLException(e);
                }
            }
        }
        return _data;
    }
    
    /**
     * For an external linked picture, return the last-seen
     *  path to the picture.
     * For an internal picture, returns null.
     */
    public URI getPictureLink() {
        if (getBlipId() != null) {
            // Internal picture, nothing to return
            return null;
        }
        
        String rId = getBlipLink();
        if (rId == null) {
            // No link recorded, nothing we can do
            return null;
        }
        
        PackagePart p = getSheet().getPackagePart();
        PackageRelationship rel = p.getRelationship(rId);
        if (rel != null) {
            return rel.getTargetURI();
        }
        return null;
    }

    private CTBlip getBlip(){
        CTPicture ct = (CTPicture)getXmlObject();
        return ct.getBlipFill().getBlip();
    }
    private String getBlipLink(){
        String link = getBlip().getLink();
        if (link.isEmpty()) return null;
        return link;
    }
    private String getBlipId(){
        String id = getBlip().getEmbed();
        if (id.isEmpty()) return null;
        return id;
    }

    public Insets getBlipClip(){
        CTPicture ct = (CTPicture)getXmlObject();
        CTRelativeRect r = ct.getBlipFill().getSrcRect();
        return (r == null) ? null : new Insets(r.getT(), r.getL(), r.getB(), r.getR());
    }

    @Override
    public void drawContent(Graphics2D graphics) {

        XSLFPictureData data = getPictureData();
    	if(data == null) return;

        XSLFImageRenderer renderer = (XSLFImageRenderer)graphics.getRenderingHint(XSLFRenderingHint.IMAGE_RENDERER);
        if(renderer == null) renderer = new XSLFImageRenderer();

        RenderableShape rShape = new RenderableShape(this);
        Rectangle2D anchor = rShape.getAnchor(graphics);
        
        Insets insets = getBlipClip();

        renderer.drawImage(graphics, data, anchor, insets);
    }


    @Override
    void copy(XSLFShape sh){
        super.copy(sh);

        XSLFPictureShape p = (XSLFPictureShape)sh;
        String blipId = p.getBlipId();
        String relId = getSheet().importBlip(blipId, p.getSheet().getPackagePart());

        CTPicture ct = (CTPicture)getXmlObject();
        CTBlip blip = ct.getBlipFill().getBlip();
        blip.setEmbed(relId);

        CTApplicationNonVisualDrawingProps nvPr = ct.getNvPicPr().getNvPr();
        if(nvPr.isSetCustDataLst()) {
            // discard any custom tags associated with the picture being copied
            nvPr.unsetCustDataLst();
        }
        if(blip.isSetExtLst()) {

            CTOfficeArtExtensionList extLst = blip.getExtLst();
            for(CTOfficeArtExtension ext : extLst.getExtArray()){
                String xpath = "declare namespace a14='http://schemas.microsoft.com/office/drawing/2010/main' $this//a14:imgProps/a14:imgLayer";
                XmlObject[] obj = ext.selectPath(xpath);
                if(obj != null && obj.length == 1){
                    XmlCursor c = obj[0].newCursor();
                    String id = c.getAttributeText(new QName("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "embed"));//selectPath("declare namespace r='http://schemas.openxmlformats.org/officeDocument/2006/relationships' $this//[@embed]");
                    String newId = getSheet().importBlip(id, p.getSheet().getPackagePart());
                    c.setAttributeText(new QName("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "embed"), newId);
                    c.dispose();
                }
            }
        }

    }
}
