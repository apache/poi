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

package org.apache.poi.sl.tests;

import static org.apache.poi.sl.tests.SLCommonUtils.xslfOnly;
import static org.apache.poi.sl.usermodel.ObjectMetaData.Application.EXCEL_V12;
import static org.apache.poi.sl.usermodel.ObjectMetaData.Application.EXCEL_V8;
import static org.apache.poi.sl.usermodel.ObjectMetaData.Application.PDF;
import static org.apache.poi.sl.usermodel.ObjectMetaData.Application.WORD_V12;
import static org.apache.poi.sl.usermodel.ObjectMetaData.Application.WORD_V8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.POIDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.storage.RawDataUtil;
import org.apache.poi.sl.usermodel.ObjectMetaData;
import org.apache.poi.sl.usermodel.ObjectShape;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestOleShape {
    private static final String PDF_SAMPLE =
        "H4sIAAAAAAAAAJWUezRUWxzHe+o2FXncVtxLpxi3FPOeKYspjMdM5J1S4TTOaDIzxzpzJo9CUrnrSiUxIeT" +
        "9jB7yqInihrhepTwqt1AT5VZCC7XcY0LWcv+5Z521zz6fvX+/vb/7t9cX78CyMiQZ0XD4W4OFEzgKQATgg4" +
        "dxJiYAwRYS+aCHACqGnHAAABCs+AIUQrCvAEQhFsSFvSEck4kTowgECnEBl6E4OxGesfLkl2Bc0g5V/MCHt" +
        "duqroTFC27YIKGAp+OL5Ou1SsmebA1XvciLk+Ucg84aLclQZhRZmh0amrG9Ina4t7Lh+ZCAHyezsg/NXsZg" +
        "vuPw8NIedsrI2sZlz2vfhLkZfIgfMr4zFvTrmfbRgMmPw1UTvWk+r4MCZfLtj2WPPStVJ0P2PiKkxo+YnJ5" +
        "Ua7v5UnefkB9ev0vSR37a8NrsC2lApaLp7086wS3Lzi2LqB3TMW2POrdRRUYMFYWs8vBo/kSQ6dYXpR6rxM" +
        "UXM0vqu4arpe5dha7XS5MYS5P1arVG653sb8pXqReVw/TfjK8R3q4Z7X7Uk9dZ2Bcl8Wpmsl80Xf1QTOxe3" +
        "Nutwus0kYge1LoHvgKLbc/f6WvdcsBfS9ctU3vSaneHNm0w/uhrm0Zett5O83s2xh2Gm8WZfWJ+/CNWruZ2" +
        "cap8tR2/U9bAfRBbYt3PL9jvb3+0usqSF6vfrFuEq8Hf6jgrx/fERpZJEjKbHtJ11jCdUwI7Oc8QrmZf2pr" +
        "L43WJn1mlT1ydV+QbrndcdN3qSEnicVhmoJyfWyprUEsIZlPyvi0tiEy7FzkOnqlE/qC6xFSpyg0E8tODa7" +
        "qiKX61hMxRkZt73ITLWIHwtZtj71NbS4/BgKHnssOMcXOp1aacX4A//V+VFN4TWl6QryxdkcAp79BZcipmP" +
        "OWOWkS6KhqUWlG+yCCxVMMtfW+9e56++gKHYEs9PNJztTI6KtyfOCDPOBppt3udRs3NpGrKfrs3i3Nivtrs" +
        "VTl5LFerXZlTbf5XumbeYsOfwnve5ksEjKy6s1Z78rpbeJq7biTdzWwU3vhZ1GqkYb9D+t6mYvWLhXn6FWi" +
        "c60VWpbBtVYHLQkmbh6Txwm6Ul3LbNW/Hs5FtLnlNX3fsAX2jPdlOI5W3HDIcm2MyNiR39rdyHlpwusjoOj" +
        "I3IKfgPMILSzHZInmQaWyUFXEi0bHsCLX8pm9Gzl2vou7E3rrkPYdt1EwW3R5Qcg8rwzk88c1p13l8v+WkY" +
        "75FHeS7XrvRsHgLy+wfr2EBRfNJ/4UVhUrihRqDktXciPGxWv1eXs396/0lqWG3YtU/A+D90hrT46cumSUN" +
        "rBdG0G2Knn3T9Kw0X96vbhxMyr92uqUNOa4aEnGqP8us6GULm7mIyFKuxnOW2MZEEuKXmOpxnnqlwiMn+ju" +
        "Xu+inC5mpG9oesxKkhcJq9bra5vR3H7l10hGbAxqu6t0LvHzaDnPIp/zeu0iXj1NNtVc+cMyUsH18u7TGXJ" +
        "XiL4W3tqaL2mq6zkgXWB6kOTB3RxW8PHAOvzfaufDptdg7qmZlEcrUzbd5jKtVb85Sr9jaMT8a3y2Q30+3/" +
        "FrsfGZDblh/mYnHhCg3ekm2q1JIYEVCd9rv42PNb9RFpuSsa4MNE0GfdSYDv6lsudikg4NE3tNugfWmfIY6" +
        "7TeYvZCItG0zmDxrQwrjsQxArZ1RzHSA72CKgURgyqQszAASQOCCWItZETaAtdg7nYc2x85cAv0ggOAA+kC" +
        "KnA4gAolQLGzG3ewgbz5oDgcA+zBEBEhUkhGDQiZvpc3tHlBMtYBFKBYsBiiz0dYILPGbs73vqynoDHLGKA" +
        "KKxH5TK3MDZzAbQBEJNPNngc1iQUf4XMjJ2nxazxR3gsSwBOFCYoCsWPOHRtJ/ahQronbyvcWYnqljcJrdu" +
        "2RK9pwE9DkJLLDioDACbOSCfAQGSEYkqhGJCGw8hKJ+xgSCgvogoN8hPldsBCM+mzZ9P0wE9pZwof8V92MD" +
        "jHkKLEAUFMA+04XC1EzX6UdMAALxcERgK444+wB0Go1CA3jANCNRGdj1UoyIZhlpPsMobf48GmkeI1Pp8xi" +
        "Nsm0eo9O3/mAoAvIFEKIQ58wPgrAtK+oJwyjAmL0+bBEPBugzGsUoiKAKhSQGmYjD4y3trXD/AmBc9IeqBwAA";

    enum Api { HSLF, XSLF }


    private static File pictureFile;

    @BeforeAll
    public static void initPicture() {
        pictureFile = POIDataSamples.getSlideShowInstance().getFile("wrench.emf");
    }

    public static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of( Api.HSLF, EXCEL_V8 ),
            Arguments.of( Api.HSLF, WORD_V8 ),
            Arguments.of( Api.HSLF, PDF ),
            Arguments.of( Api.XSLF, EXCEL_V12 ),
            Arguments.of( Api.XSLF, WORD_V12 ),
            Arguments.of( Api.XSLF, PDF )
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void embedData(Api api, ObjectMetaData.Application app) throws IOException, ReflectiveOperationException {
        final ByteArrayInputStream pptBytes;
        try (SlideShow<?,?> ppt = createSlideShow(api)) {
            final PictureData picData = ppt.addPicture(pictureFile,  PictureType.EMF);
            final Slide<?,?> slide = ppt.createSlide();
            final ObjectShape<?,?> oleShape = slide.createOleShape(picData);
            oleShape.setAnchor(new Rectangle2D.Double(100,100,100,100));
            try (OutputStream os = oleShape.updateObjectData(app, null)) {
                fillOleData(app, os);
            }
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(50000);
            ppt.write(bos);
            pptBytes = new ByteArrayInputStream(bos.toByteArray());
        }
        try (SlideShow<?,?> ppt = SlideShowFactory.create(pptBytes)) {
            final ObjectShape<?,?> oleShape = (ObjectShape<?,?>)ppt.getSlides().get(0).getShapes().get(0);
            try (InputStream bis = oleShape.readObjectData()) {
                validateOleData(app, bis);
            }
        }
    }

    private SlideShow<?,?> createSlideShow(Api api) throws IOException {
        if (api == Api.XSLF) {
            return new XMLSlideShow();
        } else {
            assumeFalse(xslfOnly());
            return SlideShowFactory.create(false);
        }
    }


    private void fillOleData(ObjectMetaData.Application app, final OutputStream out) throws IOException {
        switch (app) {
        case EXCEL_V8:
        case EXCEL_V12:
            try (Workbook wb = (app == EXCEL_V12) ? new XSSFWorkbook() : new HSSFWorkbook()) {
                wb.createSheet().createRow(0).createCell(0).setCellValue("test me");
                wb.write(out);
            }
            break;
        case WORD_V8:
            try (InputStream is = POIDataSamples.getDocumentInstance().openResourceAsStream("simple.doc")) {
                IOUtils.copy(is, out);
            }
            break;
        case WORD_V12:
            try (XWPFDocument doc = new XWPFDocument()) {
                doc.createParagraph().createRun().setText("Test me");
                doc.write(out);
            }
            break;
        case PDF:
            out.write(RawDataUtil.decompress(PDF_SAMPLE));
            break;
        default:
        case CUSTOM:
            fail("not implemented");
            break;
        }
    }

    private void validateOleData(ObjectMetaData.Application app, final InputStream in) throws IOException, ReflectiveOperationException {
        switch (app) {
        case EXCEL_V8:
        case EXCEL_V12:
            try (Workbook wb = WorkbookFactory.create(in)) {
                assertEquals("test me", wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
            }
            break;
        case WORD_V8:
            @SuppressWarnings("unchecked")
            Class<? extends POIDocument> clazz = (Class<? extends POIDocument>)Class.forName("org.apache.poi.hwpf.HWPFDocument");
            Constructor<? extends POIDocument> con = clazz.getDeclaredConstructor(InputStream.class);
            Method m = clazz.getMethod("getDocumentText");
            try (POIDocument doc = con.newInstance(in)) {
                assertEquals("This is a simple file created with Word 97-SR2.\r", m.invoke(doc));
            }
            break;
        case WORD_V12:
            try (XWPFDocument doc = new XWPFDocument(in)) {
                assertEquals("Test me", doc.getParagraphs().get(0).getText());
            }
            break;
        case PDF:
            final byte[] expected = RawDataUtil.decompress(PDF_SAMPLE);
            final byte[] actual = IOUtils.toByteArray(in);
            assertArrayEquals(expected, actual);
            break;
        default:
        case CUSTOM:
            fail("not implemented");
            break;
        }

    }
}
