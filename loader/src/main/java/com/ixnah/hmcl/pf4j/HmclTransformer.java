package com.ixnah.hmcl.pf4j;

import com.ixnah.hmcl.asm.AsmClassTransformer;
import org.objectweb.asm.tree.ClassNode;

import java.security.ProtectionDomain;

public class HmclTransformer implements AsmClassTransformer {
    @Override
    public boolean transform(ClassLoader loader, String className, ProtectionDomain protectionDomain, ClassNode classNode) {
        // TODO: org.jackhuang.hmcl.util.io.JarUtils 设置为正确的jar
        // TODO: org.jackhuang.hmcl.upgrade.UpdateHandler.isNestedApplication 从是否为栈栈底改为判断栈中是否有多个HMCL Main
        // TODO: org.jackhuang.hmcl.upgrade.UpdateHandler.startJava 劫持重启为插件重载
        return false;
    }
}
