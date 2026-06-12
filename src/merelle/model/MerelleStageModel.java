package merelle.model;

import boardifier.model.GameStageModel;
import boardifier.model.Model;
import boardifier.model.StageElementsFactory;
import boardifier.model.TextElement;

import java.util.HashMap;
import java.util.Map;

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
    private TextElement statusText;
    private TextElement phaseText;

    // state variables
    private int blackPawnsToPlace;
    private int whitePawnsToPlace;
    private boolean captureMode;
    private java.util.List<int[][]> destroyedMills = new java.util.ArrayList<>();
    private int playerWhoDestroyedMill = -1;
    private int turnsSinceMillDestroyed = 0;
    private int[][][] lastMillFormed = new int[2][][];
    private Map<String, Integer> positionHistory;

    public MerelleStageModel(String name, Model model) {
        super(name, model);
        blackPawnsToPlace = 9;
        whitePawnsToPlace = 9;
        captureMode = false;
        positionHistory = new HashMap<>();
    }

    // --- getters ---

    public MerelleBoard getBoard() { return board; }
    public MerellePawnPot getBlackPot() { return blackPot; }
    public MerellePawnPot getWhitePot() { return whitePot; }
    public MerellePawn[] getBlackPawns() { return blackPawns; }
    public MerellePawn[] getWhitePawns() { return whitePawns; }
    public TextElement getPlayerName() { return playerName; }
    public TextElement getStatusText() { return statusText; }
    public TextElement getPhaseText() { return phaseText; }
    public int getBlackPawnsToPlace() { return blackPawnsToPlace; }
    public int getWhitePawnsToPlace() { return whitePawnsToPlace; }
    public boolean isCaptureMode() { return captureMode; }
    public int[][] getLastDestroyedMill() {
        return destroyedMills.isEmpty() ? null : destroyedMills.get(0);
    }
    public java.util.List<int[][]> getDestroyedMills() { return destroyedMills; }
    public int getPlayerWhoDestroyedMill() { return playerWhoDestroyedMill; }

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
    public void setStatusText(TextElement statusText) {
        this.statusText = statusText;
        addElement(statusText);
    }
    public void setPhaseText(TextElement phaseText) {
        this.phaseText = phaseText;
        addElement(phaseText);
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
    public void setLastDestroyedMill(int[][] mill, int player) {
        destroyedMills.clear();
        destroyedMills.add(mill);
        playerWhoDestroyedMill = player;
        turnsSinceMillDestroyed = 0;
    }
    public void addDestroyedMill(int[][] mill) {
        destroyedMills.add(mill);
    }
    public void clearLastDestroyedMill() {
        destroyedMills.clear();
        playerWhoDestroyedMill = -1;
        turnsSinceMillDestroyed = 0;
    }
    public void incrementTurnsSinceMillDestroyed() {
        turnsSinceMillDestroyed++;
    }
    public int getTurnsSinceMillDestroyed() {
        return turnsSinceMillDestroyed;
    }
    public int[][] getLastMillFormed(int color) { return lastMillFormed[color]; }
    public void setLastMillFormed(int color, int[][] mill) { lastMillFormed[color] = mill; }

    // If the mills formed at (row,col) are exactly the same as the player's last mill,
    // do NOT allow capture. Otherwise, enter capture mode and remember this mill.
    public void checkAndSetCaptureMode(MerelleBoard board, int row, int col, int color) {
        java.util.List<int[][]> formedMills = board.getAllMillsContaining(row, col);
        if (formedMills.isEmpty()) return;
        int[][] lastMill = getLastMillFormed(color);
        boolean shouldCapture = false;
        if (formedMills.size() > 1) {
            shouldCapture = true;
        } else {
            if (!MerelleBoard.isSameMill(formedMills.get(0), lastMill)) {
                shouldCapture = true;
            }
        }
        if (shouldCapture) {
            setCaptureMode(true);
            setLastMillFormed(color, formedMills.get(0));
        }
    }

    // --- draw by repetition ---

    public void recordPosition() {
        String hash = computePositionHash();
        positionHistory.put(hash, positionHistory.getOrDefault(hash, 0) + 1);
    }

    private String computePositionHash() {
        StringBuilder sb = new StringBuilder();
        sb.append(model.getIdPlayer());
        for (int[] inter : MerelleBoard.INTERSECTIONS) {
            sb.append(board.getColorAt(inter[0], inter[1]));
        }
        sb.append(blackPawnsToPlace).append(whitePawnsToPlace);
        sb.append(captureMode ? 'C' : 'N');
        return sb.toString();
    }

    public boolean isDrawByRepetition() {
        for (int count : positionHistory.values()) {
            if (count >= 3) return true;
        }
        return false;
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
    private boolean hasValidMove(int color) {
        boolean flying = isFlying(color);
        MerelleBoard board = getBoard();
        boolean hasRestriction = (!destroyedMills.isEmpty() && getPlayerWhoDestroyedMill() == color);

        for (int[] src : MerelleBoard.INTERSECTIONS) {
            if (board.getColorAt(src[0], src[1]) != color) continue;
            for (int[] dst : MerelleBoard.INTERSECTIONS) {
                if (!board.isEmptyAt(dst[0], dst[1])) continue;
                if (!flying && !board.areAdjacent(src[0], src[1], dst[0], dst[1])) continue;
                if (hasRestriction) {
                    boolean reformsAny = false;
                    for (int[][] mill : destroyedMills) {
                        if (MerelleBoard.wouldReformMill(board, mill, src[0], src[1], dst[0], dst[1], color)) {
                            reformsAny = true;
                            break;
                        }
                    }
                    if (reformsAny) continue;
                }
                return true;
            }
        }
        return false;
    }

    public void checkEndOfGame() {
        // not over while pawns are still being placed
        if (blackPawnsToPlace > 0 || whitePawnsToPlace > 0) return;

        if (isDrawByRepetition()) {
            model.setIdWinner(-1);
            model.stopStage();
            return;
        }

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
        if (!hasValidMove(MerellePawn.PAWN_BLACK)) {
            model.setIdWinner(MerellePawn.PAWN_WHITE);
            model.stopStage();
            return;
        }
        if (!hasValidMove(MerellePawn.PAWN_WHITE)) {
            model.setIdWinner(MerellePawn.PAWN_BLACK);
            model.stopStage();
        }
    }

    @Override
    public StageElementsFactory getDefaultElementFactory() {
        return new MerelleStageFactory(this);
    }
}
