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
package org.apache.xmlbeans.test.performance.utils;

import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.StringBuffer;
import java.lang.Math;

public class PerfUtil
{

  // TODO: add more flavors
  public char[] createXmlData(String flavor, int size){
    StringBuffer buff = new StringBuffer(size);
    final String rootStart = "<r>";
    final String rootEnd = "</r>";
    final String childStart = "<c>";
    final String childEnd = "</c>";
    final String attribute = "att=\"attval\"";
    final String childAttribStart = "<c "+attribute+">";
    final String textChunk = "1234567890qwertyuiopasdfghjklzxcvbnm";       

    // use the given size parameter to normalize on number of chars
    // as close as possible
    if(flavor.equalsIgnoreCase("deep-elements")){
      int iChildren = (size - 7)/7;
      buff.append(rootStart);
      for(int i=0; i<iChildren; i++){
        buff.append(childStart);
      }
      for(int i=0; i<iChildren; i++){
        buff.append(childEnd);
      }
      buff.append(rootEnd);
    }
    else if(flavor.equalsIgnoreCase("deep-attributes")){
      int iChildren = (size - 7)/20;
      buff.append(rootStart);
      for(int i=0; i<iChildren; i++){
        buff.append(childAttribStart);
      }
      for(int i=0; i<iChildren; i++){
        buff.append(childEnd);
      }
      buff.append(rootEnd);
    }
    else if(flavor.equalsIgnoreCase("wide-elements")){
      int iChildren = (size - 7)/7;
      buff.append(rootStart);
      for(int i=0; i<iChildren; i++){
        buff.append(childStart+childEnd);
      }
      buff.append(rootEnd);
    }
    else if(flavor.equalsIgnoreCase("wide-text")){
      int iChildren = (size - 7)/43;
      buff.append(rootStart);
      for(int i=0; i<iChildren; i++){
        buff.append(childStart+textChunk+childEnd);
      }
      buff.append(rootEnd);
    }
    else if(flavor.equalsIgnoreCase("wide-attributes")){
      int iChildren = (size - 7)/20;
      buff.append(rootStart);
      for(int i=0; i<iChildren; i++){
        buff.append(childAttribStart+childEnd);
      }
      buff.append(rootEnd);
    }
    //System.out.println("num chars:"+buff.length());
    return buff.toString().toCharArray();
  }

  public byte[] createXmlDataBytes(String flavor, int size)
  {
    char[] chars = createXmlData(flavor,size);
    System.gc();
    StringBuffer buff = new StringBuffer(chars.length);
    buff.append(chars);
    chars = null;
    System.gc();
    return buff.toString().getBytes();
  }


  public boolean meetsTheBar(float actual, float bar){
    final double fAllowedDeviation = 0.05;
    float diff = Math.abs((actual-bar));
    if( (diff/bar > fAllowedDeviation) ) return false;
    else return true;
  }

  public boolean meetsTheBar(long actual, float bar){
    Long lActual = new Long(actual);
    return meetsTheBar(lActual.floatValue(), bar);
  }

  public char[] fileToChars(String filename) throws IOException,FileNotFoundException
  {
    BufferedInputStream bis = 
      new BufferedInputStream(new FileInputStream(filename));
    StringBuffer buff = new StringBuffer();
    int c;
    while( (c=bis.read()) != -1){
      buff.append((char)c);
    }
    
    return buff.toString().toCharArray();
  }

  public byte[] fileToBytes(String filename) throws IOException,FileNotFoundException
  {
    BufferedInputStream bis = 
      new BufferedInputStream(new FileInputStream(filename));
    StringBuffer buff = new StringBuffer();
    int c;
    while( (c=bis.read()) != -1){
      buff.append((char)c);
    }
    
    return buff.toString().getBytes();
  }

  public String createString(int size)
  {
    StringBuffer buff = new StringBuffer();
    for(int i=0; i<size; i++)
    {
      buff.append("z");
    }
    return buff.toString();
  }

}
