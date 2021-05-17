package com.murengezi.minecraft.client.renderer.entity.layers;

import com.murengezi.minecraft.client.model.ModelZombieVillager;
import com.murengezi.minecraft.client.renderer.entity.RendererLivingEntity;

public class LayerVillagerArmor extends LayerBipedArmor {

    public LayerVillagerArmor(RendererLivingEntity<?> renderer) {
        super(renderer);
    }

    protected void initArmor() {
        this.field_177189_c = new ModelZombieVillager(0.5F, 0.0F, true);
        this.field_177186_d = new ModelZombieVillager(1.0F, 0.0F, true);
    }

}