package org.apache.poi.ddf;

/**
 * Defines the constants for the various possible shape paths.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class EscherShapePathProperty
        extends EscherSimpleProperty
{

    public static final int LINE_OF_STRAIGHT_SEGMENTS = 0;
    public static final int CLOSED_POLYGON = 1;
    public static final int CURVES = 2;
    public static final int CLOSED_CURVES = 3;
    public static final int COMPLEX = 4;

    public EscherShapePathProperty( short propertyNumber, int shapePath )
    {
        super( propertyNumber, false, false, shapePath );
    }



}
