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
package org.apache.poi.hwpf.usermodel;

/**
 * Picture types supported by MS Word format
 */
public enum PictureType
{
    BMP( "image/bmp", "bmp", new byte[][] { { 'B', 'M' } } ),

    EMF( "image/x-emf", "emf", new byte[][] { { 0x01, 0x00, 0x00, 0x00 } } ),

    GIF( "image/gif", "gif", new byte[][] { { 'G', 'I', 'F' } } ),

    JPEG( "image/jpeg", "jpg", new byte[][] { { (byte) 0xFF, (byte) 0xD8 } } ),

    PICT( "image/x-pict", ".pict", new byte[0][] ),

    PNG( "image/png", "png", new byte[][] { { (byte) 0x89, 0x50, 0x4E, 0x47,
            0x0D, 0x0A, 0x1A, 0x0A } } ),

    TIFF( "image/tiff", "tiff", new byte[][] { { 0x49, 0x49, 0x2A, 0x00 },
            { 0x4D, 0x4D, 0x00, 0x2A } } ),

    UNKNOWN( "image/unknown", "", new byte[][] {} ),

    WMF( "image/x-wmf", "wmf", new byte[][] {
            { (byte) 0xD7, (byte) 0xCD, (byte) 0xC6, (byte) 0x9A, 0x00, 0x00 },
            { 0x01, 0x00, 0x09, 0x00, 0x00, 0x03 } } );

    public static PictureType findMatchingType( byte[] pictureContent )
    {
        for ( PictureType pictureType : PictureType.values() )
            for ( byte[] signature : pictureType.getSignatures() )
                if ( matchSignature( pictureContent, signature ) )
                    return pictureType;

        // TODO: DIB, PICT
        return PictureType.UNKNOWN;
    }

    private static boolean matchSignature( byte[] pictureData, byte[] signature )
    {
        if ( pictureData.length < signature.length )
            return false;

        for ( int i = 0; i < signature.length; i++ )
            if ( pictureData[i] != signature[i] )
                return false;

        return true;
    }

    private String _extension;

    private String _mime;

    private byte[][] _signatures;

    private PictureType( String mime, String extension, byte[][] signatures )
    {
        this._mime = mime;
        this._extension = extension;
        this._signatures = signatures.clone();
    }

    public String getExtension()
    {
        return _extension;
    }

    public String getMime()
    {
        return _mime;
    }

    public byte[][] getSignatures()
    {
        return _signatures;
    }

    public boolean matchSignature( byte[] pictureData )
    {
        for ( byte[] signature : getSignatures() )
            if ( matchSignature( signature, pictureData ) )
                return true;
        return false;
    }
}
