package com.ixnah.hmcl.pf4j;

import org.pf4j.*;

import java.nio.file.Path;
import java.util.List;

public class HmclPluginManager extends DefaultPluginManager {

    public HmclPluginManager() {
        super();
    }

    public HmclPluginManager(Path... pluginsRoots) {
        super(pluginsRoots);
    }

    public HmclPluginManager(List<Path> pluginsRoots) {
        super(pluginsRoots);
    }

    @Override
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
        return new CompoundPluginDescriptorFinder()
                .add(new PropertiesPluginDescriptorFinder())
                .add(new HmclManifestPluginDescriptorFinder());
    }

}
