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

package org.apache.poi.examples.hslf;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hslf.usermodel.HSLFObjectData;
import org.apache.poi.hslf.usermodel.HSLFObjectShape;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFPictureShape;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSoundData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.util.IOUtils;

/**
 * Demonstrates how you can extract misc embedded data from a ppt file
 */
@SuppressWarnings({"java:S106","java:S4823"})
public final class DataExtraction {

    private DataExtraction() {}

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            usage();
            return;
        }

        try (FileInputStream fis = new FileInputStream(args[0]);
            HSLFSlideShow ppt = new HSLFSlideShow(fis)) {

            //extract all sound files embedded in this presentation
            HSLFSoundData[] sound = ppt.getSoundData();
            for (HSLFSoundData aSound : sound) {
                handleSound(aSound);
            }

            int oleIdx = -1;
            int picIdx = -1;
            for (HSLFSlide slide : ppt.getSlides()) {
                //extract embedded OLE documents
                for (HSLFShape shape : slide.getShapes()) {
                    if (shape instanceof HSLFObjectShape) {
                        handleShape((HSLFObjectShape) shape, ++oleIdx);
                    } else if (shape instanceof HSLFPictureShape) {
                        handlePicture((HSLFPictureShape) shape, ++picIdx);
                    }
                }
            }
        }
    }

    private static void handleShape(HSLFObjectShape ole, int oleIdx) throws IOException {
        HSLFObjectData data = ole.getObjectData();
        String name = ole.getInstanceName();
        switch (name == null ? "" : name) {
            case "Worksheet":
                //read xls
                handleWorkbook(data, name, oleIdx);
                break;
            case "Document":
                //read the word document
                handleDocument(data, name, oleIdx);
                break;
            default:
                handleUnknown(data, ole.getProgId(), oleIdx);
                break;
        }

    }

    private static void handleWorkbook(HSLFObjectData data, String name, int oleIdx) throws IOException {
        try (InputStream is = data.getInputStream();
             HSSFWorkbook wb = new HSSFWorkbook(is);
             FileOutputStream out = new FileOutputStream(name + "-(" + (oleIdx) + ").xls")) {
            wb.write(out);
        }
    }

    private static void handleDocument(HSLFObjectData data, String name, int oleIdx) throws IOException {
        try (InputStream is = data.getInputStream();
             HWPFDocument doc = new HWPFDocument(is);
             FileOutputStream out = new FileOutputStream(name + "-(" + (oleIdx) + ").doc")) {
            Range r = doc.getRange();
            for (int k = 0; k < r.numParagraphs(); k++) {
                Paragraph p = r.getParagraph(k);
                System.out.println(p.text());
            }

            //save on disk
            doc.write(out);
        }
    }

    private static void handleUnknown(HSLFObjectData data, String name, int oleIdx) throws IOException {
        try (InputStream is = data.getInputStream();
             FileOutputStream out = new FileOutputStream(name + "-" + (oleIdx + 1) + ".dat")) {
            IOUtils.copy(is, out);
        }
    }

    private static void handlePicture(HSLFPictureShape p, int picIdx) throws IOException {
        HSLFPictureData data = p.getPictureData();
        String ext = data.getType().extension;
        try (FileOutputStream out = new FileOutputStream("pict-" + picIdx + ext)) {
            out.write(data.getData());
        }
    }

    private static void handleSound(HSLFSoundData aSound) throws IOException {
        String type = aSound.getSoundType();  //*.wav
        String name = aSound.getSoundName();  //typically file name

        //save the sound  on disk
        try (FileOutputStream out = new FileOutputStream(name + type)) {
            out.write(aSound.getData());
        }
    }

    private static void usage(){
        System.out.println("Usage: DataExtraction  ppt");
    }
}
