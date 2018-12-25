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

package org.apache.poi.sl.image;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;

@Internal
public class ImageHeaderBitmap {
    private static final POILogger LOG = POILogFactory.getLogger(ImageHeaderBitmap.class);
    
    private final Dimension size;
    
    public ImageHeaderBitmap(byte[] data, int offset) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new ByteArrayInputStream(data, offset, data.length-offset));
        } catch (IOException e) {
            LOG.log(POILogger.WARN, "Can't determine image dimensions", e);
        }
        // set dummy size, in case of dummy dimension can't be set
        size = (img == null)
            ? new Dimension(200,200)
            : new Dimension(
                (int)Units.pixelToPoints(img.getWidth()),
                (int)Units.pixelToPoints(img.getHeight())
            );
    }

    public Dimension getSize() {
        return size;
    }
}
