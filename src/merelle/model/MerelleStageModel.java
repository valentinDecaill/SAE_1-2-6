package merelle.model;

import boardifier.model.GameStageModel;
import boardifier.model.Model;
import boardifier.model.StageElementsFactory;
import boardifier.model.TextElement;

/**
 * Game state for the Merelle stage.
 * Stores the board, the two pots, the 18 pawns and the text for the current player name.
 * Also keeps the placement counters and the capture flag.
 *
 * Phases : placement (each player places 9 pawns), then movement on adjacent intersections,
 * and "flying" (jump anywhere) when a player has only 3 pawns left.
 */
public class MerelleStageModel extends GameStageModel {

    public static final int PHASE_PLACEMENT = 0;
    public static final int PHASE_MOVEMENT = 1;

    // game elements
    private MerelleBoard board;
    private MerellePawnPot blackPot;
    private MerellePawnPot whitePot;
    private MerellePawn[] blackPawns;
    private MerellePawn[] whitePawns;
    private TextElement playerName;

    // state variables
    private int blackPawnsToPlace;
    private int whitePawnsToPlace;
    private boolean captureMode;

    public MerelleStageModel(String name, Model model) {
        super(name, model);
        blackPawnsToPlace = 9;
        whitePawnsToPlace = 9;
        captureMode = false;
    }

    // --- getters ---

    public MerelleBoard getBoard() { return board; }
    public MerellePawnPot getBlackPot() { return blackPot; }
    public MerellePawnPot getWhitePot() { return whitePot; }
    public MerellePawn[] getBlackPawns() { return blackPawns; }
    public MerellePawn[] getWhitePawns() { return whitePawns; }
    public TextElement getPlayerName() { return playerName; }
    public int getBlackPawnsToPlace() { return blackPawnsToPlace; }
    public int getWhitePawnsToPlace() { return whitePawnsToPlace; }
    public boolean isCaptureMode() { return captureMode; }

    // --- setters (used by the factory) ---

    public void setBoard(MerelleBoard board) {
        this.board = board;
        addContainer(board);
    }
    public void setBlackPot(MerellePawnPot blackPot) {
        this.blackPot = blackPot;
        addContainer(blackPot);
    }
    public void setWhitePot(MerellePawnPot whitePot) {
        this.whitePot = whitePot;
        addContainer(whitePot);
    }
    public void setBlackPawns(MerellePawn[] blackPawns) {
        this.blackPawns = blackPawns;
        for (MerellePawn p : blackPawns) addElement(p);
    }
    public void setWhitePawns(MerellePawn[] whitePawns) {
        this.whitePawns = whitePawns;
        for (MerellePawn p : whitePawns) addElement(p);
    }
    public void setPlayerName(TextElement playerName) {
        this.playerName = playerName;
        addElement(playerName);
    }

    // --- state helpers ---

    public void decreaseBlackPawnsToPlace() {
        if (blackPawnsToPlace > 0) blackPawnsToPlace--;
    }
    public void decreaseWhitePawnsToPlace() {
        if (whitePawnsToPlace > 0) whitePawnsToPlace--;
    }
    public void setCaptureMode(boolean captureMode) {
        this.captureMode = captureMode;
    }

    // returns the current phase for the given color
    public int getPhase(int color) {
        if (color == MerellePawn.PAWN_BLACK && blackPawnsToPlace > 0) return PHASE_PLACEMENT;
        if (color == MerellePawn.PAWN_WHITE && whitePawnsToPlace > 0) return PHASE_PLACEMENT;
        return PHASE_MOVEMENT;
    }

    // true when the player has only 3 pawns left (can jump anywhere)
    public boolean isFlying(int color) {
        if (getPhase(color) == PHASE_PLACEMENT) return false;
        return board.countPawns(color) == 3;
    }

    /**
     * Check if the game is over.
     * The game ends when a player has less than 3 pawns left, or cannot move anymore.
     */
    public void checkEndOfGame() {
        // not over while pawns are still being placed
        if (blackPawnsToPlace > 0 || whitePawnsToPlace > 0) return;

        int blackOnBoard = board.countPawns(MerellePawn.PAWN_BLACK);
        int whiteOnBoard = board.countPawns(MerellePawn.PAWN_WHITE);

        if (blackOnBoard < 3) {
            model.setIdWinner(MerellePawn.PAWN_WHITE);
            model.stopStage();
            return;
        }
        if (whiteOnBoard < 3) {
            model.setIdWinner(MerellePawn.PAWN_BLACK);
            model.stopStage();
            return;
        }
        if (!board.canMove(MerellePawn.PAWN_BLACK, isFlying(MerellePawn.PAWN_BLACK))) {
            model.setIdWinner(MerellePawn.PAWN_WHITE);
            model.stopStage();
            return;
        }
        if (!board.canMove(MerellePawn.PAWN_WHITE, isFlying(MerellePawn.PAWN_WHITE))) {
            model.setIdWinner(MerellePawn.PAWN_BLACK);
            model.stopStage();
        }
    }

    @Override
    public StageElementsFactory getDefaultElementFactory() {
        return new MerelleStageFactory(this);
    }
}
