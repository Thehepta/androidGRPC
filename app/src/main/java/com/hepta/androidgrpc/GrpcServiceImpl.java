package com.hepta.androidgrpc;

import android.content.Context;
import android.util.Log;

import com.kone.pbdemo.protocol.Empty;
import com.kone.pbdemo.protocol.StringArgument;
import com.kone.pbdemo.protocol.User;
import com.kone.pbdemo.protocol.UserServiceGrpc;
import com.kone.pbdemo.protocol.cookieList;

import java.util.List;

import io.grpc.stub.StreamObserver;

public class GrpcServiceImpl extends UserServiceGrpc.UserServiceImplBase {


    public Context context;
    public String source;
    public String argument;

    public GrpcServiceImpl(Context ctx,String source, String argument){
        context = ctx;
        this.source = source;
        this.argument = argument;
    }
    @Override
    public void getUser(User request, StreamObserver<User> responseObserver) {
        System.out.println(request);
        User user = User.newBuilder()
                .setName("response name")
                .build();
        responseObserver.onNext(user);
        responseObserver.onCompleted();
    }

    @Override
    public void getUsers(User request, StreamObserver<User> responseObserver) {
        System.out.println("get users");
        System.out.println(request);
        User user = User.newBuilder()
                .setName("user1")
                .build();
        User user2 = User.newBuilder()
                .setName("user2")
                .build();
        responseObserver.onNext(user);
        responseObserver.onNext(user2);

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<User> saveUsers(StreamObserver<User> responseObserver) {

        return new StreamObserver<User>() {
            @Override
            public void onNext(User user) {
                System.out.println("get saveUsers list ---->");
                System.out.println(user);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("saveUsers error " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                User user = User.newBuilder()
                        .setName("saveUsers user1")
                        .build();
                responseObserver.onNext(user);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void dumpdex(Empty request, StreamObserver<Empty> responseObserver) {
        Log.e("rzx","dumpdex");
        dump.Entry(context,source,argument);
        dump.dumpdex(context);
        responseObserver.onNext(request);
        responseObserver.onCompleted();
    }

    @Override
    public void dumpClass(StringArgument request, StreamObserver<StringArgument> responseObserver) {

        String clsName = request.getClassName();
        Log.e("rzx","dumpClass:"+clsName);
        dump.dumpClass(clsName);

        responseObserver.onNext(request);
        responseObserver.onCompleted();
    }

    @Override
    public void getCookieList(Empty request, StreamObserver<cookieList> responseObserver) {
        List<long[]> ck = dump.getCookieList(context);

//        cookieList response =  cookieList.newBuilder().set.build();
//        User user = User.newBuilder().setName("user1").build();
//        cookieList CK = cookieList.
//        responseObserver.onNext();

    }
}