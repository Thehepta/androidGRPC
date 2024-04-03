package com.hepta.androidgrpc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dump.Entry(this,getApplicationInfo().sourceDir,"");
        LoadEntry.Entry(this,getApplicationInfo().sourceDir,"");
//        dump.dumpdex(this);
//        Map<long[],String> cooklistMaps = dump.getCookieList(this);
        Log.e("RZx","fewew");
    }
}