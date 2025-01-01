package com.ixnah.hmcl.agent;

import java.lang.instrument.Instrumentation;

public class AgentMain {

    private AgentMain() {
        throw new UnsupportedOperationException();
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new AgentClassFileTransformer());
    }
}
