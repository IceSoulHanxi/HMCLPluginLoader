package com.ixnah.hmcl.api;

import com.ixnah.hmcl.pf4j.HmclPluginManager;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public class LoaderApi {

    private LoaderApi() {
        throw new UnsupportedOperationException();
    }

    public static final String HMCL_MAIN_CLASS = "org.jackhuang.hmcl.Main";
    private static final Logger LOG = LoggerFactory.getLogger(LoaderApi.class);
    private static final Map<String, AsmClassTransformer> TRANSFORMERS = new ConcurrentHashMap<>();
    private static final ThreadLocal<Integer> CLASS_WRITE_FLAGS = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Integer> CLASS_READ_FLAGS = ThreadLocal.withInitial(() -> 0);
    private static final AtomicReference<String[]> ARGS = new AtomicReference<>();
    private static volatile PluginManager PLUGIN_MANAGER;
    private static volatile boolean USE_JAVA_AGENT = false;

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
        CLASS_WRITE_FLAGS.set(flags);
    }

    public static void setClassReadFlags(int flags) {
        CLASS_READ_FLAGS.set(flags);
    }

    public static void setArgs(String... args) {
        ARGS.set(args);
    }

    public static String[] getArgs() {
        String[] args = ARGS.get();
        return args != null ? args : new String[0];
    }

    public static PluginManager getPluginManager() {
        if (PLUGIN_MANAGER == null) {
            synchronized (LoaderApi.class) {
                if (PLUGIN_MANAGER == null) {
                    Path pluginsDir = Paths.get(System.getProperty("hmcl.plugins.dir", ".minecraft/plugins"));
                    PLUGIN_MANAGER = new HmclPluginManager(pluginsDir);
                    try {
                        Files.createDirectories(pluginsDir);
                    } catch (IOException e) {
                        LOG.error("Can't create plugins dir", e);
                    }
                }
            }
        }
        return PLUGIN_MANAGER;
    }

    public static boolean isUseJavaAgent() {
        return USE_JAVA_AGENT;
    }

    public static void setUseJavaAgent(boolean useJavaAgent) {
        USE_JAVA_AGENT = useJavaAgent;
    }

    public static Path initPluginDir(String pluginId) throws IOException {
        requireNonNull(pluginId, "pluginId is null!");
        PluginWrapper plugin = getPluginManager().getPlugin(pluginId);
        requireNonNull(plugin, () -> "plugin [" + pluginId + "] not exist");
        Path resolved = plugin.getPluginPath().resolve("../" + pluginId).toRealPath();
        return Files.createDirectories(resolved);
    }
}
