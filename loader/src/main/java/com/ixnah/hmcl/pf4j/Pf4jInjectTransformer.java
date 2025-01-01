package com.ixnah.hmcl.pf4j;

import com.ixnah.hmcl.asm.AsmClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicBoolean;

public class Pf4jInjectTransformer implements AsmClassTransformer {
    @Override
    public boolean transform(ClassLoader loader, String className, ProtectionDomain protectionDomain, ClassNode classNode) {
        AtomicBoolean modify = new AtomicBoolean();
        if ("org/jackhuang/hmcl/task/Schedulers".equals(className)) {
            classNode.methods.stream()
                    .filter(m -> "shutdown".equals(m.name) && "()V".equals(m.desc))
                    .findFirst()
                    .ifPresent(m -> disablePf4j(m, modify));
        }
        return modify.get();
    }

    private static void disablePf4j(MethodNode methodNode, AtomicBoolean modify) {
        InsnList insnList = new InsnList();
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/ixnah/hmcl/api/LoaderApi", "getPluginManager", "()Lorg/pf4j/PluginManager;"));
        insnList.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "org/pf4j/PluginManager", "stopPlugins", "()V"));
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/ixnah/hmcl/api/LoaderApi", "getPluginManager", "()Lorg/pf4j/PluginManager;"));
        insnList.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "org/pf4j/PluginManager", "unloadPlugins", "()V"));
        methodNode.instructions.insert(insnList);
        modify.set(true);
    }
}
