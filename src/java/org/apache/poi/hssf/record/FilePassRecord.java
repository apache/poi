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

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title: File Pass Record (0x002F) <p>
 *
 * Description: Indicates that the record after this record are encrypted.
 */
public final class FilePassRecord extends StandardRecord implements Cloneable {
	public static final short sid = 0x002F;
    private static final int ENCRYPTION_XOR = 0;
    private static final int ENCRYPTION_OTHER = 1;
	
	private int _encryptionType;
	private KeyData _keyData;

	private static interface KeyData extends Cloneable {
	    void read(RecordInputStream in);
	    void serialize(LittleEndianOutput out);
	    int getDataSize();
	    void appendToString(StringBuffer buffer);
	    KeyData clone(); // NOSONAR
	} 
	
	public static final class Rc4KeyData implements KeyData, Cloneable {
	    private static final int ENCRYPTION_OTHER_RC4 = 1;
	    private static final int ENCRYPTION_OTHER_CAPI_2 = 2;
	    private static final int ENCRYPTION_OTHER_CAPI_3 = 3;
        private static final int ENCRYPTION_OTHER_CAPI_4 = 4;
	    
	    private byte[] _salt;
	    private byte[] _encryptedVerifier;
	    private byte[] _encryptedVerifierHash;
	    private int _encryptionInfo;
	    private int _minorVersionNo;
	    
	    public void read(RecordInputStream in) {
	        _encryptionInfo = in.readUShort();
	        switch (_encryptionInfo) {
	            case ENCRYPTION_OTHER_RC4:
	                // handled below
	                break;
	            case ENCRYPTION_OTHER_CAPI_2:
	            case ENCRYPTION_OTHER_CAPI_3:
                case ENCRYPTION_OTHER_CAPI_4:
	                throw new EncryptedDocumentException(
	                        "HSSF does not currently support CryptoAPI encryption");
	            default:
	                throw new RecordFormatException("Unknown encryption info " + _encryptionInfo);
	        }
	        _minorVersionNo = in.readUShort();
	        if (_minorVersionNo!=1) {
	            throw new RecordFormatException("Unexpected VersionInfo number for RC4Header " + _minorVersionNo);
	        }
	        _salt = FilePassRecord.read(in, 16);
	        _encryptedVerifier = FilePassRecord.read(in, 16);
	        _encryptedVerifierHash = FilePassRecord.read(in, 16);
	    }
	    
	    public void serialize(LittleEndianOutput out) {
            out.writeShort(_encryptionInfo);
            out.writeShort(_minorVersionNo);
            out.write(_salt);
            out.write(_encryptedVerifier);
            out.write(_encryptedVerifierHash);
	    }
	    
	    public int getDataSize() {
	        return 54;
	    }

        public byte[] getSalt() {
            return _salt.clone();
        }

        public void setSalt(byte[] salt) {
            this._salt = salt.clone();
        }

        public byte[] getEncryptedVerifier() {
            return _encryptedVerifier.clone();
        }

        public void setEncryptedVerifier(byte[] encryptedVerifier) {
            this._encryptedVerifier = encryptedVerifier.clone();
        }

        public byte[] getEncryptedVerifierHash() {
            return _encryptedVerifierHash.clone();
        }

        public void setEncryptedVerifierHash(byte[] encryptedVerifierHash) {
            this._encryptedVerifierHash = encryptedVerifierHash.clone();
        }
        
        public void appendToString(StringBuffer buffer) {
            buffer.append("    .rc4.info = ").append(HexDump.shortToHex(_encryptionInfo)).append("\n");
            buffer.append("    .rc4.ver  = ").append(HexDump.shortToHex(_minorVersionNo)).append("\n");
            buffer.append("    .rc4.salt = ").append(HexDump.toHex(_salt)).append("\n");
            buffer.append("    .rc4.verifier = ").append(HexDump.toHex(_encryptedVerifier)).append("\n");
            buffer.append("    .rc4.verifierHash = ").append(HexDump.toHex(_encryptedVerifierHash)).append("\n");
        }
        
        @Override
        public Rc4KeyData clone() {
            Rc4KeyData other = new Rc4KeyData();
            other._salt = this._salt.clone();
            other._encryptedVerifier = this._encryptedVerifier.clone();
            other._encryptedVerifierHash = this._encryptedVerifierHash.clone();
            other._encryptionInfo = this._encryptionInfo;
            other._minorVersionNo = this._minorVersionNo;
            return other;
        }
	}

	public static final class XorKeyData implements KeyData, Cloneable {
	    /**
	     * key (2 bytes): An unsigned integer that specifies the obfuscation key. 
	     * See [MS-OFFCRYPTO], 2.3.6.2 section, the first step of initializing XOR
	     * array where it describes the generation of 16-bit XorKey value.
	     */
	    private int _key;

	    /**
	     * verificationBytes (2 bytes): An unsigned integer that specifies
	     * the password verification identifier.
	     */
	    private int _verifier;
	    
        public void read(RecordInputStream in) {
            _key = in.readUShort();
            _verifier = in.readUShort();
        }

        public void serialize(LittleEndianOutput out) {
            out.writeShort(_key);
            out.writeShort(_verifier);
        }

        public int getDataSize() {
            // TODO: Check!
            return 6;
        }

        public int getKey() {
            return _key;
        }
        
        public int getVerifier() {
            return _verifier;
        }
        
        public void setKey(int key) {
            this._key = key;
        }
        
        public void setVerifier(int verifier) {
            this._verifier = verifier;
        }
        
        public void appendToString(StringBuffer buffer) {
            buffer.append("    .xor.key = ").append(HexDump.intToHex(_key)).append("\n");
            buffer.append("    .xor.verifier  = ").append(HexDump.intToHex(_verifier)).append("\n");
        }
        
        @Override
        public XorKeyData clone() {
            XorKeyData other = new XorKeyData();
            other._key = this._key;
            other._verifier = this._verifier;
            return other;
        }
	}
	
	
	private FilePassRecord(FilePassRecord other) {
	    _encryptionType = other._encryptionType;
	    _keyData = other._keyData.clone();
	}
	
	public FilePassRecord(RecordInputStream in) {
		_encryptionType = in.readUShort();

		switch (_encryptionType) {
			case ENCRYPTION_XOR:
			    _keyData = new XorKeyData();
			    break;
			case ENCRYPTION_OTHER:
			    _keyData = new Rc4KeyData();
				break;
			default:
				throw new RecordFormatException("Unknown encryption type " + _encryptionType);
		}

		_keyData.read(in);
	}

	private static byte[] read(RecordInputStream in, int size) {
		byte[] result = new byte[size];
		in.readFully(result);
		return result;
	}

	public void serialize(LittleEndianOutput out) {
		out.writeShort(_encryptionType);
		assert(_keyData != null);
		_keyData.serialize(out);
	}

	protected int getDataSize() {
	    assert(_keyData != null);
	    return _keyData.getDataSize();
	}

	public Rc4KeyData getRc4KeyData() {
	    return (_keyData instanceof Rc4KeyData)
            ? (Rc4KeyData) _keyData
            : null;
	}
	
    public XorKeyData getXorKeyData() {
        return (_keyData instanceof XorKeyData)
            ? (XorKeyData) _keyData
            : null;
    }
    
    private Rc4KeyData checkRc4() {
        Rc4KeyData rc4 = getRc4KeyData();
        if (rc4 == null) {
            throw new RecordFormatException("file pass record doesn't contain a rc4 key.");
        }
        return rc4;
    }

	public short getSid() {
		return sid;
	}
	
	@Override
	public FilePassRecord clone() {
		return new FilePassRecord(this);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("[FILEPASS]\n");
		buffer.append("    .type = ").append(HexDump.shortToHex(_encryptionType)).append("\n");
		_keyData.appendToString(buffer);
		buffer.append("[/FILEPASS]\n");
		return buffer.toString();
	}
}
