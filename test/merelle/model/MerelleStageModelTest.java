package merelle.model;

import boardifier.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MerelleStageModelTest {

    private MerelleStageModel stage;
    private Model model;

    @BeforeEach
    public void setup() {
        model = new Model();
        stage = new MerelleStageModel("test", model);
        MerelleBoard board = new MerelleBoard(0, 0, stage);
        stage.setBoard(board);
    }

    @Test
    public void testGetPhase() {
        assertEquals(MerelleStageModel.PHASE_PLACEMENT, stage.getPhase(MerellePawn.PAWN_BLACK));
        assertEquals(MerelleStageModel.PHASE_PLACEMENT, stage.getPhase(MerellePawn.PAWN_WHITE));
        stage.decreaseBlackPawnsToPlace();
        stage.decreaseBlackPawnsToPlace();
        stage.decreaseBlackPawnsToPlace();
        stage.decreaseBlackPawnsToPlace();
        stage.decreaseBlackPawnsToPlace();
        stage.decreaseBlackPawnsToPlace();
        stage.decreaseBlackPawnsToPlace();
        stage.decreaseBlackPawnsToPlace();
        stage.decreaseBlackPawnsToPlace();
        assertEquals(MerelleStageModel.PHASE_MOVEMENT, stage.getPhase(MerellePawn.PAWN_BLACK));
        assertEquals(MerelleStageModel.PHASE_PLACEMENT, stage.getPhase(MerellePawn.PAWN_WHITE));
    }

    @Test
    public void testIsFlying() {
        assertFalse(stage.isFlying(MerellePawn.PAWN_BLACK));
        for (int i = 0; i < 9; i++) stage.decreaseBlackPawnsToPlace();
        MerellePawn p1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p3 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        stage.getBoard().addElement(p1, 0, 0);
        stage.getBoard().addElement(p2, 0, 3);
        stage.getBoard().addElement(p3, 0, 6);
        assertTrue(stage.isFlying(MerellePawn.PAWN_BLACK));
    }

    @Test
    public void testCaptureMode() {
        assertFalse(stage.isCaptureMode());
        stage.setCaptureMode(true);
        assertTrue(stage.isCaptureMode());
        stage.setCaptureMode(false);
        assertFalse(stage.isCaptureMode());
    }

    @Test
    public void testCheckEndOfGameLessThanTwo() {
        for (int i = 0; i < 9; i++) {
            stage.decreaseBlackPawnsToPlace();
            stage.decreaseWhitePawnsToPlace();
        }
        MerellePawn p1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        stage.getBoard().addElement(p1, 0, 0);
        stage.checkEndOfGame();
        assertTrue(model.isEndStage());
        assertEquals(MerellePawn.PAWN_WHITE, model.getIdWinner());
    }

    @Test
    public void testCheckEndOfGameTwoPawns() {
        for (int i = 0; i < 9; i++) {
            stage.decreaseBlackPawnsToPlace();
            stage.decreaseWhitePawnsToPlace();
        }
        MerellePawn p1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        stage.getBoard().addElement(p1, 0, 0);
        stage.getBoard().addElement(p2, 0, 3);
        stage.checkEndOfGame();
        assertTrue(model.isEndStage());
        assertEquals(MerellePawn.PAWN_WHITE, model.getIdWinner());
    }

    @Test
    public void testCheckEndOfGameBlocked() {
        for (int i = 0; i < 9; i++) {
            stage.decreaseBlackPawnsToPlace();
            stage.decreaseWhitePawnsToPlace();
        }
        MerellePawn p1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p2 = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        MerellePawn p3 = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        MerellePawn p4 = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        stage.getBoard().addElement(p1, 0, 0);
        stage.getBoard().addElement(p2, 0, 3);
        stage.getBoard().addElement(p3, 0, 6);
        stage.getBoard().addElement(p4, 3, 0);
        stage.checkEndOfGame();
        assertTrue(model.isEndStage());
        assertEquals(MerellePawn.PAWN_WHITE, model.getIdWinner());
    }

    @Test
    public void testCheckAndSetCaptureModeFirstMill() {
        MerellePawn p1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p3 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        stage.getBoard().addElement(p1, 0, 0);
        stage.getBoard().addElement(p2, 0, 3);
        stage.getBoard().addElement(p3, 0, 6);
        stage.checkAndSetCaptureMode(stage.getBoard(), 0, 6, MerellePawn.PAWN_BLACK);
        assertTrue(stage.isCaptureMode());
        assertNotNull(stage.getLastMillFormed(MerellePawn.PAWN_BLACK));
    }

    @Test
    public void testCheckAndSetCaptureModeSameMillNoCapture() {
        MerellePawn p1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p3 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        stage.getBoard().addElement(p1, 0, 0);
        stage.getBoard().addElement(p2, 0, 3);
        stage.getBoard().addElement(p3, 0, 6);
        // first formation -> capture allowed
        stage.checkAndSetCaptureMode(stage.getBoard(), 0, 6, MerellePawn.PAWN_BLACK);
        assertTrue(stage.isCaptureMode());
        stage.setCaptureMode(false);
        // reform the exact same mill -> capture forbidden
        stage.checkAndSetCaptureMode(stage.getBoard(), 0, 6, MerellePawn.PAWN_BLACK);
        assertFalse(stage.isCaptureMode());
    }

    @Test
    public void testDrawByRepetition() {
        for (int i = 0; i < 9; i++) {
            stage.decreaseBlackPawnsToPlace();
            stage.decreaseWhitePawnsToPlace();
        }
        stage.recordPosition();
        stage.recordPosition();
        stage.recordPosition();
        stage.checkEndOfGame();
        assertTrue(model.isEndStage());
        assertEquals(-1, model.getIdWinner());
    }

    @Test
    public void testCheckAndSetCaptureModeDifferentMill() {
        MerellePawn p1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p3 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        stage.getBoard().addElement(p1, 0, 0);
        stage.getBoard().addElement(p2, 0, 3);
        stage.getBoard().addElement(p3, 0, 6);
        // first mill
        stage.checkAndSetCaptureMode(stage.getBoard(), 0, 6, MerellePawn.PAWN_BLACK);
        assertTrue(stage.isCaptureMode());
        stage.setCaptureMode(false);
        // different mill
        MerellePawn p5 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p6 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p7 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        stage.getBoard().addElement(p5, 1, 1);
        stage.getBoard().addElement(p6, 1, 3);
        stage.getBoard().addElement(p7, 1, 5);
        stage.checkAndSetCaptureMode(stage.getBoard(), 1, 5, MerellePawn.PAWN_BLACK);
        assertTrue(stage.isCaptureMode());
    }

    @Test
    public void testTurnsSinceMillDestroyed() {
        stage.setLastDestroyedMill(new int[][]{{0,0},{0,3},{0,6}}, MerellePawn.PAWN_BLACK);
        assertEquals(0, stage.getTurnsSinceMillDestroyed());
        stage.incrementTurnsSinceMillDestroyed();
        assertEquals(1, stage.getTurnsSinceMillDestroyed());
        stage.incrementTurnsSinceMillDestroyed();
        assertEquals(2, stage.getTurnsSinceMillDestroyed());
        stage.incrementTurnsSinceMillDestroyed();
        assertEquals(3, stage.getTurnsSinceMillDestroyed());
        stage.clearLastDestroyedMill();
        assertEquals(0, stage.getTurnsSinceMillDestroyed());
        assertTrue(stage.getDestroyedMills().isEmpty());
    }

}
