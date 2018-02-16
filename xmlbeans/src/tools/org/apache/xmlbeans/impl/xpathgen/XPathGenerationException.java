package org.apache.xmlbeans.impl.xpathgen;

import org.apache.xmlbeans.XmlException;

/**
 * An exception thrown if the XPath generation process can't complete
 */
public class XPathGenerationException extends XmlException
{
    public XPathGenerationException(String m)
    {
        super(m);
    }
}
