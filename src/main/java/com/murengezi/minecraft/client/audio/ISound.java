package com.murengezi.minecraft.client.audio;

import net.minecraft.util.ResourceLocation;

public interface ISound {

    ResourceLocation getSoundLocation();

    boolean canRepeat();

    int getRepeatDelay();

    float getVolume();

    float getPitch();

    float getXPosF();

    float getYPosF();

    float getZPosF();

    ISound.AttenuationType getAttenuationType();

    enum AttenuationType {
        NONE(0), LINEAR(2);

        private final int type;

        AttenuationType(int type) {
            this.type = type;
        }

        public int getTypeInt() {
            return this.type;
        }
    }

}