package com.opensource.leo.localtask.init;


import com.opensource.leo.localtask.entrance.Annotationer;
import com.opensource.leo.localtask.entrance.TaskConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leo.lx on 4/8/16.
 */
public class InitorInitializer {
    public List<Initor> init() throws IOException, IllegalAccessException, InstantiationException {
        List<Initor> initors = new ArrayList<Initor>();
        // begin task from project
        List<Class> clazzs = Annotationer.findClass(Initor.class, PersonalInitor.class, TaskConfig.WORK_PACKAGE_DIR);
        for (Class clazz : clazzs) {
            initInitor(clazz, initors);
        }
        return initors;
    }

    private void initInitor(Class clazz, List<Initor> initors) throws IllegalAccessException, InstantiationException {
        Object initor = clazz.newInstance();
        if (initor != null && initor instanceof Initor)
            initors.add((Initor) initor);
    }
}
