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


package org.apache.poi.hdf.extractor;

import java.io.*;
import java.util.*;

/**
 * Comment me
 *
 * @author Ryan Ackley 
 */

public class NewOleFile extends RandomAccessFile
{
    private byte[] LAOLA_ID_ARRAY = new byte[]{(byte)0xd0, (byte)0xcf, (byte)0x11,
                                               (byte)0xe0, (byte)0xa1, (byte)0xb1,
                                               (byte)0x1a, (byte)0xe1};
    private int _num_bbd_blocks;
    private int _root_startblock;
    private int _sbd_startblock;
    private long _size;
    private int[] _bbd_list;
    protected int[] _big_block_depot;
    protected int[] _small_block_depot;
    Hashtable _propertySetsHT = new Hashtable();
    Vector _propertySetsV = new Vector();

    public NewOleFile(String fileName, String mode) throws FileNotFoundException
    {
        super(fileName, mode);
        try
        {
            init();
        }
        catch(Throwable e)
        {
            e.printStackTrace();
        }
    }

    private void init() throws IOException
    {

        for(int x = 0; x < LAOLA_ID_ARRAY.length; x++)
        {
            if(LAOLA_ID_ARRAY[x] != readByte())
            {
                throw new IOException("Not an OLE file");
            }
        }
        _size = length();
        _num_bbd_blocks = readInt(0x2c);
        _root_startblock = readInt(0x30);
        _sbd_startblock = readInt(0x3c);
        _bbd_list = new int[_num_bbd_blocks];
        //populate bbd_list. If _num_bbd_blocks > 109 I have to do it
        //differently
        if(_num_bbd_blocks <= 109)
        {
            seek(0x4c);
            for(int x = 0; x < _num_bbd_blocks; x++)
            {
                _bbd_list[x] = readIntLE();
            }
        }
        else
        {
            populateBbdList();
        }
        //populate the big block depot
        _big_block_depot = new int[_num_bbd_blocks * 128];
        int counter = 0;
        for(int x = 0; x < _num_bbd_blocks; x++)
        {
            byte[] bigBlock = new byte[512];
            int offset = (_bbd_list[x] + 1) * 512;
            seek(offset);
            for(int y = 0; y < 128; y++)
            {
                _big_block_depot[counter++] = readIntLE();
            }
        }
        _small_block_depot = createSmallBlockDepot();
        int[] rootChain = readChain(_big_block_depot, _root_startblock);
        initializePropertySets(rootChain);

    }
    public static void main(String args[])
    {
      try
      {
          NewOleFile file = new NewOleFile(args[0], "r");
      }
      catch(Exception e)
      {
      }
    }
    protected int[] readChain(int[] blockChain, int startBlock) throws IOException
    {

        int[] tempChain = new int[blockChain.length];
        tempChain[0] = startBlock;
        int x = 1;
        for(;;x++)
        {
            int nextVal = blockChain[tempChain[x-1]];
            if(nextVal != -2)
            {
                tempChain[x] = nextVal;
            }
            else
            {
                break;
            }
        }
        int[] newChain = new int[x];
        System.arraycopy(tempChain, 0, newChain, 0, x);

        return newChain;
    }
    private void initializePropertySets(int[] rootChain) throws IOException
    {
        for(int x = 0; x < rootChain.length; x++)
        {
            int offset = (rootChain[x] + 1) * 512;
            seek(offset);
            for(int y = 0; y < 4; y++)
            {
                //read the block the makes up the property set
                byte[] propArray = new byte[128];
                read(propArray);

                //parse the byte array for properties
                int nameSize = Utils.convertBytesToShort(propArray[0x41], propArray[0x40])/2 - 1;
                if(nameSize > 0)
                {
                    StringBuffer nameBuffer = new StringBuffer(nameSize);
                    for(int z = 0; z < nameSize; z++)
                    {
                        nameBuffer.append((char)propArray[z*2]);
                    }
                    int type = propArray[0x42];
                    int previous_pps = Utils.convertBytesToInt(propArray[0x47], propArray[0x46], propArray[0x45], propArray[0x44]);
                    int next_pps = Utils.convertBytesToInt(propArray[0x4b], propArray[0x4a], propArray[0x49], propArray[0x48]);
                    int pps_dir = Utils.convertBytesToInt(propArray[0x4f], propArray[0x4e], propArray[0x4d], propArray[0x4c]);
                    int pps_sb = Utils.convertBytesToInt(propArray[0x77], propArray[0x76], propArray[0x75], propArray[0x74]);
                    int pps_size = Utils.convertBytesToInt(propArray[0x7b], propArray[0x7a], propArray[0x79], propArray[0x78]);

                    PropertySet propSet = new PropertySet(nameBuffer.toString(),
                                                          type, previous_pps, next_pps,
                                                          pps_dir, pps_sb, pps_size,
                                                          (x*4) + y);
                    _propertySetsHT.put(nameBuffer.toString(), propSet);
                    _propertySetsV.add(propSet);
                }
            }
        }

    }
    private int[] createSmallBlockDepot() throws IOException
    {

        int[] sbd_list = readChain(_big_block_depot, _sbd_startblock);
        int[] small_block_depot = new int[sbd_list.length * 128];

        for(int x = 0; x < sbd_list.length && sbd_list[x] != -2; x++)
        {
            int offset = ((sbd_list[x] + 1) * 512);
            seek(offset);
            for(int y = 0; y < 128; y++)
            {
                small_block_depot[y] = readIntLE();
            }
        }
        return small_block_depot;
    }

    private void populateBbdList() throws IOException
    {
      seek(0x4c);
      for(int x = 0; x < 109; x++)
      {
          _bbd_list[x] = readIntLE();
      }
      int pos = 109;
      int remainder = _num_bbd_blocks - 109;
      seek(0x48);
      int numLists = readIntLE();
      seek(0x44);
      int firstList = readIntLE();

      firstList = (firstList + 1) * 512;

      for(int y = 0; y < numLists; y++)
      {
        int size = Math.min(127, remainder);
        for(int z = 0; z < size; z++)
        {
          seek(firstList + (z * 4));
          _bbd_list[pos++] = readIntLE();
        }
        if(size == 127)
        {
          seek(firstList + (127 * 4));
          firstList = readIntLE();
          firstList = (firstList + 1) * 512;
          remainder -= 127;
        }
      }

    }
    private int readInt(long offset) throws IOException
    {
        seek(offset);
        return readIntLE();
    }
    private int readIntLE() throws IOException
    {
        byte[] intBytes = new byte[4];
        read(intBytes);
        return Utils.convertBytesToInt(intBytes[3], intBytes[2], intBytes[1], intBytes[0]);
    }





}
