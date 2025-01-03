package com.ixnah.hmcl.util;

public class UpdateHandlerReplace {

    public static boolean isNestedApplication() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();

        String mainClassName = "org.jackhuang.hmcl.Main";
        int j = 0;
        for (StackTraceElement element : stacktrace) {
            if (mainClassName.equals(element.getClassName()) && "main".equals(element.getMethodName())) {
                j++;
            }
        }

        return j > 2;
    }
}
