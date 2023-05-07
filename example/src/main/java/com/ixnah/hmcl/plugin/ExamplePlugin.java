package com.ixnah.hmcl.plugin;

import com.ixnah.hmcl.api.LoaderApi;
import com.ixnah.hmcl.plugin.asm.RemoveDevTipTransformer;
import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExamplePlugin extends Plugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExamplePlugin.class);

    @Override
    public void start() {
        LOGGER.info("ExamplePlugin start");
        LoaderApi.registerTransformers(RemoveDevTipTransformer::new);
    }
}
