package com.archapp.coresmash.themes;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

/* Do _NOT_ call AssetManager#clear in the AbstractTheme class cause it doesn't contain every Resource.
 * Q: Implement a manager? */
public abstract class AbstractTheme {
    private AssetManager assetManager;
    private HashMap<Integer, ResourceData> resourceList = new HashMap<>();

    public void dispose() {
        for (Map.Entry entry : resourceList.entrySet()) {
            assetManager.unload(((ResourceData) entry.getValue()).textureName);
        }
    }

    protected void setResourcesFor(int id, String textureName, String soundName) {
        ResourceData data = resourceList.get(id);

        if (data == null) {
            data = new ResourceData();
            resourceList.put(id, data);
        } else {
            throw new RuntimeException("Resource already assigned to ID: " + id);
        }

        data.textureName = textureName;
        data.soundName = soundName;
    }

    public TextureRegion getTexture(int id) {
        ResourceData data = resourceList.get(id);
        if (data == null) return new TextureRegion(assetManager.get("default.png", Texture.class));
        return data.texture;
    }

    public Sound getSound(int id) {
        return null;
    }

    /**
     * Requires better implementation.
     * Having to call this at the spriteBatchEnd of resource loading from asset manager is _very_ error prone
     * and should be avoided by a better design.
     */
    public void finishLoading() {
        TextureAtlas atlas = assetManager.get("atlas/Balls.atlas");

        for (Map.Entry entry : resourceList.entrySet()) {
            ResourceData data = (ResourceData) entry.getValue();
            data.texture = atlas.findRegion(data.textureName);
//            data.texture = assetManager.get(data.textureName, Texture.class);
            if (data.texture == null) throw new RuntimeException(data.textureName);
        }
    }

    /** Not Implemented */
    public void load(AssetManager am) {
        assetManager = am;
    }

    public void queueForLoad(AssetManager am) {
        assetManager = am;

        am.load("atlas/Balls.atlas", TextureAtlas.class);
//        for (Map.Entry entry : resourceList.entrySet()) {
//            ResourceData data = (ResourceData) entry.getValue();
//            if (data.textureName != null) {
//                am.load(data.textureName, Texture.class);
//            }
//        }
    }

    private class ResourceData {
        String textureName;
        TextureRegion texture;
        String soundName;
    }

}
