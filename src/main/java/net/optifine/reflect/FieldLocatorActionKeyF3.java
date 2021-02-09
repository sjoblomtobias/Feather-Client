package net.optifine.reflect;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Config;

public class FieldLocatorActionKeyF3 implements IFieldLocator {
   public Field getField() {
      Class oclass = Minecraft.class;
      Field field = this.getFieldRenderChunksMany();
      if(field == null) {
         Config.log("(Reflector) Field not present: " + oclass.getName() + ".actionKeyF3 (field renderChunksMany not found)");
         return null;
      } else {
         Field field1 = ReflectorRaw.getFieldAfter(Minecraft.class, field, Boolean.TYPE, 0);
         if(field1 == null) {
            Config.log("(Reflector) Field not present: " + oclass.getName() + ".actionKeyF3");
            return null;
         } else {
            return field1;
         }
      }
   }

   private Field getFieldRenderChunksMany() {
      Minecraft minecraft = Minecraft.getMinecraft();
      boolean flag = minecraft.renderChunkMany;
      Field[] afield = Minecraft.class.getDeclaredFields();
      minecraft.renderChunkMany = true;
      Field[] afield1 = ReflectorRaw.getFields(minecraft, afield, Boolean.TYPE, Boolean.TRUE);
      minecraft.renderChunkMany = false;
      Field[] afield2 = ReflectorRaw.getFields(minecraft, afield, Boolean.TYPE, Boolean.FALSE);
      minecraft.renderChunkMany = flag;
      Set<Field> set = new HashSet(Arrays.asList(afield1));
      Set<Field> set1 = new HashSet(Arrays.asList(afield2));
      Set<Field> set2 = new HashSet(set);
      set2.retainAll(set1);
      Field[] afield3 = set2.toArray(new Field[set2.size()]);
      return afield3.length != 1?null:afield3[0];
   }
}
