package merelle.model;

import boardifier.model.ContainerElement;
import boardifier.model.GameStageModel;

/**
 * The Merelle board.
 * It is a 7x7 grid but only 24 cells are real intersections where pawns can be placed.
 * The other cells are empty and never used.
 */
public class MerelleBoard extends ContainerElement {

    // the 24 valid intersections, format {row, col}
    public static final int[][] INTERSECTIONS = {
            {0,0}, {0,3}, {0,6},
            {1,1}, {1,3}, {1,5},
            {2,2}, {2,3}, {2,4},
            {3,0}, {3,1}, {3,2}, {3,4}, {3,5}, {3,6},
            {4,2}, {4,3}, {4,4},
            {5,1}, {5,3}, {5,5},
            {6,0}, {6,3}, {6,6}
    };

    // the 16 possible mills (8 horizontals + 8 verticals)
    private static final int[][][] MILLS = {
            { {0,0},{0,3},{0,6} }, { {1,1},{1,3},{1,5} }, { {2,2},{2,3},{2,4} },
            { {3,0},{3,1},{3,2} }, { {3,4},{3,5},{3,6} },
            { {4,2},{4,3},{4,4} }, { {5,1},{5,3},{5,5} }, { {6,0},{6,3},{6,6} },
            { {0,0},{3,0},{6,0} }, { {1,1},{3,1},{5,1} }, { {2,2},{3,2},{4,2} },
            { {0,3},{1,3},{2,3} }, { {4,3},{5,3},{6,3} },
            { {2,4},{3,4},{4,4} }, { {1,5},{3,5},{5,5} }, { {0,6},{3,6},{6,6} }
    };

    public MerelleBoard(int x, int y, GameStageModel gameStageModel) {
        super("merelleboard", x, y, 7, 7, gameStageModel);
    }

    // true if (row, col) is one of the 24 valid intersections
    public boolean isValidIntersection(int row, int col) {
        for (int[] inter : INTERSECTIONS) {
            if (inter[0] == row && inter[1] == col) return true;
        }
        return false;
    }

    // two intersections are linked if they share a row or a column,
    // and no other intersection is between them on this line.
    public boolean areAdjacent(int r1, int c1, int r2, int c2) {
        if (r1 == r2 && c1 == c2) return false;
        if (r1 == r2) {
            int minC = Math.min(c1, c2);
            int maxC = Math.max(c1, c2);
            for (int c = minC + 1; c < maxC; c++) {
                if (isValidIntersection(r1, c)) return false;
            }
            return true;
        }
        if (c1 == c2) {
            int minR = Math.min(r1, r2);
            int maxR = Math.max(r1, r2);
            for (int r = minR + 1; r < maxR; r++) {
                if (isValidIntersection(r, c1)) return false;
            }
            return true;
        }
        return false;
    }

    // color of the pawn at (row, col), or -1 if empty
    public int getColorAt(int row, int col) {
        if (isEmptyAt(row, col)) return -1;
        MerellePawn p = (MerellePawn) getElement(row, col);
        return p.getColor();
    }

    // true if a pawn of "color" at (row, col) would complete a mill
    public boolean formsMill(int row, int col, int color) {
        for (int[][] mill : MILLS) {
            // check that (row,col) is part of this mill
            boolean inside = false;
            for (int[] cell : mill) {
                if (cell[0] == row && cell[1] == col) inside = true;
            }
            if (!inside) continue;
            // check that the 2 other cells already have a pawn of the same color
            boolean ok = true;
            for (int[] cell : mill) {
                if (cell[0] == row && cell[1] == col) continue;
                if (getColorAt(cell[0], cell[1]) != color) ok = false;
            }
            if (ok) return true;
        }
        return false;
    }

    // true if the pawn at (row, col) is currently in a mill
    public boolean isInMill(int row, int col) {
        int color = getColorAt(row, col);
        if (color == -1) return false;
        return formsMill(row, col, color);
    }

    // count the pawns of a color on the board
    public int countPawns(int color) {
        int count = 0;
        for (int[] inter : INTERSECTIONS) {
            if (getColorAt(inter[0], inter[1]) == color) count++;
        }
        return count;
    }

    // can the player still move at least one pawn ?
    public boolean canMove(int color, boolean flying) {
        if (flying) return countPawns(color) > 0;
        for (int[] src : INTERSECTIONS) {
            if (getColorAt(src[0], src[1]) != color) continue;
            for (int[] dst : INTERSECTIONS) {
                if (areAdjacent(src[0], src[1], dst[0], dst[1]) && isEmptyAt(dst[0], dst[1])) {
                    return true;
                }
            }
        }
        return false;
    }
}
