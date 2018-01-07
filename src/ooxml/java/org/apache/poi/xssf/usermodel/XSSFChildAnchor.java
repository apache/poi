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

package org.apache.poi.xssf.usermodel;

import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.apache.poi.util.Internal;

/**
 * @author Yegor Kozlov
 */
public final class XSSFChildAnchor extends XSSFAnchor {
    private CTTransform2D t2d;

    public XSSFChildAnchor(int x, int y, int cx, int cy) {
        t2d = CTTransform2D.Factory.newInstance();
        CTPoint2D off = t2d.addNewOff();
        CTPositiveSize2D ext = t2d.addNewExt();

        off.setX(x);
        off.setY(y);
        ext.setCx(Math.abs(cx - x));
        ext.setCy(Math.abs(cy - y));
        if(x > cx)  t2d.setFlipH(true);
        if(y > cy)  t2d.setFlipV(true);
    }

    public XSSFChildAnchor(CTTransform2D t2d) {
        this.t2d = t2d;
    }

    @Internal
    public CTTransform2D getCTTransform2D() {
        return t2d;
    }

    public int getDx1() {
        return (int)t2d.getOff().getX();
    }

    public void setDx1(int dx1) {
        t2d.getOff().setX(dx1);
    }

    public int getDy1() {
        return (int)t2d.getOff().getY();
    }

    public void setDy1(int dy1) {
        t2d.getOff().setY(dy1);
    }

    public int getDy2() {
        return (int)(getDy1() + t2d.getExt().getCy());
    }

    public void setDy2(int dy2) {
        t2d.getExt().setCy(dy2 - getDy1());
    }

    public int getDx2() {
        return (int)(getDx1() + t2d.getExt().getCx());
    }

    public void setDx2(int dx2) {
        t2d.getExt().setCx(dx2 - getDx1());
    }
}
