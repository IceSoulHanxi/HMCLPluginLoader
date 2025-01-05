package com.ixnah.hmcl.i18n;

import com.ixnah.hmcl.api.LoaderApi;
import org.pf4j.PluginWrapper;

import java.util.*;

public class PluginResourceBundle extends ResourceBundle {

    private final Map<String, ResourceBundle> bundleMap = new LinkedHashMap<>();

    public void addBundle(String pluginId, ResourceBundle bundle) {
        bundleMap.put(pluginId, bundle);
    }

    public ResourceBundle getPluginBundle(String pluginId) {
        return bundleMap.get(pluginId);
    }

    public void removeBundle(String pluginId) {
        bundleMap.remove(pluginId);
    }

    public Set<String> getPluginIds() {
        return bundleMap.keySet();
    }

    @Override
    protected Object handleGetObject(String key) {
        try {
            return bundleMap.get("HMCL").getObject(key);
        } catch (Throwable ignored) {
            for (PluginWrapper plugin : LoaderApi.getPluginManager().getResolvedPlugins()) {
                if ("HMCL".equals(plugin.getPluginId())) continue;
                try {
                    ResourceBundle bundle = bundleMap.get(plugin.getPluginId());
                    if (bundle != null) return bundle.getObject(key);
                } catch (Throwable ignored1) {
                }
            }
        }
        return null;
    }

    @Override
    public Enumeration<String> getKeys() {
        Iterator<Enumeration<String>> iterator = bundleMap.values().stream().map(ResourceBundle::getKeys).iterator();
        return new Enumeration<String>() {
            private Enumeration<String> keyEnumeration = null;

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext() || (keyEnumeration != null && keyEnumeration.hasMoreElements());
            }

            @Override
            public String nextElement() {
                if ((keyEnumeration == null || !keyEnumeration.hasMoreElements()) && iterator.hasNext()) {
                    keyEnumeration = iterator.next();
                }
                if (keyEnumeration != null && keyEnumeration.hasMoreElements()) {
                    return keyEnumeration.nextElement();
                }
                throw new NoSuchElementException();
            }
        };
    }
}
