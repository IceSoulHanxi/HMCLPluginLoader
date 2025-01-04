package com.ixnah.hmcl;

import com.ixnah.hmcl.api.LoaderApi;
import com.ixnah.hmcl.logging.LoggingTransformer;
import com.ixnah.hmcl.pf4j.HmclTransformer;
import org.pf4j.PluginManager;

public class Main {
    public static void main(String[] args) {
        LoaderApi.setArgs(args);
        LoaderApi.registerTransformers(HmclTransformer::new, LoggingTransformer::new);
        PluginManager pluginManager = LoaderApi.getPluginManager();
        pluginManager.loadPlugins();
        if (pluginManager.getPlugin("HMCL") == null) {

        }
        pluginManager.startPlugins();
    }
}
