package com.ctg.innovic.cmmpdi.dirsync.utils;

import java.lang.StackWalker.StackFrame;

public class LogUtils {

    public static String getCurrentClassName() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .getCallerClass()
                .getName();
    }


    public static String getCurrentMethodName() {
        return StackWalker.getInstance()
                .walk(frames -> frames
                        .skip(1) // Skip 'getCurrentMethodName' frame itself
                        .findFirst()
                        .map(StackFrame::getMethodName)
                        .orElse("unknown"));
    }

}