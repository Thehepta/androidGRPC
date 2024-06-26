package com.hepta.androidgrpc;

import static com.hepta.androidgrpc.dump.getDexBuffbyCookieLong;

import android.content.Context;
import android.util.Log;


import com.google.protobuf.ByteString;
import com.kone.pbdemo.protocol.DexClassLoaderInfo;
import com.kone.pbdemo.protocol.DexClassLoaders;
import com.kone.pbdemo.protocol.DexFilePoint;

import com.kone.pbdemo.protocol.DumpClassInfo;
import com.kone.pbdemo.protocol.DumpMethodInfo;
import com.kone.pbdemo.protocol.Empty;
import com.kone.pbdemo.protocol.StringArgument;
import com.kone.pbdemo.protocol.UserServiceGrpc;
import com.kone.pbdemo.protocol.Dexbuff;
import com.kone.pbdemo.protocol.DumpMethodString;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import io.grpc.stub.StreamObserver;


public class GrpcServiceImpl extends UserServiceGrpc.UserServiceImplBase {


    public Context context;
    public String source;
    public String argument;
    public ClassLoader [] classLoaders;
    public GrpcServiceImpl(Context ctx,String source, String argument){
        dump.Entry(ctx,source,argument);
        context = ctx;
        this.source = source;
        this.argument = argument;
    }

    public Class<?> AndroidFindClass(String name){
        if (classLoaders == null) {
            classLoaders = dump.getClassLoaderList();
        }
        for (ClassLoader classLoader:classLoaders){
            try {
                return classLoader.loadClass(name);
            } catch (ClassNotFoundException ignored) {

            }
        }
        return null;
    }

    @Override
    public void dexDumpToLocal(Empty request, StreamObserver<Empty> responseObserver) {
        Log.e("rzx","dumpdex");
        dump.Entry(context,source,argument);
        dump.dumpdexToLocal(context);
        responseObserver.onNext(request);
        responseObserver.onCompleted();
    }



    @Override
    public void getDexClassLoaderList(Empty request, StreamObserver<DexClassLoaders> responseObserver) {
        Map<long[],AndroidClassLoaderInfo> dexClassLoaderCookieMpas = dump.getDexClassLoaderCookieMpas(context);
        DexClassLoaders.Builder dexClassLoaderInfoList_build =  DexClassLoaders.newBuilder();
        for(long[] dexClassLoaderCookie:dexClassLoaderCookieMpas.keySet() ){
            DexClassLoaderInfo.Builder dexClassLoaderCookie_build = DexClassLoaderInfo.newBuilder();
            for(long dexCookie:dexClassLoaderCookie){
                dexClassLoaderCookie_build.addValues(dexCookie);
            }
            AndroidClassLoaderInfo classLoaderInfo = dexClassLoaderCookieMpas.get(dexClassLoaderCookie);
            if(classLoaderInfo != null){
                dexClassLoaderCookie_build.setDexpath(classLoaderInfo.FilePatch);
                dexClassLoaderCookie_build.setClassLoadType(classLoaderInfo.ClassType);
            }
            dexClassLoaderInfoList_build.addDexClassLoadInfo(dexClassLoaderCookie_build.build());
        }
        responseObserver.onNext(dexClassLoaderInfoList_build.build());
        responseObserver.onCompleted();
    }

    @Override
    public void dexDumpByDexFilePoint(DexFilePoint request, StreamObserver<Dexbuff> responseObserver) {
        long dexFilePoint =  request.getValues();
        byte[] buff = getDexBuffbyCookieLong(dexFilePoint);
        Dexbuff dexbuff = Dexbuff.newBuilder().setContent(ByteString.copyFrom(buff)).build();
        responseObserver.onNext(dexbuff);
        responseObserver.onCompleted();
    }


    @Override
    public void dumpClass(StringArgument request, StreamObserver<DumpClassInfo> responseObserver) {
        String clsName = request.getClassName();
        DumpClassInfo.Builder classInfobuilder = DumpClassInfo.newBuilder();

        Log.e("rzx","dumpClass:"+clsName);
        Class<?> cls = AndroidFindClass(clsName);

        if(cls != null){
            classInfobuilder.setStatus(true);
            Method[] Declaredmethods =  cls.getDeclaredMethods();
            for (Method method :Declaredmethods ) {
                int modifiers = method.getModifiers();
                if(Modifier.isNative(modifiers)){
                    continue;
                }
                byte[] MethodCodeItem = dump.dumpMethodByMember(method);
                DumpMethodInfo.Builder methodInfobuilder = DumpMethodInfo.newBuilder();
                if(MethodCodeItem == null){
                    methodInfobuilder.setStatus(false);
                    classInfobuilder.addDumpMethodInfo(methodInfobuilder.build());
                }else {
                    String jniSignature = JNISignatureConverter.convertToJNISignature(method);
                    methodInfobuilder.setMethodName(method.getName());
                    methodInfobuilder.setMethodSign(jniSignature);
                    methodInfobuilder.setStatus(true);
                    methodInfobuilder.setContent(ByteString.copyFrom(MethodCodeItem));
                    classInfobuilder.addDumpMethodInfo(methodInfobuilder.build());
                }


            }
            Constructor[] DeclaredConstructors =  cls.getDeclaredConstructors();
            for (Constructor method :DeclaredConstructors ) {
                byte[] MethodCodeItem = dump.dumpMethodByMember(method);
                DumpMethodInfo.Builder methodInfobuilder = DumpMethodInfo.newBuilder();
                String jniSignature = JNISignatureConverter.convertToJNISignature(method);
                methodInfobuilder.setMethodName(method.getName());
                methodInfobuilder.setMethodSign(jniSignature);
                methodInfobuilder.setContent(ByteString.copyFrom(MethodCodeItem));
                classInfobuilder.addDumpMethodInfo(methodInfobuilder.build());
            }
        }else {
            classInfobuilder.setStatus(false);
        }

        responseObserver.onNext(classInfobuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void dumpMethod(DumpMethodString request, StreamObserver<Dexbuff> responseObserver) {
        String clsName = request.getClassName();
        Class<?> cls = AndroidFindClass(clsName);
        String methodName = request.getMethodName();
        String methodSign = request.getMethodSign();
        byte[] method_code_item_buff =  dump.dumpMethodByString(cls,methodName,methodSign);
        Dexbuff buff = Dexbuff.newBuilder().setContent(ByteString.copyFrom(method_code_item_buff)).build();
        responseObserver.onNext(buff);
        responseObserver.onCompleted();
    }
}