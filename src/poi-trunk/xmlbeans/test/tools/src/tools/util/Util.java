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
package tools.util;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.JarURLConnection;
import java.security.MessageDigest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.Connection;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.Iterator;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

/**
 * Utilities to copy files, directories, etc.
 */
public class Util
{
    private static byte[] _byteBuffer = new byte[32768];
    private static char[] _charBuffer = new char[32768];
    private static boolean filteredStackTrace = true;

    /**
     * Read the contents of the given file into a String.
     */
    public static String read(File file)
            throws IOException
    {
        FileInputStream fis = new FileInputStream(file);
        return read(new InputStreamReader(fis, "UTF8"));
    }

    /**
     * Read the contents of the given input stream into a String.  This will
     * also close the stream.
     */
    public static String read(InputStream in)
            throws IOException
    {
        return read(new InputStreamReader(in, "UTF8"));
    }

    /**
     * Read the contents in the given <code>Reader</code> into a String. This
     * will also close the reader.
     */
    public static String read(Reader in)
            throws IOException
    {
        try
        {
            StringBuffer sb = new StringBuffer(_charBuffer.length);
            int amount = 0;

            while (true)
            {
                synchronized (_charBuffer)
                {
                    amount = in.read(_charBuffer);

                    if (amount != -1)
                        sb.append(_charBuffer, 0, amount);
                    else
                        break;
                }
            }
            return sb.toString();
        }
        finally
        {
            close(in);
        }
    }

    public static String read(Reader in, int length)
            throws IOException
    {
        BufferedReader bin = null;

        try
        {
            bin = new BufferedReader(in, length);
            char[] s = new char[length];
            bin.read(s, 0, length);

            return new String(s);
        }
        finally
        {
            close(bin);
        }
    }

    /**
     * Read the contents of the given file line by line into a String array
     *
     * If the second argument is true, then all whitespace lines at the end will
     * be removed from the array
     */
    public static String[] readIntoArray(File file, boolean trimLines)
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        Vector v = new Vector();

        while (true)
        {
            String s = reader.readLine();
            if (s == null) break;
            v.addElement(s);
        }
        reader.close();
        // Discard all trailing lines that are only whitespaces..
        if (trimLines)
        {
            int i = v.size();
            while (--i >= 0)
                if (isWhiteSpace((String) v.get(i)))
                    v.removeElementAt(i);
        }

        String[] strArray = new String[v.size()];
        v.copyInto(strArray);
        return strArray;
    }

    public static void copy(InputStream in, OutputStream out)
            throws IOException
    {
        try
        {
            int amount = 0;

            while (true)
            {
                synchronized (_byteBuffer)
                {
                    amount = in.read(_byteBuffer);
                    if (amount == -1)
                        break;
                    out.write(_byteBuffer, 0, amount);
                }
            }
        }
        finally
        {
            close(in);
            close(out);
        }
    }

    /**
     * Copy a stream to a file.
     */
    public static void copyToFile(InputStream in, File toFile,
                                  boolean overwrite)
            throws IOException
    {
        if (toFile.exists() && !overwrite)
            return;

        // create any parent directories
        File parent = toFile.getParentFile();
        if (parent != null)
            parent.mkdirs();

        // creates a new file only if it doesn't exist
        toFile.createNewFile();

        copy(in, new FileOutputStream(toFile));
    }

    /**
     * Copy URL to file.
     */
    public static void copyToFile(URL url, File toFile,
                                  boolean replaceNewer)
            throws IOException
    {
        Log.debug("copyToFile(url=" + url + ",\n toFile=" + toFile + ")");

        URLConnection conn = url.openConnection();
        if (toFile.exists() &&
                toFile.lastModified() > conn.getLastModified() &&
                !replaceNewer)
            return;

        copyToFile(conn.getInputStream(), toFile, true);
    }

    /**
     * Copy file to file.
     */
    public static void copyToFile(File fromFile, File toFile,
                                  boolean replaceNewer)
            throws IOException
    {
        Log.debug("copyToFile(fromFile=" + fromFile + ",\n toFile=" + toFile +
                  ")");

        // don't replace newer files unless flag is set
        if (toFile.exists() &&
                toFile.lastModified() > fromFile.lastModified() &&
                !replaceNewer)
            return;

        copyToFile(new FileInputStream(fromFile),
                   toFile, true);
    }

    /**
     * Copy file to a dir.
     */
    public static void copyToDir(File fromFile, File toDir,
                                 boolean replaceNewer)
            throws IOException
    {
        //System.out.println("copyToDir(fromFile=" + fromFile +
        //",\n toDir=" + toDir + ")");

        toDir.mkdirs();

        copyToFile(fromFile, new File(toDir, fromFile.getName()),
                   replaceNewer);
    }

    /**
     * Copy URL to a dir.
     */
    public static void copyToDir(URL url, File toDir,
                                 boolean replaceNewer)
            throws IOException
    {
        //System.out.println("copyToDir(url=" + url +
        //",\n toDir=" + toDir + ")");

        toDir.mkdirs();

        copyToFile(url, new File(toDir, url.getFile()),
                   replaceNewer);
    }

    /**
     * Recursively copy a dir to a new dir. Creates target tree if needed.
     */
    public static void copyDirToDir(File fromDir, File toDir,
                                    boolean replaceNewer, final String[] exclude)
            throws IOException
    {
        //System.out.println("copyDirToDir(fromDir=" + fromDir +
        //",\n toDir=" + toDir + ")");

        File[] fs = fromDir.listFiles();
        COPY_FILE_LOOP:
        for (int i = 0; i < fs.length; i++)
        {
            // exclude based only on last part of file name
            String name = fs[i].getName();

            if (exclude != null)
            {
                for (int j = 0; j < exclude.length; ++j)
                {
                    if (name.equals(exclude[j]))
                        continue COPY_FILE_LOOP;
                }
            }

            if (fs[i].isFile())
            {
                copyToDir(fs[i], toDir, replaceNewer);
            } else
            {
                copyDirToDir(fs[i], new File(toDir, fs[i].getName()),
                             replaceNewer, exclude);
            }
        }
    }

    /**
     * Recursively copy a jar dir entry to a new dir.  This is expensive since
     * we have to iterate over all the entries in the .zip/.jar file.
     * The <code>fromDir</code> parameter must end in '/'.
     */
    public static void copyDirToDir(ZipFile zip, String fromDir,
                                    File toDir, boolean replaceNewer, final String[] exclude)
            throws IOException
    {
        //System.out.println("copyDirToDir(zip=" + zip +
        //", fromDir=" + fromDir +
        //", toDir=" + toDir + ")");

        if (!fromDir.endsWith("/"))
            return;

        Enumeration _enum = zip.entries();
        COPY_JAR_LOOP:
        while (_enum.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry) _enum.nextElement();
            //System.out.println("  entry = " + entry.getName());

            // skip directories
            if (entry.isDirectory())
                continue;

            if (!entry.getName().startsWith(fromDir))
                continue;

            String entryFile = entry.getName().substring(fromDir.length());

            // FIXME: exclude files matching any pattern in exclude array

            // use this class' loader to obtain the resource
            URL url = Util.class.getResource("/" + entry.getName());
            if (url == null)
            {
                throw new java.io.IOException("Resource not found: " +
                                              entry.toString());
            }
            copyToFile(url, new File(toDir, entryFile), replaceNewer);
        }
    }

    /**
     * Copy a dir url to a new dir.
     */
    public static void copyDirToDir(URL url, File toDir,
                                    boolean replaceNewer, String[] exclude)
            throws IOException
    {
        Log.debug("copyDirToDir(url=" + url + ", toDir=" + toDir + ")");

        // url must end in '/'
        if (!url.getFile().endsWith("/"))
            return;

        if ("file".equals(url.getProtocol()))
        {
            copyDirToDir(new File(url.getPath()), toDir,
                         replaceNewer, exclude);
        } else if ("jar".equals(url.getProtocol()))
        {
            JarURLConnection conn = (JarURLConnection) url.openConnection();
            copyDirToDir(conn.getJarFile(), conn.getEntryName(),
                         toDir, replaceNewer, exclude);
        } else if ("zip".equals(url.getProtocol()))
        {
            URL newUrl = new URL("jar:file:" + url.getPath());
            Log.debug("changed zip url to = " + newUrl);

            copyDirToDir(newUrl, toDir, replaceNewer, exclude);
        } else
        {
            throw new IOException("Protocol not supported yet: " +
                                  url.getProtocol());
        }
    }

    /**
     * Copy a dir to a new dir.
     */
    public static void copyDirToDir(File fromDir, File toDir,
                                    boolean replaceNewer)
            throws IOException
    {
        copyDirToDir(fromDir, toDir, replaceNewer, null);
    }

    /**
     * Recursively remove a directory and it's contents.
     */
    public static void remove(String file)
    {
        remove(new File(file));
    }

    /**
     * Recursively remove a directory and it's contents.
     */
    public static void remove(File file)
    {
        if (file == null || !file.exists())
            return;

        if (file.isFile())
            removeFile(file);
        else if (file.isDirectory())
            removeDir(file);
    }

    /**
     * Remove a directory's contents.
     */
    private static void removeDir(File dir)
    {
        File[] entries = dir.listFiles();

        if (entries == null)
        {
            Log.fatal("IO Error or dir doesn't exist: " + dir);
            return;
        }

        for (int i = 0; i < entries.length; ++i)
            remove(entries[i]);

        Log.debug("removing dir: " + dir.toString());
        dir.delete();
    }

    /**
     * Remote a file.
     */
    private static void removeFile(File file)
    {
        Log.debug("removing file: " + file.toString());
        file.delete();
    }

    /**
     * @deprecated This is really overkill.
     *
     *             Parses command-line arguments into a Hashtable.
     *             <pre>
     *             command-line : ( arg-assignment )* the-rest
     *             arg-assignment : option-name option-value?
     *             option-name : "any string with a leading '-' "
     *             option-value : "any string without a leading '-' "
     *             the-rest : "any args after options ended"
     *
     *             The following rules are used:
     *             - If an option appears multiple times, then its value is OVERWRITTEN!
     *             - If no value is given for an option, then the Hashtable entry contains a Boolean object TRUE
     *             - the rest of the arguments are stored as an array under the special key @REST
     *             - @REST value is always filled, at least with an EMPTY ARRAY (and not a null!!!)
     *             - An option of "-" ends option parsing.  Use this when a value-less option is followed by the-rest
     *
     *             Examples: (1) "-foo bar -goo zabba -boo -hoo" is parsed as
     *                           {foo -> bar, goo -> zabba, -boo -> TRUE, -hoo -> TRUE}
     *                       (2) "-foo bar -foo bar2 aaa bbb ccc" is parsed as { foo -> bar2, @REST -> [aaa,bbb,ccc] }
     *
     *             Rationale:
     *               The above grammar and rules are less powerful than those given by gnu.getopt, but
     *               are easier to use for our purposes
     *             </pre>
     */
    public static HashMap parseArgs(String args[])
    {
        HashMap ht = new HashMap();
        int k;
        int n = args.length;

        for (k = 0; k < n; k++)
        {
            // Stop option processing if not an option or is the single character '-'
            if (args[k].startsWith("-"))
            {
                // eat the '-' and end option processing if it's just a '-'
                if (args[k].length() == 1)
                {
                    k++;
                    break;
                }

                String opt = args[k].substring(1); // skip -
                String optarg = null;
                if ((k < n - 1) && !args[k + 1].startsWith("-"))
                {
                    // got an option value
                    optarg = args[k + 1];
                    k++;
                }

                ht.put(opt,
                       (null != optarg) ?
                       (Object) optarg :
                       (Object) Boolean.TRUE);
            } else
            {
                break;
            }
        }

        // either we have run out of options or
        // we have hit the first non-option argument
        //
        int n_rest = n - k;
        String rest[] = new String[n_rest];
        int j;

        for (j = 0; k < n; j++, k++)
        {
            rest[j] = args[k];
        }

        ht.put("@REST", rest);

        return ht;
    }

    /**
     * @deprecated This is really overkill.
     *
     *             This is a subset of the above parser.  It assumes only boolean options, but
     *             allows arguments to be interspersed with options.
     *
     *             Parses command-line arguments into a Hashtable using the following grammar:
     *             command-line : ( option-name | argument )*
     *             option-name : "any string with a leading '-' "
     *             argument : "any string without a leading '-' "
     *
     *             The following rules are used:
     *             - If an option appears multiple times, then its value is OVERWRITTEN!
     *             - The Hashtable entry for any option contains a Boolean object TRUE
     *             - the rest of the arguments are stored as an array under the special key @REST
     *             - @REST value is always filled, at least with an EMPTY ARRAY (and not a null!!!)
     *             - An option of "-" ends option parsing.  Use this before an argument that must begin with a '-'
     *
     *             Examples: (1) "-foo bar -goo zabba -boo -hoo" is parsed as
     *             {foo -> TRUE, goo -> TRUE, -boo -> TRUE, -hoo -> TRUE, @REST -> [bar,zabba]}
     *             (2) "-foo bar -foo bar2 aaa - -bbb -ccc"
     *             is parsed as { foo -> TRUE, @REST -> [bar,bar2,aaa,-bbb,-ccc] }
     *
     *             Rationale:
     *             parseArgs does not have a way of specifying a trailing boolean option followed by an
     *             argument except through the '-' hack.  It is unable to implement, for example, the argument scanning of
     *             SystemSchemaBooter without forcing a change in the command-line syntax
     */
    public static HashMap parseOptions(String args[])
    {
        HashMap ht = new HashMap();
        int k;
        int n = args.length;
        int nOptions = 0;

        for (k = 0; k < n; k++)
        {
            // Stop option processing if not an option or is the single character '-'
            if (args[k].startsWith("-"))
            {
                nOptions++;
                // eat the '-' and end option processing if it's just a '-'
                if (args[k].length() == 1)
                {
                    k++;
                    break;
                }

                String opt = args[k].substring(1); // skip -
                ht.put(opt, (Object) Boolean.TRUE);
            }
        }

        // either we have run out of options or
        // we have hit a single '-'
        //
        int n_rest = n - nOptions;
        String rest[] = new String[n_rest];
        boolean bIgnoreOptions = false;
        int j = 0;
        // Rescan the args and put non-options in the rest array
        for (k = 0; k < n; k++)
        {
            if (bIgnoreOptions || !args[k].startsWith("-"))
            {
                rest[j++] = args[k];
            } else if (args[k].length() == 1)
            {
                bIgnoreOptions = true;
            }
        }

        ht.put("@REST", rest);

        return ht;
    }


    /**
     * Close a possibly null output stream. Ignore any exceptions.
     */
    static public void close(OutputStream stream)
    {
        if (stream == null)
            return;

        try
        {
            stream.close();
        }
        catch (Exception ignore)
        {
        }
    }

    /**
     * Close a possibly null input stream. Ignore any exceptions.
     */
    static public void close(InputStream stream)
    {
        if (stream == null)
            return;

        try
        {
            stream.close();
        }
        catch (Exception ignore)
        {
        }
    }


    /**
     * Close a possibly null reader. Ignore any exceptions.
     */
    static public void close(Reader reader)
    {
        if (reader == null)
            return;

        try
        {
            reader.close();
        }
        catch (Exception ignore)
        {
        }
    }

    /**
     * Close a possibly null writer. Ignore any exceptions.
     */
    static public void close(Writer writer)
    {
        if (writer == null)
            return;

        try
        {
            writer.close();
        }
        catch (Exception ignore)
        {
        }
    }

    /**
     * Close a possibly null server socket. Ignore any exceptions.
     */
    static public void close(ServerSocket socket)
    {
        if (socket == null)
            return;

        try
        {
            socket.close();
        }
        catch (Exception ignore)
        {
        }
    }

    /**
     * Close a possibly null socket. Ignore any exceptions.
     */
    static public void close(Socket socket)
    {
        if (socket == null)
            return;

        try
        {
            socket.close();
        }
        catch (Exception ignore)
        {
        }
    }

    /**
     * Copy from an output stream to an input stream.
     */

    static public int
            copyStream(InputStream in, OutputStream out, byte[] buffer)
            throws IOException
    {
        int c;
        int length = 0;

        if (buffer == null)
            buffer = new byte[4096];

        while ((c = in.read(buffer)) > 0)
        {
            length += c;
            out.write(buffer, 0, c);
        }

        return length;
    }

    public static String
            hexStringFromBytes(byte[] bytes)
    {
        String hex = "0123456789abcdef";
        StringBuffer buf = new StringBuffer(2 * bytes.length);

        for (int i = 0; i < bytes.length; i++)
        {
            int b = bytes[i];
            buf.append(hex.charAt((b >> 4) & 0xf));
            buf.append(hex.charAt(b & 0xf));
        }

        return buf.toString();
    }

    /**
     * Convert a nibble to a hex character
     *
     * @param	nibble	the nibble to convert.
     */
    public static char toHex(int nibble)
    {
        return hexDigit[(nibble & 0xF)];
    }

    /**
     * A table of hex digits
     */
    private static final char[] hexDigit = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static String gobbleUpReader(Reader in)
            throws Exception
    {
        StringBuffer buf = new StringBuffer();
        int c;

        while (-1 != (c = in.read()))
        {
            buf.append(c);
        }

        return buf.toString();
    }

    public static String createHashedPassword(String user, String password)
    {
        MessageDigest md = null;
        try
        {
            md = MessageDigest.getInstance("SHA1", "SUN");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }

        md.update(user.getBytes());
        md.update(password.getBytes());

        return hexStringFromBytes(md.digest());
    }

    static public String makeHtmlStringNoNewLine(String sIn)
    {
        if (null == sIn)
            return null;

        int lenIn = sIn.length();
        int iIn;

        StringBuffer outBuf = new StringBuffer(lenIn + lenIn / 4);  // Plenty of room for extra characters
        char c;

        for (iIn = 0; iIn < lenIn; ++iIn)
        {
            c = sIn.charAt(iIn);
            switch (c)
            {
                case '&':
                    outBuf.append("&amp;");
                    break;
                case '"':
                    outBuf.append("&quot;");
                    break;
                case '<':
                    outBuf.append("&lt;");
                    break;
                case '>':
                    outBuf.append("&gt;");
                    break;
                default:
                    outBuf.append(c);
                    break;
            }
        }

        return outBuf.toString();
    }

    /**
     * Helper function to split a String into an array of Strings somewhat
     * like the JDK 1.4.1 {@link java.lang.String#split(String)} method does.
     */
    public static java.util.List splitList(String s, String match)
    {
        java.util.List strings = new java.util.ArrayList();
        s.trim();

        while (!s.equals(""))
        {
            if (s.indexOf(match) != -1)
            {
                strings.add(s.substring(0, s.indexOf(match)));
                s = s.substring(s.indexOf(match) + 1, s.length());
            } else
            {
                strings.add(s);
                s = "";
            }
            s.trim();
        }

        return strings;
    }

    /**
     * Helper function to split a String into an array of Strings somewhat
     * like the JDK 1.4.1 {@link java.lang.String#split(String)} method does.
     */
    public static String[] split(String s, String match)
    {
        java.util.List strings = splitList(s, match);
        return (String[]) strings.toArray((Object[]) (new String[0]));
    }

    /**
     * Programtically turn on/off the stack trace filter.
     */
    public static void setFilteredStackTrace(boolean filter)
    {
        Util.filteredStackTrace = filter;
    }

    /**
     * State of the stack trace filter.
     */
    public static boolean isFilteredStackTrace()
    {
        return Util.filteredStackTrace;
    }

    /**
     * Helper to get the stack trace of an Exception as a String.
     *
     * @param t Use the stack trace of this exception.
     * @return The stack trace as a String.
     */
    public static String getStackTrace(Throwable t)
    {
        if (t == null)
            return null;

        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Filter a stack trace by removing any lines matching the set.  A default
     * set will always be applied, but a custom set can also be provided.
     *
     * @param t Use the stack trace of this exception.
     * @return The filtered stack trace as a String.
     */
    public static String getFilteredStackTrace(Throwable t)
    {
        return getFilteredStackTrace(t, null);
    }

    /**
     * Filter a stack trace by removing any lines matching the set.  A default
     * set will always be applied, but a custom set can also be provided.
     *
     * @param t       Use the stack trace of this exception.
     * @param filters Set of custom filters where each filter is
     *                the beginning of a class name.
     * @return The filtered stack trace as a String.
     */
    public static String getFilteredStackTrace(Throwable t, String[] filters)
    {
        return filterStack(getStackTrace(t), filters);
    }

    /**
     * Helper for the {@link #getFilteredStackTrace(java.lang.Throwable)}
     * method.
     *
     * @param stack   A stack trace as a String.
     * @param filters Set of custom filters where each filter is
     *                the beginning of a class name.
     * @return The filtered stack trace as a String.
     */
    public static String filterStack(String stack, String[] filters)
    {
        if (!isFilteredStackTrace())
            return stack;

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        BufferedReader br = new BufferedReader(new StringReader(stack));

        String line;

        try
        {
            while ((line = br.readLine()) != null)
            {
                if (!isFiltered(line, filters))
                {
                    pw.println(line);
                }
            }
        }
        catch (Exception e)
        {
            return stack;
        }

        return sw.toString();
    }

    /**
     * Helper for the {@link #getFilteredStackTrace(java.lang.Throwable)}
     * method.
     *
     * @param line    A single line from the stack trace.
     * @param filters Set of custom filters where each filter is
     *                the beginning of a class name.
     * @return true if the line should be filtered; false otherwise.
     */
    static boolean isFiltered(String line, String[] filters)
    {
        final String[] defaultFilters = new String[]{
            "org.apache.xmlbeansbeans.test.tools.moosehead",
            "org.apache.xmlbeansbeans.test.tools.harness.Main",
            "junit.framework.TestCase",
            "junit.framework.TestResult",
            "junit.framework.TestSuite",
            "junit.framework.Assert.", // don't filter AssertException
            "java.lang.reflect.Method.invoke(",
            "org.apache.tools.ant"
        };

        for (int i = 0; i < defaultFilters.length; ++i)
        {
            if (line.indexOf(defaultFilters[i]) > 0)
            {
                return true;
            }
        }

        if (filters != null)
            for (int i = 0; i < filters.length; ++i)
            {
                if (line.indexOf(filters[i]) > 0)
                    return true;
            }

        return false;
    }

    /**
     * @deprecated
     */
    public static URL getURL(String name)
    {
        return ResourceUtil.getURL(name);
    }

    /**
     * @deprecated
     */
    public static InputStream getStream(String name)
    {
        return ResourceUtil.getStream(name);
    }

    /**
     * @deprecated
     */
    public static File getFile(String name)
    {
        return ResourceUtil.getFile(name);
    }

    /**
     * /**
     * Expand key/value pairs in a String.
     * Replaces patterns in the string of the form ${key} where
     * the keys and values are taken from the hash map.  I'm sure someone
     * could write this more efficiently if they wanted to.  Replacement is
     * recursive.  Eg, if the map contains the key "foo" with value "bar", the
     * string "My dog has ${foo}." will become "My dog has bar."
     *
     * @param str String to be expanded.
     * @param map Map of key value pairs.
     * @return The string after replacement.
     *
     * @deprecated See <code>expand()</code>.
     */
    public static String _expand(String str, HashMap map)
    {
        if (str == null)
            return null;

        if (map == null)
            return str;

        StringBuffer result = new StringBuffer();

        int pos = 0;
        int open = -1;

        //System.out.println("expand("+str+")");

        while (-1 != (open = str.indexOf("${", pos)))
        {
            //System.out.println("open: " + open + " = " + str.charAt(open));
            //System.out.println("appending: " + str.substring(pos, open) + "<");

            // replace everything we've passed so far.
            result.append(str.substring(pos, open));
            pos = open + 1;

            int close = str.indexOf("}", open);
            if (close == -1)
                continue;
            //System.out.println("close: " + close + " = " + str.charAt(close));
            //System.out.println("whole region: " + str.substring(open, close+1));
            //System.out.println("match region: " + str.substring(open+2, close));

            String key = str.substring(open + 2, close);
            if (map.containsKey(key))
            {
                String value = expand((String) map.get(key), map);
                result.append(value);

                // non-recursive implementation below:
                //result.append(map.get(key));

                pos = close + 1;
                continue;
            }

            // we've passed the start character (pos = open+1) and didn't find
            // a match, so copy the '$' to the result string
            result.append('$');

        }

        result.append(str.substring(pos));

        //System.out.println("## expanded: " + result.toString());
        return result.toString();
    }

    /**
     * Expand key/value pairs in a String.
     * Replaces patterns in the string of the form ${key} where
     * the keys and values are taken from the hash map.  I'm sure someone
     * could write this more efficiently if they wanted to.  Replacement is
     * recursive.  Eg, if the map contains the key "foo" with value "bar", the
     * string "My dog has ${foo}." will become "My dog has bar."  This version
     * uses the JDK 1.4 regex classes.
     *
     * @param str String to be expanded.
     * @param map Map of key value pairs.
     * @return The string after replacement.
     */
    public static String expand(String str, HashMap map)
    {
        final Pattern p = Pattern.compile("\\$\\{.+?\\}");

        if (str == null)
            return null;

        if (map == null)
            return str;

        int last = 0;
        StringBuffer buf = new StringBuffer();
        Matcher m = p.matcher(str);

        while (m.find())
        {

            // guarenteed to return ${key} where key is at least one character
            // in length.  match will never be null.
            String match = m.group();
            int start = m.start();
            int end = m.end();

            // remove the ${ and } from the match
            String key = match.substring("${".length(),
                                         match.length() - "}".length());

            if (map.containsKey(key))
            {
                String value = expand((String) map.get(key), map);

                buf.append(str.substring(last, start));
                buf.append(value);

                last = end;
            }
        }

        buf.append(str.substring(last, str.length()));

        return buf.toString();
    }

    /**
     * Escape a string for writing to a property file.
     * This is a simplistic version of the Properties.store() escaping.
     */
    public static String escapeProperty(String s)
    {
        int len = s.length();
        StringBuffer buf = new StringBuffer(len * 2);

        for (int i = 0; i < len; ++i)
        {
            char c = s.charAt(i);

            switch (c)
            {
                case '\t':
                    buf.append('\\').append('t');
                    break;
                case '\n':
                    buf.append('\\').append('n');
                    break;
                case '\r':
                    buf.append('\\').append('r');
                    break;
                case '\f':
                    buf.append('\\').append('f');
                    break;

                case '\\':
                case ' ':
                case '=':
                case ':':
                case '#':
                case '!':
                    buf.append('\\').append(c);
                    break;

                default:
                    if ((c < 0x0020) || (c > 0x007e))
                    {
                        buf.append('\\');
                        buf.append('u');
                        buf.append(toHex((c >> 12) & 0xF));
                        buf.append(toHex((c >> 8) & 0xF));
                        buf.append(toHex((c >> 4) & 0xF));
                        buf.append(toHex(c & 0xF));
                    } else
                    {
                        buf.append(c);
                    }
            }
        }

        return buf.toString();
    }

    /**
     * Checks if a string is entirely whitespace
     */
    public static boolean isWhiteSpace(String s)
    {
        for (int i = 0; i < s.length(); i++)
            if (!Character.isWhitespace(s.charAt(i)))
                return false;

        return true;
    }

}


