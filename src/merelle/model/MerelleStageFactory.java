package merelle.model;

import boardifier.model.StageElementsFactory;
import boardifier.model.TextElement;

/**
 * Creates all the game elements of the Merelle stage :
 * the board, the two pots, the 18 pawns and the text element for the current player name.
 * The pawns are placed in their pot at the beginning.
 */
public class MerelleStageFactory extends StageElementsFactory {
    private MerelleStageModel stageModel;

    public MerelleStageFactory(MerelleStageModel gameStageModel) {
        super(gameStageModel);
        stageModel = gameStageModel;
    }

    @Override
    public void setup() {
        // current player name on top of the screen
        TextElement playerName = new TextElement("                    ", stageModel);
        playerName.setLocation(0, 0);
        stageModel.setPlayerName(playerName);

        // main board, just below the player name
        MerelleBoard board = new MerelleBoard(0, 2, stageModel);
        stageModel.setBoard(board);

        // pots on the right of the board
        MerellePawnPot blackPot = new MerellePawnPot("blackPot", 35, 2, stageModel);
        stageModel.setBlackPot(blackPot);

        MerellePawnPot whitePot = new MerellePawnPot("whitePot", 45, 2, stageModel);
        stageModel.setWhitePot(whitePot);

        // 9 black pawns put in the black pot
        MerellePawn[] blackPawns = new MerellePawn[9];
        for (int i = 0; i < 9; i++) {
            blackPawns[i] = new MerellePawn(MerellePawn.PAWN_BLACK, stageModel);
            blackPot.addElement(blackPawns[i], i, 0);
        }
        stageModel.setBlackPawns(blackPawns);

        // 9 white pawns put in the white pot
        MerellePawn[] whitePawns = new MerellePawn[9];
        for (int i = 0; i < 9; i++) {
            whitePawns[i] = new MerellePawn(MerellePawn.PAWN_WHITE, stageModel);
            whitePot.addElement(whitePawns[i], i, 0);
        }
        stageModel.setWhitePawns(whitePawns);
    }
}
