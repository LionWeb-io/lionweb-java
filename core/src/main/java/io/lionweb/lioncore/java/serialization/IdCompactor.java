package io.lionweb.lioncore.java.serialization;

import com.google.protobuf.ByteString;
import io.lionweb.lioncore.java.serialization.protobuf.CompactedId;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class IdCompactor {
    private static final int[] lookup;
    public static final char[] reverseLookup;

    static {
        lookup = new int[128];
        reverseLookup = new char[128];
        int index = 0;
        for(char i = 'a'; i <= 'z'; i++) {
            lookup[i] = index;
            reverseLookup[index] = i;
            index++;
        }
        for(char i = '0'; i <= '9'; i++) {
            lookup[i] = index;
            reverseLookup[index] = i;
            index++;
        }
        for(char i = 'A'; i <= 'Z'; i++) {
            lookup[i] = index;
            reverseLookup[index] = i;
            index++;
        }
        lookup['_'] = index;
        reverseLookup[index] = '_';
        index++;

        lookup['-'] = index;
        reverseLookup[index] = '-';
        index++;

        assert index == 64;
    }

    public static CompactedId compact(String id) {
        CompactedId.Builder builder = CompactedId.newBuilder();
        builder.setLength(id.length());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            int[] block = new int[4];
            int index = 0;
            for (char c : id.toCharArray()) {
                int i = lookup[c];
                assert i < 64;
                block[index++] = i;
                if (index == 4) {
                    buffer.write(pack(block, 4));
                    index = 0;
                    block[0] = 0;
                    block[1] = 0;
                    block[2] = 0;
                    block[3] = 0;
                }
            }
            if (index > 0) {
                buffer.write(pack(block, index));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        builder.setValue(ByteString.copyFrom(buffer.toByteArray()));

        return builder.build();
    }

    public static String expand(CompactedId id) {
        StringBuilder builder = new StringBuilder();

        ByteString value = id.getValue();
        int pos = 0;
        byte[] block = new byte[3];
        int index = 0;
        while (pos < value.size()) {
            block[index++] = value.byteAt(pos++);
            if(index == 3){
                builder.append(unpack(block, index));
                block[0] = 0;
                block[1] = 0;
                block[2] = 0;
                index = 0;
            }
        }
        if (index > 0) {
            builder.append(unpack(block, index));
        }

        return builder.subSequence(0, id.getLength()).toString();
    }


    private static byte[] pack(int[] block, int index) {
        assert index > 0;
        assert index <= 4;
        int length;
        switch (index) {
            case 1:
                length =1;
                break;
            case 2:
                length=2;
                break;
            default:
                length=3;
                break;
        }
        byte[] result = new byte[length];
        // bytes: 012345 67_0123 4567_01 234567
        // chars: 012345 01_2345 0123_45 012345
        int shiftA = block[1] << 6;
        int int0 = block[0] | shiftA;
        result[0] = (byte) int0;
        if (length == 1) {
            return result;
        }

        int shiftB = block[1] >> 2;
        int shiftC = block[2] << 4;
        int int1 = shiftB | shiftC;
        result[1] = (byte) int1;
        if (length == 2) {
            return result;
        }

        int shiftD = block[2] >> 4;
        int shiftE = block[3] << 2;
        int int2 = shiftD | shiftE;
        result[2] = (byte) int2;
        return result;
    }

    private static char[] unpack(byte[] block, int index) {
        assert index > 0;
        assert index <= 3;

        int length;
        switch (index) {
            case 1:
                length =1;
                break;
            case 2:
                length=2;
                break;
            default:
                length=4;
                break;
        }
        char[] result = new char[length];

        int int0 = block[0]& 0xFF;;
        int index0 = int0 & 63;
        result[0] = reverseLookup[index0];
        if(length == 1){
            return result;
        }

        int int1 = block[1]& 0xFF;;
        int shiftA = int0 >>> 6;
        int shiftB = int1 << 2;
        int index1 = (shiftA | shiftB) & 63;
        result[1] = reverseLookup[index1];
        if(length == 2){
            return result;
        }

        int int2 = block[2]& 0xFF;;
        int shiftC = int1 >>> 4;
        int shiftD = int2 << 4;
        int index2 = (shiftC | shiftD) & 63;
        result[2] = reverseLookup[index2];

        int shiftE = int2 >>> 2;
        int index3 = shiftE & 63;
        result[3] = reverseLookup[index3];
        return result;
    }
}
