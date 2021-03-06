package com.murengezi.chocolate.Module.Setting;

import com.murengezi.chocolate.Module.Module;

/**
 * @author Tobias Sjöblom
 * Created on 2020-05-07 at 19:04
 */
public class Setting {

    private final String name;
    private final Module parent;
    private final SettingType type;

    public Setting(String name, Module parent, SettingType type) {
        this.name = name;
        this.parent = parent;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Module getParent() {
        return parent;
    }

    public SettingType getType() {
        return type;
    }
}
