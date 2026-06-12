package merelle.model;

import boardifier.model.StageElementsFactory;
import boardifier.model.TextElement;

public class MerelleStageFactory extends StageElementsFactory {
    private MerelleStageModel stageModel;

    public MerelleStageFactory(MerelleStageModel gameStageModel) {
        super(gameStageModel);
        stageModel = gameStageModel;
    }

    @Override
    public void setup() {
        TextElement playerName = new TextElement("                    ", stageModel);
        playerName.setLocation(50, 10);
        stageModel.setPlayerName(playerName);

        TextElement statusText = new TextElement(" ", stageModel);
        statusText.setLocation(50, 32);
        stageModel.setStatusText(statusText);

        TextElement phaseText = new TextElement("Phase 1: Placement", stageModel);
        phaseText.setLocation(520, 35);
        stageModel.setPhaseText(phaseText);

        MerelleBoard board = new MerelleBoard(50, 60, stageModel);
        stageModel.setBoard(board);

        MerellePawnPot blackPot = new MerellePawnPot("blackPot", 500, 60, stageModel);
        stageModel.setBlackPot(blackPot);

        MerellePawnPot whitePot = new MerellePawnPot("whitePot", 500, 300, stageModel);
        stageModel.setWhitePot(whitePot);

        MerellePawn[] blackPawns = new MerellePawn[9];
        for (int i = 0; i < 9; i++) {
            blackPawns[i] = new MerellePawn(MerellePawn.PAWN_BLACK, stageModel);
            blackPot.addElement(blackPawns[i], i / 3, i % 3);
        }
        stageModel.setBlackPawns(blackPawns);

        MerellePawn[] whitePawns = new MerellePawn[9];
        for (int i = 0; i < 9; i++) {
            whitePawns[i] = new MerellePawn(MerellePawn.PAWN_WHITE, stageModel);
            whitePot.addElement(whitePawns[i], i / 3, i % 3);
        }
        stageModel.setWhitePawns(whitePawns);
    }
}
