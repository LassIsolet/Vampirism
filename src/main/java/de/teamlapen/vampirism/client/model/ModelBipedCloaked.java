package de.teamlapen.vampirism.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBipedCloaked extends ModelBiped {
	
	public ModelBipedCloaked(float f1, float f2, int texWidth, int texHeight) {
		super(f1, f2, texWidth, texHeight);
		
		super.bipedCloak = new ModelRenderer(this, 65, 0);
		super.bipedCloak.addBox(-7.0F, 0.0F, 0.0F, 14, 20, 1);
		super.bipedCloak.setRotationPoint(0, 0, 2);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		
		super.bipedCloak.render(f5);
	}

	
	@Override
	public void setRotationAngles(float f1, float f2, float f3, float f4,
			float f5, float f6, Entity e) {
		super.setRotationAngles(f1, f2, f3, f4, f5, f6, e);
		super.bipedCloak.rotateAngleX = f2;
	}
}
