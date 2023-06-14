package com.ixnah.hmcl.api;

import com.ixnah.hmcl.asm.AsmClassTransformer;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public class LoaderApi {

    private LoaderApi() {
        throw new UnsupportedOperationException();
    }

    private static final Map<String, AsmClassTransformer> TRANSFORMERS = new ConcurrentHashMap<>();
    private static final ThreadLocal<Integer> CLASS_WRITE_FLAGS = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Integer> CLASS_READ_FLAGS = ThreadLocal.withInitial(() -> 0);
    private static PluginManager PLUGIN_MANAGER;
    private static File LOADER_FILE;
    private static File PLUGINS_DIR;

    @SafeVarargs
    public static void registerTransformers(Supplier<AsmClassTransformer>... transformerSuppliers) {
        requireNonNull(transformerSuppliers, "transformer suppliers is null!");
        for (Supplier<AsmClassTransformer> transformerSupplier : transformerSuppliers) {
            requireNonNull(transformerSupplier, "transformer supplier is null!");
            AsmClassTransformer transformer = requireNonNull(transformerSupplier.get(), "transformer is null!");
            TRANSFORMERS.put(transformer.name(), transformer);
        }
    }

    public static void unregisterTransformer(AsmClassTransformer transformer) {
        requireNonNull(transformer, "remove transformer is null!");
        TRANSFORMERS.remove(transformer.name());
    }

    public static void unregisterTransformer(String transformerName) {
        requireNonNull(transformerName, "remove transformer name is null!");
        TRANSFORMERS.remove(transformerName);
    }

    public static void unregisterTransformer(Class<? extends AsmClassTransformer> transformerClass) {
        requireNonNull(transformerClass, "remove transformer class is null");
        TRANSFORMERS.values().removeIf(transformerClass::isInstance);
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

    public static File getLoaderFile() {
        if (LOADER_FILE == null) {
            String loaderLocation = LoaderApi.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            try {
                LOADER_FILE = new File(URLDecoder.decode(loaderLocation, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return LOADER_FILE;
    }

    public static File getPluginsDir() {
        if (PLUGINS_DIR == null) {
            PLUGINS_DIR = new File(getLoaderFile().getParentFile(), "plugins");
        }
        return PLUGINS_DIR;
    }

    public static PluginManager getPluginManager() {
        if (PLUGIN_MANAGER == null) {
            PLUGIN_MANAGER = new DefaultPluginManager(getPluginsDir().toPath());
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
