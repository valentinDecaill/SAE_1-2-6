package merelle.control;

import boardifier.control.ActionFactory;
import boardifier.control.ActionPlayer;
import boardifier.control.Controller;
import boardifier.model.Coord2D;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.Player;
import boardifier.model.action.ActionList;
import boardifier.view.ElementLook;
import boardifier.view.View;
import merelle.model.MerelleBoard;
import merelle.model.MerellePawn;
import merelle.model.MerellePawnPot;
import merelle.model.MerelleStageModel;
import merelle.view.MerelleBoardLook;
import merelle.view.MerellePawnLook;

public class MerelleController extends Controller {

    private int aiStrategy = MerelleDecider.STRATEGY_RANDOM;
    private GameElement selectedPawn = null;
    private static final int CELL_SIZE = 60;
    private static final double PAWN_RADIUS = MerellePawnLook.RADIUS;

    public MerelleController(Model model, View view) {
        super(model, view);
    }

    public void setAiStrategy(int strategy) {
        this.aiStrategy = strategy;
    }

    private boolean isJavaFxAvailable() {
        try {
            javafx.application.Platform.isFxApplicationThread();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private void updateStatusText() {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        String name = model.getCurrentPlayer().getName();
        String msg;
        if (stage.isCaptureMode()) {
            msg = name + " - Capture an opponent pawn";
        } else if (stage.getPhase(model.getIdPlayer()) == MerelleStageModel.PHASE_PLACEMENT) {
            msg = name + " - Place a pawn";
        } else if (stage.isFlying(model.getIdPlayer())) {
            msg = name + " - Fly a pawn";
        } else {
            msg = name + " - Move a pawn";
        }
        stage.getStatusText().setText(msg);
    }

    @Override
    public void update() {
        try {
            if (javafx.application.Platform.isFxApplicationThread()) {
                super.update();
                return;
            }
        } catch (IllegalStateException e) {
            super.update();
            return;
        }
        javafx.application.Platform.runLater(() -> super.update());
    }

    @Override
    public void stageLoop() {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        updateStatusText();
        update();
        while (!model.isEndStage()) {
            playTurn();
            stage.recordPosition();
            stage.checkEndOfGame();
            if (!model.isEndStage()) {
                endOfTurn();
                updateStatusText();
            }
            update();
        }
        endGame();
    }

    private void playTurn() {
        Player p = model.getCurrentPlayer();
        if (p.getType() == Player.COMPUTER) {
            playComputerTurn();
        } else {
            playHumanTurn();
        }
    }

    private void playComputerTurn() {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();

        MerelleDecider decider = new MerelleDecider(model, this);
        decider.setStrategy(aiStrategy);
        ActionList actions = decider.decide();
        ActionPlayer play = new ActionPlayer(model, this, actions);
        play.start();

        int destRow = -1, destCol = -1;
        for (java.util.List<boardifier.model.action.GameAction> pack : actions.getActions()) {
            for (boardifier.model.action.GameAction action : pack) {
                if (action instanceof boardifier.model.action.PutInContainerAction) {
                    destRow = ((boardifier.model.action.PutInContainerAction) action).getRowDest();
                    destCol = ((boardifier.model.action.PutInContainerAction) action).getColDest();
                } else if (action instanceof boardifier.model.action.MoveWithinContainerAction) {
                    destRow = ((boardifier.model.action.MoveWithinContainerAction) action).getRowDest();
                    destCol = ((boardifier.model.action.MoveWithinContainerAction) action).getColDest();
                }
            }
        }
        if (destRow != -1) {
            stage.checkAndSetCaptureMode(stage.getBoard(), destRow, destCol, model.getIdPlayer());
        }

        if (stage.isCaptureMode()) {
            int[] target = decider.chooseCaptureTarget(model.getIdPlayer());
            if (target != null) {
                tryCapture(target[0], target[1], model.getIdPlayer());
            }
            stage.setCaptureMode(false);
        }
        updateStatusText();
    }

    private void playHumanTurn() {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        int color = model.getIdPlayer();
        boolean done = false;

        while (!done && !model.isEndStage()) {
            Coord2D click = waitForClick();
            if (click == null) continue;

            if (stage.isCaptureMode()) {
                done = handleCaptureClick(click, color);
                update();
                continue;
            }

            if (stage.getPhase(color) == MerelleStageModel.PHASE_PLACEMENT) {
                if (handlePlacementClick(click, color)) {
                    done = !stage.isCaptureMode();
                }
            } else {
                if (handleMovementClick(click, color)) {
                    done = !stage.isCaptureMode();
                }
            }
            update();
        }
    }

    private Coord2D waitForClick() {
        model.setCaptureMouseEvent(true);
        Coord2D click;
        do {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return null;
            }
            click = model.getLastClick();
        } while (click.getX() == -1 && !model.isEndStage());
        model.setLastClick(new Coord2D(-1, -1));
        model.setCaptureMouseEvent(false);
        return click;
    }

    private MerellePawn findPawnAtClick(Coord2D click) {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        for (MerellePawn pawn : stage.getBlackPawns()) {
            if (isClickOnPawn(click, pawn)) return pawn;
        }
        for (MerellePawn pawn : stage.getWhitePawns()) {
            if (isClickOnPawn(click, pawn)) return pawn;
        }
        return null;
    }

    boolean isClickOnPawn(Coord2D click, MerellePawn pawn) {
        if (!pawn.isVisible()) return false;
        double cx = getPawnCenterX(pawn);
        double cy = getPawnCenterY(pawn);
        double dx = click.getX() - cx;
        double dy = click.getY() - cy;
        return dx * dx + dy * dy <= PAWN_RADIUS * PAWN_RADIUS + 100;
    }

    private double getPawnCenterX(MerellePawn pawn) {
        if (pawn.getContainer() == null) return pawn.getX() + PAWN_RADIUS;
        return pawn.getContainer().getX() + pawn.getX() + PAWN_RADIUS;
    }

    private double getPawnCenterY(MerellePawn pawn) {
        if (pawn.getContainer() == null) return pawn.getY() + PAWN_RADIUS;
        return pawn.getContainer().getY() + pawn.getY() + PAWN_RADIUS;
    }

    int[] getBoardCellFromClick(Coord2D click) {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        MerelleBoard board = stage.getBoard();
        double relX = click.getX() - board.getX();
        double relY = click.getY() - board.getY();
        if (relX < 0 || relY < 0) return null;
        int col = (int) (relX / CELL_SIZE);
        int row = (int) (relY / CELL_SIZE);
        if (col < 0 || col > 6 || row < 0 || row > 6) return null;
        return new int[]{row, col};
    }

    private boolean handlePlacementClick(Coord2D click, int color) {
        int[] cell = getBoardCellFromClick(click);
        if (cell == null) return false;
        return tryPlace(cell[0], cell[1], color);
    }

    boolean handleMovementClick(Coord2D click, int color) {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        MerelleBoard board = stage.getBoard();
        MerellePawn clickedPawn = findPawnAtClick(click);

        if (clickedPawn != null && clickedPawn.getColor() == color) {
            int[] pawnCell = board.getElementCell(clickedPawn);
            if (pawnCell == null) return false;

            if (selectedPawn == null) {
                selectedPawn = clickedPawn;
                selectedPawn.toggleSelected();
                showHighlights((MerellePawn) selectedPawn, color);
                return false;
            }

            if (selectedPawn == clickedPawn) {
                clearHighlights();
                selectedPawn.toggleSelected();
                selectedPawn = null;
                return false;
            }

            clearHighlights();
            selectedPawn.toggleSelected();
            selectedPawn = clickedPawn;
            selectedPawn.toggleSelected();
            showHighlights((MerellePawn) selectedPawn, color);
            return false;
        }

        if (selectedPawn != null) {
            int[] src = board.getElementCell(selectedPawn);
            int[] dst = getBoardCellFromClick(click);
            if (src == null || dst == null) return false;

            if (tryMove(src[0], src[1], dst[0], dst[1], color)) {
                clearHighlights();
                selectedPawn.toggleSelected();
                selectedPawn = null;
                return true;
            }
        }
        return false;
    }

    private boolean handleCaptureClick(Coord2D click, int color) {
        MerellePawn clickedPawn = findPawnAtClick(click);
        if (clickedPawn == null) return false;
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        MerelleBoard board = stage.getBoard();
        int[] cell = board.getElementCell(clickedPawn);
        if (cell == null) return false;
        return tryCapture(cell[0], cell[1], color);
    }

    private void showHighlights(MerellePawn pawn, int color) {
        clearHighlights();
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        MerelleBoard board = stage.getBoard();
        int[] src = board.getElementCell(pawn);
        if (src == null) return;
        try {
            MerelleBoardLook boardLook = (MerelleBoardLook) getElementLook(board);
            for (int[] inter : MerelleBoard.INTERSECTIONS) {
                if (isValidMove(src[0], src[1], inter[0], inter[1], color)) {
                    boardLook.highlightCell(inter[0], inter[1]);
                }
            }
        } catch (Exception e) {
            // headless or look not registered: ignore
        }
    }

    private void clearHighlights() {
        try {
            MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
            MerelleBoard board = stage.getBoard();
            MerelleBoardLook boardLook = (MerelleBoardLook) getElementLook(board);
            boardLook.clearHighlights();
        } catch (Exception e) {
            // headless or look not registered: ignore
        }
    }

    boolean isValidPlace(int row, int col) {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        MerelleBoard board = stage.getBoard();
        return board.isValidIntersection(row, col) && board.isEmptyAt(row, col);
    }

    boolean tryPlace(int row, int col, int color) {
        if (!isValidPlace(row, col)) return false;
        executePlacement(row, col, color);
        return true;
    }

    private void executePlacement(int row, int col, int color) {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        MerelleBoard board = stage.getBoard();
        MerellePawnPot pot = (color == MerellePawn.PAWN_BLACK) ? stage.getBlackPot() : stage.getWhitePot();
        GameElement pawn = takeFirstPawn(pot);
        if (pawn == null) return;

        ActionList actions = ActionFactory.generatePutInContainer(model, pawn, "merelleboard", row, col);
        new ActionPlayer(model, this, actions).start();

        if (isJavaFxAvailable()) {
            try {
                ElementLook look = getElementLook(pawn);
                if (look != null && look.getNode() != null) {
                    javafx.application.Platform.runLater(() -> {
                        look.getNode().setOpacity(0);
                        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                                javafx.util.Duration.millis(100), look.getNode());
                        ft.setToValue(1);
                        ft.play();
                    });
                }
            } catch (NullPointerException e) {
                // Boardifier internal map not initialized in headless mode, skip animation
            }
        }

        if (color == MerellePawn.PAWN_BLACK) stage.decreaseBlackPawnsToPlace();
        else stage.decreaseWhitePawnsToPlace();

        stage.checkAndSetCaptureMode(board, row, col, color);
        updateStatusText();
    }

    boolean isValidMove(int rSrc, int cSrc, int rDst, int cDst, int color) {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        MerelleBoard board = stage.getBoard();
        if (!board.isValidIntersection(rSrc, cSrc) || !board.isValidIntersection(rDst, cDst)) return false;
        if (board.isEmptyAt(rSrc, cSrc) || !board.isEmptyAt(rDst, cDst)) return false;
        if (board.getColorAt(rSrc, cSrc) != color) return false;
        if (!stage.isFlying(color) && !board.areAdjacent(rSrc, cSrc, rDst, cDst)) return false;

        if (!stage.getDestroyedMills().isEmpty() && stage.getPlayerWhoDestroyedMill() == color) {
            for (int[][] mill : stage.getDestroyedMills()) {
                if (MerelleBoard.wouldReformMill(board, mill, rSrc, cSrc, rDst, cDst, color)) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean tryMove(int rSrc, int cSrc, int rDst, int cDst, int color) {
        if (!isValidMove(rSrc, cSrc, rDst, cDst, color)) return false;
        executeMove(rSrc, cSrc, rDst, cDst, color);
        return true;
    }

    private void executeMove(int rSrc, int cSrc, int rDst, int cDst, int color) {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        MerelleBoard board = stage.getBoard();
        GameElement pawn = board.getElement(rSrc, cSrc);

        java.util.List<int[][]> destroyedMills = board.getAllMillsContaining(rSrc, cSrc);
        if (!destroyedMills.isEmpty()) {
            stage.setLastDestroyedMill(destroyedMills.get(0), color);
            for (int i = 1; i < destroyedMills.size(); i++) {
                stage.addDestroyedMill(destroyedMills.get(i));
            }
        }

        ActionList actions = ActionFactory.generateMoveWithinContainer(model, pawn, rDst, cDst);
        new ActionPlayer(model, this, actions).start();
        update();

        stage.checkAndSetCaptureMode(board, rDst, cDst, color);
        updateStatusText();
    }

    boolean tryCapture(int row, int col, int color) {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        MerelleBoard board = stage.getBoard();
        int opponent = 1 - color;
        if (!board.isValidIntersection(row, col)) return false;
        if (board.isEmptyAt(row, col)) return false;
        if (board.getColorAt(row, col) != opponent) return false;
        if (board.isInMill(row, col) && !allOpponentPawnsInMill(opponent)) return false;

        executeCapture(row, col, color);
        return true;
    }

    private void executeCapture(int row, int col, int color) {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        MerelleBoard board = stage.getBoard();
        GameElement pawn = board.getElement(row, col);

        java.util.List<int[][]> destroyedMills = board.getAllMillsContaining(row, col);
        if (!destroyedMills.isEmpty()) {
            stage.setLastDestroyedMill(destroyedMills.get(0), color);
            for (int i = 1; i < destroyedMills.size(); i++) {
                stage.addDestroyedMill(destroyedMills.get(i));
            }
        }

        ActionList actions = ActionFactory.generateRemoveFromStage(model, pawn);
        new ActionPlayer(model, this, actions).start();

        if (isJavaFxAvailable()) {
            try {
                ElementLook look = getElementLook(pawn);
                if (look != null && look.getNode() != null) {
                    javafx.application.Platform.runLater(() -> {
                        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                                javafx.util.Duration.millis(100), look.getNode());
                        ft.setToValue(0);
                        ft.play();
                    });
                }
            } catch (NullPointerException e) {
                // Boardifier internal map not initialized in headless mode, skip animation
            }
        }

        stage.setCaptureMode(false);
        updateStatusText();
    }

    boolean allOpponentPawnsInMill(int opponentColor) {
        MerelleBoard board = ((MerelleStageModel) model.getGameStage()).getBoard();
        for (int[] inter : MerelleBoard.INTERSECTIONS) {
            if (board.getColorAt(inter[0], inter[1]) == opponentColor && !board.isInMill(inter[0], inter[1])) {
                return false;
            }
        }
        return true;
    }

    GameElement takeFirstPawn(MerellePawnPot pot) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (!pot.isEmptyAt(i, j)) return pot.getElement(i, j);
            }
        }
        return null;
    }

    @Override
    public void endOfTurn() {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        if (!stage.getDestroyedMills().isEmpty()) {
            stage.incrementTurnsSinceMillDestroyed();
            if (stage.getTurnsSinceMillDestroyed() >= 3) {
                stage.clearLastDestroyedMill();
            }
        }
        model.setNextPlayer();
        updateStatusText();
    }

    @Override
    public void endGame() {
    }
}
