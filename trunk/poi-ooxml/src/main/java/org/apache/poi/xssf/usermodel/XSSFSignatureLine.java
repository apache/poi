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

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.namespace.QName;

import com.microsoft.schemas.office.excel.CTClientData;
import com.microsoft.schemas.office.excel.STObjectType;
import com.microsoft.schemas.office.office.CTSignatureLine;
import com.microsoft.schemas.vml.CTImageData;
import com.microsoft.schemas.vml.CTShape;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.ooxml.util.XPathHelper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.crypt.dsig.SignatureLine;
import org.apache.poi.schemas.vmldrawing.CTXML;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STTrueFalseBlank;

public class XSSFSignatureLine extends SignatureLine {
    private static final String MS_VML_URN = "urn:schemas-microsoft-com:vml";

    public void parse(XSSFSheet sheet) throws XmlException {
        XSSFVMLDrawing vml = sheet.getVMLDrawing(false);
        if (vml == null) {
            return;
        }
        CTSignatureLine line = XPathHelper.selectProperty(vml.getDocument(), CTSignatureLine.class, null,
              new QName[]{XSSFVMLDrawing.QNAME_VMLDRAWING},
              new QName[]{new QName(MS_VML_URN, "shape")},
              new QName[]{QNAME_SIGNATURE_LINE});

        if (line != null) {
            setSignatureShape(line);
            parse();
        }
    }

    public void add(XSSFSheet sheet, XSSFClientAnchor anchor) {
        XSSFVMLDrawing vml = sheet.getVMLDrawing(true);
        CTXML root = vml.getDocument().getXml();
        add(root, (image, type) -> addPicture(image,type,sheet));
        CTShape shape = getSignatureShape();
        CTClientData clientData = shape.addNewClientData();
        // LeftColumn, LeftOffset, TopRow, TopOffset, RightColumn, RightOffset, BottomRow, BottomOffset
        String anchorStr =
            anchor.getCol1()+", "+
            anchor.getDx1()+", "+
            anchor.getRow1()+", "+
            anchor.getDy1()+", "+
            anchor.getCol2()+", "+
            anchor.getDx2()+", "+
            anchor.getRow2()+", "+
            anchor.getDy2();
//        anchorStr = "2, 0, 3, 0, 5, 136, 9, 32";
        clientData.addAnchor(anchorStr);
        clientData.setObjectType(STObjectType.PICT);
        clientData.addSizeWithCells(STTrueFalseBlank.X);
        clientData.addCF("pict");
        clientData.addAutoPict(STTrueFalseBlank.X);
    }

    @Override
    protected void setRelationId(CTImageData imageData, String relId) {
        imageData.setRelid(relId);
    }

    private String addPicture(byte[] image, PictureType type, XSSFSheet sheet) throws InvalidFormatException {
        XSSFWorkbook wb = sheet.getWorkbook();
        XSSFVMLDrawing vml = sheet.getVMLDrawing(false);
        POIXMLRelation xtype = mapType(type);
        int idx = wb.getNextPartNumber(xtype, -1);
        POIXMLDocumentPart.RelationPart rp = vml.createRelationship(xtype, wb.getXssfFactory(), idx, false);
        POIXMLDocumentPart dp = rp.getDocumentPart();
        try (OutputStream out = dp.getPackagePart().getOutputStream()) {
            out.write(image);
        } catch (IOException e) {
            throw new POIXMLException(e);
        }
        return rp.getRelationship().getId();
    }


    private static POIXMLRelation mapType(PictureType type) throws InvalidFormatException {
        switch (type) {
            case BMP:
                return XSSFRelation.IMAGE_BMP;
            case DIB:
                return XSSFRelation.IMAGE_DIB;
            case EMF:
                return XSSFRelation.IMAGE_EMF;
            case EPS:
                return XSSFRelation.IMAGE_EPS;
            case GIF:
                return XSSFRelation.IMAGE_GIF;
            case JPEG:
                return XSSFRelation.IMAGE_JPEG;
            case PICT:
                return XSSFRelation.IMAGE_PICT;
            case PNG:
                return XSSFRelation.IMAGE_PNG;
            case TIFF:
                return XSSFRelation.IMAGE_TIFF;
            case WMF:
                return XSSFRelation.IMAGE_WMF;
            case WPG:
                return XSSFRelation.IMAGE_WPG;
            default:
                throw new InvalidFormatException("Unsupported picture format "+type);
        }
    }
}