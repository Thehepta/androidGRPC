package org.example;

import com.google.protobuf.ByteString;
import com.kone.pbdemo.protocol.*;
import io.grpc.ManagedChannel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;

public class GrpcService {

    UserServiceGrpc.UserServiceBlockingStub  iServerInface;

    GrpcService(ManagedChannel channel){
        iServerInface = UserServiceGrpc.newBlockingStub(channel);
    }

    void dumpdex(){
        Empty empty = Empty.newBuilder().build();

        iServerInface.dumpdex(empty);
    }

    void dumpClass(String className){
        StringArgument classNameArg = StringArgument.newBuilder().setClassName(className).build();
        DumpClassInfo dumpClassList = iServerInface.dumpClass(classNameArg).next();
        if(dumpClassList.getStatus()){
            for(DumpMethodInfo dumpMethodInfo:dumpClassList.getDumpMethodInfoList()){
                String MethodName = dumpMethodInfo.getMethodName();
                String MethodSign = dumpMethodInfo.getMethodSign();
                System.out.println(MethodName+":"+MethodSign);
//                dumpMethodInfo.getContent().toByteArray()
            }
        }

    }

    byte[] dumpDexMethod(String className,String MethodName,String MethodSign){
        DumpMethodString.Builder dumpMethod = DumpMethodString.newBuilder();
        dumpMethod.setClassName(className);
        dumpMethod.setMethodName(MethodName);
        dumpMethod.setMethodSign(MethodSign);
        Dexbuff buff = iServerInface.dumpMethod(dumpMethod.build());
        return buff.getContent().toByteArray();
    }

    void dumpDexFile(String Dir){
        Empty empty = Empty.newBuilder().build();
        DexInfoList dexInfoList =  iServerInface.getCookieList(empty);

        for(DexInfo dexInfo : dexInfoList.getDexinfoList()){
            DownloadFileRequest downloadFileRequest = DownloadFileRequest.newBuilder().setDexinfo(dexInfo).build();
            DownloadFileResponse downloadFileResponse = iServerInface.downloadFile(downloadFileRequest).next();
            DexInfo dex =  downloadFileResponse.getContent();
            List<Dexbuff> dexbuffList = dex.getBuffList();
            for(Dexbuff buff :dexbuffList){
                ByteString data =  buff.getContent();
                Path filePath = Paths.get(Dir, Integer.toHexString(data.hashCode())+".dex"); // 构建文件路径
                try {
                    Files.createDirectories(filePath.getParent());
                    Files.write(filePath, data.toByteArray(), StandardOpenOption.CREATE);
                    System.out.println("dex dump successfule path:"+filePath.toAbsolutePath());
                } catch (IOException e) {
                    System.err.println("dex dump erroe: " + e.getMessage());
                }

            }
        }
    }


}
