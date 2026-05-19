package merelle.view;

import boardifier.model.ContainerElement;
import boardifier.view.GridLook;

/**
 * MerelleBoardLook draws the Nine Men's Morris board, that is 3 nested squares
 * linked by 4 middle lines. There are 24 valid intersections where pawns can be placed,
 * but the model is a 7x7 grid (the other cells are just empty).
 *
 * Coordinates are shown around the board : letters A to G on the top,
 * and digits 1 to 7 on the left.
 *
 * Each cell is 4 chars wide and 2 chars tall, so that there is enough space to draw
 * the board lines between the intersections.
 */

public class MerelleBoardLook extends GridLook {

    // (row, col) of the 24 valid intersections on the 7x7 grid
    private static final int[][] INTERSECTIONS = {
            {0,0}, {0,3}, {0,6},
            {1,1}, {1,3}, {1,5},
            {2,2}, {2,3}, {2,4},
            {3,0}, {3,1}, {3,2}, {3,4}, {3,5}, {3,6},
            {4,2}, {4,3}, {4,4},
            {5,1}, {5,3}, {5,5},
            {6,0}, {6,3}, {6,6}
    };

    public MerelleBoardLook(ContainerElement board) {
        // rowHeight=2, colWidth=4, depth=0, innersTop=1, innersLeft=2, borderWidth=0
        // innersTop and innersLeft leave space for the coordinates
        // borderWidth=0 because the default grid borders are not what we want for Merelle.
        super(2, 4, board, 0, 1, 2, 0);
    }

    protected void render() {
        setSize(getWidth(), getHeight());
        clearShape();
        renderCoords();
        renderBoard();
        renderInners();
    }

    // draw letters A to G on top and digits 1 to 7 on the left
    private void renderCoords() {
        for (int j = 0; j < 7; j++) {
            int x = innersLeft + j * colWidth;
            shape[0][x] = String.valueOf((char) ('A' + j));
        }
        for (int i = 0; i < 7; i++) {
            int y = innersTop + i * rowHeight;
            shape[y][0] = String.valueOf(i + 1);
        }
    }

    // draw the 3 nested squares and the 4 middle lines, then a "+" on each intersection
    private void renderBoard() {
        // outer square
        drawHLine(0, 0, 6);
        drawHLine(6, 0, 6);
        drawVLine(0, 6, 0);
        drawVLine(0, 6, 6);
        // middle square
        drawHLine(1, 1, 5);
        drawHLine(5, 1, 5);
        drawVLine(1, 5, 1);
        drawVLine(1, 5, 5);
        // inner square
        drawHLine(2, 2, 4);
        drawHLine(4, 2, 4);
        drawVLine(2, 4, 2);
        drawVLine(2, 4, 4);
        // middle lines that link the 3 squares
        drawHLine(3, 0, 2);
        drawHLine(3, 4, 6);
        drawVLine(0, 2, 3);
        drawVLine(4, 6, 3);

        // put a "+" on each intersection (will be replaced by a pawn if there is one)
        for (int[] inter : INTERSECTIONS) {
            int x = innersLeft + inter[1] * colWidth;
            int y = innersTop + inter[0] * rowHeight;
            shape[y][x] = "+";
        }
    }

    // draw an horizontal line on the given row, between two columns of the model grid
    private void drawHLine(int row, int colStart, int colEnd) {
        int y = innersTop + row * rowHeight;
        int xStart = innersLeft + colStart * colWidth;
        int xEnd = innersLeft + colEnd * colWidth;
        for (int x = xStart + 1; x < xEnd; x++) {
            shape[y][x] = "-";
        }
    }

    // draw a vertical line on the given column, between two rows of the model grid
    private void drawVLine(int rowStart, int rowEnd, int col) {
        int x = innersLeft + col * colWidth;
        int yStart = innersTop + rowStart * rowHeight;
        int yEnd = innersTop + rowEnd * rowHeight;
        for (int y = yStart + 1; y < yEnd; y++) {
            shape[y][x] = "|";
        }
    }
}
