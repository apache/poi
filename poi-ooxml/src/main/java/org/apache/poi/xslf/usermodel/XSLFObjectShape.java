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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hpsf.ClassID;
import org.apache.poi.ooxml.POIXMLDocumentPart.RelationPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.util.XPathHelper;
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
    private static final QName[] GRAPHIC = { new QName(DML_NS, "graphic") };
    private static final QName[] GRAPHIC_DATA = { new QName(DML_NS, "graphicData") };
    private static final QName[] OLE_OBJ = { new QName(PML_NS, "oleObj") };
    private static final QName[] CT_PICTURE = { new QName(PML_NS, "pic") };

    private final CTOleObject _oleObject;
    private XSLFPictureData _data;

    /*package*/ XSLFObjectShape(CTGraphicalObjectFrame shape, XSLFSheet sheet){
        super(shape, sheet);

        // select oleObj potentially under AlternateContent
        // usually the mc:Choice element will be selected first
        try {
            _oleObject = XPathHelper.selectProperty(getXmlObject(), CTOleObject.class, null, GRAPHIC, GRAPHIC_DATA, OLE_OBJ);
        } catch (XmlException e) {
            // ole objects should be also inside AlternateContent tags, even with ECMA 376 edition 1
            throw new IllegalStateException(e);
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
        try {
            CTPicture pic = XPathHelper.selectProperty
                (getXmlObject(), CTPicture.class, XSLFObjectShape::parse, GRAPHIC, GRAPHIC_DATA, OLE_OBJ, CT_PICTURE);
            return (pic != null) ? pic.getBlipFill() : null;
        } catch (XmlException e) {
            return null;
        }
    }

    private static CTPicture parse(XMLStreamReader reader) throws XmlException {
        CTGroupShape gs = CTGroupShape.Factory.parse(reader);
        return (gs.sizeOfPicArray() > 0) ? gs.getPicArray(0) : null;
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

        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                addUpdatedData(rp.getDocumentPart().getPackagePart(),md,this);
            }
        };
    }

    private void addUpdatedData(PackagePart objectPart, ObjectMetaData metaData, ByteArrayOutputStream baos) throws IOException {
        objectPart.clear();
        try (InputStream bis = FileMagic.prepareToCheckMagic(baos.toInputStream());
             final OutputStream os = objectPart.getOutputStream()) {
            final FileMagic fm = FileMagic.valueOf(bis);

            if (fm == FileMagic.OLE2) {
                try (final POIFSFileSystem poifs = new POIFSFileSystem(bis)) {
                    poifs.getRoot().setStorageClsid(metaData.getClassID());
                    poifs.writeFilesystem(os);
                }
            } else if (metaData.getOleEntry() == null) {
                // OLE Name hasn't been specified, pass the input through
                baos.writeTo(os);
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

    /**
     * @param shapeId 1-based shapeId
     * @param picRel relationship to the picture data in the ooxml package
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
        try (XmlCursor grCur = gr.newCursor()) {
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

            try (XmlCursor picCur = grpShp.newCursor()) {
                picCur.toStartDoc();
                picCur.moveXmlContents(grCur);
            }
        }

        return frame;
    }
}
