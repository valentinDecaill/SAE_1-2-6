package merelle.model;

import boardifier.model.ContainerElement;
import boardifier.model.GameStageModel;

public class MerellePawnPot extends ContainerElement {
    public MerellePawnPot(String name, int x, int y, GameStageModel gameStageModel) {
        // call the super-constructor to create a 9x1 grid, named "name", at position x,y on the screen.
        // 9 because each player has 9 pawns to place during the placement phase.
        super(name, x, y, 9, 1, gameStageModel);
    }
}
