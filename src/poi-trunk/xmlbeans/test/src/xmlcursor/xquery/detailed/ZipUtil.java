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
package xmlcursor.xquery.detailed;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.HashMap;

/**
 *
 */
public class ZipUtil
{
     static String getStringFromZip(String pathToZip,String zipName,String file)
         throws IOException
    {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(getStreamFromZip(pathToZip,zipName,file)));

        StringBuffer stb = new StringBuffer();
        String buffer;

        while (!((buffer = in.readLine()) == null)) {
            stb.append(buffer + EOL);
        }
        return stb.toString();
    }

    static InputStream getStreamFromZip(String zipFileName,String fileName) throws IOException
       {
           Object file;
           if ( (file = ZipFileMap.get(fileName)) == null ){
               file = new ZipFile(zipFileName);
               ZipFileMap.put(fileName,file);
           }
           ZipEntry entry = ((ZipFile)file).getEntry(fileName);
           return ((ZipFile)file).getInputStream(entry);
       }

     static InputStream getStreamFromZip(String pathToZip,String zipName,String file)
         throws IOException {
         return getStreamFromZip(pathToZip+SLASH+zipName,file);
     }

    private static HashMap ZipFileMap=new HashMap();
     static final String SLASH = System.getProperty("file.separator");
     static final String EOL=System.getProperty("line.separator");
}
