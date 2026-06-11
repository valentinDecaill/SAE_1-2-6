package merelle.model;

import boardifier.model.ContainerElement;
import boardifier.model.GameStageModel;

public class MerellePawnPot extends ContainerElement {
    public MerellePawnPot(String name, int x, int y, GameStageModel gameStageModel) {
        super(name, x, y, 3, 3, gameStageModel);
    }
}
