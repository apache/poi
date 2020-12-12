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

import static org.apache.poi.openxml4j.opc.PackageRelationshipTypes.CORE_PROPERTIES_ECMA376_NS;

import java.awt.Insets;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.ooxml.util.XPathHelper;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.PictureShape;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.util.Beta;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;
import org.apache.poi.xslf.draw.SVGImageRenderer;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
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
public class XSLFPictureShape extends XSLFSimpleShape
    implements PictureShape<XSLFShape,XSLFTextParagraph> {
    private static final POILogger LOG = POILogFactory.getLogger(XSLFPictureShape.class);

    private static final String MS_DML_NS = "http://schemas.microsoft.com/office/drawing/2010/main";
    private static final String MS_SVG_NS = "http://schemas.microsoft.com/office/drawing/2016/SVG/main";
    private static final String BITMAP_URI = "{28A0092B-C50C-407E-A947-70E740481C1C}";
    private static final String SVG_URI = "{96DAC541-7B7A-43D3-8B79-37D633B846F1}";

    private static final QName EMBED_TAG = new QName(CORE_PROPERTIES_ECMA376_NS, "embed", "rel");
    private static final QName[] BLIP_FILL = { new QName(PML_NS, "blipFill") };


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
        cnv.setId(shapeId);
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
     * Is this an internal picture (image data included within
     *  the PowerPoint file), or an external linked picture
     *  (image lives outside)?
     */
    public boolean isExternalLinkedPicture() {
        return getBlipId() == null && getBlipLink() != null;
    }

    /**
     * Return the data on the (internal) picture.
     * For an external linked picture, will return null
     */
    public XSLFPictureData getPictureData() {
        if(_data == null){
            String blipId = getBlipId();
            if (blipId == null) {
                return null;
            }
            _data = (XSLFPictureData)getSheet().getRelationById(blipId);
        }
        return _data;
    }

    @Override
    public void setPlaceholder(Placeholder placeholder) {
        super.setPlaceholder(placeholder);
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

    protected CTBlipFillProperties getBlipFill() {
        CTPicture ct = (CTPicture)getXmlObject();
        CTBlipFillProperties bfp = ct.getBlipFill();
        if (bfp != null) {
            return bfp;
        }

        try {
            return XPathHelper.selectProperty(getXmlObject(), CTBlipFillProperties.class, XSLFPictureShape::parse, BLIP_FILL);
        } catch (XmlException xe) {
            return null;
        }
    }

    private static CTBlipFillProperties parse(XMLStreamReader reader) throws XmlException {
        CTPicture pic = CTPicture.Factory.parse(reader);
        return (pic != null) ? pic.getBlipFill() : null;
    }

    protected CTBlip getBlip(){
        return getBlipFill().getBlip();
    }

    @SuppressWarnings("WeakerAccess")
    protected String getBlipLink(){
        CTBlip blip = getBlip();
        if (blip != null) {
            String link = blip.getLink();
            return (link.isEmpty()) ? null : link;
        } else {
            return null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected String getBlipId(){
        CTBlip blip = getBlip();
        if (blip != null) {
            String id = blip.getEmbed();
            return (id.isEmpty()) ? null : id;
        } else {
            return null;
        }
    }

    @Override
    public Insets getClipping(){
        CTRelativeRect r = getBlipFill().getSrcRect();

        return (r == null) ? null : new Insets(
            POIXMLUnits.parsePercent(r.xgetT()),
            POIXMLUnits.parsePercent(r.xgetL()),
            POIXMLUnits.parsePercent(r.xgetB()),
            POIXMLUnits.parsePercent(r.xgetR()));
    }

    /**
     * Add a SVG image reference
     * @param svgPic a previously imported svg image
     *
     * @since POI 4.1.0
     */
    public void setSvgImage(XSLFPictureData svgPic) {
        CTBlip blip = getBlip();
        CTOfficeArtExtensionList extLst = blip.isSetExtLst() ? blip.getExtLst() : blip.addNewExtLst();

        final int bitmapId = getExt(extLst, BITMAP_URI);
        CTOfficeArtExtension extBitmap;
        if (bitmapId == -1) {
            extBitmap = extLst.addNewExt();
            extBitmap.setUri(BITMAP_URI);
            XmlCursor cur = extBitmap.newCursor();
            cur.toEndToken();
            cur.beginElement(new QName(MS_DML_NS, "useLocalDpi", "a14"));
            cur.insertNamespace("a14", MS_DML_NS);
            cur.insertAttributeWithValue("val", "0");
            cur.dispose();
        }

        final int svgId = getExt(extLst, SVG_URI);
        if (svgId != -1) {
            extLst.removeExt(svgId);
        }

        String svgRelId = getSheet().getRelationId(svgPic);
        if (svgRelId == null) {
            svgRelId = getSheet().addRelation(null, XSLFRelation.IMAGE_SVG, svgPic).getRelationship().getId();
        }

        CTOfficeArtExtension svgBitmap = extLst.addNewExt();
        svgBitmap.setUri(SVG_URI);
        XmlCursor cur = svgBitmap.newCursor();
        cur.toEndToken();
        cur.beginElement(new QName(MS_SVG_NS, "svgBlip", "asvg"));
        cur.insertNamespace("asvg", MS_SVG_NS);
        cur.insertAttributeWithValue(EMBED_TAG, svgRelId);
        cur.dispose();
    }

    @Override
    public PictureData getAlternativePictureData() {
        return getSvgImage();
    }

    public XSLFPictureData getSvgImage() {
        CTBlip blip = getBlip();
        if (blip == null) {
            return null;
        }
        CTOfficeArtExtensionList extLst = blip.getExtLst();
        if (extLst == null) {
            return null;
        }

        int size = extLst.sizeOfExtArray();
        for (int i = 0; i < size; i++) {
            XmlCursor cur = extLst.getExtArray(i).newCursor();
            try {
                if (cur.toChild(MS_SVG_NS, "svgBlip")) {
                    String svgRelId = cur.getAttributeText(EMBED_TAG);
                    return (svgRelId != null) ? (XSLFPictureData) getSheet().getRelationById(svgRelId) : null;
                }
            } finally {
                cur.dispose();
            }
        }
        return null;
    }

    /**
     * Convienence method for adding SVG images, which generates the preview image
     * @param sheet the sheet to add
     * @param svgPic the svg picture to add
     * @param previewType the preview picture type or null (defaults to PNG) - currently only JPEG,GIF,PNG are allowed
     * @param anchor the image anchor (for calculating the preview image size) or
     *               null (the preview size is taken from the svg picture bounds)
     *
     * @since POI 4.1.0
     */
    public static XSLFPictureShape addSvgImage(XSLFSheet sheet, XSLFPictureData svgPic, PictureType previewType, Rectangle2D anchor) throws IOException {

        SVGImageRenderer renderer = new SVGImageRenderer();
        try (InputStream is = svgPic.getInputStream()) {
            renderer.loadImage(is, svgPic.getType().contentType);
        }

        Dimension2D dim = renderer.getDimension();
        Rectangle2D anc = (anchor != null) ? anchor
            : new Rectangle2D.Double(0,0, Units.pixelToPoints((int)dim.getWidth()), Units.pixelToPoints((int)dim.getHeight()));

        PictureType pt = (previewType != null) ? previewType : PictureType.PNG;
        if (pt != PictureType.JPEG || pt != PictureType.GIF || pt != PictureType.PNG) {
            pt = PictureType.PNG;
        }

        BufferedImage thmBI = renderer.getImage(dim);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(100000);
        // use extension instead of enum name, because of "jpeg"
        ImageIO.write(thmBI, pt.extension.substring(1), bos);

        XSLFPictureData pngPic = sheet.getSlideShow().addPicture(new ByteArrayInputStream(bos.toByteArray()), pt);

        XSLFPictureShape shape = sheet.createPicture(pngPic);
        shape.setAnchor(anc);
        shape.setSvgImage(svgPic);
        return shape;
    }


    private int getExt(CTOfficeArtExtensionList extLst, String uri) {
        final int size = extLst.sizeOfExtArray();
        for (int i=0; i<size; i++) {
            CTOfficeArtExtension ext = extLst.getExtArray(i);
            if (uri.equals(ext.getUri())) {
                return i;
            }
        }
        return -1;
    }


    @Override
    void copy(XSLFShape sh){
        super.copy(sh);

        XSLFPictureShape p = (XSLFPictureShape)sh;
        String blipId = p.getBlipId();
        if (blipId == null) {
            LOG.log(POILogger.WARN, "unable to copy invalid picture shape");
            return;
        }

        String relId = getSheet().importBlip(blipId, p.getSheet());

        CTPicture ct = (CTPicture)getXmlObject();
        CTBlip blip = getBlipFill().getBlip();
        blip.setEmbed(relId);

        CTApplicationNonVisualDrawingProps nvPr = ct.getNvPicPr().getNvPr();
        if(nvPr.isSetCustDataLst()) {
            // discard any custom tags associated with the picture being copied
            nvPr.unsetCustDataLst();
        }
        if(blip.isSetExtLst()) {
            // TODO: check for SVG copying
            CTOfficeArtExtensionList extLst = blip.getExtLst();
            //noinspection deprecation
            for(CTOfficeArtExtension ext : extLst.getExtArray()){
                String xpath = "declare namespace a14='"+ MS_DML_NS +"' $this//a14:imgProps/a14:imgLayer";
                XmlObject[] obj = ext.selectPath(xpath);
                if(obj != null && obj.length == 1){
                    XmlCursor c = obj[0].newCursor();
                    String id = c.getAttributeText(EMBED_TAG);
                    String newId = getSheet().importBlip(id, p.getSheet());
                    c.setAttributeText(EMBED_TAG, newId);
                    c.dispose();
                }
            }
        }
    }
}