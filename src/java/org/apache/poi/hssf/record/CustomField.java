/* ====================================================================
   Copyright 2003-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.record;

public interface CustomField
        extends Cloneable
{
    /**
     * @return  The size of this field in bytes.  This operation is not valid
     *          until after the call to <code>fillField()</code>
     */
    int getSize();

    /**
     * Populates this fields data from the byte array passed in.
     * @param   data raw data
     * @param   size size of data
     * @param   offset of the record's data (provided a big array of the file)
     * @return  the number of bytes read.
     */
    int fillField(byte [] data, short size, int offset);

    /**
     * Appends the string representation of this field to the supplied
     * StringBuffer.
     *
     * @param str   The string buffer to append to.
     */
    void toString(StringBuffer str);

    /**
     * Converts this field to it's byte array form.
     * @param offset    The offset into the byte array to start writing to.
     * @param data      The data array to write to.
     * @return  The number of bytes written.
     */
    int serializeField(int offset, byte[] data);


}
