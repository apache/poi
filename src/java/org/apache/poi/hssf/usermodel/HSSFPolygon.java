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

package org.apache.poi.hssf.usermodel;

/**
 * @author Glen Stampoultzis  (glens at superlinksoftware.com)
 */
public class HSSFPolygon
        extends HSSFShape
{
    int[] xPoints;
    int[] yPoints;
    int drawAreaWidth = 100;
    int drawAreaHeight = 100;

    HSSFPolygon( HSSFShape parent, HSSFAnchor anchor )
    {
        super( parent, anchor );
    }

    public int[] getXPoints()
    {
        return xPoints;
    }

    public int[] getYPoints()
    {
        return yPoints;
    }

    public void setPoints(int[] xPoints, int[] yPoints)
    {
        this.xPoints = cloneArray(xPoints);
        this.yPoints = cloneArray(yPoints);
    }

    private int[] cloneArray( int[] a )
    {
        int[] result = new int[a.length];
        for ( int i = 0; i < a.length; i++ )
            result[i] = a[i];

        return result;
    }

    /**
     * Defines the width and height of the points in the polygon
     * @param width
     * @param height
     */
    public void setPolygonDrawArea( int width, int height )
    {
        this.drawAreaWidth = width;
        this.drawAreaHeight = height;
    }

    public int getDrawAreaWidth()
    {
        return drawAreaWidth;
    }

    public int getDrawAreaHeight()
    {
        return drawAreaHeight;
    }


}
