package com.archapp.coresmash.tilemap;

import com.archapp.coresmash.levelbuilder.ParsedTile;
import com.archapp.coresmash.tiles.TileFactory;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class TilemapBuilder {
    private final TilemapBuilderInfo builderInfo = new TilemapBuilderInfo();
    private final int maxColorCount = 16;// XXX(4/5/2018): MAGIC VALUE 16

    private boolean debugEnabled;
    private boolean isBuilt;
    private Tilemap tilemap;
    private int colorCount;
    private int maxDistance;
    private int maxMatchCount;

    private Random rand;
    private Pool<BlueprintTile> blueprintTilePool;
    private ColorGroupContainer[] colorGroupList;
    private Array<BlueprintTile> fixedTilesArray;
    private Comparator<ColorGroupContainer> compSizes, compColors;
    private Comparator<BlueprintTile> compDistance;
    private List<BlueprintTile> blueprintList;
    private BlueprintTile dummySearchTile;
    private Matcher matcher;


    public TilemapBuilder() {
        rand = new Random();
        colorGroupList = new ColorGroupContainer[maxColorCount];
        blueprintList = new ArrayList<>();
        fixedTilesArray = new Array<>();
        matcher = new Matcher();
        blueprintTilePool = new Pool<BlueprintTile>() {
            @Override
            protected BlueprintTile newObject() {
                return new BlueprintTile();
            }
        };
        dummySearchTile = new BlueprintTile();

        for (int i = 0; i < maxColorCount; ++i) { // XXX(4/5/2018): Magic value,
            colorGroupList[i] = new ColorGroupContainer(i);
        }

        compColors = new Comparator<ColorGroupContainer>() {
            @Override
            public int compare(ColorGroupContainer o1, ColorGroupContainer o2) {
                return Integer.compare(o1.groupColor, o2.groupColor);
            }
        };

        compSizes = new Comparator<ColorGroupContainer>() {
            @Override
            public int compare(ColorGroupContainer o1, ColorGroupContainer o2) {
                if (o1.groupColor < colorCount) {
                    if (o2.groupColor < colorCount) {
                        return Integer.compare(o1.list.size(), o2.list.size());
                    } else {
                        return -1;
                    }
                } else if (o2.groupColor < colorCount) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };

        compDistance = new Comparator<BlueprintTile>() {
            @Override
            public int compare(BlueprintTile o1, BlueprintTile o2) {
                return Integer.compare(o1.distance, o2.distance);
            }
        };
    }

    public void startNewTilemap(Tilemap tm) {
        if (tm == null) throw new NullPointerException();
        reset();
        tilemap = tm;
    }

    public TilemapBuilder setChained(boolean chained) {
        builderInfo.isChained = chained;
        return this;
    }

    public TilemapBuilder setColorCount(int colorCount) {
        this.colorCount = colorCount;
        checkIfCanBuild();
        maxMatchCount = 7 / colorCount + 1;
        return this;
    }

    public TilemapBuilder setOrigin(Vector2 orig) {
        builderInfo.origin.set(orig);
        return this;
    }

    public TilemapBuilder setOffset(Vector2 offs) {
        builderInfo.offset.set(offs);
        return this;
    }

    public TilemapBuilder setMinMaxRotationSpeed(int min, int max, boolean counterClockwise) {
        builderInfo.maxRotSpeed = max;
        builderInfo.minRotSpeed = min;
        builderInfo.rotateCounterClockwise = counterClockwise;
        return this;
    }

    public TilemapBuilder setMapMinMaxRotationSpeed(int min, int max, boolean counterClockwise) {
        builderInfo.maxMapRotationSpeed = max;
        builderInfo.minMapRotationSpeed = min;
        builderInfo.rotateMapCounterClockwise = counterClockwise;
        return this;
    }

    public TilemapBuilder populateFrom(List<ParsedTile> parsedTiles) {
        int randomTileID = 17; // XXX(8/26/2018): MAGIC VALUE 17
        for (ParsedTile parsedTile : parsedTiles) {
            if (parsedTile.getTileID() == randomTileID) {
                checkIfCanBuild();
                BlueprintTile tile = getTile(parsedTile.getX(), parsedTile.getY());
                if (tile == null) {
                    setTile(parsedTile.getX(), parsedTile.getY(), getRandomColorID());
                }
            } else {
                BlueprintTile tile = blueprintTilePool.obtain();
                tile.set(parsedTile.getX(), parsedTile.getY(), parsedTile.getTileID());
                fixedTilesArray.add(tile);
            }
        }
        return this;
    }

    private void applyFilter() {
        fillColorGroupContainers();
        balanceColors(); // Equalize colors
        balanceColorsOnEachRadius();
        spreadBalls();
    }

    private void balanceColors() {
        checkIfCanBuild();

        int aver = blueprintList.size() / colorCount;
        int maxIndex = colorCount - 1;

        Arrays.sort(colorGroupList, compSizes);

        while (colorGroupList[0].list.size() < aver && colorGroupList[maxIndex].list.size() != aver + 1) {
            ColorGroupContainer cgcLeastFilled = colorGroupList[0];
            ColorGroupContainer cgcMostFilled = colorGroupList[maxIndex];

            BlueprintTile tile = cgcMostFilled.list.get(rand.nextInt(cgcMostFilled.list.size()));
            cgcMostFilled.list.remove(tile);

            tile.ID = cgcLeastFilled.groupColor;
            cgcLeastFilled.list.add(tile);

            Arrays.sort(colorGroupList, compSizes);
        }
    }

    private void balanceColorsOnEachRadius() {
        Arrays.sort(colorGroupList, compSizes);

        for (ColorGroupContainer group : colorGroupList) {
            Collections.sort(group.list, compDistance);
        }

        for (ColorGroupContainer primeGroup : colorGroupList) {
            if (primeGroup.groupColor == colorCount || primeGroup.list.size() == 0) break;

            int distanceToSearchFor = maxDistance;
            BlueprintTile tmp = blueprintTilePool.obtain();

            for (int availableSize = primeGroup.list.size(); availableSize >= 0 && distanceToSearchFor > 0; --availableSize) {
                tmp.distance = distanceToSearchFor;

                if (Collections.binarySearch(primeGroup.list, tmp, compDistance) < 0) {
                    int primeIndex = getConsequetiveValueIndex(primeGroup.list);

                    if (primeIndex < 0) {
                        if (primeGroup.list.get(0).distance < distanceToSearchFor) {
                            primeIndex = 0;
                        } else {
                            break;
                        }
                    }

                    for (ColorGroupContainer donorGroup : colorGroupList) { // should I make this descend?
                        if (donorGroup.groupColor == colorCount) break;
                        if (donorGroup == primeGroup || donorGroup.list.size() == 0) continue;

                        int donorIndex = getConsequetiveValueIndexFor(distanceToSearchFor, donorGroup.list);
                        if (donorIndex >= 0) {
                            swapColorGroups(primeIndex, primeGroup, donorIndex, donorGroup);
                            break;
                        }
                    }

                }

                --distanceToSearchFor;
            }

            blueprintTilePool.free(tmp);
        }
    }

    /* Greedy implementation.
     * Should be profiled at some point
     */
    private void spreadBalls() {
        for (ColorGroupContainer primeGroup : colorGroupList) {
            ArrayList<BlueprintTile> primeMatch = findBigColorMatch(maxMatchCount, primeGroup.list);

            primeLoop:
            while (primeMatch != null) {
                for (ColorGroupContainer donorGroup : colorGroupList) {
                    if (donorGroup == primeGroup) continue;

//                    ArrayList<BlueprintTile> donorMatch = findBigColorMatch(maxMatchCount, donorGroup.list);
//                    if (donorMatch != null) {
                    for (BlueprintTile first : primeMatch) {
                        for (BlueprintTile second : donorGroup.list) {
                            if (isSwapValid(primeGroup.list.indexOf(first), primeGroup, donorGroup.list.indexOf(second), donorGroup)) {
                                swapColorGroups(primeGroup.list.indexOf(first), primeGroup, donorGroup.list.indexOf(second), donorGroup);
                                primeMatch = findBigColorMatch(maxMatchCount, primeGroup.list);
                                continue primeLoop;
                            }
                        }
                    }
//                    }

                }
                /* If loop reached this point, it means that it can't swap with any other grop
                 * and so it should stop and move to the next group.
                 */
                break;
            }

        }
    }

    private ArrayList<BlueprintTile> findBigColorMatch(int maxMatch, List<BlueprintTile> list) {
        for (BlueprintTile tile : list) {
            ArrayList<BlueprintTile> cache = matcher.getColorMatchesFromTile(tile);
            if (cache.size() > maxMatch) {
                return cache;
            }
        }
        return null;
    }

    private void swapColorGroups(int primeIndx, ColorGroupContainer primeGroup, int donorIndx, ColorGroupContainer donorGroup) {
        BlueprintTile forDonor = primeGroup.list.get(primeIndx);
        BlueprintTile forPrime = donorGroup.list.get(donorIndx);

        primeGroup.list.remove(forDonor);
        donorGroup.list.remove(forPrime);

        forDonor.ID = donorGroup.groupColor;
        forPrime.ID = primeGroup.groupColor;

        primeGroup.list.add(forPrime);
        donorGroup.list.add(forDonor);

        Collections.sort(primeGroup.list, compDistance);
        Collections.sort(donorGroup.list, compDistance);
    }

    private boolean isSwapValid(int primeIndex, ColorGroupContainer primeGroup, int donorIndex, ColorGroupContainer donorGroup) {
        boolean result = false;

        if (!distanceCheck(primeIndex, primeGroup, donorIndex, donorGroup)) return false;

        BlueprintTile prime = primeGroup.list.get(primeIndex);
        BlueprintTile donor = donorGroup.list.get(donorIndex);

        BlueprintTile dummy = blueprintTilePool.obtain();
        dummy.set(prime);
        dummy.ID = donor.ID;
        ArrayList<BlueprintTile> matches = matcher.getColorMatchesFromTile(dummy);
        if (matches.size() <= maxMatchCount) {
            dummy.set(donor);
            dummy.ID = prime.ID;
            matches = matcher.getColorMatchesFromTile(dummy);

            if (matches.size() <= maxMatchCount) {
                result = true;
            }
        }
        blueprintTilePool.free(dummy);

        return result;
    }

    private boolean distanceCheck(int primeIndex, ColorGroupContainer primeGroup, int donorIndex, ColorGroupContainer donorGroup) {
        BlueprintTile prime = primeGroup.list.get(primeIndex);
        BlueprintTile donor = donorGroup.list.get(donorIndex);

        if (prime.distance == donor.distance) return true;

        boolean isPrimeAllowed = false;
        if (primeIndex - 1 >= 0) {
            isPrimeAllowed = primeGroup.list.get(primeIndex - 1).distance == prime.distance;
        }
        if (!isPrimeAllowed && primeIndex + 1 < primeGroup.list.size()) {
            isPrimeAllowed = primeGroup.list.get(primeIndex + 1).distance == prime.distance;
        }

        boolean isDonorAllowed = false;
        if (donorIndex - 1 >= 0) {
            isDonorAllowed = donorGroup.list.get(donorIndex - 1).distance == donor.distance;
        }
        if (!isDonorAllowed && donorIndex + 1 < donorGroup.list.size()) {
            isDonorAllowed = donorGroup.list.get(donorIndex + 1).distance == donor.distance;
        }

        return isPrimeAllowed && isDonorAllowed;
    }

    private int getConsequetiveValueIndex(List<BlueprintTile> list) {
        int listSize = list.size();
        if (listSize == 0) return -1;

        BlueprintTile prev = list.get(0);
        BlueprintTile next;

        for (int i = 1; i < listSize; ++i) {
            next = list.get(i);
            if (prev.distance == next.distance) {
                return i;
            }
            prev = next;
        }
        return -1;
    }

    private int getConsequetiveValueIndexFor(int distanceToSearch, List<BlueprintTile> list) {
        int result = -1;
        int listSize = list.size();
        if (listSize == 0) return result;


        BlueprintTile tmp = blueprintTilePool.obtain();

        tmp.distance = distanceToSearch;
        int foundIndex = Collections.binarySearch(list, tmp, compDistance);

        if (foundIndex >= 0) {
            if (foundIndex - 1 >= 0) {
                if (list.get(foundIndex - 1).distance == distanceToSearch)
                    result = foundIndex - 1;
            }
            if (foundIndex + 1 < listSize) {
                if (list.get(foundIndex + 1).distance == distanceToSearch)
                    result = foundIndex + 1;
            }
        }

        blueprintTilePool.free(tmp);
        return result;
    }

    public void build() {
        if (colorCount > 1) {
            applyFilter();
        }
        putFixedTilesInBlueprint();
        // TODO(4/5/2018): this function needs to know what each id represents...

        int randomTileID = 17;
        for (BlueprintTile tile : blueprintList) {
            if (debugEnabled && tile.ID == randomTileID)
                tilemap.putTilemapTile(tile.x, tile.y, TileFactory.getTileFromID(randomTileID));
            else
                tilemap.putTilemapTile(tile.x, tile.y, TileFactory.getTileFromID(tile.ID));

        }

        tilemap.initialize(builderInfo);
        isBuilt = true;
    }

    private BlueprintTile getTile(int x, int y) {
        dummySearchTile.set(x, y);
        int index = Collections.binarySearch(blueprintList, dummySearchTile);
        if (index < 0)
            return null;
        else
            return blueprintList.get(index);
    }

    private void setTile(int x, int y, int ID) {
        BlueprintTile tile = getTile(x, y);

        if (tile == null) {
            tile = blueprintTilePool.obtain();
            blueprintList.add(tile);
        }

        tile.set(x, y, ID);
    }

    private void putFixedTilesInBlueprint() {
        for (BlueprintTile tile : fixedTilesArray) {
            setTile(tile.x, tile.y, tile.ID);
        }
    }

    private int getRandomColorID() {
        return rand.nextInt(colorCount);
    }

    private int getTileDistance(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2;     // signed deltas
        int dy = y1 - y2;
        int x = Math.abs(dx);  // absolute deltas
        int y = Math.abs(dy);

        return Math.max(x, Math.max(y, Math.abs(dx + dy)));
    }

    private void fillColorGroupContainers() {
        resetColorGroupContainers();

        for (BlueprintTile tile : blueprintList) {
            colorGroupList[tile.ID].list.add(tile);
        }
    }

    private void resetColorGroupContainers() {
        for (ColorGroupContainer cgc : colorGroupList) {
            cgc.reset();
        }
        Arrays.sort(colorGroupList, compColors);
    }

    private void checkIfCanBuild() {
        if (isBuilt || tilemap == null) throw new RuntimeException("Can't build");
        if (colorCount == 0) throw new RuntimeException("ColorCount must be set first");
    }

    private void reset() {
        for (BlueprintTile tile : blueprintList) {
            blueprintTilePool.free(tile);
        }
        blueprintList.clear();

        for (BlueprintTile tile : fixedTilesArray) {
            blueprintTilePool.free(tile);
        }
        fixedTilesArray.clear();

        tilemap = null;
        isBuilt = false;
        colorCount = 0;
        maxDistance = 0;
        maxMatchCount = 0;
        debugEnabled = false;
        builderInfo.reset();
    }

    private class ColorGroupContainer {
        private int groupColor;
        private ArrayList<BlueprintTile> list = new ArrayList<BlueprintTile>();

        public ColorGroupContainer(int colorId) {
            groupColor = colorId;
        }

        public void reset() {
            list.clear();
        }
    }

    private class BlueprintTile implements Pool.Poolable, Comparable<BlueprintTile> {
        int ID;
        int x;
        int y;
        int distance;

        BlueprintTile() {
        }

        public void set(int x, int y) {
            set(x, y, -1);
        }

        public void set(int x, int y, int ID) {
            this.x = x;
            this.y = y;
            this.ID = ID;
            distance = getTileDistance(x, y, 0, 0);
        }

        public void set(BlueprintTile copy) {
            ID = copy.ID;
            x = copy.x;
            y = copy.y;
            distance = copy.distance;
        }

        @Override
        public void reset() {
            ID = 0;
            x = 0;
            y = 0;
        }

        @Override
        public int compareTo(BlueprintTile tile) {
            int compX = Integer.compare(x, tile.x);
            return compX == 0 ? Integer.compare(y, tile.y) : compX;
        }
    }

    private class Matcher {
        private ArrayList<BlueprintTile> match = new ArrayList<>();
        private ArrayList<BlueprintTile> exclude = new ArrayList<>();

        public ArrayList<BlueprintTile> getColorMatchesFromTile(BlueprintTile tile) {
            match.clear();
            exclude.clear();
            addSurroundingColorMatches(tile);
            return (ArrayList<BlueprintTile>) match.clone();
        }

        private void addSurroundingColorMatches(BlueprintTile tile) {
            int tx = tile.x;
            int ty = tile.y;

            BlueprintTile tt;

            match.add(tile);
            exclude.add(tile);

            //top_left
            tt = getTile(tx - 1, ty + 1);
            if (tt != null && !exclude.contains(tt)) {
                if (tt.ID == tile.ID) {
                    addSurroundingColorMatches(tt);
                }
            }

            //top_right
            tt = getTile(tx, ty + 1);
            if (tt != null && !exclude.contains(tt)) {
                if (tt.ID == tile.ID) {
                    addSurroundingColorMatches(tt);
                }
            }

            //right
            tt = getTile(tx + 1, ty);
            if (tt != null && !exclude.contains(tt)) {
                if (tt.ID == tile.ID) {
                    addSurroundingColorMatches(tt);
                }
            }

            //bottom_right
            tt = getTile(tx + 1, ty - 1);
            if (tt != null && !exclude.contains(tt)) {
                if (tt.ID == tile.ID) {
                    addSurroundingColorMatches(tt);
                }
            }

            //bottom_left
            tt = getTile(tx, ty - 1);
            if (tt != null && !exclude.contains(tt)) {
                if (tt.ID == tile.ID) {
                    addSurroundingColorMatches(tt);
                }
            }


            //left
            tt = getTile(tx - 1, ty);
            if (tt != null && !exclude.contains(tt)) {
                if (tt.ID == tile.ID) {
                    addSurroundingColorMatches(tt);
                }
            }
        }

    }

    public static class TilemapBuilderInfo {
        private Vector2 origin;
        private Vector2 offset;

        private boolean isChained;
        private int minMapRotationSpeed;
        private int maxMapRotationSpeed;
        private boolean rotateMapCounterClockwise;

        private int minRotSpeed;
        private int maxRotSpeed;
        private boolean rotateCounterClockwise;

        private TilemapBuilderInfo() {
            origin = new Vector2();
            offset = new Vector2();
        }

        private void reset() {
            minMapRotationSpeed = 0;
            maxMapRotationSpeed = 0;
            minRotSpeed = 0;
            maxRotSpeed = 0;
            rotateMapCounterClockwise = false;
            rotateCounterClockwise = false;
            origin.setZero();
            offset.setZero();
            isChained = false;
        }

        public float getOriginX() {
            return origin.x;
        }

        public float getOriginY() {
            return origin.y;
        }

        public float getOffsetX() {
            return offset.x;
        }

        public float getOffsetY() {
            return offset.y;
        }

        public int getMapMaxRotSpeed() {
            return maxMapRotationSpeed;
        }

        public int getMapMinRotSpeed() {
            return minMapRotationSpeed;
        }

        public int getMaxRotSpeed() {
            return maxRotSpeed;
        }

        public int getMinRotSpeed() {
            return minRotSpeed;
        }

        public boolean isChained() {
            return isChained;
        }

        public boolean isRotatingCCW() {
            return rotateCounterClockwise;
        }
    }
}