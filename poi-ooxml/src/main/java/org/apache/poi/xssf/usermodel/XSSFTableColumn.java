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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.util.Internal;
import org.apache.poi.xssf.usermodel.helpers.XSSFXmlColumnPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXmlColumnPr;

/**
 * A table column of an {@link XSSFTable}. Use {@link XSSFTable#createColumn} to
 * create new table columns.
 *
 * @since 4.0.0
 */
public class XSSFTableColumn {

    private final XSSFTable table;
    private final CTTableColumn ctTableColumn;
    private XSSFXmlColumnPr xmlColumnPr;

    /**
     * Create a new table column.
     *
     * @param table
     *            the table which contains the column
     * @param ctTableColumn
     *            the table column xmlbean to wrap
     * @since 4.0.0
     */
    @Internal
    protected XSSFTableColumn(XSSFTable table, CTTableColumn ctTableColumn) {
        this.table = table;
        this.ctTableColumn = ctTableColumn;
    }

    /**
     * Get the table which contains this column
     *
     * @return the table containing this column
     * @since 4.0.0
     */
    public XSSFTable getTable() {
        return table;
    }

    /**
     * Get the identifier of this column, which is is unique per table.
     *
     * @return the column id
     * @since 4.0.0
     */
    public long getId() {
        return ctTableColumn.getId();
    }

    /**
     * Set the identifier of this column, which must be unique per table.
     *
     * It is up to the caller to enforce the uniqueness of the id.
     *
     * @param columnId the column id
     * @since 4.0.0
     */
    public void setId(long columnId) {
        ctTableColumn.setId(columnId);
    }

    /**
     * Get the name of the column, which is is unique per table.
     *
     * @return the column name
     * @since 4.0.0
     */
    public String getName() {
        return ctTableColumn.getName();
    }

    /**
     * Get the name of the column, which is is unique per table.
     *
     * @param columnName  the column name
     * @since 4.0.0
     */
    public void setName(String columnName) {
        ctTableColumn.setName(columnName);
    }

    /**
     * Get the XmlColumnPr (XML column properties) if this column has an XML
     * mapping.
     *
     * @return the XmlColumnPr or <code>null</code> if this column has no XML
     *         mapping
     * @since 4.0.0
     */
    public XSSFXmlColumnPr getXmlColumnPr() {
        if (xmlColumnPr == null) {
            CTXmlColumnPr ctXmlColumnPr = ctTableColumn.getXmlColumnPr();
            if (ctXmlColumnPr != null) {
                xmlColumnPr = new XSSFXmlColumnPr(this, ctXmlColumnPr);
            }
        }
        return xmlColumnPr;
    }

    /**
     * Get the column's position in its table, staring with zero from left to
     * right.
     *
     * @return the column index
     * @since 4.0.0
     */
    public int getColumnIndex() {
        return table.findColumnIndex(getName());
    }

}
