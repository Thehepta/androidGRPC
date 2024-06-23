package com.hepta.androidgrpc;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.ChannelOption;

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
                            .withChildOption(ChannelOption.SO_REUSEADDR, true)
                            .addService(new GrpcServiceImpl(context,source,argument))
                            .maxInboundMessageSize(Integer.MAX_VALUE)
                            .build()
                            .start();
                    Log.e("LoadEntry","server started, port : " + port);
                    server.awaitTermination();
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();

    }

}
