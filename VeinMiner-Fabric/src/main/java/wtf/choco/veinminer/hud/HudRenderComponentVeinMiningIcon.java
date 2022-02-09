package wtf.choco.veinminer.hud;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerMod;

/**
 * A {@link HudRenderComponent} for the vein mining icon at the user's crosshair.
 */
public final class HudRenderComponentVeinMiningIcon implements HudRenderComponent {

    private static final Identifier TEXTURE_VEINMINER_ICON = new Identifier("veinminer4bukkit", "textures/gui/veinminer_icon.png");

    @Override
    public void render(@NotNull MinecraftClient client, @NotNull MatrixStack stack, float delta) {
        Window window = client.getWindow();
        int width = window.getScaledWidth(), height = window.getScaledHeight();

        stack.push();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE_VEINMINER_ICON);
        DrawableHelper.drawTexture(stack, (width / 2) + 8, (height / 2) - 4, 0, 0, 8, 8, 8, 8);

        stack.pop();
    }

    @Override
    public boolean shouldRender() {
        return VeinMinerMod.getServerState().isActive();
    }

}
