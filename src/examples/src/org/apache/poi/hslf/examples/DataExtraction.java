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

package org.apache.poi.hslf.examples;

import java.io.FileInputStream;
import java.io.FileOutputStream;
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

/**
 * Demonstrates how you can extract misc embedded data from a ppt file
 */
public final class DataExtraction {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            usage();
            return;
        }

        try (FileInputStream is = new FileInputStream(args[0]);
            HSLFSlideShow ppt = new HSLFSlideShow(is)) {

            //extract all sound files embedded in this presentation
            HSLFSoundData[] sound = ppt.getSoundData();
            for (HSLFSoundData aSound : sound) {
                String type = aSound.getSoundType();  //*.wav
                String name = aSound.getSoundName();  //typically file name
                byte[] data = aSound.getData();       //raw bytes

                //save the sound  on disk
                try (FileOutputStream out = new FileOutputStream(name + type)) {
                    out.write(data);
                }
            }

            int oleIdx = -1, picIdx = -1;
            for (HSLFSlide slide : ppt.getSlides()) {
                //extract embedded OLE documents
                for (HSLFShape shape : slide.getShapes()) {
                    if (shape instanceof HSLFObjectShape) {
                        oleIdx++;
                        HSLFObjectShape ole = (HSLFObjectShape) shape;
                        HSLFObjectData data = ole.getObjectData();
                        String name = ole.getInstanceName();
                        if ("Worksheet".equals(name)) {

                            //read xls
                            @SuppressWarnings({"unused", "resource"})
                            HSSFWorkbook wb = new HSSFWorkbook(data.getInputStream());

                        } else if ("Document".equals(name)) {
                            try (HWPFDocument doc = new HWPFDocument(data.getInputStream())) {
                                //read the word document
                                Range r = doc.getRange();
                                for (int k = 0; k < r.numParagraphs(); k++) {
                                    Paragraph p = r.getParagraph(k);
                                    System.out.println(p.text());
                                }

                                //save on disk
                                try (FileOutputStream out = new FileOutputStream(name + "-(" + (oleIdx) + ").doc")) {
                                    doc.write(out);
                                }
                            }
                        } else {
                            try (FileOutputStream out = new FileOutputStream(ole.getProgId() + "-" + (oleIdx + 1) + ".dat");
                                InputStream dis = data.getInputStream()) {
                                byte[] chunk = new byte[2048];
                                int count;
                                while ((count = dis.read(chunk)) >= 0) {
                                    out.write(chunk, 0, count);
                                }
                            }
                        }
                    }

                    //Pictures
                    else if (shape instanceof HSLFPictureShape) {
                        picIdx++;
                        HSLFPictureShape p = (HSLFPictureShape) shape;
                        HSLFPictureData data = p.getPictureData();
                        String ext = data.getType().extension;
                        try (FileOutputStream out = new FileOutputStream("pict-" + picIdx + ext)) {
                            out.write(data.getData());
                        }
                    }
                }
            }
        }
    }

    private static void usage(){
        System.out.println("Usage: DataExtraction  ppt");
    }
}
