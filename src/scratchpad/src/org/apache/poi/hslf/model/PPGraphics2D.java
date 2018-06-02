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

package org.apache.poi.hslf.model;


import org.apache.poi.hslf.usermodel.HSLFGroupShape;
import org.apache.poi.sl.draw.SLGraphics;
import org.apache.poi.util.Removal;

/**
 * Translates Graphics2D calls into PowerPoint.
 * @deprecated since 4.0.0 - use SLGraphics
 */
@Deprecated
@Removal(version="5.0.0")
public final class PPGraphics2D extends SLGraphics {
    /**
     * Construct Java Graphics object which translates graphic calls in ppt drawing layer.
     *
     * @param group           The shape group to write the graphics calls into.
     */
    public PPGraphics2D(HSLFGroupShape group){
        super(group);
    }
}
