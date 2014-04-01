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

import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hslf.usermodel.PictureData;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.model.Picture;

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
        SlideShow ppt = new SlideShow(new HSLFSlideShow(args[0]));

        //extract all pictures contained in the presentation
        PictureData[] pdata = ppt.getPictureData();
        for (int i = 0; i < pdata.length; i++) {
            PictureData pict = pdata[i];

            // picture data
            byte[] data = pict.getData();

            int type = pict.getType();
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
            FileOutputStream out = new FileOutputStream("pict_" + i + ext);
            out.write(data);
            out.close();
        }
    }
}
