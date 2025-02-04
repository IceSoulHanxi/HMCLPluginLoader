package com.ixnah.hmcl.logging;

import com.ixnah.hmcl.api.AsmClassTransformer;
import com.ixnah.hmcl.api.LoaderApi;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.security.ProtectionDomain;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoggingTransformer implements AsmClassTransformer {
    @Override
    public boolean transform(ClassLoader loader, String className, ProtectionDomain protectionDomain, ClassNode classNode) {
        AtomicBoolean modify = new AtomicBoolean();
        if ("org/jackhuang/hmcl/util/logging/CallerFinder".equals(className)) {
            LoaderApi.setClassWriteFlags(ClassWriter.COMPUTE_MAXS);
            if (!classNode.fields.isEmpty()) {
                classNode.methods.stream().filter(m -> m.name.contains("static")).forEach(m -> ignoreCaller(m, modify));
            } else {
                classNode.methods.stream()
                        .filter(m -> "getCaller".equals(m.name) && "()Ljava/lang/String;".equals(m.desc))
                        .findFirst()
                        .ifPresent(m -> ignoreCaller(m, modify));
            }
        }
        return modify.get();
    }

    private static void ignoreCaller(MethodNode methodNode, AtomicBoolean modify) {
        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        for (AbstractInsnNode node = iterator.next(); iterator.hasNext(); node = iterator.next()) {
            if (node.getOpcode() != Opcodes.INVOKEVIRTUAL || !(node instanceof MethodInsnNode)) continue;
            MethodInsnNode startWith = (MethodInsnNode) node;
            if (!"java/lang/String".equals(startWith.owner) || !"startsWith".equals(startWith.name)) {
                continue;
            }
            startWith.setOpcode(Opcodes.INVOKESTATIC);
            startWith.owner = "com/ixnah/hmcl/logging/CallerFinderReplace";
            startWith.name = "checkCaller";
            startWith.desc = "(Ljava/lang/String;Ljava/lang/String;)Z";
            modify.set(true);
            break;
        }
    }
}
