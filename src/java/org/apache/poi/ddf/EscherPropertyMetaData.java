package org.apache.poi.ddf;

/**
 * This class stores the type and description of an escher property.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class EscherPropertyMetaData
{
    // Escher property types.
    public final static byte TYPE_UNKNOWN = (byte) 0;
    public final static byte TYPE_BOOLEAN = (byte) 1;
    public final static byte TYPE_RGB = (byte) 2;
    public final static byte TYPE_SHAPEPATH = (byte) 3;
    public final static byte TYPE_SIMPLE = (byte)4;
    public final static byte TYPE_ARRAY = (byte)5;;

    private String description;
    private byte type;


    /**
     * @param description The description of the escher property.
     */
    public EscherPropertyMetaData( String description )
    {
        this.description = description;
    }

    /**
     *
     * @param description   The description of the escher property.
     * @param type          The type of the property.
     */
    public EscherPropertyMetaData( String description, byte type )
    {
        this.description = description;
        this.type = type;
    }

    public String getDescription()
    {
        return description;
    }

    public byte getType()
    {
        return type;
    }

}
