package merelle.view;

import boardifier.model.ContainerElement;
import boardifier.view.ContainerLook;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MerellePawnPotLook extends ContainerLook {

    public MerellePawnPotLook(ContainerElement containerElement) {
        super(containerElement, 50, 50, 0);
        setVerticalAlignment(ALIGN_MIDDLE);
        setHorizontalAlignment(ALIGN_CENTER);

        int potWidth = 50 * 3;
        int potHeight = 50 * 3;
        Rectangle bg = new Rectangle(potWidth, potHeight);
        bg.setFill(Color.color(0.96, 0.96, 0.96));
        bg.setStroke(Color.color(0.75, 0.75, 0.75));
        getGroup().getChildren().add(0, bg);
    }
}
