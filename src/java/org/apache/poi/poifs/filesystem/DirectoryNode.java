
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.poifs.filesystem;

import java.io.*;

import java.util.*;

import org.apache.poi.hpsf.ClassID;
import org.apache.poi.poifs.dev.POIFSViewable;
import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.DocumentProperty;
import org.apache.poi.poifs.property.Property;

/**
 * Simple implementation of DirectoryEntry
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class DirectoryNode
    extends EntryNode
    implements DirectoryEntry, POIFSViewable, Iterable<Entry>
{

    // Map of Entry instances, keyed by their names
    private Map<String,Entry> _byname;
    // Our list of entries, kept sorted to preserve order
    private ArrayList<Entry> _entries;

    // the POIFSFileSystem we belong to
    private POIFSFileSystem   _filesystem;

    // the path described by this document
    private POIFSDocumentPath _path;

    /**
     * create a DirectoryNode. This method is not public by design; it
     * is intended strictly for the internal use of this package
     *
     * @param property the DirectoryProperty for this DirectoryEntry
     * @param filesystem the POIFSFileSystem we belong to
     * @param parent the parent of this entry
     */

    DirectoryNode(final DirectoryProperty property,
                  final POIFSFileSystem filesystem,
                  final DirectoryNode parent)
    {
        super(property, parent);
        if (parent == null)
        {
            _path = new POIFSDocumentPath();
        }
        else
        {
            _path = new POIFSDocumentPath(parent._path, new String[]
            {
                property.getName()
            });
        }
        _filesystem = filesystem;
        _byname     = new HashMap<String, Entry>();
        _entries    = new ArrayList<Entry>();
        Iterator<Property> iter = property.getChildren();

        while (iter.hasNext())
        {
            Property child     = iter.next();
            Entry    childNode = null;

            if (child.isDirectory())
            {
                childNode = new DirectoryNode(( DirectoryProperty ) child,
                                              _filesystem, this);
            }
            else
            {
                childNode = new DocumentNode(( DocumentProperty ) child,
                                             this);
            }
            _entries.add(childNode);
            _byname.put(childNode.getName(), childNode);
        }
    }

    /**
     * @return this directory's path representation
     */

    public POIFSDocumentPath getPath()
    {
        return _path;
    }
    
    /**
     * @return the filesystem that this belongs to
     */
    
    public POIFSFileSystem getFileSystem()
    {
        return _filesystem; 
    }
    
    /**
     * open a document in the directory's entry's list of entries
     *
     * @param documentName the name of the document to be opened
     *
     * @return a newly opened DocumentInputStream
     *
     * @exception IOException if the document does not exist or the
     *            name is that of a DirectoryEntry
     */

    public DocumentInputStream createDocumentInputStream(
            final String documentName)
        throws IOException
    {
        Entry document = getEntry(documentName);

        if (!document.isDocumentEntry())
        {
            throw new IOException("Entry '" + documentName
                                  + "' is not a DocumentEntry");
        }
        return new DocumentInputStream(( DocumentEntry ) document);
    }

    /**
     * create a new DocumentEntry
     *
     * @param document the new document
     *
     * @return the new DocumentEntry
     *
     * @exception IOException
     */

    DocumentEntry createDocument(final POIFSDocument document)
        throws IOException
    {
        DocumentProperty property = document.getDocumentProperty();
        DocumentNode     rval     = new DocumentNode(property, this);

        (( DirectoryProperty ) getProperty()).addChild(property);
        _filesystem.addDocument(document);
        _entries.add(rval);
        _byname.put(property.getName(), rval);
        return rval;
    }

    /**
     * Change a contained Entry's name
     *
     * @param oldName the original name
     * @param newName the new name
     *
     * @return true if the operation succeeded, else false
     */

    boolean changeName(final String oldName, final String newName)
    {
        boolean   rval  = false;
        EntryNode child = ( EntryNode ) _byname.get(oldName);

        if (child != null)
        {
            rval = (( DirectoryProperty ) getProperty())
                .changeName(child.getProperty(), newName);
            if (rval)
            {
                _byname.remove(oldName);
                _byname.put(child.getProperty().getName(), child);
            }
        }
        return rval;
    }

    /**
     * Delete an entry
     *
     * @param entry the EntryNode to be deleted
     *
     * @return true if the entry was deleted, else false
     */

    boolean deleteEntry(final EntryNode entry)
    {
        boolean rval =
            (( DirectoryProperty ) getProperty())
                .deleteChild(entry.getProperty());

        if (rval)
        {
            _entries.remove(entry);
        	_byname.remove(entry.getName());
            _filesystem.remove(entry);
        }
        return rval;
    }

    /* ********** START implementation of DirectoryEntry ********** */

    /**
     * get an iterator of the Entry instances contained directly in
     * this instance (in other words, children only; no grandchildren
     * etc.)
     *
     * @return iterator; never null, but hasNext() may return false
     *         immediately (i.e., this DirectoryEntry is empty). All
     *         objects retrieved by next() are guaranteed to be
     *         implementations of Entry.
     */

    public Iterator<Entry> getEntries()
    {
        return _entries.iterator();
    }

    /**
     * is this DirectoryEntry empty?
     *
     * @return true if this instance contains no Entry instances
     */

    public boolean isEmpty()
    {
        return _entries.isEmpty();
    }

    /**
     * find out how many Entry instances are contained directly within
     * this DirectoryEntry
     *
     * @return number of immediately (no grandchildren etc.) contained
     *         Entry instances
     */

    public int getEntryCount()
    {
        return _entries.size();
    }

    /**
     * get a specified Entry by name
     *
     * @param name the name of the Entry to obtain.
     *
     * @return the specified Entry, if it is directly contained in
     *         this DirectoryEntry
     *
     * @exception FileNotFoundException if no Entry with the specified
     *            name exists in this DirectoryEntry
     */

    public Entry getEntry(final String name)
        throws FileNotFoundException
    {
        Entry rval = null;

        if (name != null)
        {
            rval = _byname.get(name);
        }
        if (rval == null)
        {

            // either a null name was given, or there is no such name
            throw new FileNotFoundException("no such entry: \"" + name
                                            + "\"");
        }
        return rval;
    }

    /**
     * create a new DocumentEntry
     *
     * @param name the name of the new DocumentEntry
     * @param stream the InputStream from which to create the new
     *               DocumentEntry
     *
     * @return the new DocumentEntry
     *
     * @exception IOException
     */

    public DocumentEntry createDocument(final String name,
                                        final InputStream stream)
        throws IOException
    {
        return createDocument(new POIFSDocument(name, stream));
    }

    /**
     * create a new DocumentEntry; the data will be provided later
     *
     * @param name the name of the new DocumentEntry
     * @param size the size of the new DocumentEntry
     * @param writer the writer of the new DocumentEntry
     *
     * @return the new DocumentEntry
     *
     * @exception IOException
     */

    public DocumentEntry createDocument(final String name, final int size,
                                        final POIFSWriterListener writer)
        throws IOException
    {
        return createDocument(new POIFSDocument(name, size, _path, writer));
    }

    /**
     * create a new DirectoryEntry
     *
     * @param name the name of the new DirectoryEntry
     *
     * @return the new DirectoryEntry
     *
     * @exception IOException
     */

    public DirectoryEntry createDirectory(final String name)
        throws IOException
    {
        DirectoryProperty property = new DirectoryProperty(name);
        DirectoryNode     rval     = new DirectoryNode(property, _filesystem,
                                         this);

        (( DirectoryProperty ) getProperty()).addChild(property);
        _filesystem.addDirectory(property);
        _entries.add(rval);
        _byname.put(name, rval);
        return rval;
    }

    /**
     * Gets the storage clsid of the directory entry
     *
     * @return storage Class ID
     */
    public ClassID getStorageClsid()
    {
        return getProperty().getStorageClsid();
    }

    /**
     * Sets the storage clsid for the directory entry
     *
     * @param clsidStorage storage Class ID
     */
    public void setStorageClsid(ClassID clsidStorage)
    {
        getProperty().setStorageClsid(clsidStorage);
    }

    /* **********  END  implementation of DirectoryEntry ********** */
    /* ********** START implementation of Entry ********** */

    /**
     * is this a DirectoryEntry?
     *
     * @return true if the Entry is a DirectoryEntry, else false
     */

    public boolean isDirectoryEntry()
    {
        return true;
    }

    /* **********  END  implementation of Entry ********** */
    /* ********** START extension of Entry ********** */

    /**
     * extensions use this method to verify internal rules regarding
     * deletion of the underlying store.
     *
     * @return true if it's ok to delete the underlying store, else
     *         false
     */

    protected boolean isDeleteOK()
    {

        // if this directory is empty, we can delete it
        return isEmpty();
    }

    /* **********  END  extension of Entry ********** */
    /* ********** START begin implementation of POIFSViewable ********** */

    /**
     * Get an array of objects, some of which may implement
     * POIFSViewable
     *
     * @return an array of Object; may not be null, but may be empty
     */

    public Object [] getViewableArray()
    {
        return new Object[ 0 ];
    }

    /**
     * Get an Iterator of objects, some of which may implement
     * POIFSViewable
     *
     * @return an Iterator; may not be null, but may have an empty
     * back end store
     */
    @SuppressWarnings("unchecked")
    public Iterator getViewableIterator()
    {
        List components = new ArrayList();

        components.add(getProperty());
        Iterator<Entry> iter = _entries.iterator();
        while (iter.hasNext())
        {
            components.add(iter.next());
        }
        return components.iterator();
    }

    /**
     * Give viewers a hint as to whether to call getViewableArray or
     * getViewableIterator
     *
     * @return true if a viewer should call getViewableArray, false if
     *         a viewer should call getViewableIterator
     */

    public boolean preferArray()
    {
        return false;
    }

    /**
     * Provides a short description of the object, to be used when a
     * POIFSViewable object has not provided its contents.
     *
     * @return short description
     */

    public String getShortDescription()
    {
        return getName();
    }

    /**
     * Returns an Iterator over all the entries
     */
    public Iterator<Entry> iterator() {
        return getEntries(); 
    }

    /* **********  END  begin implementation of POIFSViewable ********** */
}   // end public class DirectoryNode

