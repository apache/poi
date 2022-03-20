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

package org.apache.poi.hslf.usermodel;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hslf.exceptions.CorruptPowerPointFileException;
import org.apache.poi.hslf.exceptions.EncryptedPowerPointFileException;
import org.apache.poi.hslf.record.DocumentEncryptionAtom;
import org.apache.poi.hslf.record.PersistPtrHolder;
import org.apache.poi.hslf.record.PositionDependentRecord;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.UserEditAtom;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.poifs.crypt.ChunkedCipherInputStream;
import org.apache.poi.poifs.crypt.ChunkedCipherOutputStream;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.util.BitField;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.apache.poi.util.RecordFormatException;

/**
 * This class provides helper functions for encrypted PowerPoint documents.
 */
@Internal
public class HSLFSlideShowEncrypted implements Closeable {
    DocumentEncryptionAtom dea;
    EncryptionInfo _encryptionInfo;
//    Cipher cipher = null;
    ChunkedCipherOutputStream cyos;

    private static final BitField fieldRecInst = new BitField(0xFFF0);

    private static final int[] BLIB_STORE_ENTRY_PARTS = {
            1,     // btWin32
            1,     // btMacOS
            16,    // rgbUid
            2,     // tag
            4,     // size
            4,     // cRef
            4,     // foDelay
            1,     // unused1
            1,     // cbName (@ index 33)
            1,     // unused2
            1,     // unused3
    };

    protected HSLFSlideShowEncrypted(DocumentEncryptionAtom dea) {
        this.dea = dea;
    }

    protected HSLFSlideShowEncrypted(byte[] docstream, NavigableMap<Integer,Record> recordMap) {
        // check for DocumentEncryptionAtom, which would be at the last offset
        // need to ignore already set UserEdit and PersistAtoms
        UserEditAtom userEditAtomWithEncryption = null;
        for (Map.Entry<Integer, Record> me : recordMap.descendingMap().entrySet()) {
            org.apache.poi.hslf.record.Record r = me.getValue();
            if (!(r instanceof UserEditAtom)) {
                continue;
            }
            UserEditAtom uea = (UserEditAtom)r;
            if (uea.getEncryptSessionPersistIdRef() != -1) {
                userEditAtomWithEncryption = uea;
                break;
            }
        }

        if (userEditAtomWithEncryption == null) {
            dea = null;
            return;
        }

        org.apache.poi.hslf.record.Record r = recordMap.get(userEditAtomWithEncryption.getPersistPointersOffset());
        if (!(r instanceof PersistPtrHolder)) {
            throw new RecordFormatException("Encountered an unexpected record-type: " + r);
        }
        PersistPtrHolder ptr = (PersistPtrHolder)r;

        Integer encOffset = ptr.getSlideLocationsLookup().get(userEditAtomWithEncryption.getEncryptSessionPersistIdRef());
        if (encOffset == null) {
            // encryption info doesn't exist anymore
            // SoftMaker Freeoffice produces such invalid files - check for "SMNativeObjData" ole stream
            dea = null;
            return;
        }

        r = recordMap.get(encOffset);
        if (r == null) {
            r = Record.buildRecordAtOffset(docstream, encOffset);
            recordMap.put(encOffset, r);
        }

        this.dea = (DocumentEncryptionAtom)r;

        String pass = Biff8EncryptionKey.getCurrentUserPassword();
        EncryptionInfo ei = getEncryptionInfo();
        try {
            if(!ei.getDecryptor().verifyPassword(pass != null ? pass : Decryptor.DEFAULT_PASSWORD)) {
                throw new EncryptedPowerPointFileException("PowerPoint file is encrypted. The correct password needs to be set via Biff8EncryptionKey.setCurrentUserPassword()");
            }
        } catch (GeneralSecurityException e) {
            throw new EncryptedPowerPointFileException(e);
        }
     }

    public DocumentEncryptionAtom getDocumentEncryptionAtom() {
        return dea;
    }

    protected EncryptionInfo getEncryptionInfo() {
        return (dea != null) ? dea.getEncryptionInfo() : null;
    }

    protected OutputStream encryptRecord(OutputStream plainStream, int persistId, org.apache.poi.hslf.record.Record record) {
        boolean isPlain = (dea == null
            || record instanceof UserEditAtom
            || record instanceof PersistPtrHolder
            || record instanceof DocumentEncryptionAtom
        );

        try {
            if (isPlain) {
                if (cyos != null) {
                    // write cached data to stream
                    cyos.flush();
                }
                return plainStream;
            }

            if (cyos == null) {
                Encryptor enc = getEncryptionInfo().getEncryptor();
                enc.setChunkSize(-1);
                cyos = enc.getDataStream(plainStream, 0);
            }
            cyos.initCipherForBlock(persistId, false);
        } catch (Exception e) {
            throw new EncryptedPowerPointFileException(e);
        }
        return cyos;
    }

    private static void readFully(ChunkedCipherInputStream ccis, byte[] docstream, int offset, int len) throws IOException {
        if (IOUtils.readFully(ccis, docstream, offset, len) == -1) {
            throw new EncryptedPowerPointFileException("unexpected EOF");
        }
    }

    protected void decryptRecord(byte[] docstream, int persistId, int offset) {
        if (dea == null) {
            return;
        }

        Decryptor dec = getEncryptionInfo().getDecryptor();
        dec.setChunkSize(-1);
        try (LittleEndianByteArrayInputStream lei = new LittleEndianByteArrayInputStream(docstream, offset);
                ChunkedCipherInputStream ccis = (ChunkedCipherInputStream)dec.getDataStream(lei, docstream.length-offset, 0)) {
            ccis.initCipherForBlock(persistId);

            // decrypt header and read length to be decrypted
            readFully(ccis, docstream, offset, 8);
            // decrypt the rest of the record
            int rlen = (int)LittleEndian.getUInt(docstream, offset+4);
            readFully(ccis, docstream, offset+8, rlen);

        } catch (Exception e) {
            throw new EncryptedPowerPointFileException(e);
        }
    }

    private void decryptPicBytes(byte[] pictstream, int offset, int len)
    throws IOException, GeneralSecurityException {
        // when reading the picture elements, each time a segment is read, the cipher needs
        // to be reset (usually done when calling Cipher.doFinal)
        LittleEndianByteArrayInputStream lei = new LittleEndianByteArrayInputStream(pictstream, offset);
        Decryptor dec = getEncryptionInfo().getDecryptor();
        ChunkedCipherInputStream ccis = (ChunkedCipherInputStream)dec.getDataStream(lei, len, 0);
        readFully(ccis, pictstream, offset, len);
        ccis.close();
        lei.close();
    }

    protected void decryptPicture(byte[] pictstream, int offset) {
        if (dea == null) {
            return;
        }

        try {
            // decrypt header and read length to be decrypted
            decryptPicBytes(pictstream, offset, 8);
            int recInst = fieldRecInst.getValue(LittleEndian.getUShort(pictstream, offset));
            int recType = LittleEndian.getUShort(pictstream, offset+2);
            int rlen = (int)LittleEndian.getUInt(pictstream, offset+4);
            offset += 8;
            int endOffset = offset + rlen;

            if (recType == 0xF007) {
                // TOOD: get a real example file ... to actual test the FBSE entry
                // not sure where the foDelay block is

                // File BLIP Store Entry (FBSE)
                for (int part : BLIB_STORE_ENTRY_PARTS) {
                    decryptPicBytes(pictstream, offset, part);
                }
                offset += 36;

                int cbName = LittleEndian.getUShort(pictstream, offset-3);
                if (cbName > 0) {
                    // read nameData
                    decryptPicBytes(pictstream, offset, cbName);
                    offset += cbName;
                }

                if (offset == endOffset) {
                    return; // no embedded blip
                }
                // fall through, read embedded blip now

                // update header data
                decryptPicBytes(pictstream, offset, 8);
                recInst = fieldRecInst.getValue(LittleEndian.getUShort(pictstream, offset));
                recType = LittleEndian.getUShort(pictstream, offset+2);
                // rlen = (int)LittleEndian.getUInt(pictstream, offset+4);
                offset += 8;
            }

            int rgbUidCnt = (recInst == 0x217 || recInst == 0x3D5 || recInst == 0x46B || recInst == 0x543 ||
                recInst == 0x6E1 || recInst == 0x6E3 || recInst == 0x6E5 || recInst == 0x7A9) ? 2 : 1;

            // rgbUid 1/2
            for (int i=0; i<rgbUidCnt; i++) {
                decryptPicBytes(pictstream, offset, 16);
                offset += 16;
            }

            int nextBytes;
            if (recType == 0xF01A || recType == 0XF01B || recType == 0XF01C) {
                // metafileHeader
                nextBytes = 34;
            } else {
                // tag
                nextBytes = 1;
            }

            decryptPicBytes(pictstream, offset, nextBytes);
            offset += nextBytes;

            int blipLen = endOffset - offset;
            decryptPicBytes(pictstream, offset, blipLen);
        } catch (Exception e) {
            throw new CorruptPowerPointFileException(e);
        }
    }

    protected void encryptPicture(byte[] pictstream, int offset) {
        if (dea == null) {
            return;
        }

        ChunkedCipherOutputStream ccos = null;

        try (LittleEndianByteArrayOutputStream los = new LittleEndianByteArrayOutputStream(pictstream, offset)) {
            Encryptor enc = getEncryptionInfo().getEncryptor();
            enc.setChunkSize(-1);
            ccos = enc.getDataStream(los, 0);
            int recInst = fieldRecInst.getValue(LittleEndian.getUShort(pictstream, offset));
            int recType = LittleEndian.getUShort(pictstream, offset+2);
            final int rlen = (int)LittleEndian.getUInt(pictstream, offset+4);

            ccos.write(pictstream, offset, 8);
            ccos.flush();
            offset += 8;
            int endOffset = offset + rlen;

            if (recType == 0xF007) {
                // TOOD: get a real example file ... to actual test the FBSE entry
                // not sure where the foDelay block is

                // File BLIP Store Entry (FBSE)
                int cbName = LittleEndian.getUShort(pictstream, offset+33);

                for (int part : BLIB_STORE_ENTRY_PARTS) {
                    ccos.write(pictstream, offset, part);
                    ccos.flush();
                    offset += part;
                }

                if (cbName > 0) {
                    ccos.write(pictstream, offset, cbName);
                    ccos.flush();
                    offset += cbName;
                }

                if (offset == endOffset) {
                    return; // no embedded blip
                }
                // fall through, read embedded blip now

                // update header data
                recInst = fieldRecInst.getValue(LittleEndian.getUShort(pictstream, offset));
                recType = LittleEndian.getUShort(pictstream, offset+2);
                ccos.write(pictstream, offset, 8);
                ccos.flush();
                offset += 8;
            }

            int rgbUidCnt = (recInst == 0x217 || recInst == 0x3D5 || recInst == 0x46B || recInst == 0x543 ||
                recInst == 0x6E1 || recInst == 0x6E3 || recInst == 0x6E5 || recInst == 0x7A9) ? 2 : 1;

            for (int i=0; i<rgbUidCnt; i++) {
                ccos.write(pictstream, offset, 16); // rgbUid 1/2
                ccos.flush();
                offset += 16;
            }

            if (recType == 0xF01A || recType == 0XF01B || recType == 0XF01C) {
                ccos.write(pictstream, offset, 34); // metafileHeader
                offset += 34;
                ccos.flush();
            } else {
                ccos.write(pictstream, offset, 1); // tag
                offset += 1;
                ccos.flush();
            }

            int blipLen = endOffset - offset;
            ccos.write(pictstream, offset, blipLen);
            ccos.flush();
        } catch (Exception e) {
            throw new EncryptedPowerPointFileException(e);
        } finally {
            IOUtils.closeQuietly(ccos);
        }
    }

    protected org.apache.poi.hslf.record.Record[] updateEncryptionRecord(org.apache.poi.hslf.record.Record[] records) {
        String password = Biff8EncryptionKey.getCurrentUserPassword();
        if (password == null) {
            if (dea == null) {
                // no password given, no encryption record exits -> done
                return records;
            } else {
                // need to remove password data
                dea = null;
                return removeEncryptionRecord(records);
            }
        } else {
            // create password record
            if (dea == null) {
                dea = new DocumentEncryptionAtom();
            }
            EncryptionInfo ei = dea.getEncryptionInfo();
            byte[] salt = ei.getVerifier().getSalt();
            Encryptor enc = getEncryptionInfo().getEncryptor();
            if (salt == null) {
                enc.confirmPassword(password);
            } else {
                byte[] verifier = ei.getDecryptor().getVerifier();
                enc.confirmPassword(password, null, null, verifier, salt, null);
            }

            // move EncryptionRecord to last slide position
            records = normalizeRecords(records);
            return addEncryptionRecord(records, dea);
        }
    }

    /**
     * remove duplicated UserEditAtoms and merge PersistPtrHolder.
     * Before this method is called, make sure that the offsets are correct,
     * i.e. call {@link HSLFSlideShowImpl#updateAndWriteDependantRecords(OutputStream, Map)}
     */
    protected static org.apache.poi.hslf.record.Record[] normalizeRecords(org.apache.poi.hslf.record.Record[] records) {
        // http://msdn.microsoft.com/en-us/library/office/gg615594(v=office.14).aspx
        // repeated slideIds can be overwritten, i.e. ignored

        UserEditAtom uea = null;
        PersistPtrHolder pph = null;
        TreeMap<Integer,Integer> slideLocations = new TreeMap<>();
        TreeMap<Integer,Record> recordMap = new TreeMap<>();
        List<Integer> obsoleteOffsets = new ArrayList<>();
        int duplicatedCount = 0;
        for (org.apache.poi.hslf.record.Record r : records) {
            assert(r instanceof PositionDependentRecord);
            PositionDependentRecord pdr = (PositionDependentRecord)r;
            if (pdr instanceof UserEditAtom) {
                uea = (UserEditAtom)pdr;
                continue;
            }

            if (pdr instanceof PersistPtrHolder) {
                if (pph != null) {
                    duplicatedCount++;
                }
                pph = (PersistPtrHolder)pdr;
                for (Map.Entry<Integer,Integer> me : pph.getSlideLocationsLookup().entrySet()) {
                    Integer oldOffset = slideLocations.put(me.getKey(), me.getValue());
                    if (oldOffset != null) {
                        obsoleteOffsets.add(oldOffset);
                    }
                }
                continue;
            }

            recordMap.put(pdr.getLastOnDiskOffset(), r);
        }

        if (uea == null || pph == null || uea.getPersistPointersOffset() != pph.getLastOnDiskOffset()) {
            throw new EncryptedDocumentException("UserEditAtom and PersistPtrHolder must exist and their offset need to match.");
        }

        recordMap.put(pph.getLastOnDiskOffset(), pph);
        recordMap.put(uea.getLastOnDiskOffset(), uea);

        if (duplicatedCount == 0 && obsoleteOffsets.isEmpty()) {
            return records;
        }

        uea.setLastUserEditAtomOffset(0);
        pph.clear();
        for (Map.Entry<Integer,Integer> me : slideLocations.entrySet()) {
            pph.addSlideLookup(me.getKey(), me.getValue());
        }

        for (Integer oldOffset : obsoleteOffsets) {
            recordMap.remove(oldOffset);
        }

        return recordMap.values().toArray(new org.apache.poi.hslf.record.Record[0]);
    }


    protected static org.apache.poi.hslf.record.Record[] removeEncryptionRecord(org.apache.poi.hslf.record.Record[] records) {
        int deaSlideId = -1;
        int deaOffset = -1;
        PersistPtrHolder ptr = null;
        UserEditAtom uea = null;
        List<org.apache.poi.hslf.record.Record> recordList = new ArrayList<>();
        for (org.apache.poi.hslf.record.Record r : records) {
            if (r instanceof DocumentEncryptionAtom) {
                deaOffset = ((DocumentEncryptionAtom)r).getLastOnDiskOffset();
                continue;
            } else if (r instanceof UserEditAtom) {
                uea = (UserEditAtom)r;
                deaSlideId = uea.getEncryptSessionPersistIdRef();
                uea.setEncryptSessionPersistIdRef(-1);
            } else if (r instanceof PersistPtrHolder) {
                ptr = (PersistPtrHolder)r;
            }
            recordList.add(r);
        }

        if (ptr == null || uea == null) {
            throw new EncryptedDocumentException("UserEditAtom or PersistPtrholder not found.");
        }
        if (deaSlideId == -1 && deaOffset == -1) {
            return records;
        }

        TreeMap<Integer,Integer> tm = new TreeMap<>(ptr.getSlideLocationsLookup());
        ptr.clear();
        int maxSlideId = -1;
        for (Map.Entry<Integer,Integer> me : tm.entrySet()) {
            if (me.getKey() == deaSlideId || me.getValue() == deaOffset) {
                continue;
            }
            ptr.addSlideLookup(me.getKey(), me.getValue());
            maxSlideId = Math.max(me.getKey(), maxSlideId);
        }

        uea.setMaxPersistWritten(maxSlideId);

        records = recordList.toArray(new org.apache.poi.hslf.record.Record[0]);

        return records;
    }


    protected static org.apache.poi.hslf.record.Record[] addEncryptionRecord(org.apache.poi.hslf.record.Record[] records, DocumentEncryptionAtom dea) {
        assert(dea != null);
        int ueaIdx = -1, ptrIdx = -1, deaIdx = -1, idx = -1;
        for (org.apache.poi.hslf.record.Record r : records) {
            idx++;
            if (r instanceof UserEditAtom) {
                ueaIdx = idx;
            } else if (r instanceof PersistPtrHolder) {
                ptrIdx = idx;
            } else if (r instanceof DocumentEncryptionAtom) {
                deaIdx = idx;
            }
        }
        assert(ueaIdx != -1 && ptrIdx != -1 && ptrIdx < ueaIdx);
        if (deaIdx != -1) {
            DocumentEncryptionAtom deaOld = (DocumentEncryptionAtom)records[deaIdx];
            dea.setLastOnDiskOffset(deaOld.getLastOnDiskOffset());
            records[deaIdx] = dea;
            return records;
        } else {
            PersistPtrHolder ptr = (PersistPtrHolder)records[ptrIdx];
            UserEditAtom uea = ((UserEditAtom)records[ueaIdx]);
            dea.setLastOnDiskOffset(ptr.getLastOnDiskOffset()-1);
            int nextSlideId = uea.getMaxPersistWritten()+1;
            ptr.addSlideLookup(nextSlideId, ptr.getLastOnDiskOffset()-1);
            uea.setEncryptSessionPersistIdRef(nextSlideId);
            uea.setMaxPersistWritten(nextSlideId);

            org.apache.poi.hslf.record.Record[] newRecords = new org.apache.poi.hslf.record.Record[records.length + 1];
            if (ptrIdx > 0) {
                System.arraycopy(records, 0, newRecords, 0, ptrIdx);
            }
            if (ptrIdx < records.length-1) {
                System.arraycopy(records, ptrIdx, newRecords, ptrIdx+1, records.length-ptrIdx);
            }
            newRecords[ptrIdx] = dea;
            return newRecords;
        }
    }

    @Override
    public void close() throws IOException {
        if (cyos != null) {
            cyos.close();
        }
    }
}
