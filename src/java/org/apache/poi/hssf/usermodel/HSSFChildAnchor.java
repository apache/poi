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


import org.apache.poi.ddf.EscherChildAnchorRecord;
import org.apache.poi.ddf.EscherRecord;

public final class HSSFChildAnchor extends HSSFAnchor {

    private EscherChildAnchorRecord _escherChildAnchor;

    /**
     * create anchor from existing file
     * @param escherChildAnchorRecord
     */
    public HSSFChildAnchor(EscherChildAnchorRecord escherChildAnchorRecord) {
        this._escherChildAnchor = escherChildAnchorRecord;
    }

    public HSSFChildAnchor() {
        _escherChildAnchor = new EscherChildAnchorRecord();
    }

    /**
     * create anchor from scratch
     * @param dx1 x coordinate of the left up corner
     * @param dy1 y coordinate of the left up corner
     * @param dx2 x coordinate of the right down corner
     * @param dy2 y coordinate of the right down corner
     */
    public HSSFChildAnchor(int dx1, int dy1, int dx2, int dy2) {
        super(Math.min(dx1, dx2), Math.min(dy1, dy2), Math.max(dx1, dx2), Math.max(dy1, dy2));
        if (dx1 > dx2){
            _isHorizontallyFlipped = true;
        }
        if (dy1 > dy2){
            _isVerticallyFlipped = true;
        }
    }

    @Override
    public int getDx1() {
        return _escherChildAnchor.getDx1();
    }

    @Override
    public void setDx1(int dx1) {
        _escherChildAnchor.setDx1(dx1);
    }

    @Override
    public int getDy1() {
        return _escherChildAnchor.getDy1();
    }

    @Override
    public void setDy1(int dy1) {
        _escherChildAnchor.setDy1(dy1);
    }

    @Override
    public int getDy2() {
        return _escherChildAnchor.getDy2();
    }

    @Override
    public void setDy2(int dy2) {
        _escherChildAnchor.setDy2(dy2);
    }

    @Override
    public int getDx2() {
        return _escherChildAnchor.getDx2();
    }

    @Override
    public void setDx2(int dx2) {
        _escherChildAnchor.setDx2(dx2);
    }

    /**
     * @param dx1 x coordinate of the left up corner
     * @param dy1 y coordinate of the left up corner
     * @param dx2 x coordinate of the right down corner
     * @param dy2 y coordinate of the right down corner
     */
    public void setAnchor(int dx1, int dy1, int dx2, int dy2) {
        setDx1(Math.min(dx1, dx2));
        setDy1(Math.min(dy1, dy2));
        setDx2(Math.max(dx1, dx2));
        setDy2(Math.max(dy1, dy2));
    }


    public boolean isHorizontallyFlipped() {
        return _isHorizontallyFlipped;
    }


    public boolean isVerticallyFlipped() {
        return _isVerticallyFlipped;
    }

    @Override
    protected EscherRecord getEscherAnchor() {
        return _escherChildAnchor;
    }

    @Override
    protected void createEscherAnchor() {
        _escherChildAnchor = new EscherChildAnchorRecord();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;
        HSSFChildAnchor anchor = (HSSFChildAnchor) obj;

        return anchor.getDx1() == getDx1() && anchor.getDx2() == getDx2() && anchor.getDy1() == getDy1()
                && anchor.getDy2() == getDy2();
    }

    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }
}
