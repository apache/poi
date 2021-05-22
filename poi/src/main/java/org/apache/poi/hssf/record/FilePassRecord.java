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

package org.apache.poi.hssf.record;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.binaryrc4.BinaryRC4EncryptionHeader;
import org.apache.poi.poifs.crypt.binaryrc4.BinaryRC4EncryptionVerifier;
import org.apache.poi.poifs.crypt.cryptoapi.CryptoAPIEncryptionHeader;
import org.apache.poi.poifs.crypt.cryptoapi.CryptoAPIEncryptionVerifier;
import org.apache.poi.poifs.crypt.xor.XOREncryptionHeader;
import org.apache.poi.poifs.crypt.xor.XOREncryptionVerifier;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.LittleEndianOutputStream;

/**
 * File Pass Record (0x002F) <p>
 *
 * Indicates that the record after this record are encrypted.
 */
public final class FilePassRecord extends StandardRecord {
    public static final short sid = 0x002F;
    private static final int ENCRYPTION_XOR = 0;
    private static final int ENCRYPTION_OTHER = 1;

    private final int encryptionType;
    private final EncryptionInfo encryptionInfo;

    private FilePassRecord(FilePassRecord other) {
        super(other);
        encryptionType = other.encryptionType;
        encryptionInfo = other.encryptionInfo.copy();
    }

    public FilePassRecord(EncryptionMode encryptionMode) {
        encryptionType = (encryptionMode == EncryptionMode.xor) ? ENCRYPTION_XOR : ENCRYPTION_OTHER;
        encryptionInfo = new EncryptionInfo(encryptionMode);
    }

    public FilePassRecord(RecordInputStream in) {
        encryptionType = in.readUShort();

        EncryptionMode preferredMode;
        switch (encryptionType) {
            case ENCRYPTION_XOR:
                preferredMode = EncryptionMode.xor;
                break;
            case ENCRYPTION_OTHER:
                preferredMode = EncryptionMode.cryptoAPI;
                break;
            default:
                throw new EncryptedDocumentException("invalid encryption type");
        }

        try {
            encryptionInfo = new EncryptionInfo(in, preferredMode);
        } catch (IOException e) {
            throw new EncryptedDocumentException(e);
        }
    }

    @SuppressWarnings("resource")
    @Override
    public void serialize(LittleEndianOutput out) {
        out.writeShort(encryptionType);

        byte[] data = new byte[1024];
        try (LittleEndianByteArrayOutputStream bos =
                new LittleEndianByteArrayOutputStream(data, 0)) { // NOSONAR

            switch (encryptionInfo.getEncryptionMode()) {
                case xor:
                    ((XOREncryptionHeader)encryptionInfo.getHeader()).write(bos);
                    ((XOREncryptionVerifier)encryptionInfo.getVerifier()).write(bos);
                    break;
                case binaryRC4:
                    out.writeShort(encryptionInfo.getVersionMajor());
                    out.writeShort(encryptionInfo.getVersionMinor());
                    ((BinaryRC4EncryptionHeader)encryptionInfo.getHeader()).write(bos);
                    ((BinaryRC4EncryptionVerifier)encryptionInfo.getVerifier()).write(bos);
                    break;
                case cryptoAPI:
                    out.writeShort(encryptionInfo.getVersionMajor());
                    out.writeShort(encryptionInfo.getVersionMinor());
                    out.writeInt(encryptionInfo.getEncryptionFlags());
                    ((CryptoAPIEncryptionHeader)encryptionInfo.getHeader()).write(bos);
                    ((CryptoAPIEncryptionVerifier)encryptionInfo.getVerifier()).write(bos);
                    break;
                default:
                    throw new EncryptedDocumentException("not supported");
            }

            out.write(data, 0, bos.getWriteIndex());
        } catch (IOException ioe) {
            // should never happen in practice
            throw new IllegalStateException(ioe);
        }
    }

    @Override
    protected int getDataSize() {
        UnsynchronizedByteArrayOutputStream bos = new UnsynchronizedByteArrayOutputStream();
        LittleEndianOutputStream leos = new LittleEndianOutputStream(bos);
        serialize(leos);
        return bos.size();
    }

    public EncryptionInfo getEncryptionInfo() {
        return encryptionInfo;
    }

    @Override
    public short getSid() {
        return sid;
    }

    @Override
    public FilePassRecord copy() {
        return new FilePassRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.FILE_PASS;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "type", () -> encryptionType,
            "encryptionInfo", this::getEncryptionInfo
        );
    }
}
