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

public class XWPFSignatureLine extends SignatureLine {
    static final String NS_OOXML_WP_MAIN = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
    private static final String MS_VML_URN = "urn:schemas-microsoft-com:vml";

    private CTSignatureLine line;

    public void parse(XWPFDocument doc) throws XmlException {
        line = XPathHelper.selectProperty(doc.getDocument(), CTSignatureLine.class, null,
            new QName[]{new QName(NS_OOXML_WP_MAIN, "body")},
            new QName[]{new QName(NS_OOXML_WP_MAIN, "p")},
            new QName[]{new QName(NS_OOXML_WP_MAIN, "r")},
            new QName[]{new QName(NS_OOXML_WP_MAIN, "pict")},
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

    private static int mapType(PictureType type) throws InvalidFormatException {
        switch (type) {
            case BMP:
                return Document.PICTURE_TYPE_BMP;
            case DIB:
                return Document.PICTURE_TYPE_DIB;
            case EMF:
                return Document.PICTURE_TYPE_EMF;
            case EPS:
                return Document.PICTURE_TYPE_EPS;
            case GIF:
                return Document.PICTURE_TYPE_GIF;
            case JPEG:
                return Document.PICTURE_TYPE_JPEG;
            case PICT:
                return Document.PICTURE_TYPE_PICT;
            case PNG:
                return Document.PICTURE_TYPE_PNG;
            case TIFF:
                return Document.PICTURE_TYPE_TIFF;
            case WMF:
                return Document.PICTURE_TYPE_WMF;
            case WPG:
                return Document.PICTURE_TYPE_WPG;
            default:
                throw new InvalidFormatException("Unsupported picture format "+type);
        }
    }
}
