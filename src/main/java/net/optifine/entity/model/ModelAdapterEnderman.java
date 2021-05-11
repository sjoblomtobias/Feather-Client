package net.optifine.entity.model;

import com.murengezi.minecraft.client.Minecraft;
import com.murengezi.minecraft.client.model.ModelBase;
import com.murengezi.minecraft.client.model.ModelEnderman;
import com.murengezi.minecraft.client.renderer.entity.RenderEnderman;
import com.murengezi.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityEnderman;

public class ModelAdapterEnderman extends ModelAdapterBiped {
   public ModelAdapterEnderman() {
      super(EntityEnderman.class, "enderman", 0.5F);
   }

   public ModelBase makeModel() {
      return new ModelEnderman(0.0F);
   }

   public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize) {
      RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
      RenderEnderman renderenderman = new RenderEnderman(rendermanager);
      renderenderman.mainModel = modelBase;
      renderenderman.shadowSize = shadowSize;
      return renderenderman;
   }
}
