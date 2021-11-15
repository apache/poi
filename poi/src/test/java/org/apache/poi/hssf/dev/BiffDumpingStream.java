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

package org.apache.poi.hssf.dev;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.SuppressForbidden;

/**
 * Wraps a plain {@link InputStream} and allows BIFF record information to be tapped off
 */
final class BiffDumpingStream extends InputStream {

    interface IBiffRecordListener {
        void processRecord(int globalOffset, int recordCounter, int sid, int dataSize, byte[] data) throws IOException;
    }


    private final DataInputStream _is;
    private final IBiffRecordListener _listener;
    private final byte[] _data;
    private int _recordCounter;
    private int _overallStreamPos;
    private int _currentPos;
    private int _currentSize;
    private boolean _innerHasReachedEOF;
    private final byte[] oneByte = new byte[1];

    BiffDumpingStream(InputStream is, IBiffRecordListener listener) {
        _is = new DataInputStream(is);
        _listener = listener;
        _data = new byte[RecordInputStream.MAX_RECORD_DATA_SIZE + 4];
        _recordCounter = 0;
        _overallStreamPos = 0;
        _currentSize = 0;
        _currentPos = 0;
    }

    @Override
    public int read() throws IOException {
        int ret = read(oneByte, 0, 1);
        return (ret == -1) ? -1 : oneByte[0] & 0x00FF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null || off < 0 || len < 0 || b.length < off + len) {
            throw new IllegalArgumentException();
        }
        if (_currentPos >= _currentSize) {
            fillNextBuffer();
        }
        if (_currentPos >= _currentSize) {
            return -1;
        }
        final int result = Math.min(len, _currentSize - _currentPos);
        System.arraycopy(_data, _currentPos, b, off, result);
        _currentPos += result;
        _overallStreamPos += result;
        formatBufferIfAtEndOfRec();
        return result;
    }

    @Override
    @SuppressForbidden("just delegating the call")
    public int available() throws IOException {
        return _currentSize - _currentPos + _is.available();
    }

    private void fillNextBuffer() throws IOException {
        if (_innerHasReachedEOF) {
            return;
        }
        int b0 = _is.read();
        if (b0 == -1) {
            _innerHasReachedEOF = true;
            return;
        }
        _data[0] = (byte) b0;
        _is.readFully(_data, 1, 3);
        int len = LittleEndian.getShort(_data, 2);
        _is.readFully(_data, 4, len);
        _currentPos = 0;
        _currentSize = len + 4;
        _recordCounter++;
    }

    private void formatBufferIfAtEndOfRec() throws IOException {
        if (_currentPos != _currentSize) {
            return;
        }
        int dataSize = _currentSize - 4;
        int sid = LittleEndian.getShort(_data, 0);
        int globalOffset = _overallStreamPos - _currentSize;
        _listener.processRecord(globalOffset, _recordCounter, sid, dataSize, _data);
    }

    @Override
    public void close() throws IOException {
        _is.close();
    }
}
