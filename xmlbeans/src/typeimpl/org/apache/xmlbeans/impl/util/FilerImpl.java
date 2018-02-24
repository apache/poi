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

package org.apache.xmlbeans.impl.util;

import org.apache.xmlbeans.Filer;

import java.io.IOException;
import java.io.File;
import java.io.Writer;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import repackage.Repackager;

/**
 * This implementation of Filer writes to disk.
 */
public class FilerImpl implements Filer
{
    private File classdir;
    private File srcdir;
    private Repackager repackager;
    private boolean verbose;
    private List sourceFiles;
    private boolean incrSrcGen;
    private Set seenTypes;
    private static final Charset CHARSET;

    static
    {
        Charset temp = null;
        try
        {
            temp = Charset.forName(System.getProperty("file.encoding"));
        }
        catch (Exception e) {}
        CHARSET = temp;
    }

    public FilerImpl(File classdir, File srcdir, Repackager repackager, boolean verbose, boolean incrSrcGen)
    {
        this.classdir = classdir;
        this.srcdir = srcdir;
        this.repackager = repackager;
        this.verbose = verbose;
        this.sourceFiles = (sourceFiles != null ? sourceFiles : new ArrayList());
        this.incrSrcGen = incrSrcGen;
        if (this.incrSrcGen)
            seenTypes = new HashSet();
    }

    /**
     * Creates a new schema binary file (.xsb) and returns a stream for writing to it.
     *
     * @param typename fully qualified type name
     * @return a stream to write the type to
     * @throws java.io.IOException
     */
    public OutputStream createBinaryFile(String typename) throws IOException
    {
        if (verbose)
            System.err.println("created binary: " + typename);
        // KHK: for now the typename will already be a relative filename for the binary
        //String filename = typename.replace('.', File.separatorChar) + ".xsb";
        File source = new File(classdir, typename);
        source.getParentFile().mkdirs();

        return new FileOutputStream( source );
    }

    /**
     * Creates a new binding source file (.java) and returns a writer for it.
     *
     * @param typename fully qualified type name
     * @return a stream to write the type to
     * @throws java.io.IOException
     */
    public Writer createSourceFile(String typename) throws IOException
    {
        if (incrSrcGen)
            seenTypes.add(typename);

        if (typename.indexOf('$') > 0)
        {
            typename =
                typename.substring( 0, typename.lastIndexOf( '.' ) ) + "." +
                typename.substring( typename.indexOf( '$' ) + 1 );
        }

        String filename = typename.replace('.', File.separatorChar) + ".java";

        File sourcefile = new File(srcdir, filename);
        sourcefile.getParentFile().mkdirs();
        if (verbose)
            System.err.println("created source: " + sourcefile.getAbsolutePath());

        sourceFiles.add(sourcefile);

        if (incrSrcGen && sourcefile.exists())
        {
            // Generate the file in a buffer and then compare it to the
            // file already on disk
            return new IncrFileWriter(sourcefile, repackager);
        }
        else
        {
            return repackager == null ?
                (Writer) writerForFile( sourcefile ) :
                (Writer) new RepackagingWriter( sourcefile, repackager );
        }
    }

    public List getSourceFiles()
    {
        return new ArrayList(sourceFiles);
    }

    public Repackager getRepackager()
    {
        return repackager;
    }

    private static final Writer writerForFile(File f) throws IOException
    {
        if (CHARSET == null)
            return new FileWriter(f);

        FileOutputStream fileStream = new FileOutputStream(f);
        CharsetEncoder ce = CHARSET.newEncoder();
        ce.onUnmappableCharacter(CodingErrorAction.REPORT);
        return new OutputStreamWriter(fileStream, ce);
    }

    static class IncrFileWriter extends StringWriter
    {
        private File _file;
        private Repackager _repackager;

        public IncrFileWriter(File file, Repackager repackager)
        {
            _file = file;
            _repackager = repackager;
        }

        public void close() throws IOException
        {
            super.close();

            // This is where all the real work happens
            StringBuffer sb = _repackager != null ?
                _repackager.repackage(getBuffer()) :
                getBuffer();
            String str = sb.toString();
            List diffs = new ArrayList();
            StringReader sReader = new StringReader(str);
            FileReader fReader = new FileReader(_file);

            try
            {
                Diff.readersAsText(sReader, "<generated>",
                    fReader, _file.getName(), diffs);
            }
            finally
            {
                sReader.close();
                fReader.close();
            }

            if (diffs.size() > 0)
            {
                // Diffs encountered, replace the file on disk with text from
                // the buffer
                Writer fw = writerForFile(_file);
                try
                {   fw.write(str); }
                finally
                {   fw.close(); }
            }
            else
                ; // If no diffs, don't do anything
        }
    }

    static class RepackagingWriter extends StringWriter
    {
        public RepackagingWriter ( File file, Repackager repackager )
        {
            _file = file;
            _repackager = repackager;
        }

        public void close ( ) throws IOException
        {
            super.close();

            Writer fw = writerForFile( _file );
            try
            { fw.write( _repackager.repackage( getBuffer() ).toString() ); }
            finally
            { fw.close(); }
        }

        private File _file;
        private Repackager _repackager;
    }
}
