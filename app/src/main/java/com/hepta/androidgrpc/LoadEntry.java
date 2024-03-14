package com.hepta.androidgrpc;

import android.content.Context;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

public class LoadEntry {


    public static void Entry(Context context, String source, String argument){
        new Thread(){
            @Override
            public void run() {
                int port = 9091;
                Server server = null;
                try {
                    server = NettyServerBuilder
                            .forPort(port)
                            .addService(new GrpcServiceImpl(context,source,argument))
                            .build()
                            .start();
                    System.out.println("server started, port : " + port);
                    server.awaitTermination();
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();

    }

}
