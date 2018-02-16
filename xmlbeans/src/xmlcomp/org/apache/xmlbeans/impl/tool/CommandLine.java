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

import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.impl.common.IOUtil;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandLine
{
    public CommandLine(String[] args, Collection flags, Collection scheme)
    {
        if (flags == null || scheme == null)
            throw new IllegalArgumentException("collection required (use Collections.EMPTY_SET if no options)");

        _options = new LinkedHashMap();
        ArrayList badopts = new ArrayList();
        ArrayList endargs = new ArrayList();

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].indexOf('-') == 0)
            {
                String opt = args[i].substring(1);
                String val = null;

                if (flags.contains(opt))
                    val = "";
                else if (scheme.contains(opt))
                {
                    if (i+1 < args.length)
                        val = args[++i];
                    else
                        val = "";
                }
                else
                    badopts.add(args[i]);

                _options.put(opt, val);
            }
            else
            {
                endargs.add(args[i]);
            }
        }

        _badopts = (String[])badopts.toArray(new String[badopts.size()]);
        _args = (String[])endargs.toArray(new String[endargs.size()]);
    }

    public static void printLicense()
    {
        try
        {
            IOUtil.copyCompletely(CommandLine.class.getClassLoader().getResourceAsStream("LICENSE.txt"), System.out);
        }
        catch (Exception e)
        {
            System.out.println("License available in this JAR in LICENSE.txt");
        }
    }

    public static void printVersion()
    {
        System.out.println(XmlBeans.getVendor() + ", " + XmlBeans.getTitle() + ".XmlBeans version " + XmlBeans.getVersion());
    }

    private Map _options;
    private String[] _badopts;
    private String[] _args;

    public String[] args()
    {
        String[] result = new String[_args.length];
        System.arraycopy(_args, 0, result, 0, _args.length);
        return result;
    }

    public String[] getBadOpts()
    {
        return _badopts;
    }

    public String getOpt(String opt)
    {
        return (String)_options.get(opt);
    }

    private static List collectFiles(File[] dirs)
    {
        List files = new ArrayList();
        for (int i = 0; i < dirs.length; i++)
        {
            File f = dirs[i];
            if (!f.isDirectory())
            {
                files.add(f);
            }
            else
            {
                files.addAll(collectFiles(f.listFiles()));
            }
        }
        return files;
    }

    private List _files;
    private List _urls;
    private File _baseDir;
    private static final File[] EMPTY_FILEARRAY = new File[0];
    private static final URL[] EMPTY_URLARRAY = new URL[0];

    private List getFileList()
    {
        if (_files == null)
        {
            String[] args = args();
            File[] files = new File[args.length];
            boolean noBaseDir = false;
            for (int i = 0; i < args.length; i++)
            {
                files[i] = new File(args[i]);
                if (!noBaseDir && (_baseDir == null))
                {
                    if (files[i].isDirectory())
                        _baseDir = files[i];
                    else
                        _baseDir = files[i].getParentFile();
                }
                else
                {
                    URI currUri = files[i].toURI();

                    // Give up on the basedir. There may be none
                    if (_baseDir != null && _baseDir.toURI().relativize(currUri).equals(currUri))
                    {
                        _baseDir = null;
                        noBaseDir = true;
                    }
                }
            }
            _files = Collections.unmodifiableList(collectFiles(files));
        }
        return _files;
    }

    private List getUrlList()
    {
        if (_urls == null)
        {
            String[] args = args();
            List urls = new ArrayList();

            for (int i = 0; i < args.length; i++)
            {
                if (looksLikeURL(args[i]))
                {
                    try
                    {
                        urls.add(new URL(args[i]));
                    }
                    catch (MalformedURLException mfEx)
                    {
                        System.err.println("ignoring invalid url: " + args[i] + ": " + mfEx.getMessage());
                    }
                }
            }

            _urls = Collections.unmodifiableList(urls);
        }

        return _urls;
    }

    private static boolean looksLikeURL(String str)
    {
        return str.startsWith("http:") || str.startsWith("https:") || str.startsWith("ftp:") || str.startsWith("file:");
    }

    public URL[] getURLs()
    {
        return (URL[]) getUrlList().toArray(EMPTY_URLARRAY);
    }

    public File[] getFiles()
    {
        return (File[])getFileList().toArray(EMPTY_FILEARRAY);
    }

    public File getBaseDir()
    {
        return _baseDir;
    }

    public File[] filesEndingWith(String ext)
    {
        List result = new ArrayList();
        for (Iterator i = getFileList().iterator(); i.hasNext(); )
        {
            File f = (File)i.next();
            if (f.getName().endsWith(ext) && !looksLikeURL(f.getPath()))
                result.add(f);
        }
        return (File[])result.toArray(EMPTY_FILEARRAY);
    }
}
