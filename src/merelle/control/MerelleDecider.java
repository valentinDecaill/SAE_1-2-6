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
 * AI player for Merelle. Supports two strategies :
 *   0 = RANDOM  : picks any valid action at random (weak)
 *   1 = HEURISTIC : tries to form mills, block opponent mills, and capture wisely.
 *
 * The strategy is chosen at the beginning of the game via the constructor.
 */
public class MerelleDecider extends Decider {

    public static final int STRATEGY_RANDOM = 0;
    public static final int STRATEGY_HEURISTIC = 1;

    private static final Random random = new Random();
    private int strategy;

    public MerelleDecider(Model model, Controller control) {
        super(model, control);
        // Default strategy : random. Can be changed by the controller before playing.
        this.strategy = STRATEGY_RANDOM;
    }

    public void setStrategy(int strategy) {
        this.strategy = strategy;
    }

    public int getStrategy() {
        return strategy;
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

    // ===================== PLACEMENT =====================

    private ActionList decidePlacement(MerelleStageModel stage, int color) {
        if (strategy == STRATEGY_HEURISTIC) {
            return decidePlacementHeuristic(stage, color);
        }
        return decidePlacementRandom(stage, color);
    }

    private ActionList decidePlacementRandom(MerelleStageModel stage, int color) {
        MerelleBoard board = stage.getBoard();
        List<int[]> empty = getEmptyIntersections(board);
        if (empty.isEmpty()) {
            ActionList emptyList = new ActionList();
            emptyList.setDoEndOfTurn(true);
            return emptyList;
        }
        int[] cell = empty.get(random.nextInt(empty.size()));
        return executePlacement(stage, color, cell[0], cell[1]);
    }

    private ActionList decidePlacementHeuristic(MerelleStageModel stage, int color) {
        MerelleBoard board = stage.getBoard();
        List<int[]> empty = getEmptyIntersections(board);

        if (empty.isEmpty()) {
            ActionList emptyList = new ActionList();
            emptyList.setDoEndOfTurn(true);
            return emptyList;
        }

        // Priority 1 : place a pawn that forms a mill
        for (int[] cell : empty) {
            if (board.formsMill(cell[0], cell[1], color)) {
                return executePlacement(stage, color, cell[0], cell[1]);
            }
        }

        // Priority 2 : block an opponent mill
        int opponent = 1 - color;
        for (int[] cell : empty) {
            if (board.formsMill(cell[0], cell[1], opponent)) {
                return executePlacement(stage, color, cell[0], cell[1]);
            }
        }

        // Priority 3 : place adjacent to own pawns to prepare a mill
        List<int[]> good = new ArrayList<>();
        for (int[] cell : empty) {
            for (int[] adj : getAdjacentIntersections(board, cell[0], cell[1])) {
                if (board.getColorAt(adj[0], adj[1]) == color) {
                    good.add(cell);
                    break;
                }
            }
        }
        if (!good.isEmpty()) {
            int[] cell = good.get(random.nextInt(good.size()));
            return executePlacement(stage, color, cell[0], cell[1]);
        }

        // Fallback : random
        int[] cell = empty.get(random.nextInt(empty.size()));
        return executePlacement(stage, color, cell[0], cell[1]);
    }

    private ActionList executePlacement(MerelleStageModel stage, int color, int row, int col) {
        MerelleBoard board = stage.getBoard();
        MerellePawnPot pot = (color == MerellePawn.PAWN_BLACK) ? stage.getBlackPot() : stage.getWhitePot();
        GameElement pawn = takeFirstPawn(pot);

        if (color == MerellePawn.PAWN_BLACK) stage.decreaseBlackPawnsToPlace();
        else stage.decreaseWhitePawnsToPlace();

        if (board.formsMill(row, col, color)) stage.setCaptureMode(true);

        ActionList actions = ActionFactory.generatePutInContainer(model, pawn, "merelleboard", row, col);
        actions.setDoEndOfTurn(true);
        return actions;
    }

    // ===================== MOVEMENT =====================

    private ActionList decideMovement(MerelleStageModel stage, int color) {
        if (strategy == STRATEGY_HEURISTIC) {
            return decideMovementHeuristic(stage, color);
        }
        return decideMovementRandom(stage, color);
    }

    private ActionList decideMovementRandom(MerelleStageModel stage, int color) {
        MerelleBoard board = stage.getBoard();
        boolean flying = stage.isFlying(color);
        List<int[]> moves = getValidMoves(board, color, flying);

        if (moves.isEmpty()) {
            ActionList empty = new ActionList();
            empty.setDoEndOfTurn(true);
            return empty;
        }

        int[] mv = moves.get(random.nextInt(moves.size()));
        return executeMovement(stage, color, mv[0], mv[1], mv[2], mv[3]);
    }

    private ActionList decideMovementHeuristic(MerelleStageModel stage, int color) {
        MerelleBoard board = stage.getBoard();
        boolean flying = stage.isFlying(color);
        List<int[]> moves = getValidMoves(board, color, flying);

        if (moves.isEmpty()) {
            ActionList empty = new ActionList();
            empty.setDoEndOfTurn(true);
            return empty;
        }

        // Priority 1 : move to form a mill
        for (int[] mv : moves) {
            if (board.formsMill(mv[2], mv[3], color)) {
                return executeMovement(stage, color, mv[0], mv[1], mv[2], mv[3]);
            }
        }

        // Priority 2 : move to block an opponent mill
        int opponent = 1 - color;
        for (int[] mv : moves) {
            if (board.formsMill(mv[2], mv[3], opponent)) {
                return executeMovement(stage, color, mv[0], mv[1], mv[2], mv[3]);
            }
        }

        // Priority 3 : move to a position adjacent to more own pawns
        int bestScore = -1;
        List<int[]> bestMoves = new ArrayList<>();
        for (int[] mv : moves) {
            int score = countAdjacentOwnPawns(board, mv[2], mv[3], color);
            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(mv);
            } else if (score == bestScore) {
                bestMoves.add(mv);
            }
        }
        int[] mv = bestMoves.get(random.nextInt(bestMoves.size()));
        return executeMovement(stage, color, mv[0], mv[1], mv[2], mv[3]);
    }

    private ActionList executeMovement(MerelleStageModel stage, int color, int rSrc, int cSrc, int rDst, int cDst) {
        MerelleBoard board = stage.getBoard();
        GameElement pawn = board.getElement(rSrc, cSrc);

        if (board.formsMill(rDst, cDst, color)) stage.setCaptureMode(true);

        ActionList actions = ActionFactory.generateMoveWithinContainer(model, pawn, rDst, cDst);
        actions.setDoEndOfTurn(true);
        return actions;
    }

    // ===================== CAPTURE =====================

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

        if (strategy == STRATEGY_HEURISTIC && !free.isEmpty()) {
            // Heuristic : capture the pawn that is most dangerous (part of most potential mills)
            int[] best = null;
            int bestThreat = -1;
            for (int[] target : free) {
                int threat = countPotentialMills(board, target[0], target[1], opponent);
                if (threat > bestThreat) {
                    bestThreat = threat;
                    best = target;
                }
            }
            return best;
        }

        if (!free.isEmpty()) return free.get(random.nextInt(free.size()));
        if (!inMill.isEmpty()) return inMill.get(random.nextInt(inMill.size()));
        return null;
    }

    // ===================== HELPERS =====================

    private List<int[]> getEmptyIntersections(MerelleBoard board) {
        List<int[]> empty = new ArrayList<>();
        for (int[] inter : MerelleBoard.INTERSECTIONS) {
            if (board.isEmptyAt(inter[0], inter[1])) empty.add(inter);
        }
        return empty;
    }

    private List<int[]> getValidMoves(MerelleBoard board, int color, boolean flying) {
        List<int[]> moves = new ArrayList<>();
        for (int[] src : MerelleBoard.INTERSECTIONS) {
            if (board.getColorAt(src[0], src[1]) != color) continue;
            for (int[] dst : MerelleBoard.INTERSECTIONS) {
                if (!board.isEmptyAt(dst[0], dst[1])) continue;
                if (!flying && !board.areAdjacent(src[0], src[1], dst[0], dst[1])) continue;
                moves.add(new int[]{ src[0], src[1], dst[0], dst[1] });
            }
        }
        return moves;
    }

    private List<int[]> getAdjacentIntersections(MerelleBoard board, int row, int col) {
        List<int[]> adj = new ArrayList<>();
        for (int[] inter : MerelleBoard.INTERSECTIONS) {
            if (board.areAdjacent(row, col, inter[0], inter[1])) {
                adj.add(inter);
            }
        }
        return adj;
    }

    private int countAdjacentOwnPawns(MerelleBoard board, int row, int col, int color) {
        int count = 0;
        for (int[] adj : getAdjacentIntersections(board, row, col)) {
            if (board.getColorAt(adj[0], adj[1]) == color) count++;
        }
        return count;
    }

    // Count how many mills the opponent could form with a pawn at (row, col)
    private int countPotentialMills(MerelleBoard board, int row, int col, int color) {
        int count = 0;
        for (int[][] mill : getMillsContaining(row, col)) {
            int own = 0;
            int empty = 0;
            for (int[] cell : mill) {
                int c = board.getColorAt(cell[0], cell[1]);
                if (c == color) own++;
                else if (c == -1) empty++;
            }
            if (own == 2 && empty == 1) count++;
        }
        return count;
    }

    private List<int[][]> getMillsContaining(int row, int col) {
        List<int[][]> result = new ArrayList<>();
        // All 16 mills of the Merelle board
        int[][][] allMills = {
                { {0,0},{0,3},{0,6} }, { {1,1},{1,3},{1,5} }, { {2,2},{2,3},{2,4} },
                { {3,0},{3,1},{3,2} }, { {3,4},{3,5},{3,6} },
                { {4,2},{4,3},{4,4} }, { {5,1},{5,3},{5,5} }, { {6,0},{6,3},{6,6} },
                { {0,0},{3,0},{6,0} }, { {1,1},{3,1},{5,1} }, { {2,2},{3,2},{4,2} },
                { {0,3},{1,3},{2,3} }, { {4,3},{5,3},{6,3} },
                { {2,4},{3,4},{4,4} }, { {1,5},{3,5},{5,5} }, { {0,6},{3,6},{6,6} }
        };
        for (int[][] mill : allMills) {
            for (int[] cell : mill) {
                if (cell[0] == row && cell[1] == col) {
                    result.add(mill);
                    break;
                }
            }
        }
        return result;
    }

    private GameElement takeFirstPawn(MerellePawnPot pot) {
        for (int i = 0; i < 9; i++) {
            if (!pot.isEmptyAt(i, 0)) return pot.getElement(i, 0);
        }
        return null;
    }
}
