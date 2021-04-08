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

package org.apache.poi.xslf.usermodel;

import static org.apache.poi.openxml4j.opc.PackageRelationshipTypes.CORE_PROPERTIES_ECMA376_NS;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.apache.xmlbeans.XmlCursor;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.presentationml.x2006.main.CTApplicationNonVisualDrawingProps;
import org.openxmlformats.schemas.presentationml.x2006.main.CTExtension;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPicture;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.CTTLCommonMediaNodeData;
import org.openxmlformats.schemas.presentationml.x2006.main.CTTLCommonTimeNodeData;
import org.openxmlformats.schemas.presentationml.x2006.main.CTTimeNodeList;
import org.openxmlformats.schemas.presentationml.x2006.main.STTLTimeIndefinite;
import org.openxmlformats.schemas.presentationml.x2006.main.STTLTimeNodeFillType;
import org.openxmlformats.schemas.presentationml.x2006.main.STTLTimeNodeRestartType;
import org.openxmlformats.schemas.presentationml.x2006.main.STTLTimeNodeType;

/**
 * Reimplement and test examples classes so the necessary XmlBeans schema objects are included
 * in the lite schema
 */
class TestXSLFExamples {
    @Test
    void LinkVideoToPptx() throws IOException, URISyntaxException {
        String videoFileName = "file_example_MP4_640_3MG.mp4";
        File previewJpg = POIDataSamples.getDocumentInstance().getFile("abstract1.jpg");

        try (XMLSlideShow pptx = new XMLSlideShow()) {
            XSLFSlide slide1 = pptx.createSlide();

            PackagePart pp = slide1.getPackagePart();
            URI mp4uri = new URI("./" + videoFileName);
            PackageRelationship prsEmbed1 = pp.addRelationship(mp4uri, TargetMode.EXTERNAL, "http://schemas.microsoft.com/office/2007/relationships/media");
            PackageRelationship prsExec1 = pp.addRelationship(mp4uri, TargetMode.EXTERNAL, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/video");


            XSLFPictureData snap = pptx.addPicture(previewJpg, PictureData.PictureType.JPEG);
            XSLFPictureShape pic1 = slide1.createPicture(snap);
            pic1.setAnchor(new Rectangle(100, 100, 500, 400));

            CTPicture xpic1 = (CTPicture) pic1.getXmlObject();
            CTHyperlink link1 = xpic1.getNvPicPr().getCNvPr().addNewHlinkClick();
            link1.setId("");
            link1.setAction("ppaction://media");


            CTApplicationNonVisualDrawingProps nvPr = xpic1.getNvPicPr().getNvPr();
            nvPr.addNewVideoFile().setLink(prsExec1.getId());
            CTExtension ext = nvPr.addNewExtLst().addNewExt();
            ext.setUri("{DAA4B4D4-6D71-4841-9C94-3DE7FCFB9230}");

            String p14Ns = "http://schemas.microsoft.com/office/powerpoint/2010/main";
            XmlCursor cur = ext.newCursor();
            cur.toEndToken();
            cur.beginElement(new QName(p14Ns, "media", "p14"));
            cur.insertNamespace("p14", p14Ns);
            cur.insertAttributeWithValue(new QName(CORE_PROPERTIES_ECMA376_NS, "link"), prsEmbed1.getId());
            cur.dispose();


            CTSlide xslide = slide1.getXmlObject();
            CTTimeNodeList ctnl;
            if (!xslide.isSetTiming()) {
                CTTLCommonTimeNodeData ctn = xslide.addNewTiming().addNewTnLst().addNewPar().addNewCTn();
                ctn.setDur(STTLTimeIndefinite.INDEFINITE);
                ctn.setRestart(STTLTimeNodeRestartType.NEVER);
                ctn.setNodeType(STTLTimeNodeType.TM_ROOT);
                ctnl = ctn.addNewChildTnLst();
            } else {
                ctnl = xslide.getTiming().getTnLst().getParArray(0).getCTn().getChildTnLst();
            }
            CTTLCommonMediaNodeData cmedia = ctnl.addNewVideo().addNewCMediaNode();
            cmedia.setVol(80000);
            CTTLCommonTimeNodeData ctn = cmedia.addNewCTn();
            ctn.setFill(STTLTimeNodeFillType.HOLD);
            ctn.setDisplay(false);
            ctn.addNewStCondLst().addNewCond().setDelay(STTLTimeIndefinite.INDEFINITE);
            cmedia.addNewTgtEl().addNewSpTgt().setSpid(pic1.getShapeId());

            try (XMLSlideShow ppt2 = XSLFTestDataSamples.writeOutAndReadBack(pptx)) {
                XSLFShape sh = ppt2.getSlides().get(0).getShapes().get(0);
                assertTrue(sh instanceof XSLFPictureShape);
            }
        }
    }
}
