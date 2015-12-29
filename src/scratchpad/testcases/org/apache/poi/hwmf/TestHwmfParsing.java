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

package org.apache.poi.hwmf;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hwmf.record.HwmfFill.HwmfImageRecord;
import org.apache.poi.hwmf.record.HwmfRecord;
import org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.junit.Ignore;
import org.junit.Test;

public class TestHwmfParsing {
    @Test
    public void parse() throws IOException {
        File f = POIDataSamples.getSlideShowInstance().getFile("santa.wmf");
        FileInputStream fis = new FileInputStream(f);
        HwmfPicture wmf = new HwmfPicture(fis);
        fis.close();
        List<HwmfRecord> records = wmf.getRecords();
        assertEquals(581, records.size());
    }

    @Test
    @Ignore
    public void extract() throws IOException {
        File dir = new File("test-data/slideshow");
        File files[] = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().matches("(?i).*\\.pptx?$");
            }
        });

        boolean outputFiles = false;

        File outdir = new File("build/ppt");
        if (outputFiles) {
            outdir.mkdirs();
        }
        int wmfIdx = 1;
        for (File f : files) {
            try {
                SlideShow<?,?> ss = SlideShowFactory.create(f);
                for (PictureData pd : ss.getPictureData()) {
                    if (pd.getType() != PictureType.WMF) continue;
                    byte wmfData[] = pd.getData();
                    if (outputFiles) {
                        String filename = String.format(Locale.ROOT, "pic%04d.wmf", wmfIdx);
                        FileOutputStream fos = new FileOutputStream(new File(outdir, filename));
                        fos.write(wmfData);
                        fos.close();
                    }

                    HwmfPicture wmf = new HwmfPicture(new ByteArrayInputStream(wmfData));

                    int bmpIndex = 1;
                    for (HwmfRecord r : wmf.getRecords()) {
                        if (r instanceof HwmfImageRecord) {
                            BufferedImage bi = ((HwmfImageRecord)r).getImage();
                            if (outputFiles) {
                                String filename = String.format(Locale.ROOT, "pic%04d-%04d.png", wmfIdx, bmpIndex);
                                ImageIO.write(bi, "PNG", new File(outdir, filename));
                            }
                            bmpIndex++;
                        }
                    }

                    wmfIdx++;
                }
                ss.close();
            } catch (Exception e) {
                System.out.println(f+" ignored.");
            }
        }
    }

}
