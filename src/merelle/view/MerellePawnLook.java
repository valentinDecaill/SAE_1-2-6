package merelle.view;

import boardifier.model.GameElement;
import boardifier.view.ElementLook;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import merelle.model.MerellePawn;

public class MerellePawnLook extends ElementLook {

    private Circle circle;
    public static final double RADIUS = 22;

    public MerellePawnLook(GameElement element) {
        super(element, (int)(RADIUS * 2), (int)(RADIUS * 2));
        setAnchorType(ANCHOR_TOPLEFT);
        circle = new Circle(RADIUS, RADIUS, RADIUS);
        getNode().getChildren().add(circle);
    }

    public void onSelectionChange() {
        render();
    }

    protected void render() {
        MerellePawn pawn = (MerellePawn) element;
        if (pawn.getColor() == MerellePawn.PAWN_BLACK) {
            circle.setFill(Color.BLACK);
            circle.setStroke(Color.DARKGRAY);
        } else {
            circle.setFill(Color.WHITE);
            circle.setStroke(Color.BLACK);
        }
        circle.setStrokeWidth(2);
        if (element.isSelected()) {
            circle.setEffect(new DropShadow(10, Color.GOLD));
        } else {
            circle.setEffect(null);
        }
    }
}
