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


public final class HSSFChildAnchor extends HSSFAnchor {
    public HSSFChildAnchor()
    {
    }

    public HSSFChildAnchor( int dx1, int dy1, int dx2, int dy2 )
    {
        super( dx1, dy1, dx2, dy2 );
    }

    public void setAnchor(int dx1, int dy1, int dx2, int dy2)
    {
        this.dx1 = dx1;
        this.dy1 = dy1;
        this.dx2 = dx2;
        this.dy2 = dy2;
    }

    public boolean isHorizontallyFlipped()
    {
        return dx1 > dx2;
    }

    public boolean isVerticallyFlipped()
    {
        return dy1 > dy2;
    }
}
