package org.apache.poi.ddf;

/**
 * Interface for listening to escher serialization events.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public interface EscherSerializationListener
{
    /**
     * Fired before a given escher record is serialized.
     *
     * @param offset    The position in the data array at which the record will be serialized.
     * @param recordId  The id of the record about to be serialized.
     */
    void beforeRecordSerialize(int offset, short recordId, EscherRecord record);

    /**
     * Fired after a record has been serialized.
     *
     * @param offset    The position of the end of the serialized record + 1
     * @param recordId  The id of the record about to be serialized
     * @param size      The number of bytes written for this record.  If it is a container
     *                  record then this will include the size of any included records.
     */
    void afterRecordSerialize(int offset, short recordId, int size, EscherRecord record);
}
