package org.example;

import com.kone.pbdemo.protocol.DumpClassInfo;
import com.kone.pbdemo.protocol.DumpMethodInfo;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.jf.baksmali.fix.FixDumpClassCodeItem;
import org.jf.baksmali.fix.FixDumpMethodCodeItem;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class srializationTest {



    public static void main(String[] arg) {

        srializationTest test = new srializationTest();
        String host = "192.168.0.86";

        test.srialization(host);
        test.desrialization();
    }

    void desrialization (){

        Map<String, FixDumpClassCodeItem> fixDexDumpClassCodeItem = null;
        try {
            FileInputStream fileIn = new FileInputStream("c078d7b9.data");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            fixDexDumpClassCodeItem = (Map<String, FixDumpClassCodeItem>) in.readObject();
            in.close();
            fileIn.close();
            System.out.println("Map deserialized successfully.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        for (Map.Entry<String, FixDumpClassCodeItem> entry : fixDexDumpClassCodeItem.entrySet()) {

            String key = entry.getKey();
            FixDumpClassCodeItem fixDumpClassCodeItem = entry.getValue();
            System.out.println("clsName: " + key );
            for (Map.Entry<String, FixDumpMethodCodeItem> Methodentry : fixDumpClassCodeItem.methodCodeItemList.entrySet()) {
                String methodString = Methodentry.getKey();
                System.out.println("methodString: " + methodString );
            }
        }
    }

    void srialization(String host){
        int port = 9091;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
        GrpcService service = new GrpcService(channel);

        String [] clsList = {
                "com.hepta.androidgrpc.GrpcServiceImpl",
                "com.hepta.androidgrpc.dump",
                "com.hepta.androidgrpc.JNISignatureConverter",
                "com.hepta.androidgrpc.LoadEntry",
                "com.hepta.androidgrpc.MainActivity"
        };

        Map<String, FixDumpClassCodeItem> dumpClassCodeItemList = new HashMap<>();
        for(String clsName : clsList){
            String classTypeString = SignatureConverter.ClassNameToJNISigner(clsName);
            DumpClassInfo dumpClassList = service.dumpClass(clsName);

            Map<String, FixDumpMethodCodeItem> methodCodeItems = new HashMap<>();
            for(DumpMethodInfo dumpMethodInfo:dumpClassList.getDumpMethodInfoList()){
                String MethodName = dumpMethodInfo.getMethodName();
                String MethodSign = dumpMethodInfo.getMethodSign();
                String MethodString = MethodName+MethodSign;
                FixDumpMethodCodeItem fixDumpMethodCodeItem = new FixDumpMethodCodeItem(dumpMethodInfo.toByteArray());
                methodCodeItems.put(MethodString,fixDumpMethodCodeItem);
            }
            FixDumpClassCodeItem fixDumpClassCodeItem = new FixDumpClassCodeItem(methodCodeItems);
            dumpClassCodeItemList.put(classTypeString,fixDumpClassCodeItem);
        }

        try {
            FileOutputStream fileOut = new FileOutputStream("c078d7b9.data");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(dumpClassCodeItemList);
            out.close();
            fileOut.close();
            System.out.println("Map serialized successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
