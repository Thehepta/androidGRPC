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

    int kOatFileIndex = 0;
    int kDexFileIndexStart = 1;
    UserServiceGrpc.UserServiceBlockingStub  iServerInface;

    GrpcService(ManagedChannel channel){
        iServerInface = UserServiceGrpc.newBlockingStub(channel);
    }

    void dumpdex(){
        Empty empty = Empty.newBuilder().build();
        iServerInface.dexDumpToLocal(empty);
    }

    DumpClassInfo dumpClass(String className) {
        StringArgument classNameArg = StringArgument.newBuilder().setClassName(className).build();
        DumpClassInfo dumpClassList = iServerInface.dumpClass(classNameArg).next();
        if (dumpClassList.getStatus()) {
            return dumpClassList;
        }
        return null;
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
            System.out.println("android app dex path: "+dexInfo.getDexpath());
            long[] Cookie_longArray = dexInfo.getValuesList().stream().mapToLong(Long::longValue).toArray();
            for(int i = kDexFileIndexStart;i<Cookie_longArray.length;++i){
                long DexFile_Point = Cookie_longArray[i];
                iServerInface.dexDumpByDexFilePoint(DexFile_Point);
            }








            DownloadFileRequest downloadFileRequest = DownloadFileRequest.newBuilder().setDexinfo(dexInfo).build();
            DownloadFileResponse downloadFileResponse = iServerInface.dexDumpDownload(downloadFileRequest).next();
            DexInfo dex =  downloadFileResponse.getContent();
            List<Dexbuff> dexbuffList = dex.getBuffList();
            for(Dexbuff buff :dexbuffList){
                ByteString data =  buff.getContent();
                Path filePath = Paths.get(Dir, Integer.toHexString(data.hashCode())+".dex"); // 构建文件路径
                try {
                    Files.createDirectories(filePath.getParent());
                    Files.write(filePath, data.toByteArray(), StandardOpenOption.CREATE);
                    System.out.println("local dump successfule To path:"+filePath.toAbsolutePath());
                } catch (IOException e) {
                    System.err.println("dex dump erroe: " + e.getMessage());
                }

            }
        }
    }


}
