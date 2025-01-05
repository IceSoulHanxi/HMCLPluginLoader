package com.ixnah.hmcl.api;

import org.objectweb.asm.tree.ClassNode;

import java.security.ProtectionDomain;

public interface AsmClassTransformer {

    default String name() {
        return this.getClass().getSimpleName();
    }

    default int order() {
        return 100;
    }

    boolean transform(ClassLoader loader, String className, ProtectionDomain protectionDomain, ClassNode classNode);
}
