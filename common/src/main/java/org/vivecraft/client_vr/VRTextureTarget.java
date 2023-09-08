package org.vivecraft.client_vr;

import org.vivecraft.client.Xplat;
import org.vivecraft.client.extensions.RenderTargetExtension;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;

public class VRTextureTarget extends RenderTarget {

    private final String name;
    public VRTextureTarget(String name, int width, int height, boolean usedepth, boolean onMac, int texid, boolean depthtex, boolean linearFilter, boolean useStencil) {
        super(usedepth);
        this.name = name;
        RenderSystem.assertOnGameThreadOrInit();
        ((RenderTargetExtension) this).setTextid(texid);
        ((RenderTargetExtension) this).isLinearFilter(linearFilter);
        ((RenderTargetExtension) this).setUseStencil(useStencil);
        this.resize(width, height, onMac);
        if (useStencil) {
            Xplat.enableRenderTargetStencil(this);
        }
        this.setClearColor(0, 0, 0, 0);
    }

    @Override
    public String toString() {
        StringBuilder stringbuilder = new StringBuilder("\n");
        if (this.name != null) {
            stringbuilder.append("Name:   ").append(this.name).append("\n");
        }
        stringbuilder.append("Size:   ").append(this.viewWidth).append(" x ").append(this.viewHeight).append("\n");
        stringbuilder.append("FB ID:  ").append(this.frameBufferId).append("\n");
        stringbuilder.append("Tex ID: ").append(this.colorTextureId).append("\n");
        return stringbuilder.toString();
    }

}
