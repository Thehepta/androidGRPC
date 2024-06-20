package org.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class dumpDex {

    public static void main(String[] args) {


        String host = "192.168.1.2";      //remote android ip
        String dir = "D:\\apk\\work\\vip\\dump";   //pc dex dir
        int port = 9091;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
        GrpcService service = new GrpcService(channel);
        service.dumpDexFile(dir);

    }
}
