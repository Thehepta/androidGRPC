package org.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class dumpDexToPC {

    public static void main(String[] args) {


        String host = "192.168.0.86";      //remote android ip
        String dir = "D:\\apk\\ccdump";   //pc dex dir
        int port = 9091;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
        GrpcService service = new GrpcService(channel);
        service.dumpDexFile(dir);

    }
}
