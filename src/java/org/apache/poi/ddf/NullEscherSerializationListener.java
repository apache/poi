package org.apache.poi.ddf;

/**
 * Ignores all serialization events.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class NullEscherSerializationListener implements EscherSerializationListener
{
    public void beforeRecordSerialize( int offset, short recordId, EscherRecord record )
    {
        // do nothing
    }

    public void afterRecordSerialize( int offset, short recordId, int size, EscherRecord record )
    {
        // do nothing
    }

}
