package org.apache.poi.ddf;

/**
 * A color property.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class EscherRGBProperty
        extends EscherSimpleProperty
{

    public EscherRGBProperty( short propertyNumber, int rgbColor )
    {
        super( propertyNumber, false, false, rgbColor );
    }

    public int getRgbColor()
    {
        return propertyValue;
    }

    public byte getRed()
    {
        return (byte) ( propertyValue & 0xFF );
    }

    public byte getGreen()
    {
        return (byte) ( (propertyValue >> 8) & 0xFF );
    }

    public byte getBlue()
    {
        return (byte) ( (propertyValue >> 16) & 0xFF );
    }

}
