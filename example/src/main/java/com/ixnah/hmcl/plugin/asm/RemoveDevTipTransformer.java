package com.ixnah.hmcl.plugin.asm;

import com.ixnah.hmcl.api.LoaderApi;
import com.ixnah.hmcl.asm.AsmClassTransformer;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.security.ProtectionDomain;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class RemoveDevTipTransformer implements AsmClassTransformer {

    @Override
    public boolean transform(ClassLoader loader, String className, ProtectionDomain protectionDomain, ClassNode classNode) {
        AtomicBoolean modify = new AtomicBoolean();
        if ("org/jackhuang/hmcl/ui/main/MainPage".equals(className)) {
            classNode.methods.stream()
                    .filter(m -> "<init>".equals(m.name) && "()V".equals(m.desc))
                    .findFirst()
                    .ifPresent(m -> removeDevTip(m, modify));
        }
        return modify.get();
    }

    private static void removeDevTip(MethodNode methodNode, AtomicBoolean modify) {
        boolean replace = false;
        for (AbstractInsnNode instruction : methodNode.instructions) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
                if ("org/jackhuang/hmcl/Metadata".equals(methodInsnNode.owner)
                        && "isNightly".equals(methodInsnNode.name)
                        && "()Z".equals(methodInsnNode.desc)
                        && !replace) {
                    replace = true;
                } else if ("javafx/collections/ObservableList".equals(methodInsnNode.owner)
                        && "add".equals(methodInsnNode.name)
                        && "(Ljava/lang/Object;)Z".equals(methodInsnNode.desc)
                        && replace) {
                    methodInsnNode.setOpcode(Opcodes.INVOKESTATIC);
                    methodInsnNode.owner = "java/util/Objects";
                    methodInsnNode.name = "equals";
                    methodInsnNode.desc = "(Ljava/lang/Object;Ljava/lang/Object;)Z";
                    methodInsnNode.itf = false;
                    modify.set(true);
                }
            } else if (instruction instanceof TypeInsnNode) {
                TypeInsnNode typeInsnNode = (TypeInsnNode) instruction;
                if (typeInsnNode.getOpcode() == Opcodes.NEW
                        && "javafx/scene/layout/StackPane".equals(typeInsnNode.desc)
                        && replace) {
                    replace = false;
                }
            }
        }
    }
}
