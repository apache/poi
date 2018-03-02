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

import java.io.BufferedReader;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/**
 * A utility class for getting data from jar files
 */
public class JarUtil {

    final static String EOL = System.getProperty("line.separator");

    /**
     * returns an File Object within the given jarFile as a String. jarFile must exist in classpath
     * pre: jar containing resource is in the classpath
     *
     * @param pathToResource
     * @return File
     */
    public static File getResourceFromJarasFile(String pathToResource)
            throws IOException {

        String[] tokens = pathToResource.split("/");
        String fileName = tokens[tokens.length - 1];
        tokens = fileName.split("\\.");
        String extension= (tokens.length < 2) ? null:"." + tokens[1];
        String prefix= ( tokens[0].length()<3 ) ? tokens[0]+"abc":tokens[0];
        File temp = File.createTempFile(prefix,extension );
        temp.deleteOnExit();
        PrintWriter pr = null;
        try {
            pr = new PrintWriter(new FileWriter(temp));
            String content = getResourceFromJar(pathToResource);
            pr.write(content);
        }
        finally {
            if (pr != null) pr.close();
        }
        return temp;
    }


    /**
     * returns an item within the given jarFile as a String. jarFile must exist in classpath
     *
     * @param pathToResource
     * @return String
     */

    public static String getResourceFromJar(String pathToResource)
            throws IOException {

        URL url = ClassLoader.getSystemClassLoader().getResource(
                pathToResource);
        if (url == null) {
            throw new FileNotFoundException(
                    "Resource: " + pathToResource + " was not found ");
        }
        JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
        JarFile jar = jarConnection.getJarFile();
        if (jar.getJarEntry(pathToResource) == null) {
            throw new FileNotFoundException(
                    "Resource: " + pathToResource + " was not found ");
        }

        JarEntry item = jar.getJarEntry(pathToResource);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(jar.getInputStream(item)));

        StringBuffer stb = new StringBuffer();
        String buffer;

        while (!((buffer = in.readLine()) == null)) {
            stb.append(buffer + EOL);
        }
        return stb.toString();
    }

    /**
     * returns an item within the given jarFile as a Stream
     *
     * @param pathToResource
     * @return String
     */

    public static InputStream getResourceFromJarasStream(String pathToResource)
            throws IOException {
         InputStream resource=ClassLoader.getSystemClassLoader().getResourceAsStream(
                pathToResource);
        if ( resource==null ){
            throw new IOException(" Resource "+pathToResource+" was not found. " +
                    "Make sure Jar w/ resource is on classpath");
        }
        return resource;
    }

    /**
     * Returns the classpath entry of a given item on the classpath. The item should be a jarFile reference
     *
     * @param jarFile
     * @return String
     * @throws FileNotFoundException
     */

    public static String getFilePath(String jarFile)
            throws FileNotFoundException {
        String sClassPath = System.getProperty("java.class.path");
        int jarIndex = sClassPath.indexOf(jarFile);
        if (jarIndex <= 0) {
            throw new FileNotFoundException(
                    "File: " + jarFile + " was not found on the classpath");
        }

        String P = File.pathSeparator;
        String[] pathList = sClassPath.split(P);
        for (int i = 0; i < pathList.length; i++) {
            if (pathList[i].toLowerCase().endsWith(jarFile.toLowerCase())) {
                return pathList[i];
            }
        }
        throw new FileNotFoundException(
                "File: " + jarFile + " was not found when iterating classpath");
    }
}

