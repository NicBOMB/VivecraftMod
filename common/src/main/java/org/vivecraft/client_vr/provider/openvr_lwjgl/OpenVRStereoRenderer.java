package org.vivecraft.client_vr.provider.openvr_lwjgl;

import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.openvr.HiddenAreaMesh;
import org.lwjgl.openvr.HmdMatrix44;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;

import java.nio.IntBuffer;

import static org.vivecraft.common.utils.Utils.convertRM44ToMM44;
import static org.vivecraft.common.utils.Utils.logger;

import static org.lwjgl.openvr.OpenVR.VRCompositor;
import static org.lwjgl.openvr.VR.*;
import static org.lwjgl.openvr.VRCompositor.VRCompositor_PostPresentHandoff;
import static org.lwjgl.openvr.VRCompositor.VRCompositor_Submit;
import static org.lwjgl.openvr.VRSystem.*;

public class OpenVRStereoRenderer extends VRRenderer {
    private HiddenAreaMesh[] hiddenMeshes = new HiddenAreaMesh[2];
    private final MCOpenVR openvr;

    public OpenVRStereoRenderer(MCOpenVR vr) {
        super(vr);
        this.openvr = vr;
        hiddenMeshes[0] = HiddenAreaMesh.calloc();
        hiddenMeshes[1] = HiddenAreaMesh.calloc();
    }

    @Override
    public Tuple<Integer, Integer> getRenderTextureSizes() {
        if (this.resolution == null) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer intbyreference = stack.callocInt(1);
                IntBuffer intbyreference1 = stack.callocInt(1);
                VRSystem_GetRecommendedRenderTargetSize(intbyreference, intbyreference1);
                this.resolution = new Tuple<>(intbyreference.get(0), intbyreference1.get(0));
                logger.info("OpenVR Render Res {} x {}", this.resolution.getA(), this.resolution.getB());
                this.ss = this.openvr.getSuperSampling();
                logger.info("OpenVR Supersampling: {}", this.ss);
            }

            for (int i = 0; i < 2; ++i) {
                this.hiddenMeshes[i] = VRSystem_GetHiddenAreaMesh(i, 0, this.hiddenMeshes[i]);
                int j = this.hiddenMeshes[i].unTriangleCount();

                if (j <= 0) {
                    logger.warn("No stencil mesh found for eye {}", i);
                } else {
                    this.hiddenMesheVertecies[i] = new float[this.hiddenMeshes[i].unTriangleCount() * 3 * 2];
                    MemoryUtil.memFloatBuffer(MemoryUtil.memAddress(this.hiddenMeshes[i].pVertexData()), this.hiddenMesheVertecies[i].length).get(this.hiddenMesheVertecies[i]);

                    for (int k = 0; k < this.hiddenMesheVertecies[i].length; k += 2) {
                        this.hiddenMesheVertecies[i][k] *= (float) this.resolution.getA();
                        this.hiddenMesheVertecies[i][k + 1] *= (float) this.resolution.getB();
                    }

                    logger.info("Stencil mesh loaded for eye {}", i);
                }
            }
        }
        return this.resolution;
    }

    @Override
    public Matrix4f getProjectionMatrix(int eyeType, double nearClip, double farClip) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (eyeType == 0) {
                return convertRM44ToMM44(
                    VRSystem_GetProjectionMatrix(0, (float) nearClip, (float) farClip, HmdMatrix44.calloc(stack)).m(),
                    new Matrix4f()
                );
            } else {
                return convertRM44ToMM44(
                    VRSystem_GetProjectionMatrix(1, (float) nearClip, (float) farClip, HmdMatrix44.calloc(stack)).m(),
                    new Matrix4f()
                );
            }
        }
    }

    @Override
    public void createRenderTexture(int lwidth, int lheight) {
        this.LeftEyeTextureId = GlStateManager._genTexture();
        int i = GlStateManager._getInteger(GL11C.GL_TEXTURE_BINDING_2D);
        RenderSystem.bindTexture(this.LeftEyeTextureId);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, lwidth, lheight, 0, GL11C.GL_RGBA, GL11C.GL_INT, null);
        RenderSystem.bindTexture(i);
        this.openvr.texType0.handle(this.LeftEyeTextureId);
        this.openvr.texType0.eColorSpace(EColorSpace_ColorSpace_Gamma);
        this.openvr.texType0.eType(ETextureType_TextureType_OpenGL);

        this.RightEyeTextureId = GlStateManager._genTexture();
        i = GlStateManager._getInteger(GL11C.GL_TEXTURE_BINDING_2D);
        RenderSystem.bindTexture(this.RightEyeTextureId);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, lwidth, lheight, 0, GL11C.GL_RGBA, GL11C.GL_INT, null);
        RenderSystem.bindTexture(i);
        this.openvr.texType1.handle(this.RightEyeTextureId);
        this.openvr.texType1.eColorSpace(EColorSpace_ColorSpace_Gamma);
        this.openvr.texType1.eType(ETextureType_TextureType_OpenGL);
    }

    @Override
    public void endFrame() throws RenderConfigException {
        if (VRCompositor.Submit != 0) {
            int i = VRCompositor_Submit(0, this.openvr.texType0, null, 0);
            int j = VRCompositor_Submit(1, this.openvr.texType1, null, 0);
            VRCompositor_PostPresentHandoff();

            if (i + j > 0) {
                throw new RenderConfigException("Compositor Error", Component.literal("Texture submission error: Left/Right " + getCompostiorError(i) + "/" + getCompostiorError(j)));
            }
        }
    }

    public static String getCompostiorError(int code) {
        return switch (code) {
            case EVRCompositorError_VRCompositorError_None -> "None";
            case EVRCompositorError_VRCompositorError_RequestFailed -> "RequestFailed";
            case EVRCompositorError_VRCompositorError_IncompatibleVersion -> "IncompatibleVersion";
            case EVRCompositorError_VRCompositorError_DoNotHaveFocus -> "DoesNotHaveFocus";
            case EVRCompositorError_VRCompositorError_InvalidTexture -> "InvalidTexture";
            case EVRCompositorError_VRCompositorError_IsNotSceneApplication -> "IsNotSceneApplication";
            case EVRCompositorError_VRCompositorError_TextureIsOnWrongDevice -> "TextureIsOnWrongDevice";
            case EVRCompositorError_VRCompositorError_TextureUsesUnsupportedFormat -> "TextureUsesUnsupportedFormat";
            case EVRCompositorError_VRCompositorError_SharedTexturesNotSupported -> "SharedTexturesNotSupported";
            case EVRCompositorError_VRCompositorError_IndexOutOfRange -> "IndexOutOfRange";
            case EVRCompositorError_VRCompositorError_AlreadySubmitted -> "AlreadySubmitted";
            case EVRCompositorError_VRCompositorError_InvalidBounds -> "InvalidBounds";
            case EVRCompositorError_VRCompositorError_AlreadySet -> "AlreadySet";
            default -> "Unknown";
        };
    }

    @Override
    public boolean providesStencilMask() {
        return true;
    }

    @Override
    public String getName() {
        return "OpenVR";
    }

    @Override
    public void destroy() {
        super.destroy();
        if (this.LeftEyeTextureId > -1) {
            TextureUtil.releaseTextureId(this.LeftEyeTextureId);
            this.LeftEyeTextureId = -1;
        }

        if (this.RightEyeTextureId > -1) {
            TextureUtil.releaseTextureId(this.RightEyeTextureId);
            this.RightEyeTextureId = -1;
        }
    }
}
