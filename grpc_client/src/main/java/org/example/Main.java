package org.example;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kone.pbdemo.protocol.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.jf.baksmali.fix.FixDumpClassCodeItem;
import org.jf.baksmali.fix.FixDumpMethodCodeItem;
import org.jf.baksmali.fix.FixMain;

import java.util.HashMap;
import java.util.Map;

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


        String host = "10.1.2.242";
        int port = 9091;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
        GrpcService service = new GrpcService(channel);


        String dir = "D:\\apk\\dumpdex";
        service.dumpDexFile(dir);

//         byte[] dump_code_item = service.dumpDexMethod("com.hepta.androidgrpc.MainActivity","onCreate","(Landroid/os/Bundle;)V");
//        DumpClassInfo dumpClassList = service.dumpClass("com.hepta.androidgrpc.GrpcServiceImpl");
//        DumpClassInfo dumpClassList2 = service.dumpClass("com.hepta.androidgrpc.dump");
//        DumpClassInfo dumpClassList3 = service.dumpClass("com.hepta.androidgrpc.JNISignatureConverter");
//        DumpClassInfo dumpClassList4 = service.dumpClass("com.hepta.androidgrpc.LoadEntry");
//        DumpClassInfo dumpClassList5 = service.dumpClass("com.hepta.androidgrpc.MainActivity");

//        fixMain.Main1();





        Thread.sleep(2000);
        channel.shutdown();
    }


    public static void dumpText(){
        String host = "192.168.0.86";
        int port = 9091;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
        GrpcService service = new GrpcService(channel);

        String [] clsList = {
                "com.hepta.androidgrpc.GrpcServiceImpl",
                "com.hepta.androidgrpc.dump",
                "com.hepta.androidgrpc.JNISignatureConverter",
                "com.hepta.androidgrpc.LoadEntry",
                "com.hepta.androidgrpc.MainActivity"
        };

        Map<String, FixDumpClassCodeItem> dumpClassCodeItemList = new HashMap<>();
        for(String clsName : clsList){
            String classTypeString = JNISignatureConverter.ClassNameToJNISigner(clsName);
            DumpClassInfo dumpClassList = service.dumpClass(clsName);

            Map<String, FixDumpMethodCodeItem> methodCodeItems = new HashMap<>();
            for(DumpMethodInfo dumpMethodInfo:dumpClassList.getDumpMethodInfoList()){
                String MethodName = dumpMethodInfo.getMethodName();
                String MethodSign = dumpMethodInfo.getMethodSign();
                String MethodString = MethodName+MethodSign;
                FixDumpMethodCodeItem fixDumpMethodCodeItem = new FixDumpMethodCodeItem(dumpMethodInfo.toByteArray());
                methodCodeItems.put(MethodString,fixDumpMethodCodeItem);
            }
            FixDumpClassCodeItem fixDumpClassCodeItem = new FixDumpClassCodeItem(methodCodeItems);
            dumpClassCodeItemList.put(classTypeString,fixDumpClassCodeItem);

        }
    }

}