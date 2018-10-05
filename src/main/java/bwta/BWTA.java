package bwta;

import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwem.BWEM;
import bwem.Base;
import bwem.ChokePoint;
import bwem.area.Area;
import bwem.tile.Tile;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;
import java.util.stream.Collectors;

public class BWTA {
    private static BWEM bwem;
    private static Game game;
    static Map<Area, Region> regionMap;
    static Map<ChokePoint, Chokepoint> chokeMap;
    static Map<Base, BaseLocation> baseMap;
    private static Set<Region> regions;
    private static Set<Chokepoint> chokepoints;
    private static Set<BaseLocation> baseLocations;

    public static void readMap(final Game gamePointer) {
        game = gamePointer;
        bwem = new BWEM(game);
    }

    public static void analyze() {
        bwem.initialize();

        regionMap = new HashMap<>();
        for (final Area a : bwem.getMap().getAreas()) {
            regionMap.put(a, new Region(a));
        }
        regions = new HashSet<>(regionMap.values());

        chokeMap = new HashMap<>();
        for (final ChokePoint c : bwem.getMap().getChokePoints()) {
            chokeMap.put(c, new Chokepoint(c));
        }
        chokepoints = new HashSet<>(chokeMap.values());

        baseMap = new HashMap<>();
        for (final Base b : bwem.getMap().getBases()) {
            baseMap.put(b, new BaseLocation(b));
        }
        baseLocations = new HashSet<>(baseMap.values());
    }

    public static Set<Region> getRegions() {
        return regions;
    }

    public static Set<Chokepoint> getChokepoints() {
        return chokepoints;
    }

    public static Set<BaseLocation> getBaseLocations() {
        return baseLocations;
    }

    public static Set<BaseLocation> getStartLocations() {
        return getBaseLocations().stream()
                .filter(BaseLocation::isStartLocation)
                .collect(Collectors.toSet());
    }

    public static BaseLocation getStartLocation(final Player player) {
        return getNearestBaseLocation(player.getStartLocation());
    }

    public static Region getRegion(final TilePosition tileposition) {
        return regionMap.get(bwem.getMap().getNearestArea(tileposition));
    }

    public static Region getRegion(final Position position) {
        return regionMap.get(bwem.getMap().getNearestArea(position.toWalkPosition()));
    }

    public static Chokepoint getNearestChokepoint(final TilePosition tileposition) {
        return getNearestChokepoint(tileposition.toPosition());
    }

    public static Chokepoint getNearestChokepoint(final Position position) {
        return chokepoints.stream().min((a, b) -> (int) (a.getCenter().getDistance(position) - b.getCenter().getDistance(position))).get();
    }

    public static BaseLocation getNearestBaseLocation(final TilePosition tileposition) {
        return baseLocations.stream().min((a, b) -> (int) (a.getTilePosition().getDistance(tileposition) - b.getTilePosition().getDistance(tileposition))).get();
    }

    public static BaseLocation getNearestBaseLocation(final Position position) {
        return baseLocations.stream().min((a, b) ->(int) (a.getPosition().getDistance(position) - b.getPosition().getDistance(position))).get();
    }

    public static boolean isConnected(final TilePosition a, final TilePosition b) {
        return bwem.getMap().getNearestArea(a).isAccessibleFrom(bwem.getMap().getNearestArea(b));
    }


    public static double getGroundDistance(final TilePosition start, final TilePosition end) {
        final MutableInt L = new MutableInt();
        bwem.getMap().getPath(start.toPosition(), end.toPosition(), L);
        return L.intValue();
    }

    public static List<TilePosition> getShortestPath(final TilePosition start, final TilePosition end) {
        final List<TilePosition> path = new ArrayList<>();

        final Iterator<ChokePoint> it = bwem.getMap().getPath(start.toPosition(), end.toPosition()).iterator();

        ChokePoint curr = null;
        while (it.hasNext()) {
            final ChokePoint next = it.next();
            if (curr != null) {
                final TilePosition t0 = curr.getCenter().toTilePosition();
                final TilePosition t1 = next.getCenter().toTilePosition();
                //trace a ray
                int dx = Math.abs(t1.x - t0.x);
                int dy = Math.abs(t1.y - t0.y);
                int x = t0.x;
                int y = t0.y;
                int n = 1 + dx + dy;
                final int x_inc = (t1.x > t0.x) ? 1 : -1;
                final int y_inc = (t1.x > t0.x) ? 1 : -1;
                int error = dx - dy;
                dx *= 2;
                dy *= 2;

                for (; n > 0; --n) {
                    path.add(new TilePosition(x, y));

                    if (error > 0) {
                        x += x_inc;
                        error -= dy;
                    }
                    else {
                        y += y_inc;
                        error += dx;
                    }
                }
            }
            curr = next;
        }
        return path;
    }
}
