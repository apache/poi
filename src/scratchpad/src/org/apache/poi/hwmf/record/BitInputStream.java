package org.apache.poi.hwmf.record;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Taken from https://code.google.com/p/javasupport/source/browse/branches/java-examples/src/main/java/deng/huffman/dukehuff/test/BitInputStream.java?r=587
 * 
 * reads bits-at-a-time where the number of bits is between 1 and 32.
 * Updated for version 2.0 to extend java.io.InputStream
 * <P>
 * @author Owen Astrachan
 * @version 1.0, July 2000
 * @version 2.0, October 2004
 */

public class BitInputStream extends InputStream
{
    private InputStream     myInput;
    private int             myBitCount;
    private int             myBuffer;
    private File            myFile;
    
    private static final int bmask[] = {
        0x00, 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, 0xff,
        0x1ff,0x3ff,0x7ff,0xfff,0x1fff,0x3fff,0x7fff,0xffff,
        0x1ffff,0x3ffff,0x7ffff,0xfffff,0x1fffff,0x3fffff,
        0x7fffff,0xffffff,0x1ffffff,0x3ffffff,0x7ffffff,
        0xfffffff,0x1fffffff,0x3fffffff,0x7fffffff,0xffffffff
    };

    private static final int BITS_PER_BYTE = 8;
    /**
     * Construct a bit-at-a-time input stream from a file
     */
    public BitInputStream(String filename)
    {
        this(new File(filename));
    }
    
    /**
     * Construct a bit-at-a-time input stream from <code>file</code>
     * @param file is the File that is the source of the input
     */
    public BitInputStream(File file)
    {
        myFile = file;  
        try {
            reset();
        } catch (IOException e) {
            // eat the exception
        }
        
    }
    
    public BitInputStream(InputStream in){
        myInput = in;
        myFile = null;
    }
    
    /**
     * This is a reset-able stream, so return true.
     * @return true
     */
    public boolean markSupported(){
        return myFile != null;
    }

    /**
     * reset stream to beginning. The implementation creates a new
     * stream.
     * @throws IOException
     */
    
    public void reset() throws IOException
    {
        if (! markSupported()){
            throw new IOException("not resettable");
        }
        try{
            close();
            myInput = new BufferedInputStream(new FileInputStream(myFile));
        }
        catch (FileNotFoundException fnf){
            System.err.println("error opening " + myFile.getName() + " " + fnf);
        }
        myBuffer = myBitCount = 0;
    } 

    /**
     * closes the input stream
     */
    
    public void close()
    {
        try{
            if (myInput != null) {
                myInput.close();
            }
        }
        catch (java.io.IOException ioe){
            System.err.println("error closing bit stream " + ioe);
        }
    }

    /**
     * returns the number of bits requested as rightmost bits in
     * returned value, returns -1 if not enough bits available to
     * satisfy the request
     *
     * @param howManyBits is the number of bits to read and return
     * @return the value read, only rightmost <code>howManyBits</code>
     * are valid, returns -1 if not enough bits left
     */

    public int read(int howManyBits) throws IOException
    {
        int retval = 0;
        if (myInput == null){
            return -1;
        }
        
        while (howManyBits > myBitCount){
            retval |= ( myBuffer << (howManyBits - myBitCount) );
            howManyBits -= myBitCount;
            try{
                if ( (myBuffer = myInput.read()) == -1) {
                    return -1;
                }
            }
            catch (IOException ioe) {
                throw new IOException("bitreading trouble "+ioe);
            }
            myBitCount = BITS_PER_BYTE;
        }

        if (howManyBits > 0){
            retval |= myBuffer >> (myBitCount - howManyBits);
            myBuffer &= bmask[myBitCount - howManyBits];
            myBitCount -= howManyBits;
        }
        return retval;
    }

    /**
     * Required by classes extending InputStream, returns
     * the next byte from this stream as an int value.
     * @return the next byte from this stream
     */
    public int read() throws IOException {
        return read(8);
    }
}


