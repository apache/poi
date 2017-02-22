package org.apache.poi.hwpf;

public class RC4 {
    private byte[] state;
    public int x;
    public int y;

    RC4() {
        state = new byte[256];
    }

    private void prepareKey(byte[] key_data_ptr, int key_data_len, RC4 key) {
        int index1;
        int index2;
        byte[] state;
        int counter;
        state = key.state;
        for (counter = 0; counter < 256; counter++) state[counter] = (byte) counter;
        key.x = 0;
        key.y = 0;
        index1 = 0;
        index2 = 0;
        for (counter = 0; counter < 256; counter++) {
            index2 = ((key_data_ptr[index1] & 0xff) + (state[counter] & 0xff) + index2) & 0xff;
            byte bTemp = state[counter];
            state[counter] = state[index2];
            state[index2] = bTemp;
            index1 = ((index1 + 1) % key_data_len);
        }
    }

    void makeKey(int block, RC4 rc4key, MD5 md5) {
        byte[] pwArray = new byte[64];
        MD5 temp = new MD5();
        for (int i = 0; i < 64; i++) pwArray[i] = 0;
        System.arraycopy(md5.digest, 0, pwArray, 0, 5);
        pwArray[5] = (byte) (block & 0xFF);
        pwArray[6] = (byte) ((block >> 8) & 0xFF);
        pwArray[7] = (byte) ((block >> 16) & 0xFF);
        pwArray[8] = (byte) ((block >> 24) & 0xFF);
        pwArray[9] = (byte) 0x80;
        pwArray[56] = (byte) 0x48;
        temp.md5Init();
        temp.md5Update(pwArray, 64);
        temp.getMD5StoreDigest(temp);
        prepareKey(temp.digest, 16, rc4key);
    }

    void rc4(byte[] buffer_ptr, int buffer_len, RC4 key) {
        int x;
        int y;
        byte[] state;
        int xorIndex;
        int counter;
        x = key.x;
        y = key.y;
        state = key.state;
        for (counter = 0; counter < buffer_len; counter++) {
            x = ((x + 1) & 0xff);
            y = (((state[x] & 0xff) + y) & 0xff);
            byte bTemp = state[x];
            state[x] = state[y];
            state[y] = bTemp;
            xorIndex = (((state[x] & 0xff) + (state[y] & 0xff)) & 0xff);
            buffer_ptr[counter] ^= (state[xorIndex]);
        }
        key.x = x;
        key.y = y;
    }
}
