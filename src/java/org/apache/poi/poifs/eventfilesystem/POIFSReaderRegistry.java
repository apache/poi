
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

package org.apache.poi.poifs.eventfilesystem;

import java.util.*;

import org.apache.poi.poifs.filesystem.DocumentDescriptor;
import org.apache.poi.poifs.filesystem.POIFSDocumentPath;

/**
 * A registry for POIFSReaderListeners and the DocumentDescriptors of
 * the documents those listeners are interested in
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 * @version %I%, %G%
 */

class POIFSReaderRegistry
{

    // the POIFSReaderListeners who listen to all POIFSReaderEvents
    private Set omnivorousListeners;

    // Each mapping in this Map has a key consisting of a
    // POIFSReaderListener and a value cosisting of a Set of
    // DocumentDescriptors for the documents that POIFSReaderListener
    // is interested in; used to efficiently manage the registry
    private Map selectiveListeners;

    // Each mapping in this Map has a key consisting of a
    // DocumentDescriptor and a value consisting of a Set of
    // POIFSReaderListeners for the document matching that
    // DocumentDescriptor; used when a document is found, to quickly
    // get the listeners interested in that document
    private Map chosenDocumentDescriptors;

    /**
     * Construct the registry
     */

    POIFSReaderRegistry()
    {
        omnivorousListeners       = new HashSet();
        selectiveListeners        = new HashMap();
        chosenDocumentDescriptors = new HashMap();
    }

    /**
     * register a POIFSReaderListener for a particular document
     *
     * @param listener the listener
     * @param path the path of the document of interest
     * @param documentName the name of the document of interest
     */

    void registerListener(final POIFSReaderListener listener,
                          final POIFSDocumentPath path,
                          final String documentName)
    {
        if (!omnivorousListeners.contains(listener))
        {

            // not an omnivorous listener (if it was, this method is a
            // no-op)
            Set descriptors = ( Set ) selectiveListeners.get(listener);

            if (descriptors == null)
            {

                // this listener has not registered before
                descriptors = new HashSet();
                selectiveListeners.put(listener, descriptors);
            }
            DocumentDescriptor descriptor = new DocumentDescriptor(path,
                                                documentName);

            if (descriptors.add(descriptor))
            {

                // this listener wasn't already listening for this
                // document -- add the listener to the set of
                // listeners for this document
                Set listeners =
                    ( Set ) chosenDocumentDescriptors.get(descriptor);

                if (listeners == null)
                {

                    // nobody was listening for this document before
                    listeners = new HashSet();
                    chosenDocumentDescriptors.put(descriptor, listeners);
                }
                listeners.add(listener);
            }
        }
    }

    /**
     * register for all documents
     *
     * @param listener the listener who wants to get all documents
     */

    void registerListener(final POIFSReaderListener listener)
    {
        if (!omnivorousListeners.contains(listener))
        {

            // wasn't already listening for everything, so drop
            // anything listener might have been listening for and
            // then add the listener to the set of omnivorous
            // listeners
            removeSelectiveListener(listener);
            omnivorousListeners.add(listener);
        }
    }

    /**
     * get am iterator of listeners for a particular document
     *
     * @param path the document path
     * @param name the name of the document
     *
     * @return an Iterator POIFSReaderListeners; may be empty
     */

    Iterator getListeners(final POIFSDocumentPath path, final String name)
    {
        Set rval               = new HashSet(omnivorousListeners);
        Set selectiveListeners =
            ( Set ) chosenDocumentDescriptors.get(new DocumentDescriptor(path,
                name));

        if (selectiveListeners != null)
        {
            rval.addAll(selectiveListeners);
        }
        return rval.iterator();
    }

    private void removeSelectiveListener(final POIFSReaderListener listener)
    {
        Set selectedDescriptors = ( Set ) selectiveListeners.remove(listener);

        if (selectedDescriptors != null)
        {
            Iterator iter = selectedDescriptors.iterator();

            while (iter.hasNext())
            {
                dropDocument(listener, ( DocumentDescriptor ) iter.next());
            }
        }
    }

    private void dropDocument(final POIFSReaderListener listener,
                              final DocumentDescriptor descriptor)
    {
        Set listeners = ( Set ) chosenDocumentDescriptors.get(descriptor);

        listeners.remove(listener);
        if (listeners.size() == 0)
        {
            chosenDocumentDescriptors.remove(descriptor);
        }
    }
}   // end package scope class POIFSReaderRegistry

