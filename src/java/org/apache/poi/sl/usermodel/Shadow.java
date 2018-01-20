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

package org.apache.poi.sl.usermodel;

import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;



public interface Shadow<
S extends Shape<S,P>,
P extends TextParagraph<S,P,? extends TextRun>
> {
    SimpleShape<S,P> getShadowParent();
    
    /**
     * @return the offset of this shadow in points
     */
    double getDistance();
    
    /**
     * 
     * @return the direction to offset the shadow in angles
     */
    double getAngle();

    /**
     * 
     * @return the blur radius of the shadow
     * TODO: figure out how to make sense of this property when rendering shadows 
     */
    double getBlur();

    /**
     * @return the color of this shadow. 
     * Depending whether the parent shape is filled or stroked, this color is used to fill or stroke this shadow
     */
    SolidPaint getFillStyle();    
}
