package com.ixnah.hmcl.pf4j;

import com.ixnah.hmcl.asm.AsmClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.security.ProtectionDomain;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class HmclTransformer implements AsmClassTransformer {
    @Override
    public boolean transform(ClassLoader loader, String className, ProtectionDomain protectionDomain, ClassNode classNode) {
        // TODO: org.jackhuang.hmcl.util.io.JarUtils 设置为正确的jar
        // TODO: org.jackhuang.hmcl.upgrade.UpdateHandler.startJava 劫持重启为插件重载
        AtomicBoolean modify = new AtomicBoolean();
        if ("org/jackhuang/hmcl/upgrade/UpdateHandler".equals(className)) {
            classNode.methods.stream()
                    .filter(m -> "processArguments".equals(m.name) && "([Ljava/lang/String;)Z".equals(m.desc))
                    .findFirst()
                    .ifPresent(m -> isNestedApplication(m, modify));
        } else if ("org/jackhuang/hmcl/Launcher".equals(className)) {
            classNode.methods.stream()
                    .filter(m -> "start".equals(m.name) && "(Ljavafx/stage/Stage;)V".equals(m.desc))
                    .findFirst()
                    .ifPresent(m -> setupContextClassLoader(m, modify));
        }
        return modify.get();
    }

    private static void isNestedApplication(MethodNode methodNode, AtomicBoolean modify) {
        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        for (AbstractInsnNode insn = null; iterator.hasNext(); insn = iterator.next()) {
            if (!(insn instanceof MethodInsnNode)) continue;
            MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
            if (methodInsnNode.getOpcode() != Opcodes.INVOKESTATIC
                    || !"org/jackhuang/hmcl/upgrade/UpdateHandler".equals(methodInsnNode.owner)
                    || !"isNestedApplication".equals(methodInsnNode.name)) {
                continue;
            }
            methodInsnNode.owner = "com/ixnah/hmcl/util/UpdateHandlerReplace";
            modify.set(true);
            break;
        }
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
}
