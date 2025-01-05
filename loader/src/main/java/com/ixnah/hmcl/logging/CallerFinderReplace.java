package com.ixnah.hmcl.logging;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CallerFinderReplace {

    private static final List<String> IGNORE_PACKAGES = new CopyOnWriteArrayList<>();

    static {
        IGNORE_PACKAGES.add(CallerFinderReplace.class.getPackage().getName() + ".");
        IGNORE_PACKAGES.add("org.slf4j.helpers.");
    }

    public static boolean checkCaller(String className, String ignorePackageName) {
        return className.startsWith(ignorePackageName) || IGNORE_PACKAGES.stream().anyMatch(className::startsWith);
    }
}
