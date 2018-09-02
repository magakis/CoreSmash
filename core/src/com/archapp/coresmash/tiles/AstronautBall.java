package com.archapp.coresmash.tiles;

import com.archapp.coresmash.sound.SoundManager;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tilemap.TilemapTile;

public class AstronautBall extends Tile implements Matchable {
    private final int matchID;

    public AstronautBall(TileType type) {
        super(type);

        switch (getTileType()) {
            case ASTRONAUT_BALL1:
                matchID = TileType.REGULAR_BALL1.getID();
                break;
            case ASTRONAUT_BALL2:
                matchID = TileType.REGULAR_BALL2.getID();
                break;
            case ASTRONAUT_BALL3:
                matchID = TileType.REGULAR_BALL3.getID();
                break;
            case ASTRONAUT_BALL4:
                matchID = TileType.REGULAR_BALL4.getID();
                break;
            case ASTRONAUT_BALL5:
                matchID = TileType.REGULAR_BALL5.getID();
                break;
            case ASTRONAUT_BALL6:
                matchID = TileType.REGULAR_BALL6.getID();
                break;
            case ASTRONAUT_BALL7:
                matchID = TileType.REGULAR_BALL7.getID();
                break;
            case ASTRONAUT_BALL8:
                matchID = TileType.REGULAR_BALL8.getID();
                break;
            default:
                throw new RuntimeException("Somehow this was provided to an AstronautTile: " + type.name());
        }
    }

    public int getMatchID() {
        return matchID;
    }

    @Override
    public boolean matchesWith(int id) {
        return matchID == id;
    }

    @Override
    public void onDestroy(TilemapTile self, TilemapManager tilemapManager) {
        SoundManager.get().play(SoundManager.SoundTrack.ASTRONAUT_RELEASE);
    }
}
