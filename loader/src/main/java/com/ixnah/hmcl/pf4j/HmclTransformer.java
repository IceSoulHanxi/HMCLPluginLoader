package com.ixnah.hmcl.pf4j;

import com.ixnah.hmcl.api.AsmClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.security.ProtectionDomain;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class HmclTransformer implements AsmClassTransformer {
    @Override
    public boolean transform(ClassLoader loader, String className, ProtectionDomain protectionDomain, ClassNode classNode) {
        // TODO: org.jackhuang.hmcl.util.io.JarUtils 设置为正确的jar
        AtomicBoolean modify = new AtomicBoolean();
        if ("org/jackhuang/hmcl/Launcher".equals(className)) {
            classNode.methods.stream()
                    .filter(m -> "start".equals(m.name) && "(Ljavafx/stage/Stage;)V".equals(m.desc))
                    .findFirst()
                    .ifPresent(m -> setupContextClassLoader(m, modify));
        } else if ("org/jackhuang/hmcl/ui/Controllers".equals(className)) {
            classNode.methods.forEach(m -> {
                if ("initialize".equals(m.name) && "(Ljavafx/stage/Stage;)V".equals(m.desc)) {
                    onStart(m, modify);
                } else if ("shutdown".equals(m.name) && "()V".equals(m.desc)) {
                    onShutdown(m, modify);
                }
            });
        }
        return modify.get();
    }

    private static void setupContextClassLoader(MethodNode methodNode, AtomicBoolean modify) {
        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        for (AbstractInsnNode insn = null; iterator.hasNext(); insn = iterator.next()) {
            if (!(insn instanceof MethodInsnNode)) continue;
            MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
            if (methodInsnNode.getOpcode() != Opcodes.INVOKESTATIC
                    || !"java/lang/Thread".equals(methodInsnNode.owner)
                    || !"currentThread".equals(methodInsnNode.name)) {
                continue;
            }
            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
            iterator.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
            iterator.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false));
            iterator.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Thread", "setContextClassLoader", "(Ljava/lang/ClassLoader;)V", false));
            iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC, methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc, false));
            modify.set(true);
            break;
        }
    }

    private static void onStart(MethodNode methodNode, AtomicBoolean modify) {
        InsnList insnList = new InsnList();
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/ixnah/hmcl/api/LoaderApi", "getPluginManager", "()Lorg/pf4j/PluginManager;"));
        insnList.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "org/pf4j/PluginManager", "startPlugins", "()V"));
        methodNode.instructions.insert(insnList);
        modify.set(true);
    }

    private static void onShutdown(MethodNode methodNode, AtomicBoolean modify) {
        InsnList insnList = new InsnList();
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/ixnah/hmcl/api/LoaderApi", "getPluginManager", "()Lorg/pf4j/PluginManager;"));
        insnList.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "org/pf4j/PluginManager", "stopPlugins", "()V"));
        methodNode.instructions.insert(insnList);
        modify.set(true);
    }
}
