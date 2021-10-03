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

package org.apache.poi.sl.draw.geom;

import java.awt.geom.Path2D;

import org.apache.poi.sl.usermodel.PaintStyle;

public interface PathIf {

    void addCommand(PathCommand cmd);

    /**
     * Convert the internal represenation to java.awt.geom.Path2D
     */
    Path2D.Double getPath(Context ctx);

    boolean isStroked();

    void setStroke(boolean stroke);

    boolean isFilled();

    PaintStyle.PaintModifier getFill();

    void setFill(PaintStyle.PaintModifier fill);

    long getW();

    void setW(long w);

    long getH();

    void setH(long h);

    boolean isExtrusionOk();

    void setExtrusionOk(boolean extrusionOk);

}