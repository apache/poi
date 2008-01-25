
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
        

package org.apache.poi.poifs.storage;

import java.io.*;
import java.util.Random;

import junit.framework.*;

/**
 * Class to test RawDataBlock functionality
 *
 * @author Marc Johnson
 */

public class TestRawDataBlock
    extends TestCase
{

    /**
     * Constructor TestRawDataBlock
     *
     * @param name
     */

    public TestRawDataBlock(String name)
    {
        super(name);
    }

    /**
     * Test creating a normal RawDataBlock
     *
     * @exception IOException
     */

    public void testNormalConstructor()
        throws IOException
    {
        byte[] data = new byte[ 512 ];

        for (int j = 0; j < 512; j++)
        {
            data[ j ] = ( byte ) j;
        }
        RawDataBlock block = new RawDataBlock(new ByteArrayInputStream(data));

        assertTrue("Should not be at EOF", !block.eof());
        byte[] out_data = block.getData();

        assertEquals("Should be same length", data.length, out_data.length);
        for (int j = 0; j < 512; j++)
        {
            assertEquals("Should be same value at offset " + j, data[ j ],
                         out_data[ j ]);
        }
    }

    /**
     * Test creating an empty RawDataBlock
     *
     * @exception IOException
     */

    public void testEmptyConstructor()
        throws IOException
    {
        byte[]       data  = new byte[ 0 ];
        RawDataBlock block = new RawDataBlock(new ByteArrayInputStream(data));

        assertTrue("Should be at EOF", block.eof());
        try
        {
            block.getData();
        }
        catch (IOException ignored)
        {

            // as expected
        }
    }

    /**
     * Test creating a short RawDataBlock
     */

    public void testShortConstructor()
    {
        for (int k = 1; k < 512; k++)
        {
            byte[] data = new byte[ k ];

            for (int j = 0; j < k; j++)
            {
                data[ j ] = ( byte ) j;
            }
            RawDataBlock block = null;

            try
            {
                block = new RawDataBlock(new ByteArrayInputStream(data));
                fail("Should have thrown IOException creating short block");
            }
            catch (IOException ignored)
            {

                // as expected
            }
        }
    }
    
    /**
     * Tests that when using a slow input stream, which
     *  won't return a full block at a time, we don't
     *  incorrectly think that there's not enough data
     */
    public void testSlowInputStream() throws Exception {
        for (int k = 1; k < 512; k++) {
            byte[] data = new byte[ 512 ];
            for (int j = 0; j < data.length; j++) {
                data[j] = (byte) j;
            }
            
            // Shouldn't complain, as there is enough data,
            //  even if it dribbles through
            RawDataBlock block = 
            	new RawDataBlock(new SlowInputStream(data, k));
            assertFalse(block.eof());
        }
        
        // But if there wasn't enough data available, will
        //  complain
        for (int k = 1; k < 512; k++) {
            byte[] data = new byte[ 511 ];
            for (int j = 0; j < data.length; j++) {
                data[j] = (byte) j;
            }
            
            // Shouldn't complain, as there is enough data
            try {
	            RawDataBlock block = 
	            	new RawDataBlock(new SlowInputStream(data, k));
	            fail();
            } catch(IOException e) {
            	// as expected
            }
        }
    }
    
    /**
     * An input stream which will return a maximum of
     *  a given number of bytes to read, and often claims
     *  not to have any data
     */
    public static class SlowInputStream extends InputStream {
    	private Random rnd = new Random();
    	private byte[] data;
    	private int chunkSize;
    	private int pos = 0;
    	
    	public SlowInputStream(byte[] data, int chunkSize) {
    		this.chunkSize = chunkSize;
    		this.data = data;
    	}
    	
    	/**
    	 * 75% of the time, claim there's no data available
    	 */
    	private boolean claimNoData() {
    		if(rnd.nextFloat() < 0.25f) {
    			return false;
    		}
    		return true;
    	}
    	
		public int read() throws IOException {
			if(pos >= data.length) {
				return -1;
			}
			int ret = data[pos];
			pos++;
			
			if(ret < 0) ret += 256;
			return ret;
		}

		/**
		 * Reads the requested number of bytes, or the chunk
		 *  size, whichever is lower.
		 * Quite often will simply claim to have no data
		 */
		public int read(byte[] b, int off, int len) throws IOException {
			// Keep the length within the chunk size
			if(len > chunkSize) {
				len = chunkSize;
			}
			// Don't read off the end of the data
			if(pos + len > data.length) {
				len = data.length - pos;
				
				// Spot when we're out of data
				if(len == 0) {
					return -1;
				}
			}
			
			// 75% of the time, claim there's no data
			if(claimNoData()) {
				return 0;
			}
			
			// Copy, and return what we read
			System.arraycopy(data, pos, b, off, len);
			pos += len;
			return len;
		}

		public int read(byte[] b) throws IOException {
			return read(b, 0, b.length);
		}
		
    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out
            .println("Testing org.apache.poi.poifs.storage.RawDataBlock");
        junit.textui.TestRunner.run(TestRawDataBlock.class);
    }
}
