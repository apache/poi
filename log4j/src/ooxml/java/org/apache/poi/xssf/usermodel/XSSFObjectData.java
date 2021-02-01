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

package org.apache.poi.xssf.usermodel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.ObjectData;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOfficeArtExtension;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOfficeArtExtensionList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShape;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShapeNonVisual;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTOleObject;

/**
 * Represents binary object (i.e. OLE) data stored in the file.  Eg. A GIF, JPEG etc...
 */
public class XSSFObjectData extends XSSFSimpleShape implements ObjectData {
    private static final POILogger LOG = POILogFactory.getLogger(XSSFObjectData.class);
    
    /**
     * A default instance of CTShape used for creating new shapes.
     */
    private static CTShape prototype;

    private CTOleObject oleObject;

    protected XSSFObjectData(XSSFDrawing drawing, CTShape ctShape) {
        super(drawing, ctShape);
    }

    /**
     * Prototype with the default structure of a new auto-shape.
     */
    /**
     * Prototype with the default structure of a new auto-shape.
     */
    protected static CTShape prototype() {
        final String drawNS = "http://schemas.microsoft.com/office/drawing/2010/main";

        if(prototype == null) {
            CTShape shape = CTShape.Factory.newInstance();

            CTShapeNonVisual nv = shape.addNewNvSpPr();
            CTNonVisualDrawingProps nvp = nv.addNewCNvPr();
            nvp.setId(1);
            nvp.setName("Shape 1");
//            nvp.setHidden(true);
            CTOfficeArtExtensionList extLst = nvp.addNewExtLst();
            // https://msdn.microsoft.com/en-us/library/dd911027(v=office.12).aspx
            CTOfficeArtExtension ext = extLst.addNewExt();
            ext.setUri("{63B3BB69-23CF-44E3-9099-C40C66FF867C}");
            XmlCursor cur = ext.newCursor();
            cur.toEndToken();
            cur.beginElement(new QName(drawNS, "compatExt", "a14"));
            cur.insertNamespace("a14", drawNS);
            cur.insertAttributeWithValue("spid", "_x0000_s1");
            cur.dispose();
            
            nv.addNewCNvSpPr();

            CTShapeProperties sp = shape.addNewSpPr();
            CTTransform2D t2d = sp.addNewXfrm();
            CTPositiveSize2D p1 = t2d.addNewExt();
            p1.setCx(0);
            p1.setCy(0);
            CTPoint2D p2 = t2d.addNewOff();
            p2.setX(0);
            p2.setY(0);

            CTPresetGeometry2D geom = sp.addNewPrstGeom();
            geom.setPrst(STShapeType.RECT);
            geom.addNewAvLst();

            prototype = shape;
        }
        return prototype;
    }

    
    
    
    @Override
    public String getOLE2ClassName() {
        return getOleObject().getProgId();
    }

    /**
     * @return the CTOleObject associated with the shape 
     */
    public CTOleObject getOleObject() {
        if (oleObject == null) {
            long shapeId = getCTShape().getNvSpPr().getCNvPr().getId();
            oleObject = getSheet().readOleObject(shapeId);
            if (oleObject == null) {
                throw new POIXMLException("Ole object not found in sheet container - it's probably a control element");
            }
        }
        return oleObject;
    }
    
    @Override
    public byte[] getObjectData() throws IOException {
        InputStream is = getObjectPart().getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(is, bos);
        is.close();
        return bos.toByteArray();
    }
    
    /**
     * @return the package part of the object data
     */
    public PackagePart getObjectPart() {
        if (!getOleObject().isSetId()) {
            throw new POIXMLException("Invalid ole object found in sheet container");
        }
        POIXMLDocumentPart pdp = getSheet().getRelationById(getOleObject().getId());
        return (pdp == null) ? null : pdp.getPackagePart();
    }

    @Override
    public boolean hasDirectoryEntry() {
        InputStream is = null;
        try {
            is = getObjectPart().getInputStream();
            is = FileMagic.prepareToCheckMagic(is);
            return FileMagic.valueOf(is) == FileMagic.OLE2;
        } catch (IOException e) {
            LOG.log(POILogger.WARN, "can't determine if directory entry exists", e);
            return false;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Override
    public DirectoryEntry getDirectory() throws IOException {
        try (InputStream is = getObjectPart().getInputStream()) {
            return new POIFSFileSystem(is).getRoot();
        }
    }

    /**
     * The filename of the embedded image
     */
    @Override
    public String getFileName() {
        return getObjectPart().getPartName().getName();
    }
    
    protected XSSFSheet getSheet() {
        return (XSSFSheet)getDrawing().getParent();
    }

    @Override
    public XSSFPictureData getPictureData() {
        XmlCursor cur = getOleObject().newCursor();
        try {
            if (cur.toChild(XSSFRelation.NS_SPREADSHEETML, "objectPr")) {
                String blipId = cur.getAttributeText(new QName(PackageRelationshipTypes.CORE_PROPERTIES_ECMA376_NS, "id"));
                return (XSSFPictureData)getSheet().getRelationById(blipId);
            }
            return null;
        } finally {
            cur.dispose();
        }
    }

    @Override
    public String getContentType() {
        return getObjectPart().getContentType();
    }
}
