package com.ixnah.hmcl.pf4j;

import com.ixnah.hmcl.api.LoaderApi;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class HmclPluginClassLoader extends PluginClassLoader {
    protected static final Map<String, Boolean> SECURE_JAR_MAP = new ConcurrentHashMap<>();
    protected final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    protected final PluginDescriptor pluginDescriptor;

    public HmclPluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent) {
        super(pluginManager, pluginDescriptor, parent);
        this.pluginDescriptor = pluginDescriptor;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> result = classes.get(name);

        if (result != null) {
            return result;
        }

        String path = name.replace('.', '/');
        URL url = this.findResource(path.concat(".class"));
        if (url != null) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                URLConnection connection = url.openConnection();
                connection.connect();
                try (InputStream is = connection.getInputStream()) {
                    byte[] buffer = new byte[1024];
                    while (true) {
                        int read = is.read(buffer);
                        if (read == -1) break;
                        bos.write(buffer, 0, read);
                    }
                }

                if (!LoaderApi.isUseJavaAgent() && bos.size() > 0) {
                    ClassReader reader = new ClassReader(bos.toByteArray());
                    ClassNode node = new ClassNode();
                    reader.accept(node, LoaderApi.getClassReadFlogs());
                    boolean modify = LoaderApi.allTransformer().map(t -> t.transform(this, path, null, node))
                            .reduce(Boolean::logicalOr).orElse(false);
                    if (modify) {
                        ClassWriter writer = new ClassWriter(LoaderApi.getClassWriteFlags());
                        node.accept(writer);
                        LoaderApi.setClassWriteFlags(0);
                        bos.reset();
                        bos.write(writer.toByteArray());
                    }
                }

                if (bos.size() > 0) {
                    tryCreatePackage(name);

                    URL connUrl = connection.getURL();
                    CodeSigner[] signers = null;
                    if (connection instanceof JarURLConnection) {
                        JarURLConnection jarConnection = (JarURLConnection) connection;
                        connUrl = jarConnection.getJarFileURL();
                        if (isSecureJar(jarConnection.getJarFile())) {
                            InputStream inputStream = jarConnection.getInputStream();
                            byte[] buffer = new byte[1024];
                            while (true) {
                                int read = inputStream.read(buffer);
                                if (read == -1) break;
                            }
                            signers = jarConnection.getJarEntry().getCodeSigners();
                        }
                    }
                    result = defineClass(name, bos.toByteArray(), 0, bos.size(), new CodeSource(connUrl, signers));
                }
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }

        if (result == null) {
            result = super.findClass(name);
        }

        classes.put(name, result);
        return result;
    }

    private void tryCreatePackage(String name) {
        int dot = name.lastIndexOf('.');
        if (dot != -1) {
            String pkgName = name.substring(0, dot);
            if (getPackage(pkgName) == null) {
                try {
                    if (pluginDescriptor instanceof HmclPluginDescriptor
                            && ((HmclPluginDescriptor) pluginDescriptor).getManifest() != null) {
                        Manifest manifest = ((HmclPluginDescriptor) pluginDescriptor).getManifest();
                        URL fileUrl = Arrays.stream(this.getURLs()).findFirst().orElse(null);
                        definePackage(pkgName, manifest, fileUrl);
                    } else {
                        definePackage(pkgName, null, null, null, null, null, null, null);
                    }
                } catch (IllegalArgumentException ex) {
                    if (getPackage(pkgName) == null) {
                        throw new IllegalStateException("Cannot find package " + pkgName);
                    }
                }
            }
        }
    }

    private static boolean isSecureJar(JarFile jarFile) {
        return SECURE_JAR_MAP.computeIfAbsent(jarFile.getName(), key ->
                jarFile.stream().anyMatch(it -> {
                    if (it.isDirectory()) return false;
                    String name = it.getName().toUpperCase(Locale.ROOT);
                    return name.startsWith("META-INF") && (name.endsWith(".DSA") ||
                            name.endsWith(".RSA") ||
                            name.endsWith(".EC") ||
                            name.endsWith(".SF"));
                }));
    }
}
