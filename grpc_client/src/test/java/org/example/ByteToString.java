package org.example;

import com.google.protobuf.ByteString;

public class ByteToString {

    public static void main(String[] args) {
        byte [] code_item = {0x1a,0x00, (byte) 0xba,0x00,0x1a,0x01, (byte) 0xbb,0x00};
        byte [] code_item2= ByteString.copyFrom(code_item).toByteArray();
        System.out.println( ByteString.copyFrom(code_item));
    }
}
