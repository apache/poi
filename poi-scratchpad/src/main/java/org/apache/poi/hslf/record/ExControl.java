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

package org.apache.poi.hslf.record;


/**
 * A container record that specifies information about an ActiveX control. It contains:
 * <p>
 * 1. ExControlAtom (4091)
 * 2. ExOleObjAtom (4035)
 * 3. CString (4026), Instance MenuName (1) used for menus and the Links dialog box.
 * 4. CString (4026), Instance ProgID (2) that stores the OLE Programmatic Identifier.
 *  A ProgID is a string that uniquely identifies a given object.
 * 5. CString (4026), Instance ClipboardName (3) that appears in the paste special dialog.
 * 6. MetaFile( 4033), optional
 * </p>
 *
 *
 * @author Yegor kozlov
 */
public final class ExControl extends ExEmbed {

    /**
     * Set things up, and find our more interesting children
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected ExControl(byte[] source, int start, int len) {
        super(source, start, len);
    }

    /**
     * Create a new ExEmbed, with blank fields
     */
    public ExControl() {
        super();

        _children[0] = embedAtom = new ExControlAtom();
    }

    /**
     * Gets the {@link ExControlAtom}.
     *
     * @return the {@link ExControlAtom}.
     */
    public ExControlAtom getExControlAtom()
    {
        return (ExControlAtom)_children[0];
    }

    /**
     * Returns the type (held as a little endian in bytes 3 and 4)
     * that this class handles.
     *
     * @return the record type.
     */
    public long getRecordType() {
        return RecordTypes.ExControl.typeID;
    }
}
