package org.apache.poi.hpsf;

/**
 * <p>This exception is thrown when trying to write a (yet) unsupported variant
 * type.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 * @since 2003-08-08
 * @version $Id$
 */
public class WritingNotSupportedException
    extends UnsupportedVariantTypeException
{

    /**
     * <p>Constructor</p>
     * 
     * @param variantType
     * @param value
     */
    public WritingNotSupportedException(long variantType, Object value)
    {
        super(variantType, value);
    }

}
