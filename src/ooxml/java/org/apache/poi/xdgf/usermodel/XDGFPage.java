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

package org.apache.poi.xdgf.usermodel;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.util.Internal;
import org.apache.poi.xdgf.geom.Dimension2dDouble;

import com.microsoft.schemas.office.visio.x2012.main.PageType;

/**
 * Provides the API to work with an underlying page
 */
public class XDGFPage {

    private PageType _page;
    protected XDGFPageContents _content;
    protected XDGFPages _pages;
    protected XDGFSheet _pageSheet;

    public XDGFPage(PageType page, XDGFPageContents content,
            XDGFDocument document, XDGFPages pages) {
        _page = page;
        _content = content;
        _pages = pages;
        content.setPage(this);

        if (page.isSetPageSheet())
            _pageSheet = new XDGFPageSheet(page.getPageSheet(), document);
    }

    @Internal
    protected PageType getXmlObject() {
        return _page;
    }

    public long getID() {
        return _page.getID();
    }

    public String getName() {
        return _page.getName();
    }

    public XDGFPageContents getContent() {
        return _content;
    }

    public XDGFSheet getPageSheet() {
        return _pageSheet;
    }

    public long getPageNumber() {
        return _pages.getPageList().indexOf(this) + 1;
    }

    /**
     * @return width/height of page
     */
    public Dimension2dDouble getPageSize() {
        XDGFCell w = _pageSheet.getCell("PageWidth");
        XDGFCell h = _pageSheet.getCell("PageHeight");

        if (w == null || h == null)
            throw new POIXMLException("Cannot determine page size");

        return new Dimension2dDouble(Double.parseDouble(w.getValue()),
                Double.parseDouble(h.getValue()));
    }

    /**
     * @return origin of coordinate system
     */
    public Point2D.Double getPageOffset() {
        XDGFCell xoffcell = _pageSheet.getCell("XRulerOrigin");
        XDGFCell yoffcell = _pageSheet.getCell("YRulerOrigin");

        double xoffset = 0;
        double yoffset = 0;

        if (xoffcell != null)
            xoffset = Double.parseDouble(xoffcell.getValue());

        if (yoffcell != null)
            yoffset = Double.parseDouble(yoffcell.getValue());

        return new Point2D.Double(xoffset, yoffset);
    }

    /**
     * @return bounding box of page
     */
    public Rectangle2D getBoundingBox() {
        Dimension2dDouble sz = getPageSize();
        Point2D.Double offset = getPageOffset();

        return new Rectangle2D.Double(-offset.getX(), -offset.getY(),
                sz.getWidth(), sz.getHeight());
    }
}
