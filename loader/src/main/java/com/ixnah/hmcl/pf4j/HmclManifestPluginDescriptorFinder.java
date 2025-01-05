package com.ixnah.hmcl.pf4j;

import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.PluginDescriptor;
import org.pf4j.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static com.ixnah.hmcl.api.LoaderApi.HMCL_MAIN_CLASS;

public class HmclManifestPluginDescriptorFinder extends ManifestPluginDescriptorFinder {
    private static final Logger log = LoggerFactory.getLogger(HmclManifestPluginDescriptorFinder.class);

    @Override
    public boolean isApplicable(Path pluginPath) {
        return Files.exists(pluginPath) && FileUtils.isZipOrJarFile(pluginPath);
    }

    @Override
    protected PluginDescriptor createPluginDescriptor(Manifest manifest) {
        Attributes attributes = manifest.getMainAttributes();
        if(!Objects.equals(attributes.getValue("Main-Class"), HMCL_MAIN_CLASS)){
            return super.createPluginDescriptor(manifest);
        }
        log.info("Found HMCL");
        HmclPluginDescriptor pluginDescriptor = createPluginDescriptorInstance();
        pluginDescriptor.setPluginId("HMCL");
        pluginDescriptor.setPluginVersion(attributes.getValue("Implementation-Version"));
        pluginDescriptor.setPluginClass("com.ixnah.hmcl.pf4j.HmclPlugin");
        pluginDescriptor.setManifest(manifest);
        return pluginDescriptor;
    }

    @Override
    protected HmclPluginDescriptor createPluginDescriptorInstance() {
        return new HmclPluginDescriptor();
    }
}
