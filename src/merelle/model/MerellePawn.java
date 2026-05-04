package merelle.model;

import boardifier.model.ElementTypes;
import boardifier.model.GameElement;
import boardifier.model.GameStageModel;

/**
 * A basic pawn element, with only 2 fixed parameters : number and color
 * There are no setters because the state of a Hole pawn is fixed.
 */
public class MerellePawn extends GameElement {

    private int color;
    public static int PAWN_BLACK = 0;
    public static int PAWN_RED = 1;

    public MerellePawn(int color, GameStageModel gameStageModel) {
        super(gameStageModel);
        ElementTypes.register("merellepawn", 50);
        this.type = ElementTypes.getType("merellepawn");
        this.color = color;
    }
    public int getColor() {
        return color;
    }
}
