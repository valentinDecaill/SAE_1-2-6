package merelle.view;

import boardifier.model.GameStageModel;
import boardifier.view.GameStageView;
import boardifier.view.TextLook;
import merelle.model.MerellePawn;
import merelle.model.MerelleStageModel;

/**
 * MerelleStageView creates all the looks for the game elements built by the MerelleStageFactory.
 * The desired UI is the following :
 *
 * player : <name>
 *
 *   A   B   C   D   E   F   G       Black pot      White pot
 * 1 +-----------+-----------+        ╔═╗            ╔═╗
 *   |           |           |        ║B║            ║W║
 * 2 |   +-------+-------+   |        ╠═╣            ╠═╣
 *   |   |       |       |   |        ║B║            ║W║
 * 3 |   |   +---+---+   |   |        ...            ...
 *   |   |   |       |   |   |
 * 4 +---+---+       +---+---+
 *   |   |   |       |   |   |
 * 5 |   |   +---+---+   |   |
 *   |   |       |       |   |
 * 6 |   +-------+-------+   |
 *   |           |           |
 * 7 +-----------+-----------+
 *
 * The main board can not be instanciated directly as a ClassicBoardLook because the Merelle board
 * is not a regular chessboard. So MerelleBoardLook is a subclass of GridLook that draws the board itself.
 * The two pots are instances of MerellePawnPotLook (subclass of TableLook with flexible cells).
 */
public class MerelleStageView extends GameStageView {

    public MerelleStageView(String name, GameStageModel gameStageModel) {
        super(name, gameStageModel);
    }

    public void createLooks() {
        MerelleStageModel model = (MerelleStageModel)gameStageModel;

        // current player name shown on top of the scene
        addLook(new TextLook(model.getPlayerName()));

        // main board
        addLook(new MerelleBoardLook(model.getBoard()));

        // the two pots that store the pawns waiting to be placed
        addLook(new MerellePawnPotLook(model.getBlackPot()));
        addLook(new MerellePawnPotLook(model.getRedPot()));

        // 9 black pawns + 9 white pawns
        for (MerellePawn pawn : model.getBlackPawns()) {
            addLook(new MerellePawnLook(pawn));
        }
        for (MerellePawn pawn : model.getRedPawns()) {
            addLook(new MerellePawnLook(pawn));
        }
    }
}
