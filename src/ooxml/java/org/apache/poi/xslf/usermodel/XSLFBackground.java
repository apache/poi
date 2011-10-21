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

package org.apache.poi.xslf.usermodel;

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackground;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackgroundProperties;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Background shape
 *
 * @author Yegor Kozlov
 */
public class XSLFBackground extends XSLFSimpleShape {

    /* package */XSLFBackground(CTBackground shape, XSLFSheet sheet) {
        super(shape, sheet);
    }

    public void draw(Graphics2D graphics) {
        Dimension pg = getSheet().getSlideShow().getPageSize();
        Rectangle anchor = new Rectangle(0, 0, pg.width, pg.height);
        CTBackgroundProperties pr = ((CTBackground) getXmlObject()).getBgPr();
        if (pr == null) return;

        XSLFTheme theme = getSheet().getTheme();
        if (pr.isSetSolidFill()) {
            Color color = theme.getSolidFillColor(pr.getSolidFill());
            graphics.setPaint(color);
            graphics.fill(anchor);
        }
        if (pr.isSetBlipFill()) {

            String blipId = pr.getBlipFill().getBlip().getEmbed();
            PackagePart p = getSheet().getPackagePart();
            PackageRelationship rel = p.getRelationship(blipId);
            if (rel != null) {
                try {
                    BufferedImage img = ImageIO.read(p.getRelatedPart(rel).getInputStream());
                    graphics.drawImage(img, (int) anchor.getX(), (int) anchor.getY(),
                            (int) anchor.getWidth(), (int) anchor.getHeight(), null);
                }
                catch (Exception e) {
                    return;
                }
            }
        }
    }

}
