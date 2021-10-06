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

package org.apache.poi.xslf.draw.geom;

import org.apache.poi.sl.draw.geom.CustomGeometry;
import org.apache.poi.util.Beta;
import org.openxmlformats.schemas.drawingml.x2006.main.CTAdjustHandleList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTConnectionSite;
import org.openxmlformats.schemas.drawingml.x2006.main.CTCustomGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGeomGuide;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGeomGuideList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGeomRect;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2DList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPolarAdjustHandle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTXYAdjustHandle;

/**
 * Wrapper / delegate for XmlBeans custom geometry
 */
@Beta
public class XSLFCustomGeometry {
    public static CustomGeometry convertCustomGeometry(CTCustomGeometry2D custGeom) {
        CustomGeometry cg = new CustomGeometry();

        if (custGeom.isSetAhLst()) {
            CTAdjustHandleList ahLst = custGeom.getAhLst();
            for (CTXYAdjustHandle xy : ahLst.getAhXYArray()) {
                cg.addAdjustHandle(new XSLFXYAdjustHandle(xy));
            }
            for (CTPolarAdjustHandle pol : ahLst.getAhPolarArray()) {
                cg.addAdjustHandle(new XSLFPolarAdjustHandle(pol));
            }
        }

        if (custGeom.isSetAvLst()) {
            CTGeomGuideList avLst = custGeom.getAvLst();
            for (CTGeomGuide gg : avLst.getGdArray()) {
                cg.addAdjustGuide(new XSLFAdjustValue(gg));
            }
        }

        if (custGeom.isSetGdLst()) {
            CTGeomGuideList gdLst = custGeom.getGdLst();
            for (CTGeomGuide gg : gdLst.getGdArray()) {
                cg.addGeomGuide(new XSLFGuide(gg));
            }
        }

        if (custGeom.isSetRect()) {
            CTGeomRect r = custGeom.getRect();
            cg.setTextBounds(
                r.xgetL().getStringValue(),
                r.xgetT().getStringValue(),
                r.xgetR().getStringValue(),
                r.xgetB().getStringValue());
        }

        if (custGeom.isSetCxnLst()) {
            for (CTConnectionSite cxn : custGeom.getCxnLst().getCxnArray()) {
                cg.addConnectionSite(new XSLFConnectionSite((cxn)));
            }
        }

        CTPath2DList pl = custGeom.getPathLst();
        if (pl != null) {
            for (CTPath2D p : pl.getPathArray()) {
                cg.addPath(new XSLFPath(p));
            }
        }

        return cg;
    }
}
