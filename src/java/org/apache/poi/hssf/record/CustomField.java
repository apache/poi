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
