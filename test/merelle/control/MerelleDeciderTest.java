package merelle.control;

import boardifier.model.Model;
import boardifier.model.action.ActionList;
import boardifier.view.View;
import merelle.model.MerelleBoard;
import merelle.model.MerellePawn;
import merelle.model.MerelleStageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MerelleDeciderTest {

    private Model model;
    private MerelleStageModel stage;
    private MerelleController controller;
    private MerelleDecider decider;

    @BeforeEach
    public void setup() {
        model = new Model();
        model.addHumanPlayer("p1");
        model.addComputerPlayer("cpu");
        stage = new MerelleStageModel("merelle", model);
        stage.createElements(stage.getDefaultElementFactory());
        model.setGameStage(stage);
        controller = new MerelleController(model, new View(model));
        decider = new MerelleDecider(model, controller);
    }

    @Test
    public void testDecidePlacementRandom() {
        decider.setStrategy(MerelleDecider.STRATEGY_RANDOM);
        ActionList actions = decider.decide();
        assertNotNull(actions);
        assertFalse(actions.getActions().isEmpty());
    }

    @Test
    public void testDecidePlacementHeuristicFormsMill() {
        MerelleBoard board = stage.getBoard();
        board.addElement(stage.getBlackPawns()[0], 0, 0);
        board.addElement(stage.getBlackPawns()[1], 0, 3);
        stage.decreaseBlackPawnsToPlace();
        stage.decreaseBlackPawnsToPlace();

        decider.setStrategy(MerelleDecider.STRATEGY_HEURISTIC);
        ActionList actions = decider.decide();
        assertNotNull(actions);

        assertTrue(board.isEmptyAt(0, 6));
    }

    @Test
    public void testChooseCaptureTarget() {
        MerelleBoard board = stage.getBoard();
        MerellePawn black1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn black2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn white1 = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        board.addElement(black1, 0, 0);
        board.addElement(black2, 0, 3);
        board.addElement(white1, 1, 1);

        int[] target = decider.chooseCaptureTarget(MerellePawn.PAWN_BLACK);
        assertNotNull(target);
        assertEquals(1, target[0]);
        assertEquals(1, target[1]);
    }

    @Test
    public void testChooseCaptureTargetRespectsMillRule() {
        MerelleBoard board = stage.getBoard();
        MerellePawn black1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn black2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn black3 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn white1 = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        MerellePawn white2 = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        MerellePawn white3 = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        board.addElement(black1, 0, 0);
        board.addElement(black2, 0, 3);
        board.addElement(black3, 0, 6);
        board.addElement(white1, 1, 1);
        board.addElement(white2, 1, 3);
        board.addElement(white3, 1, 5);

        int[] target = decider.chooseCaptureTarget(MerellePawn.PAWN_BLACK);
        assertNotNull(target);
        assertTrue(board.isInMill(target[0], target[1]));
    }
}
