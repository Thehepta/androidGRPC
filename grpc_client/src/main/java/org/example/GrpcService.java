package org.example;

import com.kone.pbdemo.protocol.*;
import io.grpc.ManagedChannel;

public class GrpcService {

    UserServiceGrpc.UserServiceBlockingStub  iServerInface;

    GrpcService(ManagedChannel channel){
        iServerInface = UserServiceGrpc.newBlockingStub(channel);
    }

    void dumpdex(){
        Empty empty = Empty.newBuilder().build();

        iServerInface.dumpdex(empty);
    }

    void dumpClass(){
        StringArgument className = StringArgument.newBuilder().setClassName("com.yunmai.valueoflife.MainActivity$a").build();
//        StringArgument className = StringArgument.newBuilder().setClassName("com.ccb.start.MainActivity").build();
        iServerInface.dumpClass(className);
    }


    void dumpDexFile(){
        Empty empty = Empty.newBuilder().build();
        DexInfoList dexInfoList =  iServerInface.getCookieList(empty);
        for(DexInfo dexInfo : dexInfoList.getDexinfoList()){
            DownloadFileRequest downloadFileRequest = DownloadFileRequest.newBuilder().setDexinfo(dexInfo).build();
            DownloadFileResponse downloadFileResponse = iServerInface.downloadFile(downloadFileRequest).next();
            DexInfo dex =  downloadFileResponse.getContent();
        }

    }
}
