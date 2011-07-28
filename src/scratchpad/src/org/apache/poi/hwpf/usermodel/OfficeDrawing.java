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
package org.apache.poi.hwpf.usermodel;

/**
 * User-friendly interface to office drawing objects
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
public interface OfficeDrawing
{
    /**
     * Returns picture data if this shape has (single?) associated picture data
     */
    byte[] getPictureData();

    /**
     * Bottom of the rectangle enclosing shape relative to the origin of the
     * shape
     */
    int getRectangleBottom();

    /**
     * Left of rectangle enclosing shape relative to the origin of the shape
     */
    int getRectangleLeft();

    /**
     * Right of rectangle enclosing shape relative to the origin of the shape
     */
    int getRectangleRight();

    /**
     * Top of rectangle enclosing shape relative to the origin of the shape
     */
    int getRectangleTop();

    /**
     * Shape Identifier
     */
    int getShapeId();

}
