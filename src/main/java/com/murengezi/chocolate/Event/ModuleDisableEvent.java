package com.murengezi.chocolate.Event;

import com.darkmagician6.eventapi.events.Event;
import com.murengezi.chocolate.Module.Module;

/**
 * @author Tobias Sjöblom
 * Created on 2021-01-09 at 23:56
 */
public class ModuleDisableEvent implements Event {

    private final Module module;
    private final boolean save;

    public ModuleDisableEvent(Module module, boolean save) {
        this.module = module;
        this.save = save;
    }

    public Module getModule() {
        return module;
    }

    public boolean doSave() {
        return save;
    }
}
