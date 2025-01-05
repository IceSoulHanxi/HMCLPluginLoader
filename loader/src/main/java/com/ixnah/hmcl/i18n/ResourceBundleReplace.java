package com.ixnah.hmcl.i18n;

import com.ixnah.hmcl.api.LoaderApi;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;

import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class ResourceBundleReplace {

    public static ResourceBundle getBundle(String baseName, Locale locale) {
        return getBundle(baseName, locale, null);
    }

    public static ResourceBundle getBundle(String baseName, Locale targetLocale, ResourceBundle.Control control) {
        PluginResourceBundle resourceBundle = new PluginResourceBundle();
        PluginManager pluginManager = LoaderApi.getPluginManager();
        PluginWrapper hmcl = Objects.requireNonNull(pluginManager.getPlugin("HMCL"));
        resourceBundle.addBundle("HMCL", control != null
                ? ResourceBundle.getBundle(baseName, targetLocale, hmcl.getPluginClassLoader(), control)
                : ResourceBundle.getBundle(baseName, targetLocale, hmcl.getPluginClassLoader()));
        for (PluginWrapper plugin : pluginManager.getResolvedPlugins()) {
            if ("HMCL".equals(plugin.getPluginId())) continue;
            try {
                resourceBundle.addBundle(plugin.getPluginId(), control != null
                        ? ResourceBundle.getBundle(baseName, targetLocale, plugin.getPluginClassLoader(), control)
                        : ResourceBundle.getBundle(baseName, targetLocale, plugin.getPluginClassLoader()));
            } catch (Exception ignored) {
            }
        }
        return resourceBundle;
    }
}
