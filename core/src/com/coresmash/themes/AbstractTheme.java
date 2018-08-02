package com.coresmash.themes;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

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
        }

        data.textureName = textureName;
        data.soundName = soundName;
    }

    public Texture getTexture(int id) {
        ResourceData data = resourceList.get(id);
        if (data == null) return assetManager.get("default.png", Texture.class);
        //        if (data == null) throw new RuntimeException("No texture found for ID: "+id);
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
        for (Map.Entry entry : resourceList.entrySet()) {
            ResourceData data = (ResourceData) entry.getValue();
            data.texture = assetManager.get(data.textureName, Texture.class);
            assert (data.texture != null);
        }
    }

    /** Not Implemented */
    public void load(AssetManager am) {
        assetManager = am;
    }

    public void queueForLoad(AssetManager am) {
        assetManager = am;
        for (Map.Entry entry : resourceList.entrySet()) {
            ResourceData data = (ResourceData) entry.getValue();
            if (data.textureName != null) {
                am.load(data.textureName, Texture.class);
            }
        }
    }

    private class ResourceData {
        String textureName;
        Texture texture;
        String soundName;
    }

}
