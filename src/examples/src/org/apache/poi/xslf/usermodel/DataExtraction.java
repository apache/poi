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

import org.apache.poi.openxml4j.opc.PackagePart;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Demonstrates how you can extract data from a .pptx file
 *
 * @author Yegor Kozlov
 */
public final class DataExtraction {

    public static void main(String args[]) throws Exception {

        if (args.length == 0) {
           System.out.println("Input file is required");
           return;
        }

        FileInputStream is = new FileInputStream(args[0]);
        XMLSlideShow ppt = new XMLSlideShow(is);
        is.close();

        // Get the document's embedded files.
        List<PackagePart> embeds = ppt.getAllEmbedds();
        for (PackagePart p : embeds) {
            String type = p.getContentType();
            String name = p.getPartName().getName();  //typically file name
            
            InputStream pIs = p.getInputStream();
            // make sense of the part data
            pIs.close();
            
        }

        // Get the document's embedded files.
        List<XSLFPictureData> images = ppt.getAllPictures();
        for (XSLFPictureData data : images) {
            PackagePart p = data.getPackagePart();

            String type = p.getContentType();
            String name = data.getFileName();

            InputStream pIs = p.getInputStream();
            // make sense of the image data
            pIs.close();



        }

        Dimension pageSize = ppt.getPageSize();  // size of the canvas in points
        for(XSLFSlide slide : ppt.getSlides()) {
            for(XSLFShape shape : slide){
                Rectangle2D anchor = shape.getAnchor();  // position on the canvas
                if(shape instanceof XSLFTextShape) {
                    XSLFTextShape txShape = (XSLFTextShape)shape;
                    System.out.println(txShape.getText());
                } else if (shape instanceof XSLFPictureShape){
                    XSLFPictureShape pShape = (XSLFPictureShape)shape;
                    XSLFPictureData pData = pShape.getPictureData();
                    System.out.println(pData.getFileName());
                } else {
                    System.out.println("Process me: " + shape.getClass());
                }
            }
        }
    }

}
