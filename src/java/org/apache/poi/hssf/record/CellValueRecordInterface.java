
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 * CellValueRecordInterface.java
 *
 * Created on October 2, 2001, 8:27 PM
 */
package org.apache.poi.hssf.record;

/**
 * The cell value record interface is implemented by all classes of type Record that
 * contain cell values.  It allows the containing sheet to move through them and compare
 * them.
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 *
 * @see org.apache.poi.hssf.model.Sheet
 * @see org.apache.poi.hssf.record.Record
 * @see org.apache.poi.hssf.record.RecordFactory
 */

public interface CellValueRecordInterface
{

    /**
     * get the row this cell occurs on
     *
     * @return the row
     */

    public short getRow();

    /**
     * get the column this cell defines within the row
     *
     * @return the column
     */

    public short getColumn();

    /**
     * set the row this cell occurs on
     * @param row the row this cell occurs within
     */

    public void setRow(short row);

    /**
     * set the column this cell defines within the row
     *
     * @param col the column this cell defines
     */

    public void setColumn(short col);

    public void setXFIndex(short xf);

    public short getXFIndex();

    /**
     * returns whether this cell is before the passed in cell
     *
     * @param i  another cell interface record to compare
     * @return true if the cells is before, or false if not
     */

    public boolean isBefore(CellValueRecordInterface i);

    /**
     * returns whether this cell is after the passed in cell
     *
     * @param i  record to compare
     * @return true if the cell is after, false if not
     */

    public boolean isAfter(CellValueRecordInterface i);

    /**
     * returns whether this cell represents the same cell (NOT VALUE)
     *
     * @param i  record to compare
     * @return true if the cells are the same cell (positionally), false if not.
     */

    public boolean isEqual(CellValueRecordInterface i);
}
