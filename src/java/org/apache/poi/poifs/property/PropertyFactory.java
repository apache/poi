
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.poifs.property;

import java.io.IOException;

import java.util.*;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.storage.ListManagedBlock;

/**
 * Factory for turning an array of RawDataBlock instances containing
 * Proprty data into an array of proper Property objects.
 *
 * The array produced may be sparse, in that any portion of data that
 * should correspond to a Property, but which does not map to a proper
 * Property (i.e., a DirectoryProperty, DocumentProperty, or
 * RootProperty) will get mapped to a null Property in the array.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

class PropertyFactory
{

    // no need for an accessible constructor
    private PropertyFactory()
    {
    }

    /**
     * Convert raw data blocks to an array of Property's
     *
     * @param blocks to be converted
     *
     * @return the converted List of Property objects. May contain
     *         nulls, but will not be null
     *
     * @exception IOException if any of the blocks are empty
     */

    static List convertToProperties(ListManagedBlock [] blocks)
        throws IOException
    {
        List properties = new ArrayList();

        for (int j = 0; j < blocks.length; j++)
        {
            byte[] data           = blocks[ j ].getData();
            int    property_count = data.length
                                    / POIFSConstants.PROPERTY_SIZE;
            int    offset         = 0;

            for (int k = 0; k < property_count; k++)
            {
                switch (data[ offset + PropertyConstants.PROPERTY_TYPE_OFFSET ])
                {

                    case PropertyConstants.DIRECTORY_TYPE :
                        properties
                            .add(new DirectoryProperty(properties.size(),
                                                       data, offset));
                        break;

                    case PropertyConstants.DOCUMENT_TYPE :
                        properties.add(new DocumentProperty(properties.size(),
                                                            data, offset));
                        break;

                    case PropertyConstants.ROOT_TYPE :
                        properties.add(new RootProperty(properties.size(),
                                                        data, offset));
                        break;

                    default :
                        properties.add(null);
                        break;
                }
                offset += POIFSConstants.PROPERTY_SIZE;
            }
        }
        return properties;
    }
}   // end package scope class PropertyFactory

