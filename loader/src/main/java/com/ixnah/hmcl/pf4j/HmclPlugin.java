package com.ixnah.hmcl.pf4j;

import com.ixnah.hmcl.api.LoaderApi;
import com.ixnah.hmcl.logging.HmclLoggerAdapter;
import com.ixnah.hmcl.logging.HmclLoggerFactory;
import org.pf4j.Plugin;

import static com.ixnah.hmcl.api.LoaderApi.HMCL_MAIN_CLASS;

public class HmclPlugin extends Plugin {

    @Override
    public void start() {
        try {
            ClassLoader classLoader = LoaderApi.getPluginManager().getPluginClassLoader("HMCL");
            Thread.currentThread().setContextClassLoader(classLoader);
            HmclLoggerFactory.initHmclLogger(new HmclLoggerAdapter(classLoader));
            classLoader.loadClass(HMCL_MAIN_CLASS).getMethod("main", String[].class).invoke(null, (Object) LoaderApi.getArgs());
            System.out.println("HMCL Plugin Started");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
