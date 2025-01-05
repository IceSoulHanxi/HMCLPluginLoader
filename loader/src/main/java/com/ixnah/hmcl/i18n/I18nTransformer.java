package com.ixnah.hmcl.i18n;

import com.ixnah.hmcl.api.AsmClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.security.ProtectionDomain;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class I18nTransformer implements AsmClassTransformer {
    @Override
    public boolean transform(ClassLoader loader, String className, ProtectionDomain protectionDomain, ClassNode classNode) {
        AtomicBoolean modify = new AtomicBoolean();
        if ("org/jackhuang/hmcl/util/i18n/Locales$SupportedLocale".equals(className)) {
            classNode.methods.stream()
                    .filter(m -> "getResourceBundle".equals(m.name) && "()Ljava/util/ResourceBundle;".equals(m.desc))
                    .findFirst()
                    .ifPresent(m -> loadPluginI18n(m, modify));
        }
        return modify.get();
    }

    private static void loadPluginI18n(MethodNode methodNode, AtomicBoolean modify) {
        // INVOKESTATIC java/util/ResourceBundle.getBundle(Ljava/lang/String;Ljava/util/Locale;)Ljava/util/ResourceBundle;
        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        for (AbstractInsnNode insn = null; iterator.hasNext(); insn = iterator.next()) {
            if (!(insn instanceof MethodInsnNode)) continue;
            MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
            if (methodInsnNode.getOpcode() != Opcodes.INVOKESTATIC
                    || !"java/util/ResourceBundle".equals(methodInsnNode.owner)
                    || !"getBundle".equals(methodInsnNode.name)){
                continue;
            }
            methodInsnNode.owner = "com/ixnah/hmcl/i18n/ResourceBundleReplace";
            modify.set(true);
        }
    }
}
