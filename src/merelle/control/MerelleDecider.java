package merelle.control;

import boardifier.control.ActionFactory;
import boardifier.control.Controller;
import boardifier.control.Decider;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import merelle.model.MerelleBoard;
import merelle.model.MerellePawn;
import merelle.model.MerellePawnPot;
import merelle.model.MerelleStageModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AI player for Merelle. Uses a random strategy : picks any valid action.
 */
public class MerelleDecider extends Decider {

    private static final Random random = new Random();

    public MerelleDecider(Model model, Controller control) {
        super(model, control);
    }

    @Override
    public ActionList decide() {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        int color = model.getIdPlayer();
        if (stage.getPhase(color) == MerelleStageModel.PHASE_PLACEMENT) {
            return decidePlacement(stage, color);
        }
        return decideMovement(stage, color);
    }

    // pick a random empty intersection and put a pawn there
    private ActionList decidePlacement(MerelleStageModel stage, int color) {
        MerelleBoard board = stage.getBoard();
        List<int[]> empty = new ArrayList<>();
        for (int[] inter : MerelleBoard.INTERSECTIONS) {
            if (board.isEmptyAt(inter[0], inter[1])) empty.add(inter);
        }
        int[] cell = empty.get(random.nextInt(empty.size()));

        MerellePawnPot pot = (color == MerellePawn.PAWN_BLACK) ? stage.getBlackPot() : stage.getWhitePot();
        GameElement pawn = takeFirstPawn(pot);

        if (color == MerellePawn.PAWN_BLACK) stage.decreaseBlackPawnsToPlace();
        else stage.decreaseWhitePawnsToPlace();

        if (board.formsMill(cell[0], cell[1], color)) stage.setCaptureMode(true);

        ActionList actions = ActionFactory.generatePutInContainer(model, pawn, "merelleboard", cell[0], cell[1]);
        actions.setDoEndOfTurn(true);
        return actions;
    }

    // pick a random valid (src, dst) move on the board
    private ActionList decideMovement(MerelleStageModel stage, int color) {
        MerelleBoard board = stage.getBoard();
        boolean flying = stage.isFlying(color);
        List<int[]> moves = new ArrayList<>();
        for (int[] src : MerelleBoard.INTERSECTIONS) {
            if (board.getColorAt(src[0], src[1]) != color) continue;
            for (int[] dst : MerelleBoard.INTERSECTIONS) {
                if (!board.isEmptyAt(dst[0], dst[1])) continue;
                if (!flying && !board.areAdjacent(src[0], src[1], dst[0], dst[1])) continue;
                moves.add(new int[]{ src[0], src[1], dst[0], dst[1] });
            }
        }
        if (moves.isEmpty()) {
            ActionList empty = new ActionList();
            empty.setDoEndOfTurn(true);
            return empty;
        }

        int[] mv = moves.get(random.nextInt(moves.size()));
        GameElement pawn = board.getElement(mv[0], mv[1]);
        if (board.formsMill(mv[2], mv[3], color)) stage.setCaptureMode(true);

        ActionList actions = ActionFactory.generateMoveWithinContainer(model, pawn, mv[2], mv[3]);
        actions.setDoEndOfTurn(true);
        return actions;
    }

    /**
     * Called by the controller after a mill : returns {row, col} of an opponent pawn to capture.
     * The rule forbids capturing a pawn in a mill, unless all opponent pawns are in mills.
     */
    public int[] chooseCaptureTarget(int color) {
        MerelleBoard board = ((MerelleStageModel) model.getGameStage()).getBoard();
        int opponent = 1 - color;
        List<int[]> free = new ArrayList<>();
        List<int[]> inMill = new ArrayList<>();
        for (int[] inter : MerelleBoard.INTERSECTIONS) {
            if (board.getColorAt(inter[0], inter[1]) != opponent) continue;
            if (board.isInMill(inter[0], inter[1])) inMill.add(inter);
            else free.add(inter);
        }
        if (!free.isEmpty()) return free.get(random.nextInt(free.size()));
        if (!inMill.isEmpty()) return inMill.get(random.nextInt(inMill.size()));
        return null;
    }

    private GameElement takeFirstPawn(MerellePawnPot pot) {
        for (int i = 0; i < 9; i++) {
            if (!pot.isEmptyAt(i, 0)) return pot.getElement(i, 0);
        }
        return null;
    }
}
