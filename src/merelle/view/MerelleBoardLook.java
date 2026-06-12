package merelle.view;

import boardifier.model.ContainerElement;
import boardifier.view.GridLook;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

public class MerelleBoardLook extends GridLook {

    private static final int CELL_SIZE = 60;

    private final List<Circle> highlights = new ArrayList<>();

    public MerelleBoardLook(ContainerElement board) {
        super(CELL_SIZE, CELL_SIZE, board, 0, 0, 0, 0, Color.TRANSPARENT);
        setVerticalAlignment(ALIGN_MIDDLE);
        setHorizontalAlignment(ALIGN_CENTER);
    }

    @Override
    protected void render() {
        super.render();
        drawLines();
        drawDots();
    }

    private void drawLines() {
        addLine(0, 0, 0, 6);
        addLine(6, 0, 6, 6);
        addLine(0, 0, 6, 0);
        addLine(0, 6, 6, 6);
        addLine(1, 1, 1, 5);
        addLine(5, 1, 5, 5);
        addLine(1, 1, 5, 1);
        addLine(1, 5, 5, 5);
        addLine(2, 2, 2, 4);
        addLine(4, 2, 4, 4);
        addLine(2, 2, 4, 2);
        addLine(2, 4, 4, 4);
        addLine(3, 0, 3, 2);
        addLine(3, 4, 3, 6);
        addLine(0, 3, 2, 3);
        addLine(4, 3, 6, 3);
    }

    private void drawDots() {
        double offset = CELL_SIZE / 2.0;
        for (int[] inter : merelle.model.MerelleBoard.INTERSECTIONS) {
            double cx = inter[1] * CELL_SIZE + offset;
            double cy = inter[0] * CELL_SIZE + offset;
            Circle dot = new Circle(cx, cy, 4);
            dot.setFill(Color.BLACK);
            getGroup().getChildren().add(dot);
        }
    }

    private void addLine(int r1, int c1, int r2, int c2) {
        double offset = CELL_SIZE / 2.0;
        double x1 = c1 * CELL_SIZE + offset;
        double y1 = r1 * CELL_SIZE + offset;
        double x2 = c2 * CELL_SIZE + offset;
        double y2 = r2 * CELL_SIZE + offset;
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(2);
        getGroup().getChildren().add(line);
    }

    public void highlightCell(int row, int col) {
        double offset = CELL_SIZE / 2.0;
        double cx = col * CELL_SIZE + offset;
        double cy = row * CELL_SIZE + offset;
        Circle circle = new Circle(cx, cy, 10);
        circle.setFill(Color.LIMEGREEN);
        circle.setOpacity(0.7);
        circle.setStroke(Color.DARKGREEN);
        circle.setStrokeWidth(1);
        getGroup().getChildren().add(circle);
        highlights.add(circle);
    }

    public void clearHighlights() {
        for (Circle c : highlights) {
            getGroup().getChildren().remove(c);
        }
        highlights.clear();
    }
}
