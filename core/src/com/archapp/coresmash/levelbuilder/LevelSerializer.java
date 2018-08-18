package com.archapp.coresmash.levelbuilder;

//NOT Thread Safe Implementation

import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

final class LevelSerializer {
    private static final String NAMESPACE = "";
    private XmlSerializer serializer = new KXmlSerializer();

    LevelSerializer() {}

//    public void serializeToFile(String fileName, LevelSettingsBuilder builder, TilemapManager tilemapManager) {
//        FileHandle file = Gdx.files.external("/CoreSmash/levels/"+fileName+".xml");
//
//        try {
//            serializer.setOutput(file.writer(false));
//            serializer.startDocument("UTF-8", false);
//            serializer.startTag(NAMESPACE, "Level");
//            createElementsFrom(builder);
//            serializer.endTag(NAMESPACE, "Level");
//            serializer.endDocument();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    private void createElementsFrom(LevelSettingsBuilder builder) throws IOException {
//        serializer.startTag(NAMESPACE, "Settings");
//        createElement("Lives", String.valueOf(builder.livesAmount));
//        createElement("Moves", String.valueOf(builder.movesAmount));
//        createElement("Time", String.valueOf(builder.timeAmount));
//        createElement("LauncherSize", String.valueOf(builder.laucherSize));
//        createElement("LauncherCooldown", String.valueOf(builder.launcherCooldown));
//        createElement("BallSpeed", String.valueOf(builder.movingBallSpeed));
//        createElement("ColorCount", String.valueOf(builder.colorCount));
//        serializer.endTag(NAMESPACE, "Settings");
//    }

    private void createElement(String name, String value) throws IOException {
        serializer.startTag(NAMESPACE, name).text(value).endTag(NAMESPACE,name);
    }

//    public static final class LevelSettingsBuilder {
//        private int livesAmount;
//        private int movesAmount;
//        private int timeAmount;
//        private int laucherSize;
//        private float launcherCooldown;
//        private int movingBallSpeed;
//        private int colorCount;
//
//
//        public LevelSettingsBuilder setLives(int lives) {
//            livesAmount = lives;
//            return this;
//        }
//
//        public LevelSettingsBuilder setMoves(int amount) {
//            movesAmount = amount;
//            return this;
//        }
//
//        public LevelSettingsBuilder setTime(int amount) {
//            timeAmount = amount;
//            return this;
//        }
//
//        public LevelSettingsBuilder setLaucherSize(int size) {
//            laucherSize = size;
//            return this;
//        }
//
//        public LevelSettingsBuilder setLauncherCooldown(float cooldown) {
//            launcherCooldown = cooldown;
//            return this;
//        }
//
//        public LevelSettingsBuilder setMovingBallSpeed(int speed) {
//            movingBallSpeed = speed;
//            return this;
//        }
//
//        public LevelSettingsBuilder setColorCount(int colorCount) {
//            this.colorCount = colorCount;
//            return this;
//        }
//    }
}
