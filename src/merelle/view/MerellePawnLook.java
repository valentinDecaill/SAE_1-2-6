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
    private boolean capturable = false;

    public MerellePawnLook(GameElement element) {
        super(element, 1);
        setAnchorType(ANCHOR_TOPLEFT);
        circle = new Circle(RADIUS, RADIUS, RADIUS);
        getGroup().getChildren().add(circle);
    }

    public void setCapturable(boolean capturable) {
        this.capturable = capturable;
        render();
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
        } else if (capturable) {
            circle.setEffect(new DropShadow(10, Color.RED));
        } else {
            circle.setEffect(null);
        }
    }
}
