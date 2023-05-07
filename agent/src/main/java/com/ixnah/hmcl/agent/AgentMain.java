package com.ixnah.hmcl.agent;

import com.ixnah.hmcl.api.LoaderApi;
import com.ixnah.hmcl.pf4j.Pf4jInjectTransformer;

import java.lang.instrument.Instrumentation;

public class AgentMain {

    private AgentMain() {
        throw new UnsupportedOperationException();
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        LoaderApi.registerTransformers(Pf4jInjectTransformer::new);
        inst.addTransformer(new AgentClassFileTransformer());
    }
}
