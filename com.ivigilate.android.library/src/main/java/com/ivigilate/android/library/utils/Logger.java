package com.ivigilate.android.library.utils;

import android.util.Log;

import com.ivigilate.android.library.BuildConfig;

public class Logger {
    static String className;
    static String methodName;
    static int lineNumber;

    static boolean isDebugMode = BuildConfig.DEBUG;

    private Logger(){
        /* Protect from instantiations */
    }

    private static String createLog(String message, Object... args) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        buffer.append(methodName);
        buffer.append(":");
        buffer.append(lineNumber);
        buffer.append("] ");
        // If no varargs are supplied, treat it as a request to log the string without formatting.
        buffer.append(args.length == 0 ? message : String.format(message, args));

        return buffer.toString();
    }

    private static void getMethodNames(StackTraceElement[] sElements){
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }

    public static void d(String message, Object... args){
        if (isDebugMode) {
            getMethodNames(new Throwable().getStackTrace());
            Log.d(className, createLog(message, args));
        }
    }

    public static void v(String message, Object... args){
        if (isDebugMode) {
            getMethodNames(new Throwable().getStackTrace());
            Log.v(className, createLog(message, args));
        }
    }

    public static void i(String message, Object... args){
        if (isDebugMode) {
            getMethodNames(new Throwable().getStackTrace());
            Log.i(className, createLog(message, args));
        }
    }

    public static void w(String message, Object... args){
        if (isDebugMode) {
            getMethodNames(new Throwable().getStackTrace());
            Log.w(className, createLog(message, args));
        }
    }

    public static void e(String message, Object... args){
        if (isDebugMode) {
            // Throwable instance must be created before any methods
            getMethodNames(new Throwable().getStackTrace());
            Log.e(className, createLog(message, args));
        }
    }

    public static void wtf(String message, Object... args){
        if (isDebugMode) {
            getMethodNames(new Throwable().getStackTrace());
            Log.wtf(className, createLog(message, args));
        }
    }
}

