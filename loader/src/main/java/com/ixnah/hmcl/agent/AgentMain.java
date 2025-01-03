package com.ixnah.hmcl.agent;

import com.ixnah.hmcl.api.LoaderApi;

import java.lang.instrument.Instrumentation;

public class AgentMain {

    private AgentMain() {
        throw new UnsupportedOperationException();
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        LoaderApi.setUseJavaAgent(true);
        inst.addTransformer(new AgentClassFileTransformer());
    }
}
