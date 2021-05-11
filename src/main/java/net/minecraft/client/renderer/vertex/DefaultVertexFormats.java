package net.minecraft.client.renderer.vertex;

import net.optifine.config.Config;
import net.optifine.reflect.ReflectorClass;
import net.optifine.reflect.ReflectorField;
import net.optifine.shaders.SVertexFormat;

import java.lang.reflect.Field;

public class DefaultVertexFormats {

	public static VertexFormat BLOCK = new VertexFormat(), ITEM = new VertexFormat();
	private static final VertexFormat BLOCK_VANILLA = BLOCK, ITEM_VANILLA = ITEM;
	public static ReflectorClass Attributes = new ReflectorClass("net.minecraftforge.client.model.Attributes");
	public static ReflectorField Attributes_DEFAULT_BAKED_FORMAT = new ReflectorField(Attributes, "DEFAULT_BAKED_FORMAT");
	private static final VertexFormat FORGE_BAKED = SVertexFormat.duplicate((VertexFormat) getFieldValue(Attributes_DEFAULT_BAKED_FORMAT));
	public static final VertexFormat OLDMODEL_POSITION_TEX_NORMAL = new VertexFormat(), PARTICLE_POSITION_TEX_COLOR_LMAP = new VertexFormat(), POSITION = new VertexFormat(), POSITION_COLOR = new VertexFormat(), POSITION_TEX = new VertexFormat(), POSITION_NORMAL = new VertexFormat(), POSITION_TEX_COLOR = new VertexFormat(), POSITION_TEX_NORMAL = new VertexFormat(), POSITION_TEX_LMAP_COLOR = new VertexFormat(), POSITION_TEX_COLOR_NORMAL = new VertexFormat();
	public static final VertexFormatElement POSITION_3F = new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3), COLOR_4UB = new VertexFormatElement(0, VertexFormatElement.EnumType.UBYTE, VertexFormatElement.EnumUsage.COLOR, 4), TEX_2F = new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.UV, 2), TEX_2S = new VertexFormatElement(1, VertexFormatElement.EnumType.SHORT, VertexFormatElement.EnumUsage.UV, 2), NORMAL_3B = new VertexFormatElement(0, VertexFormatElement.EnumType.BYTE, VertexFormatElement.EnumUsage.NORMAL, 3), PADDING_1B = new VertexFormatElement(0, VertexFormatElement.EnumType.BYTE, VertexFormatElement.EnumUsage.PADDING, 1);

	public static void updateVertexFormats() {
		if (Config.isShaders()) {
			BLOCK = SVertexFormat.makeDefVertexFormatBlock();
			ITEM = SVertexFormat.makeDefVertexFormatItem();

			if (Attributes_DEFAULT_BAKED_FORMAT.exists()) {
				SVertexFormat.setDefBakedFormat((VertexFormat) Attributes_DEFAULT_BAKED_FORMAT.getValue());
			}
		} else {
			BLOCK = BLOCK_VANILLA;
			ITEM = ITEM_VANILLA;

			if (Attributes_DEFAULT_BAKED_FORMAT.exists()) {
				SVertexFormat.copy(FORGE_BAKED, (VertexFormat) Attributes_DEFAULT_BAKED_FORMAT.getValue());
			}
		}
	}

	public static Object getFieldValue(ReflectorField p_getFieldValue_0_) {
		try {
			Field field = p_getFieldValue_0_.getTargetField();

			if (field == null) {
				return null;
			} else {
				return field.get(null);
			}
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			return null;
		}
	}

	static {
		BLOCK.func_181721_a(POSITION_3F);
		BLOCK.func_181721_a(COLOR_4UB);
		BLOCK.func_181721_a(TEX_2F);
		BLOCK.func_181721_a(TEX_2S);
		ITEM.func_181721_a(POSITION_3F);
		ITEM.func_181721_a(COLOR_4UB);
		ITEM.func_181721_a(TEX_2F);
		ITEM.func_181721_a(NORMAL_3B);
		ITEM.func_181721_a(PADDING_1B);
		OLDMODEL_POSITION_TEX_NORMAL.func_181721_a(POSITION_3F);
		OLDMODEL_POSITION_TEX_NORMAL.func_181721_a(TEX_2F);
		OLDMODEL_POSITION_TEX_NORMAL.func_181721_a(NORMAL_3B);
		OLDMODEL_POSITION_TEX_NORMAL.func_181721_a(PADDING_1B);
		PARTICLE_POSITION_TEX_COLOR_LMAP.func_181721_a(POSITION_3F);
		PARTICLE_POSITION_TEX_COLOR_LMAP.func_181721_a(TEX_2F);
		PARTICLE_POSITION_TEX_COLOR_LMAP.func_181721_a(COLOR_4UB);
		PARTICLE_POSITION_TEX_COLOR_LMAP.func_181721_a(TEX_2S);
		POSITION.func_181721_a(POSITION_3F);
		POSITION_COLOR.func_181721_a(POSITION_3F);
		POSITION_COLOR.func_181721_a(COLOR_4UB);
		POSITION_TEX.func_181721_a(POSITION_3F);
		POSITION_TEX.func_181721_a(TEX_2F);
		POSITION_NORMAL.func_181721_a(POSITION_3F);
		POSITION_NORMAL.func_181721_a(NORMAL_3B);
		POSITION_NORMAL.func_181721_a(PADDING_1B);
		POSITION_TEX_COLOR.func_181721_a(POSITION_3F);
		POSITION_TEX_COLOR.func_181721_a(TEX_2F);
		POSITION_TEX_COLOR.func_181721_a(COLOR_4UB);
		POSITION_TEX_NORMAL.func_181721_a(POSITION_3F);
		POSITION_TEX_NORMAL.func_181721_a(TEX_2F);
		POSITION_TEX_NORMAL.func_181721_a(NORMAL_3B);
		POSITION_TEX_NORMAL.func_181721_a(PADDING_1B);
		POSITION_TEX_LMAP_COLOR.func_181721_a(POSITION_3F);
		POSITION_TEX_LMAP_COLOR.func_181721_a(TEX_2F);
		POSITION_TEX_LMAP_COLOR.func_181721_a(TEX_2S);
		POSITION_TEX_LMAP_COLOR.func_181721_a(COLOR_4UB);
		POSITION_TEX_COLOR_NORMAL.func_181721_a(POSITION_3F);
		POSITION_TEX_COLOR_NORMAL.func_181721_a(TEX_2F);
		POSITION_TEX_COLOR_NORMAL.func_181721_a(COLOR_4UB);
		POSITION_TEX_COLOR_NORMAL.func_181721_a(NORMAL_3B);
		POSITION_TEX_COLOR_NORMAL.func_181721_a(PADDING_1B);
	}

}
