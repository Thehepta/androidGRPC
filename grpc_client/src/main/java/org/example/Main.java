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


//        StringArgument className = StringArgument.newBuilder().setClassName("com.yunmai.valueoflife.MainActivity$a").build();
//        StringArgument className = StringArgument.newBuilder().setClassName("com.ccb.start.MainActivity").build();
        String dir = "D:\\apk\\dumpdex";
        service.dumpDexFile(dir);

//        userServiceBlockingStub.dumpClass(className);
        

        Thread.sleep(2000);
        channel.shutdown();
    }
}