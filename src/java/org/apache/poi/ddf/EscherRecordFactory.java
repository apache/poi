package org.apache.poi.ddf;

/**
 * The escher record factory interface allows for the creation of escher
 * records from a pointer into a data array.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public interface EscherRecordFactory
{
    /**
     * Create a new escher record from the data provided.  Does not attempt
     * to fill the contents of the record however.
     */
    EscherRecord createRecord( byte[] data, int offset );
}
