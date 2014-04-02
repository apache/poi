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

import org.apache.poi.hslf.usermodel.*;
import org.apache.poi.hslf.model.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Paragraph;

import java.io.*;

/**
 * Demonstrates how you can extract misc embedded data from a ppt file
 *
 * @author Yegor Kozlov
 */
public final class DataExtraction {

    public static void main(String args[]) throws Exception {

        if (args.length == 0) {
            usage();
            return;
        }

        FileInputStream is = new FileInputStream(args[0]);
        SlideShow ppt = new SlideShow(is);
        is.close();

        //extract all sound files embedded in this presentation
        SoundData[] sound = ppt.getSoundData();
        for (int i = 0; i < sound.length; i++) {
            String type = sound[i].getSoundType();  //*.wav
            String name = sound[i].getSoundName();  //typically file name
            byte[] data = sound[i].getData();       //raw bytes

            //save the sound  on disk
            FileOutputStream out = new FileOutputStream(name + type);
            out.write(data);
            out.close();
        }

        //extract embedded OLE documents
        Slide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            Shape[] shape = slide[i].getShapes();
            for (int j = 0; j < shape.length; j++) {
                if (shape[j] instanceof OLEShape) {
                    OLEShape ole = (OLEShape) shape[j];
                    ObjectData data = ole.getObjectData();
                    String name = ole.getInstanceName();
                    if ("Worksheet".equals(name)) {

                        //read xls
                        HSSFWorkbook wb = new HSSFWorkbook(data.getData());

                    } else if ("Document".equals(name)) {
                        HWPFDocument doc = new HWPFDocument(data.getData());
                        //read the word document
                        Range r = doc.getRange();
                        for(int k = 0; k < r.numParagraphs(); k++) {
                            Paragraph p = r.getParagraph(k);
                            System.out.println(p.text());
                         }

                        //save on disk
                        FileOutputStream out = new FileOutputStream(name + "-("+(j)+").doc");
                        doc.write(out);
                        out.close();
                     }  else {
                        FileOutputStream out = new FileOutputStream(ole.getProgID() + "-"+(j+1)+".dat");
                        InputStream dis = data.getData();
                        byte[] chunk = new byte[2048];
                        int count;
                        while ((count = dis.read(chunk)) >= 0) {
                          out.write(chunk,0,count);
                        }
                        is.close();
                        out.close();
                    }
                }

            }
        }

        //Pictures
        for (int i = 0; i < slide.length; i++) {
            Shape[] shape = slide[i].getShapes();
            for (int j = 0; j < shape.length; j++) {
                if (shape[j] instanceof Picture) {
                    Picture p = (Picture) shape[j];
                    PictureData data = p.getPictureData();
                    String name = p.getPictureName();
                    int type = data.getType();
                    String ext;
                    switch (type) {
                        case Picture.JPEG:
                            ext = ".jpg";
                            break;
                        case Picture.PNG:
                            ext = ".png";
                            break;
                        case Picture.WMF:
                            ext = ".wmf";
                            break;
                        case Picture.EMF:
                            ext = ".emf";
                            break;
                        case Picture.PICT:
                            ext = ".pict";
                            break;
                        case Picture.DIB:
                            ext = ".dib";
                            break;
                        default:
                            continue;
                    }
                    FileOutputStream out = new FileOutputStream("pict-" + j + ext);
                    out.write(data.getData());
                    out.close();
                }

            }
        }

    }

    private static void usage(){
        System.out.println("Usage: DataExtraction  ppt");
    }
}
