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

import javax.xml.namespace.QName;

import com.microsoft.schemas.office.office.CTSignatureLine;
import com.microsoft.schemas.vml.CTImageData;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.ooxml.util.XPathHelper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.crypt.dsig.SignatureLine;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPicture;

import static org.apache.poi.xssf.usermodel.XSSFRelation.NS_WORDPROCESSINGML;

public class XWPFSignatureLine extends SignatureLine {
    private static final String MS_VML_URN = "urn:schemas-microsoft-com:vml";

    private CTSignatureLine line;

    public void parse(XWPFDocument doc) throws XmlException {
        line = XPathHelper.selectProperty(doc.getDocument(), CTSignatureLine.class, null,
            new QName[]{new QName(NS_WORDPROCESSINGML, "body")},
            new QName[]{new QName(NS_WORDPROCESSINGML, "p")},
            new QName[]{new QName(NS_WORDPROCESSINGML, "r")},
            new QName[]{new QName(NS_WORDPROCESSINGML, "pict")},
            new QName[]{new QName(MS_VML_URN, "shape")},
            new QName[]{QNAME_SIGNATURE_LINE});
        if (line != null) {
            setSignatureShape(line);
            parse();
        }
    }

    public void add(XWPFParagraph paragraph) {
        XWPFRun r = paragraph.createRun();
        CTPicture pict = r.getCTR().addNewPict();
        add(pict, (image, type) -> paragraph.getDocument().addPictureData(image, mapType(type)));
    }

    @Override
    protected void setRelationId(CTImageData imageData, String relId) {
        imageData.setId2(relId);
    }

    private static PictureType mapType(org.apache.poi.common.usermodel.PictureType type) throws InvalidFormatException {
        switch (type) {
            case BMP:
                return PictureType.BMP;
            case DIB:
                return PictureType.DIB;
            case EMF:
                return PictureType.EMF;
            case EPS:
                return PictureType.EPS;
            case GIF:
                return PictureType.GIF;
            case JPEG:
                return PictureType.JPEG;
            case PICT:
                return PictureType.PICT;
            case PNG:
                return PictureType.PNG;
            case TIFF:
                return PictureType.TIFF;
            case WMF:
                return PictureType.WMF;
            case WPG:
                return PictureType.WPG;
            case WDP:
                return PictureType.WDP;
            default:
                throw new InvalidFormatException("Unsupported picture format "+type);
        }
    }
}
