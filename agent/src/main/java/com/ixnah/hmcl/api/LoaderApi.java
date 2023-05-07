package com.ixnah.hmcl.api;

import com.ixnah.hmcl.asm.AsmClassTransformer;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class LoaderApi {

    private LoaderApi() {
        throw new UnsupportedOperationException();
    }

    private static final Map<String, AsmClassTransformer> TRANSFORMERS = new ConcurrentHashMap<>();
    private static final ThreadLocal<Integer> CLASS_WRITE_FLAGS = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Integer> CLASS_READ_FLAGS = ThreadLocal.withInitial(() -> 0);
    private static PluginManager PLUGIN_MANAGER;

    @SafeVarargs
    public static void registerTransformers(Supplier<AsmClassTransformer>... transformerSuppliers) {
        for (Supplier<AsmClassTransformer> transformerSupplier : transformerSuppliers) {
            if (transformerSupplier != null) {
                AsmClassTransformer transformer = transformerSupplier.get();
                if (transformer != null) {
                    TRANSFORMERS.put(transformer.name(), transformer);
                }
            }
        }
    }

    public static void unregisterTransformer(AsmClassTransformer transformer) {
        TRANSFORMERS.remove(transformer.name());
    }

    public static void unregisterTransformer(String transformerName) {
        TRANSFORMERS.remove(transformerName);
    }

    public static void unregisterTransformer(Class<? extends AsmClassTransformer> transformerClass) {
        TRANSFORMERS.values().stream().filter(transformerClass::isInstance).forEach(LoaderApi::unregisterTransformer);
    }

    public static Stream<AsmClassTransformer> allTransformer() {
        return TRANSFORMERS.values().stream().sorted(Comparator.comparingInt(AsmClassTransformer::order));
    }

    public static int getClassWriteFlags() {
        Integer flags = CLASS_WRITE_FLAGS.get();
        return flags != null ? flags : 0;
    }

    public static int getClassReadFlogs() {
        Integer flags = CLASS_READ_FLAGS.get();
        return flags != null ? flags : 0;
    }

    public static void setClassWriteFlags(int flags) {
        CLASS_WRITE_FLAGS.remove();
        CLASS_WRITE_FLAGS.set(flags);
    }

    public static void setClassReadFlags(int flags) {
        CLASS_READ_FLAGS.remove();
        CLASS_READ_FLAGS.set(flags);
    }

    public static PluginManager getPluginManager() {
        if (PLUGIN_MANAGER == null) {
            File mcBaseDir = new File(".minecraft/plugins"); // TODO: 读配置获取hmcl默认游戏路径
            PLUGIN_MANAGER = new DefaultPluginManager(mcBaseDir.toPath());
        }
        return PLUGIN_MANAGER;
    }

    private static Logger hmclLogger;

    public static Logger getHmclLogger() {
        if (hmclLogger == null) {
            try {
                Field hmclLoggerField = Class.forName("org.jackhuang.hmcl.util.Logging").getField("LOG");
                hmclLogger = (Logger) hmclLoggerField.get(null);
            } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
                e.printStackTrace();
                hmclLogger = Logger.getLogger("HMCL");
            }
        }
        return hmclLogger;
    }
}
