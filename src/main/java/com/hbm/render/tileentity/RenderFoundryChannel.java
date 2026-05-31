package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.machine.FoundryChannel;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.tileentity.machine.TileEntityFoundryChannel;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;


@AutoRegister
public class RenderFoundryChannel extends TileEntitySpecialRenderer<TileEntityFoundryChannel> {
    public static final ResourceLocation LAVA_TEXTURE = new ResourceLocation(Tags.MODID, "textures/models/machines/lava_gray.png");

    @Override
    public void render(TileEntityFoundryChannel tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        BlockPos pos = tile.getPos();
        if (getWorld().isAirBlock(pos) || !tile.hasWorld()) {
            return;
        }

        World world = tile.getWorld();
        FoundryChannel channel = (FoundryChannel) tile.getBlockType();

		boolean doRender = (tile.amount > 0 || tile.renderAmount > 0) && (tile.type != null || tile.renderType != null);
		if (!doRender) {
			return;
		}

		NTMMaterial mat = tile.renderType != null ? tile.renderType : tile.type;
		int hex = mat.moltenColor;
        double brightener = 0.7D;
        int rComp = (hex >> 16) & 0xFF;
        int gComp = (hex >> 8) & 0xFF;
        int bComp = hex & 0xFF;
        rComp = Math.min((int) (rComp / 0.7D), 255);
        gComp = Math.min((int) (gComp / 0.7D), 255);
        bComp = Math.min((int) (bComp / 0.7D), 255);
        float nr = (float) (255D - (255D - rComp) * brightener) / 255F;
        float ng = (float) (255D - (255D - gComp) * brightener) / 255F;
        float nb = (float) (255D - (255D - bComp) * brightener) / 255F;

		double maxLevel = 0.25D;
		int capacity = tile.getCapacity();
		if(capacity <= 0) capacity = 1;

		double fraction = (double) tile.renderAmount / capacity;
		double prevFrac = (double) tile.prevRenderAmount / capacity;
		double interpFrac = prevFrac + (fraction - prevFrac) * partialTicks;
		interpFrac = Math.max(0, Math.min(1, interpFrac));

		double level = interpFrac * maxLevel;

		double droop = MathHelper.sin((float) (interpFrac * Math.PI)) * 0.03F;
		double centerLevel = Math.max(0, level - droop);
		double edgeLevel = Math.min(maxLevel, level);
		double connLevel = (centerLevel + edgeLevel) * 0.5;

		bindTexture(LAVA_TEXTURE);

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.disableLighting();

		GlStateManager.color(nr, ng, nb, 1.0F);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);

		if (channel.canConnectTo(world, pos, EnumFacing.EAST)) { // +X
			renderLiquid(buffer, 0.625D, 0.125D, 0.3125D, 1D, 0.125D + connLevel, 0.6875D);
		}
		if (channel.canConnectTo(world, pos, EnumFacing.WEST)) { // -X
			renderLiquid(buffer, 0D, 0.125D, 0.3125D, 0.375D, 0.125D + connLevel, 0.6875D);
		}
		if (channel.canConnectTo(world, pos, EnumFacing.SOUTH)) { // +Z
			renderLiquid(buffer, 0.3125D, 0.125D, 0.625D, 0.6875D, 0.125D + connLevel, 1D);
		}
		if (channel.canConnectTo(world, pos, EnumFacing.NORTH)) { // -Z
			renderLiquid(buffer, 0.3125D, 0.125D, 0D, 0.6875D, 0.125D + connLevel, 0.375D);
		}

		renderLiquid(buffer, 0.375D, 0.125D, 0.375D, 0.625D, 0.125D + centerLevel, 0.625D);

        tessellator.draw();

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void renderLiquid(BufferBuilder buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        buffer.pos(minX, minY, minZ).tex(0, 0).endVertex();
        buffer.pos(minX, minY, maxZ).tex(0, 1).endVertex();
        buffer.pos(maxX, minY, maxZ).tex(1, 1).endVertex();
        buffer.pos(maxX, minY, minZ).tex(1, 0).endVertex();

        buffer.pos(minX, maxY, minZ).tex(0, 0).endVertex();
        buffer.pos(minX, maxY, maxZ).tex(0, 1).endVertex();
        buffer.pos(maxX, maxY, maxZ).tex(1, 1).endVertex();
        buffer.pos(maxX, maxY, minZ).tex(1, 0).endVertex();
    }
}
