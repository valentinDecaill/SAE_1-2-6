package merelle.model;

import boardifier.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MerelleBoardTest {

    private MerelleBoard board;
    private MerelleStageModel stage;

    @BeforeEach
    public void setup() {
        Model model = new Model();
        stage = new MerelleStageModel("test", model);
        board = new MerelleBoard(0, 0, stage);
    }

    @Test
    public void testIsValidIntersection() {
        assertTrue(board.isValidIntersection(0, 0));
        assertTrue(board.isValidIntersection(0, 3));
        assertTrue(board.isValidIntersection(0, 6));
        assertFalse(board.isValidIntersection(3, 3));
        assertTrue(board.isValidIntersection(6, 6));
        assertFalse(board.isValidIntersection(1, 0));
        assertFalse(board.isValidIntersection(2, 0));
        assertFalse(board.isValidIntersection(0, 1));
        assertFalse(board.isValidIntersection(-1, 0));
        assertFalse(board.isValidIntersection(7, 0));
    }

    @Test
    public void testAreAdjacent() {
        assertTrue(board.areAdjacent(0, 0, 0, 3));
        assertTrue(board.areAdjacent(0, 3, 0, 6));
        assertTrue(board.areAdjacent(0, 0, 3, 0));
        assertTrue(board.areAdjacent(3, 0, 6, 0));
        assertTrue(board.areAdjacent(1, 1, 1, 3));
        assertTrue(board.areAdjacent(2, 2, 2, 3));
        assertFalse(board.areAdjacent(0, 0, 0, 6));
        assertFalse(board.areAdjacent(0, 0, 1, 1));
        assertFalse(board.areAdjacent(0, 0, 6, 6));
        assertFalse(board.areAdjacent(0, 0, 0, 0));
    }

    @Test
    public void testFormsMill() {
        MerellePawn p1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        board.addElement(p1, 0, 0);
        board.addElement(p2, 0, 3);
        assertTrue(board.formsMill(0, 6, MerellePawn.PAWN_BLACK));
        assertFalse(board.formsMill(0, 6, MerellePawn.PAWN_WHITE));
    }

    @Test
    public void testIsInMill() {
        MerellePawn p1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p3 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        board.addElement(p1, 0, 0);
        board.addElement(p2, 0, 3);
        board.addElement(p3, 0, 6);
        assertTrue(board.isInMill(0, 0));
        assertTrue(board.isInMill(0, 3));
        assertTrue(board.isInMill(0, 6));
    }

    @Test
    public void testCountPawns() {
        assertEquals(0, board.countPawns(MerellePawn.PAWN_BLACK));
        assertEquals(0, board.countPawns(MerellePawn.PAWN_WHITE));
        MerellePawn p1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p2 = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        board.addElement(p1, 0, 0);
        board.addElement(p2, 0, 3);
        assertEquals(1, board.countPawns(MerellePawn.PAWN_BLACK));
        assertEquals(1, board.countPawns(MerellePawn.PAWN_WHITE));
    }

    @Test
    public void testCanMove() {
        assertFalse(board.canMove(MerellePawn.PAWN_BLACK, false));
        MerellePawn p1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        board.addElement(p1, 0, 0);
        assertTrue(board.canMove(MerellePawn.PAWN_BLACK, false));
        MerellePawn p2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p3 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        board.addElement(p2, 0, 3);
        board.addElement(p3, 0, 6);
        assertTrue(board.canMove(MerellePawn.PAWN_BLACK, false));
        assertTrue(board.canMove(MerellePawn.PAWN_BLACK, true));
    }

    @Test
    public void testIsSameMill() {
        int[][] mill1 = { {0,0}, {0,3}, {0,6} };
        int[][] mill2 = { {0,6}, {0,0}, {0,3} };
        int[][] mill3 = { {1,1}, {1,3}, {1,5} };
        assertTrue(MerelleBoard.isSameMill(mill1, mill2));
        assertFalse(MerelleBoard.isSameMill(mill1, mill3));
        assertFalse(MerelleBoard.isSameMill(mill1, null));
        assertFalse(MerelleBoard.isSameMill(null, mill2));
    }

    @Test
    public void testGetAllMillsContaining() {
        MerellePawn p1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p3 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        board.addElement(p1, 0, 0);
        board.addElement(p2, 0, 3);
        board.addElement(p3, 0, 6);
        java.util.List<int[][]> mills = board.getAllMillsContaining(0, 3);
        assertEquals(1, mills.size());
        assertTrue(MerelleBoard.isSameMill(mills.get(0), new int[][]{{0,0},{0,3},{0,6}}));
    }

    @Test
    public void testGetAllMillsContainingDoubleMill() {
        // Place pawns to form two mills at the center (0,3)-(3,3)-(6,3) and (2,3)-(3,3)-(4,3)
        // Actually center is (3,3) which is not a valid intersection in standard Merelle.
        // Let's use (3,2) which is part of horizontal mill (3,0)-(3,1)-(3,2)
        // and vertical mill (2,2)-(3,2)-(4,2)
        MerellePawn p1 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p2 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p3 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p4 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn p5 = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        board.addElement(p1, 3, 0);
        board.addElement(p2, 3, 1);
        board.addElement(p3, 3, 2);
        board.addElement(p4, 2, 2);
        board.addElement(p5, 4, 2);
        java.util.List<int[][]> mills = board.getAllMillsContaining(3, 2);
        assertEquals(2, mills.size());
    }
}
