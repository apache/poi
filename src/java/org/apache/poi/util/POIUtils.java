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
package org.apache.poi.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

@Internal
public class POIUtils
{

    /**
     * Copies an Entry into a target POIFS directory, recursively
     */
    @Internal
    public static void copyNodeRecursively( Entry entry, DirectoryEntry target )
            throws IOException
    {
        // System.err.println("copyNodeRecursively called with "+entry.getName()+
        // ","+target.getName());
        DirectoryEntry newTarget = null;
        if ( entry.isDirectoryEntry() )
        {
            newTarget = target.createDirectory( entry.getName() );
            Iterator<Entry> entries = ( (DirectoryEntry) entry ).getEntries();

            while ( entries.hasNext() )
            {
                copyNodeRecursively( entries.next(), newTarget );
            }
        }
        else
        {
            DocumentEntry dentry = (DocumentEntry) entry;
            DocumentInputStream dstream = new DocumentInputStream( dentry );
            target.createDocument( dentry.getName(), dstream );
            dstream.close();
        }
    }

    /**
     * Copies nodes from one POIFS to the other minus the excepts
     * 
     * @param source
     *            is the source POIFS to copy from
     * @param target
     *            is the target POIFS to copy to
     * @param excepts
     *            is a list of Strings specifying what nodes NOT to copy
     */
    public static void copyNodes( DirectoryEntry sourceRoot,
            DirectoryEntry targetRoot, List<String> excepts )
            throws IOException
    {
        Iterator<Entry> entries = sourceRoot.getEntries();
        while ( entries.hasNext() )
        {
            Entry entry = entries.next();
            if ( !excepts.contains( entry.getName() ) )
            {
                copyNodeRecursively( entry, targetRoot );
            }
        }
    }

    /**
     * Copies nodes from one POIFS to the other minus the excepts
     * 
     * @param source
     *            is the source POIFS to copy from
     * @param target
     *            is the target POIFS to copy to
     * @param excepts
     *            is a list of Strings specifying what nodes NOT to copy
     */
    public static void copyNodes( POIFSFileSystem source,
            POIFSFileSystem target, List<String> excepts ) throws IOException
    {
        // System.err.println("CopyNodes called");
        copyNodes( source.getRoot(), target.getRoot(), excepts );
    }
}
