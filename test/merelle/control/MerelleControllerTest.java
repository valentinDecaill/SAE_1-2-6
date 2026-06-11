package merelle.control;

import boardifier.model.Coord2D;
import boardifier.model.Model;
import boardifier.view.View;
import merelle.model.MerelleBoard;
import merelle.model.MerellePawn;
import merelle.model.MerellePawnPot;
import merelle.model.MerelleStageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MerelleControllerTest {

    private Model model;
    private MerelleStageModel stage;
    private MerelleController controller;

    @BeforeEach
    public void setup() {
        model = new Model();
        model.addHumanPlayer("p1");
        model.addHumanPlayer("p2");
        stage = new MerelleStageModel("merelle", model);
        stage.createElements(stage.getDefaultElementFactory());
        model.setGameStage(stage);
        controller = new MerelleController(model, new View(model));
    }

    @Test
    public void testTryPlaceValid() {
        boolean result = controller.tryPlace(0, 0, MerellePawn.PAWN_BLACK);
        assertTrue(result);
        assertEquals(MerellePawn.PAWN_BLACK, stage.getBoard().getColorAt(0, 0));
        assertEquals(8, stage.getBlackPawnsToPlace());
    }

    @Test
    public void testTryPlaceInvalidIntersection() {
        boolean result = controller.tryPlace(1, 0, MerellePawn.PAWN_BLACK);
        assertFalse(result);
    }

    @Test
    public void testTryPlaceOccupied() {
        controller.tryPlace(0, 0, MerellePawn.PAWN_BLACK);
        boolean result = controller.tryPlace(0, 0, MerellePawn.PAWN_WHITE);
        assertFalse(result);
    }

    @Test
    public void testTryCaptureValid() {
        MerelleBoard board = stage.getBoard();
        MerellePawn black = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn white = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        board.addElement(black, 0, 0);
        board.addElement(white, 0, 3);

        boolean result = controller.tryCapture(0, 3, MerellePawn.PAWN_BLACK);
        assertTrue(result);
        assertEquals(-1, board.getColorAt(0, 3));
    }

    @Test
    public void testTryCaptureOwnPawn() {
        MerelleBoard board = stage.getBoard();
        MerellePawn black = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        board.addElement(black, 0, 0);

        boolean result = controller.tryCapture(0, 0, MerellePawn.PAWN_BLACK);
        assertFalse(result);
    }

    @Test
    public void testTryCaptureMillProtected() {
        MerelleBoard board = stage.getBoard();
        MerellePawn black1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn black2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn black3 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn white1 = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        MerellePawn white2 = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        board.addElement(black1, 0, 0);
        board.addElement(black2, 0, 3);
        board.addElement(black3, 0, 6);
        board.addElement(white1, 1, 1);
        board.addElement(white2, 1, 3);

        boolean result = controller.tryCapture(1, 1, MerellePawn.PAWN_BLACK);
        assertTrue(result);
    }

    @Test
    public void testTryCaptureMillForbidden() {
        MerelleBoard board = stage.getBoard();
        MerellePawn black1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn black2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn black3 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn white1 = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        MerellePawn white2 = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        MerellePawn white3 = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        MerellePawn white4 = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        board.addElement(black1, 0, 0);
        board.addElement(black2, 0, 3);
        board.addElement(black3, 0, 6);
        board.addElement(white1, 1, 1);
        board.addElement(white2, 1, 3);
        board.addElement(white3, 1, 5);
        board.addElement(white4, 2, 2);

        boolean result = controller.tryCapture(1, 1, MerellePawn.PAWN_BLACK);
        assertFalse(result);
    }

    @Test
    public void testTakeFirstPawn() {
        MerellePawnPot pot = stage.getBlackPot();
        assertNotNull(controller.takeFirstPawn(pot));
    }

    @Test
    public void testAllOpponentPawnsInMill() {
        MerelleBoard board = stage.getBoard();
        MerellePawn black1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn black2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn black3 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        board.addElement(black1, 0, 0);
        board.addElement(black2, 0, 3);
        board.addElement(black3, 0, 6);

        assertTrue(controller.allOpponentPawnsInMill(MerellePawn.PAWN_BLACK));
    }

    @Test
    public void testNotAllOpponentPawnsInMill() {
        MerelleBoard board = stage.getBoard();
        MerellePawn black1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn black2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        board.addElement(black1, 0, 0);
        board.addElement(black2, 0, 3);

        assertFalse(controller.allOpponentPawnsInMill(MerellePawn.PAWN_BLACK));
    }

    @Test
    public void testGetBoardCellFromClick_Center() {
        Coord2D click = new Coord2D(80, 90);
        int[] cell = controller.getBoardCellFromClick(click);
        assertNotNull(cell);
        assertEquals(0, cell[0]);
        assertEquals(0, cell[1]);
    }

    @Test
    public void testGetBoardCellFromClick_Outside() {
        Coord2D click = new Coord2D(10, 10);
        assertNull(controller.getBoardCellFromClick(click));
    }

    @Test
    public void testIsClickOnPawn_Hit() {
        MerelleBoard board = stage.getBoard();
        MerellePawn pawn = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        board.addElement(pawn, 3, 3);
        pawn.setLocation(188, 188, false);
        Coord2D click = new Coord2D(260, 270);
        assertTrue(controller.isClickOnPawn(click, pawn));
    }

    @Test
    public void testIsClickOnPawn_Miss() {
        MerelleBoard board = stage.getBoard();
        MerellePawn pawn = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        board.addElement(pawn, 3, 3);
        Coord2D click = new Coord2D(320, 270);
        assertFalse(controller.isClickOnPawn(click, pawn));
    }

    @Test
    public void testHandleMovementClickSelectPawn() {
        MerelleBoard board = stage.getBoard();
        MerellePawn pawn = stage.getBlackPawns()[0];
        board.addElement(pawn, 3, 3);
        pawn.setLocation(188, 188, false);
        Coord2D click = new Coord2D(260, 270);
        boolean result = controller.handleMovementClick(click, MerellePawn.PAWN_BLACK);
        assertFalse(result);
        assertTrue(pawn.isSelected());
    }

    @Test
    public void testHandleMovementClickDeselectPawn() {
        MerelleBoard board = stage.getBoard();
        MerellePawn pawn = stage.getBlackPawns()[0];
        board.addElement(pawn, 3, 3);
        pawn.setLocation(188, 188, false);
        Coord2D click = new Coord2D(260, 270);
        controller.handleMovementClick(click, MerellePawn.PAWN_BLACK);
        boolean result = controller.handleMovementClick(click, MerellePawn.PAWN_BLACK);
        assertFalse(result);
        assertFalse(pawn.isSelected());
    }

    @Test
    public void testHandleMovementClickChangeSelection() {
        MerelleBoard board = stage.getBoard();
        MerellePawn pawn1 = stage.getBlackPawns()[0];
        MerellePawn pawn2 = stage.getBlackPawns()[1];
        board.addElement(pawn1, 3, 3);
        board.addElement(pawn2, 3, 4);
        pawn1.setLocation(188, 188, false);
        pawn2.setLocation(248, 188, false);

        Coord2D click1 = new Coord2D(260, 270);
        Coord2D click2 = new Coord2D(320, 270);

        controller.handleMovementClick(click1, MerellePawn.PAWN_BLACK);
        assertTrue(pawn1.isSelected());
        assertFalse(pawn2.isSelected());

        controller.handleMovementClick(click2, MerellePawn.PAWN_BLACK);
        assertFalse(pawn1.isSelected());
        assertTrue(pawn2.isSelected());
    }

    @Test
    public void testExecuteCaptureUpdatesDestroyedMill() {
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
        // white mill at row 1
        board.addElement(white1, 1, 1);
        board.addElement(white2, 1, 3);
        board.addElement(white3, 1, 5);

        controller.tryCapture(1, 1, MerellePawn.PAWN_BLACK);
        assertNotNull(stage.getLastDestroyedMill());
        assertEquals(MerellePawn.PAWN_BLACK, stage.getPlayerWhoDestroyedMill());
    }

    @Test
    public void testIsValidMoveReformsDestroyedMill() {
        MerelleBoard board = stage.getBoard();
        MerellePawn black1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn black2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn black3 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn white1 = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        board.addElement(black1, 0, 0);
        board.addElement(black2, 0, 3);
        board.addElement(black3, 0, 6);
        board.addElement(white1, 1, 1);

        controller.tryCapture(1, 1, MerellePawn.PAWN_BLACK);

        // Move black pawn away to break the mill
        board.removeElement(black3);
        board.addElement(black3, 1, 5);

        // Trying to move back to reform the destroyed mill should be invalid
        assertFalse(controller.isValidMove(1, 5, 0, 6, MerellePawn.PAWN_BLACK));
    }

}
