package com.ixnah.hmcl.upgrade;

import com.ixnah.hmcl.api.AsmClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.security.ProtectionDomain;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpgradeTransformer implements AsmClassTransformer {
    @Override
    public boolean transform(ClassLoader loader, String className, ProtectionDomain protectionDomain, ClassNode classNode) {
        // TODO: org.jackhuang.hmcl.upgrade.UpdateHandler.startJava 劫持重启为插件重载
        AtomicBoolean modify = new AtomicBoolean();
        if ("org/jackhuang/hmcl/upgrade/UpdateHandler".equals(className)) {
            classNode.methods.stream()
                    .filter(m -> "processArguments".equals(m.name) && "([Ljava/lang/String;)Z".equals(m.desc))
                    .findFirst()
                    .ifPresent(m -> isNestedApplication(m, modify));
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
            methodInsnNode.owner = "com/ixnah/hmcl/upgrade/UpdateHandlerReplace";
            modify.set(true);
            break;
        }
    }
}
