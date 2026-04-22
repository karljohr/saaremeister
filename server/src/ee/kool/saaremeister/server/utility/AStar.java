package ee.kool.saaremeister.server.utility;

import java.util.*;

public class AStar {
    private final int maxX, maxY;
    private final int[][] grid;
    private final int[][] neighbours = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}, // Main directions
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1} // Diagonals (for smoother movement)
    };

    public AStar(int[][] grid) {
        this.grid = grid;
        this.maxY = grid.length;
        this.maxX = grid[0].length;
    }

    public static class Node {
        public int x, y;
        int gScore, hScore;
        Node parent;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int getFScore() { return gScore + hScore; }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Node node)) return false;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() { return Objects.hash(x, y); }
    }

    public List<Node> findPath(int srcX, int srcY, int dstX, int dstY) {
        // Limit coordinates
        srcX = Math.max(0, Math.min(srcX, maxX - 1));
        srcY = Math.max(0, Math.min(srcY, maxY - 1));
        dstX = Math.max(0, Math.min(dstX, maxX - 1));
        dstY = Math.max(0, Math.min(dstY, maxY - 1));

        if (grid[dstY][dstX] == 1) return null;

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::getFScore));
        Set<Node> closedSet = new HashSet<>();

        Node startNode = new Node(srcX, srcY);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.x == dstX && current.y == dstY) {
                List<Node> path = new ArrayList<>();
                while (current != null) {
                    path.add(current);
                    current = current.parent;
                }
                Collections.reverse(path);
                return path;
            }

            closedSet.add(current);

            for (int[] n : neighbours) {
                int nx = current.x + n[0];
                int ny = current.y + n[1];

                // Border check for neighbors
                if (nx < 0 || nx >= maxX || ny < 0 || ny >= maxY || grid[ny][nx] == 1) continue;

                // Not allow cutting through corners (for diagonals)
                if (n[0] != 0 && n[1] != 0) {
                    if (grid[current.y][nx] == 1 || grid[ny][current.x] == 1) continue;
                }

                Node neighbor = new Node(nx, ny);
                if (closedSet.contains(neighbor)) continue;

                int moveCost = (n[0] != 0 && n[1] != 0) ? 14 : 10;
                int newGScore = current.gScore + moveCost;

                if (!openSet.contains(neighbor) || newGScore < neighbor.gScore) {
                    neighbor.parent = current;
                    neighbor.gScore = newGScore;
                    neighbor.hScore = (Math.abs(nx - dstX) + Math.abs(ny - dstY)) * 10;
                    openSet.remove(neighbor);
                    openSet.add(neighbor);
                }
            }
        }
        return null;
    }
}