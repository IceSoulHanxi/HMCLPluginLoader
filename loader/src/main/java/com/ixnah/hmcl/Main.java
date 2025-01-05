package com.ixnah.hmcl;

import com.ixnah.hmcl.api.LoaderApi;
import com.ixnah.hmcl.i18n.I18nTransformer;
import com.ixnah.hmcl.logging.HmclLoggerAdapter;
import com.ixnah.hmcl.logging.HmclLoggerFactory;
import com.ixnah.hmcl.logging.LoggingTransformer;
import com.ixnah.hmcl.pf4j.HmclTransformer;
import com.ixnah.hmcl.upgrade.UpgradeTransformer;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static com.ixnah.hmcl.api.LoaderApi.HMCL_MAIN_CLASS;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LoaderApi.setArgs(args);
        LoaderApi.registerTransformers(HmclTransformer::new, UpgradeTransformer::new, LoggingTransformer::new, I18nTransformer::new);
        PluginManager pluginManager = LoaderApi.getPluginManager();
        pluginManager.loadPlugins();
        if (pluginManager.getPlugin("HMCL") == null) {
            // TODO: 提示未找到HMCL 尝试下载
        }
        startHmcl();
    }

    private static void startHmcl() {
        try {
            ClassLoader classLoader = LoaderApi.getPluginManager().getPluginClassLoader("HMCL");
            Thread.currentThread().setContextClassLoader(classLoader);
            HmclLoggerFactory.initHmclLogger(new HmclLoggerAdapter(classLoader));
            classLoader.loadClass(HMCL_MAIN_CLASS).getMethod("main", String[].class).invoke(null, (Object) LoaderApi.getArgs());
        } catch (Exception e) {
            if (LoaderApi.getPluginManager().isDevelopment()) {
                try (PrintStream ps = new PrintStream("hmclpl.txt", "UTF-8")) {
                    e.printStackTrace(ps);
                } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
            }
            LOG.error("Can't start HMCL", e);
        } finally {
            LoaderApi.getPluginManager().unloadPlugins();
        }
    }
}
