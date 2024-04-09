package org.example;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kone.pbdemo.protocol.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {



    public static void  DownloadFile(UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub, long[] cookie, String outDir)
    {
//        DownloadFileRequest  downloadRequest =  DownloadFileRequest.newBuilder().setCookie(cookie).build();
////        downloadRequest.getFilePath()
//        userServiceBlockingStub.downloadFile(downloadRequest);

    }

    public static void main(String[] args) throws InterruptedException {


        String host = "192.168.0.86";
        int port = 9091;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
        GrpcService service = new GrpcService(channel);


//        String dir = "D:\\apk\\dumpdex";
//        service.dumpDexFile(dir);

//         byte[] dump_code_item = service.dumpDexMethod("com.hepta.androidgrpc.MainActivity","onCreate","(Landroid/os/Bundle;)V");
         service.dumpClass("com.hepta.androidgrpc.GrpcServiceImpl");
         System.out.println("sfewf");




        Thread.sleep(2000);
        channel.shutdown();
    }
}