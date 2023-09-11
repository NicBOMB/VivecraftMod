package org.vivecraft.mod_compat_vr.iris.extensions;

import org.vivecraft.client_vr.render.RenderPass;

import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shadows.ShadowRenderTargets;

public interface PipelineManagerExtension {

    ShadowRenderTargets getShadowRenderTargets();
    void setShadowRenderTargets(ShadowRenderTargets targets);

    // needed for sodium terrain shaders, to get all pipelines, and not just the one from the current pass
    WorldRenderingPipeline getVRPipeline(RenderPass pass);

    WorldRenderingPipeline getVanillaPipeline();

}
