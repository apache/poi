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

package org.apache.poi.hslf.extractor;

import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.model.HSLFPictureShape;
import org.apache.poi.hslf.model.HSLFSlideShowImpl;

import java.io.IOException;
import java.io.FileOutputStream;

/**
 * Utility to extract pictures from a PowerPoint file.
 *
 * @author Yegor Kozlov
 */
public final class ImageExtractor {
    public static void main(String args[]) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage:");
            System.err.println("\tImageExtractor <file>");
            return;
        }
        HSLFSlideShow ppt = new HSLFSlideShow(new HSLFSlideShowImpl(args[0]));

        //extract all pictures contained in the presentation
        HSLFPictureData[] pdata = ppt.getPictureData();
        for (int i = 0; i < pdata.length; i++) {
            HSLFPictureData pict = pdata[i];

            // picture data
            byte[] data = pict.getData();

            int type = pict.getType();
            String ext;
            switch (type) {
                case HSLFPictureShape.JPEG:
                    ext = ".jpg";
                    break;
                case HSLFPictureShape.PNG:
                    ext = ".png";
                    break;
                case HSLFPictureShape.WMF:
                    ext = ".wmf";
                    break;
                case HSLFPictureShape.EMF:
                    ext = ".emf";
                    break;
                case HSLFPictureShape.PICT:
                    ext = ".pict";
                    break;
                case HSLFPictureShape.DIB:
                    ext = ".dib";
                    break;
                default:
                    continue;
            }
            FileOutputStream out = new FileOutputStream("pict_" + i + ext);
            out.write(data);
            out.close();
        }
    }
}
