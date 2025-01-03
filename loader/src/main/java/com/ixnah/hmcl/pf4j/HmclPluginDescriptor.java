package com.ixnah.hmcl.pf4j;

import org.pf4j.DefaultPluginDescriptor;

import java.util.jar.Manifest;

public class HmclPluginDescriptor extends DefaultPluginDescriptor {

    private Manifest manifest;

    public Manifest getManifest() {
        return manifest;
    }

    public void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }

    @Override
    protected HmclPluginDescriptor setPluginId(String pluginId) {
        return (HmclPluginDescriptor) super.setPluginId(pluginId);
    }

    @Override
    protected HmclPluginDescriptor setPluginDescription(String pluginDescription) {
        return (HmclPluginDescriptor) super.setPluginDescription(pluginDescription);
    }

    @Override
    protected HmclPluginDescriptor setPluginClass(String pluginClassName) {
        return (HmclPluginDescriptor) super.setPluginClass(pluginClassName);
    }

    @Override
    protected HmclPluginDescriptor setPluginVersion(String version) {
        return (HmclPluginDescriptor) super.setPluginVersion(version);
    }

    @Override
    protected HmclPluginDescriptor setProvider(String provider) {
        return (HmclPluginDescriptor) super.setProvider(provider);
    }

    @Override
    protected HmclPluginDescriptor setRequires(String requires) {
        return (HmclPluginDescriptor) super.setRequires(requires);
    }

    @Override
    protected HmclPluginDescriptor setDependencies(String dependencies) {
        return (HmclPluginDescriptor) super.setDependencies(dependencies);
    }

    @Override
    protected HmclPluginDescriptor setDependencies(String... dependencies) {
        return (HmclPluginDescriptor) super.setDependencies(dependencies);
    }

    @Override
    public HmclPluginDescriptor setLicense(String license) {
        return (HmclPluginDescriptor) super.setLicense(license);
    }
}
