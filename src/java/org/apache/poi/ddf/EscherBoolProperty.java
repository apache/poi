package org.apache.poi.ddf;

/**
 * Represents a boolean property.  The actual utility of this property is in doubt because many
 * of the properties marked as boolean seem to actually contain special values.  In other words
 * they're not true booleans.
 *
 * @author Glen Stampoultzis
 * @see EscherSimpleProperty
 * @see EscherProperty
 */
public class EscherBoolProperty
        extends EscherSimpleProperty
{
    /**
     * Create an instance of an escher boolean property.
     *
     * @param propertyNumber The property number
     * @param value      The 32 bit value of this bool property
     */
    public EscherBoolProperty( short propertyNumber, int value )
    {
        super( propertyNumber, false, false, value );
    }

    /**
     * Whether this boolean property is true
     */
    public boolean isTrue()
    {
        return propertyValue != 0;
    }

    /**
     * Whether this boolean property is false
     */
    public boolean isFalse()
    {
        return propertyValue == 0;
    }

//    public String toString()
//    {
//        return "propNum: " + getPropertyNumber()
//                + ", complex: " + isComplex()
//                + ", blipId: " + isBlipId()
//                + ", value: " + (getValue() != 0);
//    }

}
