package com.ixnah.hmcl.pf4j;

import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.PluginDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static com.ixnah.hmcl.api.LoaderApi.HMCL_MAIN_CLASS;

public class HmclManifestPluginDescriptorFinder extends ManifestPluginDescriptorFinder {
    private static final Logger log = LoggerFactory.getLogger(HmclManifestPluginDescriptorFinder.class);
    @Override
    protected PluginDescriptor createPluginDescriptor(Manifest manifest) {
        Attributes attributes = manifest.getMainAttributes();
        if(!Objects.equals(attributes.getValue("Main-Class"), HMCL_MAIN_CLASS)){
            return super.createPluginDescriptor(manifest);
        }
        log.info("Found HMCL plugin");
        HmclPluginDescriptor pluginDescriptor = createPluginDescriptorInstance();
        pluginDescriptor.setPluginId("HMCL");
        pluginDescriptor.setPluginVersion(attributes.getValue("Implementation-Version"));
        pluginDescriptor.setPluginClass("com.ixnah.hmcl.pf4j.HmclPlugin");
        return pluginDescriptor;
    }

    @Override
    protected HmclPluginDescriptor createPluginDescriptorInstance() {
        return new HmclPluginDescriptor();
    }
}
