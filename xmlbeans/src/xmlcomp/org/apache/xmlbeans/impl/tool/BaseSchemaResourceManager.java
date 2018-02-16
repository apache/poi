/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.impl.tool;

import org.apache.xmlbeans.impl.xb.xsdownload.DownloadedSchemasDocument;
import org.apache.xmlbeans.impl.xb.xsdownload.DownloadedSchemaEntry;
import org.apache.xmlbeans.impl.xb.xsdownload.DownloadedSchemasDocument.DownloadedSchemas;
import org.apache.xmlbeans.impl.util.HexBin;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlBeans;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;

public abstract class BaseSchemaResourceManager extends SchemaImportResolver
{
    private static final String USER_AGENT = "XMLBeans/" + XmlBeans.getVersion() + " (" + XmlBeans.getTitle() + ")";

    private String _defaultCopyDirectory;
    private DownloadedSchemasDocument _importsDoc;
    private Map _resourceForFilename = new HashMap();
    private Map _resourceForURL = new HashMap();
    private Map _resourceForNamespace = new HashMap();
    private Map _resourceForDigest = new HashMap();
    private Map _resourceForCacheEntry = new HashMap();
    private Set _redownloadSet = new HashSet();

    protected BaseSchemaResourceManager()
    {
        // concrete subclasses should call init in their constructors
    }

    protected final void init()
    {
        if (fileExists(getIndexFilename()))
        {
            try
            {
                _importsDoc = DownloadedSchemasDocument.Factory.parse( inputStreamForFile( getIndexFilename() ) );
            }
            catch (IOException e)
            {
                _importsDoc = null;
            }
            catch (Exception e)
            {
                throw (IllegalStateException)(new IllegalStateException("Problem reading xsdownload.xml: please fix or delete this file")).initCause(e);
            }
        }
        if (_importsDoc == null)
        {
            try
            {
                _importsDoc = DownloadedSchemasDocument.Factory.parse(
                    "<dls:downloaded-schemas xmlns:dls='http://www.bea.com/2003/01/xmlbean/xsdownload' defaultDirectory='" + getDefaultSchemaDir() + "'/>"
                );
            }
            catch (Exception e)
            {
                throw (IllegalStateException)(new IllegalStateException()).initCause(e);
            }
        }

        String defaultDir = _importsDoc.getDownloadedSchemas().getDefaultDirectory();
        if (defaultDir == null)
            defaultDir = getDefaultSchemaDir();;
        _defaultCopyDirectory = defaultDir;

        // now initialize data structures
        DownloadedSchemaEntry[] entries = _importsDoc.getDownloadedSchemas().getEntryArray();
        for (int i = 0; i < entries.length; i++)
        {
            updateResource(entries[i]);
        }
    }

    public final void writeCache() throws IOException
    {
        InputStream input = _importsDoc.newInputStream(new XmlOptions().setSavePrettyPrint());
        writeInputStreamToFile(input, getIndexFilename());
    }

    public final void processAll(boolean sync, boolean refresh, boolean imports)
    {
        if (refresh)
        {
            _redownloadSet = new HashSet();
        }
        else
        {
            _redownloadSet = null;
        }

        String[] allFilenames = getAllXSDFilenames();

        if (sync)
            syncCacheWithLocalXsdFiles(allFilenames, false);

        SchemaResource[] starters = (SchemaResource[])
                _resourceForFilename.values().toArray(new SchemaResource[0]);

        if (refresh)
            redownloadEntries(starters);

        if (imports)
            resolveImports(starters);

        _redownloadSet = null;
    }

    public final void process(String[] uris, String[] filenames, boolean sync, boolean refresh, boolean imports)
    {
        if (refresh)
        {
            _redownloadSet = new HashSet();
        }
        else
        {
            _redownloadSet = null;
        }

        if (filenames.length > 0)
            syncCacheWithLocalXsdFiles(filenames, true);
        else if (sync)
            syncCacheWithLocalXsdFiles(getAllXSDFilenames(), false);

        Set starterset = new HashSet();

        for (int i = 0; i < uris.length; i++)
        {
            SchemaResource resource = (SchemaResource)lookupResource(null, uris[i]);
            if (resource != null)
                starterset.add(resource);
        }

        for (int i = 0; i < filenames.length; i++)
        {
            SchemaResource resource = (SchemaResource)_resourceForFilename.get(filenames);
            if (resource != null)
                starterset.add(resource);
        }

        SchemaResource[] starters = (SchemaResource[])
               starterset.toArray(new SchemaResource[0]);

        if (refresh)
            redownloadEntries(starters);

        if (imports)
            resolveImports(starters);

        _redownloadSet = null;
    }

    /**
     * Adds items to the cache that point to new files that aren't
     * described in the cache, and optionally deletes old entries.
     *
     * If an old file is gone and a new file is
     * found with exactly the same contents, the cache entry is moved
     * to point to the new file.
     */
    public final void syncCacheWithLocalXsdFiles(String[] filenames, boolean deleteOnlyMentioned)
    {
        Set seenResources = new HashSet();
        Set vanishedResources = new HashSet();

        for (int i = 0; i < filenames.length; i++)
        {
            String filename = filenames[i];

            // first, if the filename matches exactly, trust the filename
            SchemaResource resource = (SchemaResource)_resourceForFilename.get(filename);
            if (resource != null)
            {
                if (fileExists(filename))
                    seenResources.add(resource);
                else
                    vanishedResources.add(resource);
                continue;
            }

            // new file that is not in the index?
            // not if the digest is known to the index and the original file is gone - that's a rename!
            String digest = null;
            try
            {
                digest = shaDigestForFile(filename);
                resource = (SchemaResource)_resourceForDigest.get(digest);
                if (resource != null)
                {
                    String oldFilename = resource.getFilename();
                    if (!fileExists(oldFilename))
                    {
                        warning("File " + filename + " is a rename of " + oldFilename);
                        resource.setFilename(filename);
                        seenResources.add(resource);
                        if (_resourceForFilename.get(oldFilename) == resource)
                            _resourceForFilename.remove(oldFilename);
                        if (_resourceForFilename.containsKey(filename))
                            _resourceForFilename.put(filename, resource);
                        continue;
                    }
                }
            }
            catch (IOException e)
            {
                // unable to read digest... no problem, ignore then
            }

            // ok, this really is a new XSD file then, of unknown URL origin
            DownloadedSchemaEntry newEntry = addNewEntry();
            newEntry.setFilename(filename);
            warning("Caching information on new local file " + filename);
            if (digest != null)
                newEntry.setSha1(digest);

            seenResources.add(updateResource(newEntry));
        }

        if (deleteOnlyMentioned)
            deleteResourcesInSet(vanishedResources, true);
        else
            deleteResourcesInSet(seenResources, false);
    }

    /**
     * Iterates through every entry and refetches it from its primary URL,
     * if known.  Replaces the contents of the file if the data is different.
     */
    private void redownloadEntries(SchemaResource[] resources)
    {
        for (int i = 0; i < resources.length; i++)
        {
            redownloadResource(resources[i]);
        }
    }

    private void deleteResourcesInSet(Set seenResources, boolean setToDelete)
    {
        Set seenCacheEntries = new HashSet();
        for (Iterator i = seenResources.iterator(); i.hasNext(); )
        {
            SchemaResource resource = (SchemaResource)i.next();
            seenCacheEntries.add(resource._cacheEntry);
        }

        DownloadedSchemas downloadedSchemas = _importsDoc.getDownloadedSchemas();
        for (int i = 0; i < downloadedSchemas.sizeOfEntryArray(); i++)
        {
            DownloadedSchemaEntry cacheEntry = downloadedSchemas.getEntryArray(i);

            if (seenCacheEntries.contains(cacheEntry) == setToDelete)
            {
                SchemaResource resource = (SchemaResource)_resourceForCacheEntry.get(cacheEntry);
                warning("Removing obsolete cache entry for " + resource.getFilename());

                if (resource != null)
                {
                    _resourceForCacheEntry.remove(cacheEntry);

                    if (resource == _resourceForFilename.get(resource.getFilename()))
                        _resourceForFilename.remove(resource.getFilename());

                    if (resource == _resourceForDigest.get(resource.getSha1()))
                        _resourceForDigest.remove(resource.getSha1());

                    if (resource == _resourceForNamespace.get(resource.getNamespace()))
                        _resourceForNamespace.remove(resource.getNamespace());

                    // Finally, any or all URIs
                    String[] urls = resource.getSchemaLocationArray();
                    for (int j = 0; j < urls.length; j++)
                    {
                        if (resource == _resourceForURL.get(urls[j]))
                            _resourceForURL.remove(urls[j]);
                    }
                }

                downloadedSchemas.removeEntry(i);
                i -= 1;
            }
        }
    }

    private SchemaResource updateResource(DownloadedSchemaEntry entry)
    {
        // The file
        String filename = entry.getFilename();
        if (filename == null)
            return null;

        SchemaResource resource = new SchemaResource(entry);
        _resourceForCacheEntry.put(entry, resource);

        if (!_resourceForFilename.containsKey(filename))
            _resourceForFilename.put(filename, resource);

        // The digest
        String digest = resource.getSha1();
        if (digest != null)
        {
            if (!_resourceForDigest.containsKey(digest))
                _resourceForDigest.put(digest, resource);
        }

        // Next, the namespace
        String namespace = resource.getNamespace();
        if (namespace != null)
        {
            if (!_resourceForNamespace.containsKey(namespace))
                _resourceForNamespace.put(namespace, resource);
        }

        // Finally, any or all URIs
        String[] urls = resource.getSchemaLocationArray();
        for (int j = 0; j < urls.length; j++)
        {
            if (!_resourceForURL.containsKey(urls[j]))
                _resourceForURL.put(urls[j], resource);
        }

        return resource;
    }

    private static DigestInputStream digestInputStream(InputStream input)
    {
        MessageDigest sha;
        try
        {
            sha = MessageDigest.getInstance("SHA");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw (IllegalStateException)(new IllegalStateException().initCause(e));
        }

        DigestInputStream str = new DigestInputStream(input, sha);

        return str;
    }

    private DownloadedSchemaEntry addNewEntry()
    {
        return _importsDoc.getDownloadedSchemas().addNewEntry();
    }

    private class SchemaResource implements SchemaImportResolver.SchemaResource
    {
        SchemaResource(DownloadedSchemaEntry entry)
        {
            _cacheEntry = entry;
        }

        DownloadedSchemaEntry _cacheEntry;

        public void setFilename(String filename)
        {
            _cacheEntry.setFilename(filename);
        }

        public String getFilename()
        {
            return _cacheEntry.getFilename();
        }

        public Schema getSchema()
        {
            if (!fileExists(getFilename()))
                redownloadResource(this);

            try
            {
                return SchemaDocument.Factory.parse(inputStreamForFile(getFilename())).getSchema();
            }
            catch (Exception e)
            {
                return null; // return null if _any_ problems reading schema file
            }
        }

        public String getSha1()
        {
            return _cacheEntry.getSha1();
        }

        public String getNamespace()
        {
            return _cacheEntry.getNamespace();
        }

        public void setNamespace(String namespace)
        {
            _cacheEntry.setNamespace(namespace);
        }

        public String getSchemaLocation()
        {
            if (_cacheEntry.sizeOfSchemaLocationArray() > 0)
                return _cacheEntry.getSchemaLocationArray(0);
            return null;
        }

        public String[] getSchemaLocationArray()
        {
            return _cacheEntry.getSchemaLocationArray();
        }

        public int hashCode()
        {
            return getFilename().hashCode();
        }

        public boolean equals(Object obj)
        {
            return this == obj || getFilename().equals(((SchemaResource)obj).getFilename());
        }

        public void addSchemaLocation(String schemaLocation)
        {
            _cacheEntry.addSchemaLocation(schemaLocation);
        }
    }

    /**
     * Called when the ImportLoader wishes to resolve the
     * given import.  Should return a SchemaResource whose
     * "equals" relationship reveals when a SchemaResource is
     * duplicated and shouldn't be examined again.
     *
     * Returns null if the resource reference should be ignored.
     */
    public SchemaImportResolver.SchemaResource lookupResource(String nsURI, String schemaLocation)
    {
        SchemaResource result = fetchFromCache(nsURI, schemaLocation);
        if (result != null)
        {
            if (_redownloadSet != null)
            {
                redownloadResource(result);
            }
            return result;
        }

        if (schemaLocation == null)
        {
            warning("No cached schema for namespace '" + nsURI + "', and no url specified");
            return null;
        }

        result = copyOrIdentifyDuplicateURL(schemaLocation, nsURI);
        if (_redownloadSet != null)
            _redownloadSet.add(result);
        return result;
    }

    private SchemaResource fetchFromCache(String nsURI, String schemaLocation)
    {
        SchemaResource result;

        if (schemaLocation != null)
        {
            result = (SchemaResource)_resourceForURL.get(schemaLocation);
            if (result != null)
                return result;
        }

        if (nsURI != null)
        {
            result = (SchemaResource)_resourceForNamespace.get(nsURI);
            if (result != null)
                return result;
        }

        return null;
    }

    private String uniqueFilenameForURI(String schemaLocation) throws IOException, URISyntaxException
    {
        String localFilename = new URI( schemaLocation ).getRawPath();
        int i = localFilename.lastIndexOf('/');
        if (i >= 0)
            localFilename = localFilename.substring(i + 1);
        if (localFilename.endsWith(".xsd"))
            localFilename = localFilename.substring(0, localFilename.length() - 4);
        if (localFilename.length() == 0)
            localFilename = "schema";

        // TODO: remove other unsafe characters for filenames?

        String candidateFilename = localFilename;
        int suffix = 1;
        while (suffix < 1000)
        {
            String candidate = _defaultCopyDirectory + "/" + candidateFilename + ".xsd";
            if (!fileExists(candidate))
                return candidate;
            suffix += 1;
            candidateFilename = localFilename + suffix;
        }

        throw new IOException("Problem with filename " + localFilename + ".xsd");
    }

    private void redownloadResource(SchemaResource resource)
    {
        if (_redownloadSet != null)
        {
            if (_redownloadSet.contains(resource))
                return;
            _redownloadSet.add(resource);
        }

        String filename = resource.getFilename();
        String schemaLocation = resource.getSchemaLocation();
        String digest = null;

        // nothing to do?
        if (schemaLocation == null || filename == null)
            return;

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try
        {
            URL url = new URL( schemaLocation );
            URLConnection conn = url.openConnection();
            conn.addRequestProperty("User-Agent", USER_AGENT);
            conn.addRequestProperty("Accept", "application/xml, text/xml, */*");
            DigestInputStream input = digestInputStream(conn.getInputStream());
            IOUtil.copyCompletely(input, buffer);
            digest = HexBin.bytesToString(input.getMessageDigest().digest());
        }
        catch (Exception e)
        {
            warning("Could not copy remote resource " + schemaLocation + ":" + e.getMessage());
            return;
        }

        if (digest.equals(resource.getSha1()) && fileExists(filename))
        {
            warning("Resource " + filename + " is unchanged from " + schemaLocation + ".");
            return;
        }

        try
        {
            InputStream source = new ByteArrayInputStream(buffer.toByteArray());
            writeInputStreamToFile(source, filename);
        }
        catch (IOException e)
        {
            warning("Could not write to file " + filename + " for " + schemaLocation + ":" + e.getMessage());
            return;
        }

        warning("Refreshed " + filename + " from " + schemaLocation);
    }

    private SchemaResource copyOrIdentifyDuplicateURL(String schemaLocation, String namespace)
    {
        String targetFilename;
        String digest;
        SchemaResource result;

        try
        {
            targetFilename = uniqueFilenameForURI(schemaLocation);
        }
        catch (URISyntaxException e)
        {
            warning("Invalid URI '" + schemaLocation + "':" + e.getMessage());
            return null;
        }
        catch (IOException e)
        {
            warning("Could not create local file for " + schemaLocation + ":" + e.getMessage());
            return null;
        }

        try
        {
            URL url = new URL( schemaLocation );
            DigestInputStream input = digestInputStream(url.openStream());
            writeInputStreamToFile(input, targetFilename);
            digest = HexBin.bytesToString(input.getMessageDigest().digest());
        }
        catch (Exception e)
        {
            warning("Could not copy remote resource " + schemaLocation + ":" + e.getMessage());
            return null;
        }

        result = (SchemaResource)_resourceForDigest.get(digest);
        if (result != null)
        {
            deleteFile(targetFilename);
            result.addSchemaLocation(schemaLocation);
            if (!_resourceForURL.containsKey(schemaLocation))
                _resourceForURL.put(schemaLocation, result);
            return result;
        }

        warning("Downloaded " + schemaLocation + " to " + targetFilename);

        DownloadedSchemaEntry newEntry = addNewEntry();
        newEntry.setFilename(targetFilename);
        newEntry.setSha1(digest);
        if (namespace != null)
            newEntry.setNamespace(namespace);
        newEntry.addSchemaLocation(schemaLocation);
        return updateResource(newEntry);
    }

    /**
     * Updates actual namespace in the table.
     */
    public void reportActualNamespace(SchemaImportResolver.SchemaResource rresource, String actualNamespace)
    {
        SchemaResource resource = (SchemaResource)rresource;
        String oldNamespace = resource.getNamespace();
        if (oldNamespace != null && _resourceForNamespace.get(oldNamespace) == resource)
            _resourceForNamespace.remove(oldNamespace);
        if (!_resourceForNamespace.containsKey(actualNamespace))
            _resourceForNamespace.put(actualNamespace, resource);
        resource.setNamespace(actualNamespace);
    }

    private String shaDigestForFile(String filename) throws IOException
    {
        DigestInputStream str = digestInputStream(inputStreamForFile(filename));

        byte[] dummy = new byte[4096];
        for (int i = 1; i > 0; i = str.read(dummy));

        str.close();

        return HexBin.bytesToString(str.getMessageDigest().digest());
    }

    // SOME METHODS TO OVERRIDE ============================

    protected String getIndexFilename()
    {
        return "./xsdownload.xml";
    }

    protected String getDefaultSchemaDir()
    {
        return "./schema";
    }

    /**
     * Produces diagnostic messages such as "downloading X to file Y".
     */
    abstract protected void warning(String msg);

    /**
     * Returns true if the given filename exists.  The filenames
     * are of the form "/foo/bar/zee.xsd" and should be construed
     * as rooted at the root of the project.
     */
    abstract protected boolean fileExists(String filename);

    /**
     * Gets the data in the given filename as an InputStream.
     */
    abstract protected InputStream inputStreamForFile(String filename) throws IOException;

    /**
     * Writes an entire file in one step.  An InputStream is passed and
     * copied to the file.
     */
    abstract protected void writeInputStreamToFile(InputStream input, String filename) throws IOException;

    /**
     * Deletes a file.  Sometimes immediately after writing a new file
     * we notice that it's exactly the same as an existing file and
     * we delete it. We never delete a file that was given to us
     * by the user.
     */
    abstract protected void deleteFile(String filename);

    /**
     * Returns a list of all the XSD filesnames in the project.
     */
    abstract protected String[] getAllXSDFilenames();
}
