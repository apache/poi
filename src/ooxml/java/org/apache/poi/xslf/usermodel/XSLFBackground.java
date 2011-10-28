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

import org.openxmlformats.schemas.presentationml.x2006.main.CTBackground;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackgroundProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrixReference;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrix;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBackgroundFillStyleList;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;

import javax.xml.namespace.QName;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Background shape
 *
 * @author Yegor Kozlov
 */
public class XSLFBackground extends XSLFSimpleShape {

    /* package */XSLFBackground(CTBackground shape, XSLFSheet sheet) {
        super(shape, sheet);
    }

    @Override
    public Rectangle2D getAnchor(){
        Dimension pg = getSheet().getSlideShow().getPageSize();
        return new Rectangle2D.Double(0, 0, pg.getWidth(), pg.getHeight());
    }

    public void draw(Graphics2D graphics) {
        Rectangle2D anchor = getAnchor();

        XmlObject spPr = null;
        CTBackground bg = (CTBackground)getXmlObject();
        if(bg.isSetBgPr()){
            spPr = bg.getBgPr();
        } else if (bg.isSetBgRef()){
            CTStyleMatrixReference bgRef= bg.getBgRef();
            int idx = (int)bgRef.getIdx() - 1000;
            XSLFTheme theme = getSheet().getTheme();
            CTBackgroundFillStyleList bgStyles =
                    theme.getXmlObject().getThemeElements().getFmtScheme().getBgFillStyleLst();

            // TODO pass this to getPaint
            XmlObject bgStyle = bgStyles.selectPath("*")[idx];
        }

        if(spPr == null){
            return;
        }

        Paint fill = getPaint(graphics, spPr);
        if(fill != null) {
            graphics.setPaint(fill);
            graphics.fill(anchor);
        }
    }

    
}
