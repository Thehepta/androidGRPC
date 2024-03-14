package com.hepta.androidgrpc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LoadEntry.Entry(this,getApplicationInfo().sourceDir,"");

    }
}