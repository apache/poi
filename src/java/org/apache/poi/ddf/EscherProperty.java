/* ====================================================================
   Copyright 2004   Apache Software Foundation

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
package org.apache.poi.ddf;

/**
 * This is the abstract base class for all escher properties.
 *
 * @see EscherOptRecord
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
abstract public class EscherProperty
{
    private short  id;

    /**
     * The id is distinct from the actual property number.  The id includes the property number the blip id
     * flag and an indicator whether the property is complex or not.
     */
    public EscherProperty( short id )
    {
        this.id   = id;
    }

    /**
     * Constructs a new escher property.  The three parameters are combined to form a property
     * id.
     */
    public EscherProperty( short propertyNumber, boolean isComplex, boolean isBlipId )
    {
        this.id   = (short)(propertyNumber +
                (isComplex ? 0x8000 : 0x0) +
                (isBlipId ? 0x4000 : 0x0));
    }

    public short getId()
    {
        return id;
    }

    public short getPropertyNumber()
    {
        return (short) ( id & (short) 0x3FFF );
    }

    public boolean isComplex()
    {
        return ( id & (short) 0x8000 ) != 0;
    }

    public boolean isBlipId()
    {
        return ( id & (short) 0x4000 ) != 0;
    }

    public String getName()
    {
        return EscherProperties.getPropertyName(id);
    }

    /**
     * Most properties are just 6 bytes in length.  Override this if we're
     * dealing with complex properties.
     */
    public int getPropertySize()
    {
        return 6;
    }

    /**
     * Escher properties consist of a simple fixed length part and a complex variable length part.
     * The fixed length part is serialized first.
     */
    abstract public int serializeSimplePart( byte[] data, int pos );
    /**
     * Escher properties consist of a simple fixed length part and a complex variable length part.
     * The fixed length part is serialized first.
     */
    abstract public int serializeComplexPart( byte[] data, int pos );
}
