package com.hepta.androidgrpc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.ipshow);

        final List<String> ipList = getHostAddresses(3);
        if (ipList.isEmpty()) {
            // should never happen... flw
            textView.setText("No network interfaces found?");
        } else {
            textView.setText("show network");
            textView.setText(String.join("\n", ipList));
        }
        Log.e("RZx","fewew");
        LoadEntry.Entry(this,getApplicationInfo().sourceDir,"");
    }


    static List<String> getHostAddresses(final int limit) {
        try {
            return Collections
                    .list(NetworkInterface.getNetworkInterfaces())
                    .stream()
                    .flatMap(ni -> Collections.list(ni.getInetAddresses()).stream())
                    .filter(ina -> !ina.isLoopbackAddress() && !ina.isLinkLocalAddress())
                    .map(InetAddress::getHostAddress)
                    .filter(Objects::nonNull)
//                    .map(ip -> {
//                        // strip of the scope id for IPv6 if present
//                        //noinspection ConstantConditions
//                        int i = ip.indexOf('%');
//                        return i != -1 ? ip.substring(0, i) : ip;
//                    })
                    // sorting will move IPv6 to the end of the list
                    .sorted()
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (@NonNull final Exception ignore) {
            // ignore
        }

        return new ArrayList<>();
    }
}