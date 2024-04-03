package org.example;

import com.kone.pbdemo.protocol.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

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


    void GetDexFileByCookie(){
        Empty empty = Empty.newBuilder().build();
        CookieList cookieList =  iServerInface.getCookieList(empty);
        for(Cookie cookie : cookieList.getLongArraysList()){
            DownloadFileRequest downloadFileRequest = DownloadFileRequest.newBuilder().setCookie(cookie).build();
            DownloadFileResponse downloadFileResponse = iServerInface.downloadFile(downloadFileRequest).next();
        }

    }
}
