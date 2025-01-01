package com.ixnah.hmcl.logging;

import com.ixnah.hmcl.asm.AsmClassTransformer;
import org.objectweb.asm.tree.*;

import java.security.ProtectionDomain;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoggingTransformer implements AsmClassTransformer {
    @Override
    public boolean transform(ClassLoader loader, String className, ProtectionDomain protectionDomain, ClassNode classNode) {
        AtomicBoolean modify = new AtomicBoolean();
        if ("org/jackhuang/hmcl/util/logging/CallerFinder".equals(className)) {
            classNode.methods.stream()
                    .filter(m -> "getCaller".equals(m.name) && "()Ljava/lang/String;".equals(m.desc))
                    .findFirst()
                    .ifPresent(m -> ignoreCaller(m, modify));
        }
        return modify.get();
    }

    private static void ignoreCaller(MethodNode methodNode, AtomicBoolean modify) {
        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        for (AbstractInsnNode node = null; iterator.hasNext(); node = iterator.next()) {
            if (!(node instanceof LdcInsnNode)) continue;
            LdcInsnNode ldc = (LdcInsnNode) node;
            if (!"java.lang.reflect.".equals(ldc.cst)) continue;
            VarInsnNode aload3 = (VarInsnNode) ldc.getPrevious();
            MethodInsnNode startWith = (MethodInsnNode) ldc.getNext();
            JumpInsnNode ifne = (JumpInsnNode) startWith.getNext();
            iterator.add(new LdcInsnNode("com.ixnah.hmcl.logging."));
            iterator.add(new VarInsnNode(aload3.getOpcode(), aload3.var));
            iterator.add(new JumpInsnNode(ifne.getOpcode(), ifne.label));
            iterator.add(new MethodInsnNode(startWith.getOpcode(), startWith.owner, startWith.name, startWith.desc, false));
            modify.set(true);
            break;
        }
    }
}
