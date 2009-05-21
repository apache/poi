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

package org.apache.poi.hslf.blip;

import org.apache.poi.hslf.usermodel.PictureData;
import org.apache.poi.hslf.model.Picture;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;

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
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Creates BufferedImage using javax.imageio.ImageIO and draws it in the specified graphics.
 *
 * @author  Yegor Kozlov.
 */
public final class BitmapPainter implements ImagePainter {
    protected POILogger logger = POILogFactory.getLogger(this.getClass());

    public void paint(Graphics2D graphics, PictureData pict, Picture parent) {
        BufferedImage img;
        try {
               img = ImageIO.read(new ByteArrayInputStream(pict.getData()));
        }
        catch (Exception e){
            logger.log(POILogger.WARN, "ImageIO failed to create image. image.type: " + pict.getType());
            return;
        }
        Rectangle anchor = parent.getAnchor();
        Image scaledImg = img.getScaledInstance(anchor.width, anchor.height, Image.SCALE_SMOOTH);
        graphics.drawImage(scaledImg, anchor.x, anchor.y, null);
    }

}
