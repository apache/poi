package org.apache.poi.hpsf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>Adds writing capability to the {@link Property} class.</p>
 * 
 * <p>Please be aware that this class' functionality will be merged into the
 * {@link Property} class at a later time, so the API will change.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 * @since 2003-08-03
 * @version $Id$
 */
public class MutableProperty extends Property
{

    /**
     * <p>Creates an empty property. It must be filled using the set method to
     * be usable.</p>
     */
    public MutableProperty()
    { }



    /**
     * <p>Sets the property's ID.</p>
     *
     * @param id the ID
     */
    public void setID(final long id)
    {
        this.id = id;
    }



    /**
     * <p>Sets the property's type.</p>
     *
     * @param type the property's type
     */
    public void setType(final long type)
    {
        this.type = type;
    }



    /**
     * <p>Sets the property's value.</p>
     *
     * @param value the property's value
     */
    public void setValue(final Object value)
    {
        this.value = value;
    }



    /**
     * <p>Writes the property to an output stream.</p>
     * 
     * @param out The output stream to write to.
     * @return the number of bytes written to the stream
     * 
     * @exception IOException if an I/O error occurs
     * @exception WritingNotSupportedException if a variant type is to be
     * written that is not yet supported
     */
    public int write(final OutputStream out)
        throws IOException, WritingNotSupportedException
    {
        int length = 0;
        long variantType = getType();
        length += TypeWriter.writeUIntToStream(out, variantType);
        length += VariantSupport.write(out, variantType, getValue());
        return length;
    }

}
