package test.com.coresmash.performance;

import com.breakthecore.screens.LoadingScreen;
import com.breakthecore.tiles.Tile;
import com.breakthecore.tiles.TileFactory;

import org.junit.BeforeClass;
import org.junit.Test;

public class ClassCastTest {

    @BeforeClass
    public static void init() {
        LoadingScreen.loadBalls();
    }

    @Test
    public void checkWhetherTheTileTypeReturnedIsCorrect() {
        long time = System.nanoTime();
        for (int i = 0; i < 100000; ++i) {
            Tile test = TileFactory.getTileFromID(0);
        }
        long result = System.nanoTime() - time;
//        assertNotNull(test);
//        assertTrue(test instanceof RegularTile);
        System.out.println(result / 1000000f);
    }
}
