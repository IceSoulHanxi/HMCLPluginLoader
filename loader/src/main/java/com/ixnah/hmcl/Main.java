package com.ixnah.hmcl;

import com.ixnah.hmcl.api.LoaderApi;
import com.ixnah.hmcl.logging.LoggingTransformer;
import com.ixnah.hmcl.pf4j.Pf4jInjectTransformer;
import org.pf4j.PluginManager;

public class Main {
    public static void main(String[] args) {
        LoaderApi.setArgs(args);
        LoaderApi.registerTransformers(Pf4jInjectTransformer::new, LoggingTransformer::new);
        PluginManager pluginManager = LoaderApi.getPluginManager();
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
    }
}
