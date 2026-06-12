package merelle.view;

import boardifier.model.GameStageModel;
import boardifier.view.GameStageView;
import boardifier.view.TextLook;
import merelle.model.MerellePawn;
import merelle.model.MerelleStageModel;

public class MerelleStageView extends GameStageView {

    public MerelleStageView(String name, GameStageModel gameStageModel) {
        super(name, gameStageModel);
        this.width = 700;
        this.height = 550;
    }

    public void createLooks() {
        MerelleStageModel model = (MerelleStageModel) gameStageModel;
        addLook(new TextLook(16, "0x000000", model.getPlayerName()));
        addLook(new TextLook(16, "0x000000", model.getStatusText()));
        addLook(new MerelleBoardLook(model.getBoard()));
        addLook(new MerellePawnPotLook(model.getBlackPot()));
        addLook(new MerellePawnPotLook(model.getWhitePot()));
        for (MerellePawn pawn : model.getBlackPawns()) {
            addLook(new MerellePawnLook(pawn));
        }
        for (MerellePawn pawn : model.getWhitePawns()) {
            addLook(new MerellePawnLook(pawn));
        }
    }
}
