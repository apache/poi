/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.hwpf.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hwpf.model.types.LFOAbstractType;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

import static org.apache.logging.log4j.util.Unbox.box;

/**
 * The PlfLfo structure contains the list format override data for the document.
 * <p>
 * Documentation quoted from Page 424 of 621. [MS-DOC] -- v20110315 Word (.doc)
 * Binary File Format
 */
public class PlfLfo {
    private static final Logger LOGGER = LogManager.getLogger(PlfLfo.class);

    private static final int MAX_NUMBER_OF_LFO = 100_000;

    /**
     * An unsigned integer that specifies the count of elements in both the
     * rgLfo and rgLfoData arrays.
     */
    private int _lfoMac;

    private LFO[] _rgLfo;

    private LFOData[] _rgLfoData;


    PlfLfo( byte[] tableStream, int fcPlfLfo, int lcbPlfLfo )
    {
        /*
         * The PlfLfo structure contains the list format override data for the
         * document. -- Page 424 of 621. [MS-DOC] -- v20110315 Word (.doc)
         * Binary File Format
         */
        int offset = fcPlfLfo;

        /*
         * lfoMac (4 bytes): An unsigned integer that specifies the count of
         * elements in both the rgLfo and rgLfoData arrays. -- Page 424 of 621.
         * [MS-DOC] -- v20110315 Word (.doc) Binary File Format
         */
        long lfoMacLong = LittleEndian.getUInt( tableStream, offset );
        offset += LittleEndianConsts.INT_SIZE;

        if ( lfoMacLong > Integer.MAX_VALUE )
        {
            throw new UnsupportedOperationException(
                    "Apache POI doesn't support rgLfo/rgLfoData size large than "
                            + Integer.MAX_VALUE + " elements" );
        }

        IOUtils.safelyAllocateCheck(lfoMacLong, MAX_NUMBER_OF_LFO);

        this._lfoMac = (int) lfoMacLong;
        _rgLfo = new LFO[_lfoMac];
        _rgLfoData = new LFOData[_lfoMac];

        /*
         * An array of LFO structures. The number of elements in this array is
         * specified by lfoMac. -- Page 424 of 621. [MS-DOC] -- v20110315 Word
         * (.doc) Binary File Format
         */
        for ( int x = 0; x < _lfoMac; x++ )
        {
            LFO lfo = new LFO( tableStream, offset );
            offset += LFOAbstractType.getSize();
            _rgLfo[x] = lfo;
        }

        /*
         * An array of LFOData that is parallel to rgLfo. The number of elements
         * that are contained in this array is specified by lfoMac. -- Page 424
         * of 621. [MS-DOC] -- v20110315 Word (.doc) Binary File Format
         */
        for ( int x = 0; x < _lfoMac; x++ )
        {
            LFOData lfoData = new LFOData( tableStream, offset,
                    _rgLfo[x].getClfolvl() );
            offset += lfoData.getSizeInBytes();
            _rgLfoData[x] = lfoData;
        }

        if ( ( offset - fcPlfLfo ) != lcbPlfLfo )
        {
            LOGGER.atWarn().log("Actual size of PlfLfo is {} bytes, but expected {}", box(offset - fcPlfLfo),box(lcbPlfLfo));
        }
    }

    void add( LFO lfo, LFOData lfoData )
    {
        // _lfoMac is the size of the array
        _rgLfo = Arrays.copyOf(_rgLfo, _lfoMac + 1);
        _rgLfo[_lfoMac] = lfo;

        _rgLfoData = Arrays.copyOf(_rgLfoData, _lfoMac + 1);
        _rgLfoData[_lfoMac] = lfoData;

        _lfoMac = _lfoMac + 1;
    }

    @Override
    public boolean equals( Object obj ) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlfLfo other = (PlfLfo) obj;
        return _lfoMac == other._lfoMac &&
                Arrays.equals(_rgLfo, other._rgLfo) &&
                Arrays.equals(_rgLfoData, other._rgLfoData);
    }

    /**
     * An unsigned integer that specifies the count of elements in both the
     * rgLfo and rgLfoData arrays.
     */
    public int getLfoMac()
    {
        return _lfoMac;
    }

    public int getIlfoByLsid( int lsid )
    {
        for ( int i = 0; i < _lfoMac; i++ )
        {
            if ( _rgLfo[i].getLsid() == lsid )
            {
                return i + 1;
            }
        }
        throw new NoSuchElementException( "LFO with lsid " + lsid
                + " not found" );
    }

    /**
     * @param ilfo 1-based index
     * @return The {@link LFO} stored at the given index
     */
    public LFO getLfo( int ilfo ) throws NoSuchElementException
    {
        if ( ilfo <= 0 || ilfo > _lfoMac )
        {
            throw new NoSuchElementException( "LFO with ilfo " + ilfo
                    + " not found. lfoMac is " + _lfoMac );
        }
        return _rgLfo[ilfo - 1];
    }

    /**
     * @param ilfo 1-based index
     * @return The {@link LFOData} stored at the given index
     */
    public LFOData getLfoData( int ilfo ) throws NoSuchElementException
    {
        if ( ilfo <= 0 || ilfo > _lfoMac )
        {
            throw new NoSuchElementException( "LFOData with ilfo " + ilfo
                    + " not found. lfoMac is " + _lfoMac );
        }
        return _rgLfoData[ilfo - 1];
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{_lfoMac, _rgLfo, _rgLfoData});
    }

    void writeTo( FileInformationBlock fib, ByteArrayOutputStream outputStream )
            throws IOException
    {
        final int offset = outputStream.size();
        fib.setFcPlfLfo( offset );

        LittleEndian.putUInt( _lfoMac, outputStream );

        byte[] bs = new byte[LFOAbstractType.getSize() * _lfoMac];
        for ( int i = 0; i < _lfoMac; i++ )
        {
            _rgLfo[i].serialize( bs, i * LFOAbstractType.getSize() );
        }
        outputStream.write( bs, 0, LFOAbstractType.getSize() * _lfoMac );

        for ( int i = 0; i < _lfoMac; i++ )
        {
            _rgLfoData[i].writeTo( outputStream );
        }
        fib.setLcbPlfLfo( outputStream.size() - offset );
    }
}
