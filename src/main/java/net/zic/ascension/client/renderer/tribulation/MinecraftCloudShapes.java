package net.zic.ascension.client.renderer.tribulation;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * cloud shapes cut out of the vanilla cloud texture, so the tribulation's clouds look like the ones in the sky.
 * like vanilla, every pixel is a cell and a pixel is cloud when it is not see through.
 * each separate cloud in the texture becomes a shape, and the big ones are cut into pieces
 */
public final class MinecraftCloudShapes {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/environment/clouds.png");
    //smaller clouds are just specks, bigger ones are cut into windows of CROP cells
    private static final int MIN_CELLS = 4;
    private static final int MAX_CELLS = 80;
    private static final int CROP = 8;

    //which sides of a cell have no other cell of the shape next to them, and so have a face
    public static final int OPEN_NORTH = 1;
    public static final int OPEN_SOUTH = 2;
    public static final int OPEN_WEST = 4;
    public static final int OPEN_EAST = 8;

    private static @Nullable List<Shape> shapes;
    private static float averageCells;

    private MinecraftCloudShapes() {}

    //read once on first use
    public static List<Shape> get() {
        if (shapes == null) {
            shapes = load();
            averageCells = (float) shapes.stream().mapToInt(Shape::size).average().orElse(1.0D);
        }
        return shapes;
    }

    public static float averageCells() {
        get();
        return averageCells;
    }

    private static List<Shape> load() {
        List<Shape> loaded = new ArrayList<>();
        try (InputStream input = Minecraft.getInstance().getResourceManager().open(TEXTURE);
             NativeImage image = NativeImage.read(input)) {
            int width = image.getWidth();
            int height = image.getHeight();
            boolean[] cloud = new boolean[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    //the same test vanilla uses for an empty cell
                    cloud[x + y * width] = ARGB.alpha(image.getPixel(x, y)) >= 10;
                }
            }
            boolean[] seen = new boolean[width * height];
            for (int start = 0; start < cloud.length; start++) {
                if (!cloud[start] || seen[start]) continue;
                List<int[]> piece = flood(cloud, seen, width, height, start, 0, 0, width, height);
                if (piece.size() <= MAX_CELLS) {
                    addShape(loaded, piece);
                } else {
                    cut(loaded, piece, width, height);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load the cloud texture for tribulation clouds", e);
        }
        //something to draw even without the texture
        if (loaded.isEmpty()) {
            List<int[]> square = new ArrayList<>();
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    square.add(new int[]{x, z});
                }
            }
            addShape(loaded, square);
        }
        return List.copyOf(loaded);
    }

    //cuts a big cloud into windows, keeping the biggest connected piece of each
    private static void cut(List<Shape> loaded, List<int[]> piece, int width, int height) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (int[] cell : piece) {
            minX = Math.min(minX, cell[0]);
            minY = Math.min(minY, cell[1]);
            maxX = Math.max(maxX, cell[0]);
            maxY = Math.max(maxY, cell[1]);
        }
        boolean[] inPiece = new boolean[width * height];
        for (int[] cell : piece) {
            inPiece[cell[0] + cell[1] * width] = true;
        }
        for (int windowY = minY; windowY <= maxY; windowY += CROP) {
            for (int windowX = minX; windowX <= maxX; windowX += CROP) {
                boolean[] seen = new boolean[width * height];
                List<int[]> biggest = List.of();
                for (int y = windowY; y < Math.min(windowY + CROP, height); y++) {
                    for (int x = windowX; x < Math.min(windowX + CROP, width); x++) {
                        int index = x + y * width;
                        if (!inPiece[index] || seen[index]) continue;
                        List<int[]> part = flood(inPiece, seen, width, height, index, windowX, windowY, windowX + CROP, windowY + CROP);
                        if (part.size() > biggest.size()) biggest = part;
                    }
                }
                addShape(loaded, biggest);
            }
        }
    }

    //the cells connected to start through their sides, staying inside the given bounds
    private static List<int[]> flood(boolean[] cloud, boolean[] seen, int width, int height, int start,
                                     int minX, int minY, int maxX, int maxY) {
        List<int[]> piece = new ArrayList<>();
        ArrayDeque<Integer> open = new ArrayDeque<>();
        open.add(start);
        seen[start] = true;
        while (!open.isEmpty()) {
            int index = open.poll();
            int x = index % width;
            int y = index / width;
            piece.add(new int[]{x, y});
            int[][] neighbours = {{x + 1, y}, {x - 1, y}, {x, y + 1}, {x, y - 1}};
            for (int[] neighbour : neighbours) {
                int nx = neighbour[0];
                int ny = neighbour[1];
                if (nx < Math.max(minX, 0) || ny < Math.max(minY, 0) || nx >= Math.min(maxX, width) || ny >= Math.min(maxY, height)) continue;
                int next = nx + ny * width;
                if (cloud[next] && !seen[next]) {
                    seen[next] = true;
                    open.add(next);
                }
            }
        }
        return piece;
    }

    private static void addShape(List<Shape> loaded, List<int[]> piece) {
        if (piece.size() < MIN_CELLS) return;
        float centreX = 0.0F;
        float centreZ = 0.0F;
        for (int[] cell : piece) {
            centreX += cell[0];
            centreZ += cell[1];
        }
        centreX /= piece.size();
        centreZ /= piece.size();

        Set<Long> cells = new HashSet<>();
        for (int[] cell : piece) {
            cells.add(key(cell[0], cell[1]));
        }
        float[] offsetX = new float[piece.size()];
        float[] offsetZ = new float[piece.size()];
        byte[] open = new byte[piece.size()];
        for (int i = 0; i < piece.size(); i++) {
            int x = piece.get(i)[0];
            int z = piece.get(i)[1];
            //the middle of the cell, in cells from the middle of the shape
            offsetX[i] = x - centreX;
            offsetZ[i] = z - centreZ;
            int sides = 0;
            if (!cells.contains(key(x, z - 1))) sides |= OPEN_NORTH;
            if (!cells.contains(key(x, z + 1))) sides |= OPEN_SOUTH;
            if (!cells.contains(key(x - 1, z))) sides |= OPEN_WEST;
            if (!cells.contains(key(x + 1, z))) sides |= OPEN_EAST;
            open[i] = (byte) sides;
        }
        loaded.add(new Shape(offsetX, offsetZ, open));
    }

    private static long key(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    /**
     * one cloud, as the middle of each cell in cells from the middle of the cloud
     * @param open which sides of each cell have a face, see the OPEN_ flags
     */
    public record Shape(float[] offsetX, float[] offsetZ, byte[] open) {
        public int size() {
            return offsetX.length;
        }
    }
}
