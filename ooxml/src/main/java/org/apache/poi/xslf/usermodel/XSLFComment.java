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

package org.apache.poi.xslf.usermodel;

import java.awt.geom.Point2D;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.sl.usermodel.Comment;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.Units;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.presentationml.x2006.main.CTComment;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommentAuthor;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommentAuthorList;

/**
 * XSLF Comment
 *
 * @since POI 4.0.0
 */
public class XSLFComment implements Comment {

    final CTComment comment;
    final XSLFCommentAuthors authors;

    XSLFComment(final CTComment comment, final XSLFCommentAuthors authors) {
        this.comment = comment;
        this.authors = authors;
    }

    @Override
    public String getAuthor() {
        return authors.getAuthorById(comment.getAuthorId()).getName();
    }

    @Override
    public void setAuthor(final String author) {
        if (author == null) {
            throw new IllegalArgumentException("author must not be null");
        }
        final CTCommentAuthorList list = authors.getCTCommentAuthorsList();
        long maxId = -1;
        for (final CTCommentAuthor aut : list.getCmAuthorArray()) {
            maxId = Math.max(aut.getId(), maxId);
            if (author.equals(aut.getName())) {
                comment.setAuthorId(aut.getId());
                return;
            }
        }
        // author not found -> add new author
        final CTCommentAuthor newAuthor = list.addNewCmAuthor();
        newAuthor.setName(author);
        newAuthor.setId(maxId+1);
        newAuthor.setInitials(author.replaceAll(	"\\s*(\\w)\\S*", "$1").toUpperCase(LocaleUtil.getUserLocale()));
        comment.setAuthorId(maxId+1);
    }

    @Override
    public String getAuthorInitials() {
        final CTCommentAuthor aut = authors.getAuthorById(comment.getAuthorId());
        return aut == null ? null : aut.getInitials();
    }

    @Override
    public void setAuthorInitials(final String initials) {
        final CTCommentAuthor aut = authors.getAuthorById(comment.getAuthorId());
        if (aut != null) {
            aut.setInitials(initials);
        }
    }

    @Override
    public String getText() {
        return comment.getText();
    }

    @Override
    public void setText(final String text) {
        comment.setText(text);
    }

    @Override
    public Date getDate() {
        final Calendar cal = comment.getDt();
        return (cal == null) ? null : cal.getTime();
    }

    @Override
    public void setDate(final Date date) {
        final Calendar cal = LocaleUtil.getLocaleCalendar();
        cal.setTime(date);
        comment.setDt(cal);
    }

    @Override
    public Point2D getOffset() {
        final CTPoint2D pos = comment.getPos();
        return new Point2D.Double(
            Units.toPoints(POIXMLUnits.parseLength(pos.xgetX())),
            Units.toPoints(POIXMLUnits.parseLength(pos.xgetY())));
    }

    @Override
    public void setOffset(final Point2D offset) {
        CTPoint2D pos = comment.getPos();
        if (pos == null) {
            pos = comment.addNewPos();
        }
        pos.setX(Units.toEMU(offset.getX()));
        pos.setY(Units.toEMU(offset.getY()));
    }
}
