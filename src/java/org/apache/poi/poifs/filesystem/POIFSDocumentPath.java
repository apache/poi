
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

package org.apache.poi.poifs.filesystem;

import java.io.File;

/**
 * Class POIFSDocumentPath
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 * @version %I%, %G%
 */

public class POIFSDocumentPath
{
    private String[] components;
    private int      hashcode = 0;

    /**
     * constructor for the path of a document that is not in the root
     * of the POIFSFileSystem
     *
     * @param components the Strings making up the path to a document.
     *                   The Strings must be ordered as they appear in
     *                   the directory hierarchy of the the document
     *                   -- the first string must be the name of a
     *                   directory in the root of the POIFSFileSystem,
     *                   and every Nth (for N > 1) string thereafter
     *                   must be the name of a directory in the
     *                   directory identified by the (N-1)th string.
     *                   <p>
     *                   If the components parameter is null or has
     *                   zero length, the POIFSDocumentPath is
     *                   appropriate for a document that is in the
     *                   root of a POIFSFileSystem
     *
     * @exception IllegalArgumentException if any of the elements in
     *                                     the components parameter
     *                                     are null or have zero
     *                                     length
     */

    public POIFSDocumentPath(final String [] components)
        throws IllegalArgumentException
    {
        if (components == null)
        {
            this.components = new String[ 0 ];
        }
        else
        {
            this.components = new String[ components.length ];
            for (int j = 0; j < components.length; j++)
            {
                if ((components[ j ] == null)
                        || (components[ j ].length() == 0))
                {
                    throw new IllegalArgumentException(
                        "components cannot contain null or empty strings");
                }
                this.components[ j ] = components[ j ];
            }
        }
    }

    /**
     * simple constructor for the path of a document that is in the
     * root of the POIFSFileSystem. The constructor that takes an
     * array of Strings can also be used to create such a
     * POIFSDocumentPath by passing it a null or empty String array
     */

    public POIFSDocumentPath()
    {
        this.components = new String[ 0 ];
    }

    /**
     * constructor that adds additional subdirectories to an existing
     * path
     *
     * @param path the existing path
     * @param components the additional subdirectory names to be added
     *
     * @exception IllegalArgumentException if any of the Strings in
     *                                     components is null or zero
     *                                     length
     */

    public POIFSDocumentPath(final POIFSDocumentPath path,
                             final String [] components)
        throws IllegalArgumentException
    {
        if (components == null)
        {
            this.components = new String[ path.components.length ];
        }
        else
        {
            this.components =
                new String[ path.components.length + components.length ];
        }
        for (int j = 0; j < path.components.length; j++)
        {
            this.components[ j ] = path.components[ j ];
        }
        if (components != null)
        {
            for (int j = 0; j < components.length; j++)
            {
                if ((components[ j ] == null)
                        || (components[ j ].length() == 0))
                {
                    throw new IllegalArgumentException(
                        "components cannot contain null or empty strings");
                }
                this.components[ j + path.components.length ] =
                    components[ j ];
            }
        }
    }

    /**
     * equality. Two POIFSDocumentPath instances are equal if they
     * have the same number of component Strings, and if each
     * component String is equal to its coresponding component String
     *
     * @param o the object we're checking equality for
     *
     * @return true if the object is equal to this object
     */

    public boolean equals(final Object o)
    {
        boolean rval = false;

        if ((o != null) && (o.getClass() == this.getClass()))
        {
            if (this == o)
            {
                rval = true;
            }
            else
            {
                POIFSDocumentPath path = ( POIFSDocumentPath ) o;

                if (path.components.length == this.components.length)
                {
                    rval = true;
                    for (int j = 0; j < this.components.length; j++)
                    {
                        if (!path.components[ j ]
                                .equals(this.components[ j ]))
                        {
                            rval = false;
                            break;
                        }
                    }
                }
            }
        }
        return rval;
    }

    /**
     * calculate and return the hashcode
     *
     * @return hashcode
     */

    public int hashCode()
    {
        if (hashcode == 0)
        {
            for (int j = 0; j < components.length; j++)
            {
                hashcode += components[ j ].hashCode();
            }
        }
        return hashcode;
    }

    /**
     * @return the number of components
     */

    public int length()
    {
        return components.length;
    }

    /**
     * get the specified component
     *
     * @param n which component (0 ... length() - 1)
     *
     * @return the nth component;
     *
     * @exception ArrayIndexOutOfBoundsException if n < 0 or n >=
     *                                           length()
     */

    public String getComponent(int n)
        throws ArrayIndexOutOfBoundsException
    {
        return components[ n ];
    }

    /**
     * <p>Returns the path's parent or <code>null</code> if this path
     * is the root path.</p>
     *
     * @author Rainer Klute (klute@rainer-klute.de)
     * @since 2002-01-24
     *
     * @return path of parent, or null if this path is the root path
     */

    public POIFSDocumentPath getParent()
    {
        final int length = components.length - 1;

        if (length < 0)
        {
            return null;
        }
        POIFSDocumentPath parent = new POIFSDocumentPath(null);

        parent.components = new String[ length ];
        System.arraycopy(components, 0, parent.components, 0, length);
        return parent;
    }

    /**
     * <p>Returns a string representation of the path. Components are
     * separated by the platform-specific file separator.</p>
     *
     * @author Rainer Klute (klute@rainer-klute.de)
     * @since 2002-01-24
     *
     * @return string representation
     */

    public String toString()
    {
        final StringBuffer b = new StringBuffer();
        final int          l = length();

        b.append(File.separatorChar);
        for (int i = 0; i < l; i++)
        {
            b.append(getComponent(i));
            if (i < l - 1)
            {
                b.append(File.separatorChar);
            }
        }
        return b.toString();
    }
}   // end public class POIFSDocumentPath

