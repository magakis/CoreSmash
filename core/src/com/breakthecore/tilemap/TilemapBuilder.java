package com.breakthecore.tilemap;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.breakthecore.levelbuilder.ParsedTile;
import com.breakthecore.tiles.TileFactory;
import com.breakthecore.tiles.TileIndex;
import com.breakthecore.tiles.TileType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class TilemapBuilder {
    private final TilemapBuilderInfo builderInfo = new TilemapBuilderInfo();
    private final int maxColorCount = 16;// XXX(4/5/2018): MAGIC VALUE 16
    private final int blueprintSize = 30;// XXX(4/5/2018): MAGIC VALUE 30
    private final int centerTile = blueprintSize / 2;

    private boolean debugEnabled;
    private boolean isBuilt;
    private Tilemap tilemap;
    private int colorCount;
    private int maxDistance;
    private int blueprintTileCount;
    int maxMatchCount;

    private Random rand;
    private Pool<BlueprintTile> blueprintTilePool;
    private ColorGroupContainer[] colorGroupList;
    private Array<BlueprintTile> fixedTilesArray;
    private Comparator<ColorGroupContainer> compSizes, compColors;
    private Comparator<BlueprintTile> compDistance;
    private BlueprintTile[][] blueprintMap;
    private Matcher matcher;

    private Vector2 origin;
    private Vector2 offset;

    private boolean isChained;
    private int minMapRotationSpeed;
    private int maxMapRotationSpeed;
    private boolean rotateMapCounterClockwise;

    private int minRotSpeed;
    private int maxRotSpeed;
    private boolean rotateCounterClockwise;

    public TilemapBuilder() {
        rand = new Random();
        colorGroupList = new ColorGroupContainer[maxColorCount];
        blueprintMap = new BlueprintTile[blueprintSize][blueprintSize];
        fixedTilesArray = new Array<>();
        matcher = new Matcher();
        blueprintTilePool = new Pool<BlueprintTile>() {
            @Override
            protected BlueprintTile newObject() {
                return new BlueprintTile();
            }
        };
        origin = new Vector2();
        offset = new Vector2();

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
        isChained = chained;
        return this;
    }

    public TilemapBuilder placeMiddleTile() {
        setTile(0, 0, 17);
        return this;
    }

    public TilemapBuilder setColorCount(int colorCount) {
        this.colorCount = colorCount;
        checkIfCanBuild();
        maxMatchCount = 7 / colorCount + 1;
        return this;
    }

    public TilemapBuilder generateRadius(int radius) {
        checkIfCanBuild();

        for (int y = -radius - 1; y < radius + 1; ++y) {
            for (int x = -radius - 1; x < radius + 1; ++x) {
                if (getTileDistance(x, y, 0, 0) <= radius) {
                    if (debugEnabled) {
                        setTile(x, y, 17); // XXX(5/5/2018): MAGIC VALUE 17
                    } else {
                        setTile(x, y, getRandomColorID());
                    }
                }
            }
        }
        return this;
    }

    public TilemapBuilder generateTriangle(int size, boolean flipY) {
        int sizeY = size * 2;
        if (flipY) {
            for (int y = -sizeY; y <= size; ++y) {
                for (int x = -size - y; x <= size; ++x) {
                    setTile(x, y, getRandomColorID());
                }
            }
        } else {
            for (int y = -size; y <= sizeY; ++y) {
                for (int x = -size; x <= size - y; ++x) {
                    setTile(x, y, getRandomColorID());
                }
            }
        }
        return this;
    }

    public TilemapBuilder generateStar(int size) {
        generateTriangle(size, false);
        generateTriangle(size, true);
        return this;
    }

    public TilemapBuilder balanceColorAmounts() {
        checkIfCanBuild();

        fillColorGroupContainers();
        int aver = blueprintTileCount / colorCount;
        int maxIndex = colorCount - 1;

        Arrays.sort(colorGroupList, compSizes);

        while (colorGroupList[0].list.size() < aver) {
            ColorGroupContainer cgcLeastFilled = colorGroupList[0];
            ColorGroupContainer cgcMostFilled = colorGroupList[maxIndex];

            BlueprintTile tile = cgcMostFilled.list.get(rand.nextInt(cgcMostFilled.list.size()));
            cgcMostFilled.list.remove(tile);

            tile.ID = cgcLeastFilled.groupColor;
            cgcLeastFilled.list.add(tile);

            Arrays.sort(colorGroupList, compSizes);
        }

        return this;
    }

    public TilemapBuilder forceEachColorOnEveryRadius() {
        checkIfCanBuild();

        fillColorGroupContainers();
        for (ColorGroupContainer cgc : colorGroupList) {
            Collections.sort(cgc.list, compDistance);
        }

        int distanceToSearchFor;

        startLoop:
        for (ColorGroupContainer cgc : colorGroupList) {
            distanceToSearchFor = 1;
            while (distanceToSearchFor <= maxDistance) {
                if (cgc.list.size() == 0) break;
                for (int listIndex = 0; listIndex < cgc.list.size(); ++listIndex) {
                    BlueprintTile tile = cgc.list.get(listIndex);
                    int distance = tile.distance;

                    if (distance > distanceToSearchFor || listIndex == cgc.list.size() - 1 && distance < distanceToSearchFor) {
                        BlueprintTile tileToSwapA = null;
                        int prevDistance = -1;

                        for (int i = 0; i < cgc.list.size(); ++i) {
                            int curDistance = cgc.list.get(i).distance;
                            if (prevDistance == curDistance) {
                                tileToSwapA = cgc.list.get(i);
                                break;
                            } else {
                                prevDistance = curDistance;
                            }
                        }

                        if (tileToSwapA == null) break startLoop;

                        ColorGroupContainer innerCgc = null;
                        BlueprintTile tileToSwapB = null;

                        loopB:
                        for (ColorGroupContainer color : colorGroupList) {
                            innerCgc = color;
                            if (cgc == innerCgc) continue;

                            prevDistance = -1;
                            for (int i = 0; i < innerCgc.list.size(); ++i) {
                                int curDistance = innerCgc.list.get(i).distance;
                                if (prevDistance == curDistance && curDistance == distanceToSearchFor) {
                                    tileToSwapB = innerCgc.list.get(i);
                                    break loopB;
                                } else {
                                    prevDistance = curDistance;
                                }
                            }
                        }

                        if (tileToSwapB == null) break startLoop;

                        BlueprintTile tmpTile = tileToSwapB;
                        tileToSwapB = tileToSwapA;
                        tileToSwapA = tmpTile;

                        cgc.list.remove(tileToSwapA);
                        cgc.list.add(tileToSwapB);

                        innerCgc.list.remove(tileToSwapB);
                        innerCgc.list.add(tileToSwapA);

                        Collections.sort(cgc.list, compDistance);
                        Collections.sort(innerCgc.list, compDistance);

                        ++distanceToSearchFor;
                    } else if (distance == distanceToSearchFor) {
                        ++distanceToSearchFor;
                        break;
                    }
                }
            }
        }
        return this;
    }

    public TilemapBuilder setOrigin(Vector2 orig) {
        origin.set(orig);
        return this;
    }

    public TilemapBuilder setOffset(Vector2 offs) {
        offset.set(offs);
        return this;
    }

    public TilemapBuilder setMinMaxRotationSpeed(int min, int max, boolean counterClockwise) {
        maxRotSpeed = max;
        minRotSpeed = min;
        rotateCounterClockwise = counterClockwise;
        return this;
    }

    public TilemapBuilder setMapMinMaxRotationSpeed(int min, int max, boolean counterClockwise) {
        maxMapRotationSpeed = max;
        minMapRotationSpeed = min;
        rotateMapCounterClockwise = counterClockwise;
        return this;
    }

    public TilemapBuilder setMinMaxRotationSpeed(int min, int max) {
        return setMinMaxRotationSpeed(min, max, false);
    }

    public TilemapBuilder reduceCenterTileColorMatch(int max, boolean strict) {
        BlueprintTile centerTile = getTile(0, 0);
        if (centerTile == null) return this;

        ArrayList<BlueprintTile> matches = matcher.getColorMatchesFromTile(centerTile);
        int tries = 0;
        while (matches.size() > max) {
            if (tries == colorCount) {
                if (strict) {
                    BlueprintTile rngTile = matches.get(rand.nextInt(matches.size() - 1) + 1);
                    rngTile.ID = getNextColor(rngTile.ID);
                    tries = 0;
                } else {
                    return this;
                }
            }
            centerTile.ID = getNextColor(centerTile.ID);
            matches = matcher.getColorMatchesFromTile(centerTile);
            ++tries;
        }
        return this;
    }

    public TilemapBuilder reduceColorMatches(int maxAllowed) {
        int max = maxAllowed;

        for (int y = 0; y < blueprintSize; ++y) {
            for (int x = 0; x < blueprintSize; ++x) {
                BlueprintTile tile = blueprintMap[y][x];
                if (tile == null) continue;

                ArrayList<BlueprintTile> matches = matcher.getColorMatchesFromTile(tile);
                int tries = 0;
                while (matches.size() > max) {
                    tile.ID = getNextColor(tile.ID);
                    matches = matcher.getColorMatchesFromTile(tile);
                    ++tries;
                    if (tries == colorCount) {
                        ++max;
                        tries = 0;
                    }
                }
            }
        }
        return this;
    }

    public TilemapBuilder reduceColorMatches(int maxAllowed, int numOfPasses) {
        int passesLeft = numOfPasses;
        while (passesLeft > 0) {
            for (int y = 0; y < blueprintSize; ++y) {
                for (int x = 0; x < blueprintSize; ++x) {
                    BlueprintTile tile = blueprintMap[y][x];
                    if (tile == null) continue;

                    ArrayList<BlueprintTile> matches = matcher.getColorMatchesFromTile(tile);
                    if (matches.size() > maxAllowed) {
                        tile.ID = getNextColor(tile.ID);
                    }
                }
            }
            --passesLeft;
        }
        return this;
    }

    public TilemapBuilder debug() {
        debugEnabled = true;
        return this;
    }

    public TilemapBuilder populateFrom(List<ParsedTile> parsedTiles) {
        int randomTileID = TileIndex.get().getIdOf(TileType.RANDOM_REGULAR);
        for (ParsedTile parsedTile : parsedTiles) {
            if (parsedTile.getTileID() == randomTileID) {
                checkIfCanBuild();
                BlueprintTile tile = getTile(parsedTile.getX(), parsedTile.getY());
                if (tile == null) {
                    setTile(parsedTile.getX(), parsedTile.getY(), getRandomColorID());
                }
            } else {
                BlueprintTile tile = blueprintTilePool.obtain();
                tile.set(parsedTile.getX(), parsedTile.getY());
                tile.ID = parsedTile.getTileID();
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

        int aver = blueprintTileCount / colorCount;
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
//        ArrayList<BlueprintTile> cache = null;

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

    private ColorGroupContainer getColorGroupForID(int id) {
        for (ColorGroupContainer colorGroup : colorGroupList) {
            if (colorGroup.groupColor == id) return colorGroup;
        }
        return null;
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

        int randomTileID = TileIndex.get().getIdOf(TileType.RANDOM_REGULAR);
        for (BlueprintTile[] arr : blueprintMap) {
            for (BlueprintTile tile : arr) {
                if (tile == null) continue;
                if (debugEnabled && tile.ID == randomTileID)
                    tilemap.putTilemapTile(tile.x, tile.y, TileFactory.getTileFromID(randomTileID));
                else
                    tilemap.putTilemapTile(tile.x, tile.y, TileFactory.getTileFromID(tile.ID));
            }
        }

        tilemap.initialize(builderInfo);
        isBuilt = true;
    }

    private BlueprintTile getTile(int x, int y) {
        return blueprintMap[y + centerTile][x + centerTile];
    }

    private void setTile(int x, int y, int ID) {
        BlueprintTile tile = getTile(x, y);

        if (tile == null) {
            tile = blueprintTilePool.obtain();
            tile.distance = getTileDistance(x, y, 0, 0);

            if (tile.distance > maxDistance) {
                maxDistance = tile.distance;
            }

            ++blueprintTileCount;
            blueprintMap[y + centerTile][x + centerTile] = tile;
        }

        tile.ID = ID;
        tile.x = x;
        tile.y = y;
    }

    private void putFixedTilesInBlueprint() {
        for (BlueprintTile tile : fixedTilesArray) {
            setTile(tile.x, tile.y, tile.ID);
        }
        fixedTilesArray.clear();
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

        for (int y = 0; y < blueprintSize; ++y) {
            for (int x = 0; x < blueprintSize; ++x) {
                BlueprintTile tile = blueprintMap[y][x];
                if (tile == null) continue;

                colorGroupList[tile.ID].list.add(tile);
            }
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

    private int getNextColor(int currentColor) {
        return currentColor + 1 == colorCount ? 0 : currentColor + 1;
    }

    private void reset() {
        for (int y = 0; y < blueprintSize; ++y) {
            for (int x = 0; x < blueprintSize; ++x) {
                BlueprintTile tile = blueprintMap[y][x];
                if (tile == null) continue;

                blueprintTilePool.free(tile);
                blueprintMap[y][x] = null;
            }
        }

        origin.setZero();
        offset.setZero();
        fixedTilesArray.clear();
        tilemap = null;
        isBuilt = false;
        isChained = false;
        colorCount = 0;
        maxDistance = 0;
        maxMatchCount = 0;
        blueprintTileCount = 0;
        debugEnabled = false;
        minMapRotationSpeed = 0;
        maxMapRotationSpeed = 0;
        minRotSpeed = 0;
        maxRotSpeed = 0;
        rotateMapCounterClockwise = false;
        rotateCounterClockwise = false;
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

    private class BlueprintTile implements Pool.Poolable {
        int ID;
        int x;
        int y;
        int distance;

        public void set(int x, int y) {
            this.x = x;
            this.y = y;
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
            if (checkBounds(tx - 1, ty + 1)) {
                tt = getTile(tx - 1, ty + 1);
                if (tt != null && !exclude.contains(tt)) {
                    if (tt.ID == tile.ID) {
                        addSurroundingColorMatches(tt);
                    }
                }
            }

            //top_right
            if (checkBounds(tx, ty + 1)) {
                tt = getTile(tx, ty + 1);
                if (tt != null && !exclude.contains(tt)) {
                    if (tt.ID == tile.ID) {
                        addSurroundingColorMatches(tt);
                    }
                }
            }

            //right
            if (checkBounds(tx + 1, ty)) {
                tt = getTile(tx + 1, ty);
                if (tt != null && !exclude.contains(tt)) {
                    if (tt.ID == tile.ID) {
                        addSurroundingColorMatches(tt);
                    }
                }
            }

            //bottom_right
            if (checkBounds(tx + 1, ty - 1)) {
                tt = getTile(tx + 1, ty - 1);
                if (tt != null && !exclude.contains(tt)) {
                    if (tt.ID == tile.ID) {
                        addSurroundingColorMatches(tt);
                    }
                }
            }

            //bottom_left
            if (checkBounds(tx, ty - 1)) {
                tt = getTile(tx, ty - 1);
                if (tt != null && !exclude.contains(tt)) {
                    if (tt.ID == tile.ID) {
                        addSurroundingColorMatches(tt);
                    }
                }
            }


            //left
            if (checkBounds(tx - 1, ty)) {
                tt = getTile(tx - 1, ty);
                if (tt != null && !exclude.contains(tt)) {
                    if (tt.ID == tile.ID) {
                        addSurroundingColorMatches(tt);
                    }
                }
            }
        }

    }

    private boolean checkBounds(int x, int y) {
        x += centerTile;
        y += centerTile;
        return x >= 0 && x < blueprintSize &&
                y >= 0 && y < blueprintSize;
    }

    public class TilemapBuilderInfo {
        private TilemapBuilderInfo() {
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