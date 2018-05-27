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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.POIXMLDocumentPart.RelationPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.hpsf.ClassID;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.Ole10Native;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.usermodel.ObjectMetaData;
import org.apache.poi.sl.usermodel.ObjectMetaData.Application;
import org.apache.poi.sl.usermodel.ObjectShape;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObjectData;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrameNonVisual;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTOleObject;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPicture;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPictureNonVisual;

public class XSLFObjectShape extends XSLFGraphicFrame implements ObjectShape<XSLFShape,XSLFTextParagraph> {

    /* package */ static final String OLE_URI = "http://schemas.openxmlformats.org/presentationml/2006/ole";

    private CTOleObject _oleObject;
    private XSLFPictureData _data;

    /*package*/ XSLFObjectShape(CTGraphicalObjectFrame shape, XSLFSheet sheet){
        super(shape, sheet);

        CTGraphicalObjectData god = shape.getGraphic().getGraphicData();
        XmlCursor xc = god.newCursor();
        // select oleObj potentially under AlternateContent
        // usually the mc:Choice element will be selected first
        xc.selectPath("declare namespace p='"+PML_NS+"' .//p:oleObj");
        try {
            if (!xc.toNextSelection()) {
                throw new IllegalStateException("p:oleObj element was not found in\n " + god);
            }

            XmlObject xo = xc.getObject();
            // Pesky XmlBeans bug - see Bugzilla #49934
            // it never happens when using the full ooxml-schemas jar but may happen with the abridged poi-ooxml-schemas
            if (xo instanceof XmlAnyTypeImpl){
                String errStr =
                    "Schemas (*.xsb) for CTOleObject can't be loaded - usually this happens when OSGI " +
                    "loading is used and the thread context classloader has no reference to " +
                    "the xmlbeans classes - use POIXMLTypeLoader.setClassLoader() to set the loader, " +
                    "e.g. with CTOleObject.class.getClassLoader()"
                ;
                throw new IllegalStateException(errStr);
            }
            _oleObject = (CTOleObject)xo;
        } finally {
            xc.dispose();
        }
    }

    @Internal
    public CTOleObject getCTOleObject(){
        return _oleObject;
    }

    @Override
    public XSLFObjectData getObjectData() {
        String oleRel = getCTOleObject().getId();
        return getSheet().getRelationPartById(oleRel).getDocumentPart();
    }

    @Override
    public String getProgId() {
        return (_oleObject == null) ? null : _oleObject.getProgId();
    }
    
    @Override
    public String getFullName() {
        return (_oleObject == null) ? null : _oleObject.getName();
    }
    
    
    /**
     * Return the data on the (internal) picture.
     * For an external linked picture, will return null
     */
    @Override
    public XSLFPictureData getPictureData() {
        if(_data == null){
            String blipId = getBlipId();
            if (blipId == null) {
                return null;
            }

            PackagePart p = getSheet().getPackagePart();
            PackageRelationship rel = p.getRelationship(blipId);
            if (rel != null) {
                try {
                    PackagePart imgPart = p.getRelatedPart(rel);
                    _data = new XSLFPictureData(imgPart);
                }
                catch (Exception e) {
                    throw new POIXMLException(e);
                }
            }
        }
        return _data;
    }

    protected CTBlip getBlip(){
        return getBlipFill().getBlip();
    }

    protected String getBlipId(){
        String id = getBlip().getEmbed();
        if (id.isEmpty()) {
            return null;
        }
        return id;
    }

    protected CTBlipFillProperties getBlipFill() {
        String xquery =
                "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' "
              + ".//p:blipFill"
              ;
        XmlObject xo = selectProperty(XmlObject.class, xquery);
        try {
            xo = CTPicture.Factory.parse(xo.getDomNode());
        } catch (XmlException xe) {
            return null;
        }
        return ((CTPicture)xo).getBlipFill();
    }


    @Override
    public OutputStream updateObjectData(final Application application, final ObjectMetaData metaData) throws IOException {
        final ObjectMetaData md = (application != null) ? application.getMetaData() : metaData;
        if (md == null || md.getClassID() == null) {
            throw new IllegalArgumentException("either application and/or metaData needs to be set.");
        }


        final XSLFSheet sheet = getSheet();

        final RelationPart rp;
        if (_oleObject.isSetId()) {
            // object data was already set
            rp = sheet.getRelationPartById(_oleObject.getId());
        } else {
            // object data needs to be initialized
            try {
                final XSLFRelation descriptor = XSLFRelation.OLE_OBJECT;
                final OPCPackage pack = sheet.getPackagePart().getPackage();
                int nextIdx = pack.getUnusedPartIndex(descriptor.getDefaultFileName());
                rp = sheet.createRelationship(descriptor, XSLFFactory.getInstance(), nextIdx, false);
                _oleObject.setId(rp.getRelationship().getId());
            } catch (InvalidFormatException e) {
                throw new IOException("Unable to add new ole embedding", e);
            }

            // setting spid only works with a vml drawing object
            // oleObj.setSpid("_x0000_s"+(1025+objectIdx));
        }

        _oleObject.setProgId(md.getProgId());
        _oleObject.setName(md.getObjectName());

        return new XSLFObjectOutputStream(rp.getDocumentPart().getPackagePart(),md);
    }

    private static class XSLFObjectOutputStream extends ByteArrayOutputStream {
        final PackagePart objectPart;
        final ObjectMetaData metaData;
        private XSLFObjectOutputStream(final PackagePart objectPart, final ObjectMetaData metaData) {
            super(100000);
            this.objectPart = objectPart;
            this.metaData = metaData;
        }

        public void close() throws IOException {
            objectPart.clear();
            try (final OutputStream os = objectPart.getOutputStream()) {
                final ByteArrayInputStream bis = new ByteArrayInputStream(this.buf, 0, size());
                final FileMagic fm = FileMagic.valueOf(this.buf);

                if (fm == FileMagic.OLE2) {
                    try (final POIFSFileSystem poifs = new POIFSFileSystem(bis)) {
                        poifs.getRoot().setStorageClsid(metaData.getClassID());
                        poifs.writeFilesystem(os);
                    }
                } else if (metaData.getOleEntry() == null) {
                    // OLE Name hasn't been specified, pass the input through
                    os.write(this.buf, 0, size());
                } else {
                    try (final POIFSFileSystem poifs = new POIFSFileSystem()) {
                        final ClassID clsId = metaData.getClassID();
                        if (clsId != null) {
                            poifs.getRoot().setStorageClsid(clsId);
                        }
                        poifs.createDocument(bis, metaData.getOleEntry());

                        Ole10Native.createOleMarkerEntry(poifs);

                        poifs.writeFilesystem(os);
                    }
                }
            }
        }
    }


    /**
     *
     *
     * @param shapeId 1-based shapeId
     * @param picRel relationship to the picture data in the ooxml package
     * @return
     */
    static CTGraphicalObjectFrame prototype(int shapeId, String picRel){
        CTGraphicalObjectFrame frame = CTGraphicalObjectFrame.Factory.newInstance();
        CTGraphicalObjectFrameNonVisual nvGr = frame.addNewNvGraphicFramePr();

        CTNonVisualDrawingProps cnv = nvGr.addNewCNvPr();
        // usually the shape name has its index based on the n-th embeding, but having
        // the prototype separate from the actual updating of the object, we use the shape id
        cnv.setName("Object " + shapeId);
        cnv.setId(shapeId);

        // add empty property elements otherwise Powerpoint doesn't load the file ...
        nvGr.addNewCNvGraphicFramePr();
        nvGr.addNewNvPr();

        frame.addNewXfrm();
        CTGraphicalObjectData gr = frame.addNewGraphic().addNewGraphicData();
        gr.setUri(OLE_URI);
        XmlCursor grCur = gr.newCursor();
        grCur.toEndToken();
        grCur.beginElement(new QName(PML_NS, "oleObj"));
        grCur.insertElement(new QName(PML_NS, "embed"));


        CTGroupShape grpShp = CTGroupShape.Factory.newInstance();
        CTPicture pic = grpShp.addNewPic();
        CTPictureNonVisual nvPicPr = pic.addNewNvPicPr();
        CTNonVisualDrawingProps cNvPr = nvPicPr.addNewCNvPr();
        cNvPr.setName("");
        cNvPr.setId(0);
        nvPicPr.addNewCNvPicPr();
        nvPicPr.addNewNvPr();


        CTBlipFillProperties blip = pic.addNewBlipFill();
        blip.addNewBlip().setEmbed(picRel);
        blip.addNewStretch().addNewFillRect();

        CTShapeProperties spPr = pic.addNewSpPr();
        CTTransform2D xfrm = spPr.addNewXfrm();
        CTPoint2D off = xfrm.addNewOff();
        off.setX(1270000);
        off.setY(1270000);
        CTPositiveSize2D xext = xfrm.addNewExt();
        xext.setCx(1270000);
        xext.setCy(1270000);

        spPr.addNewPrstGeom().setPrst(STShapeType.RECT);


        XmlCursor picCur = grpShp.newCursor();
        picCur.toStartDoc();
        picCur.moveXmlContents(grCur);
        picCur.dispose();

        grCur.dispose();


        return frame;
    }
}
