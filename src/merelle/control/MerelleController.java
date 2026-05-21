package merelle.control;

import boardifier.control.ActionFactory;
import boardifier.control.ActionPlayer;
import boardifier.control.Controller;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.Player;
import boardifier.model.action.ActionList;
import boardifier.view.View;
import merelle.model.MerelleBoard;
import merelle.model.MerellePawn;
import merelle.model.MerellePawnPot;
import merelle.model.MerelleStageModel;

import java.util.Scanner;

/**
 * Main controller for the Merelle game.
 * Reads the human player input and asks the decider to play for the computer.
 *
 * Input syntax :
 *   - placement   : "A1"     (destination)
 *   - movement    : "A1 D1"  (source then destination)
 *   - capture     : "A1"     (opponent pawn to remove)
 *   - "stop"      : ends the game immediately
 */
public class MerelleController extends Controller {

    private Scanner input;

    public MerelleController(Model model, View view) {
        super(model, view);
        input = new Scanner(System.in);
    }

    @Override
    public void stageLoop() {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        stage.getPlayerName().setText(model.getCurrentPlayer().getName());
        update();

        while (!model.isEndStage()) {
            playTurn();
            stage.checkEndOfGame();
            if (!model.isEndStage()) {
                endOfTurn();
            }
            update();
        }
        endGame();
    }

    private void playTurn() {
        Player p = model.getCurrentPlayer();
        if (p.getType() == Player.COMPUTER) {
            System.out.println(p.getName() + " (computer) is thinking...");
            MerelleDecider decider = new MerelleDecider(model, this);
            ActionPlayer play = new ActionPlayer(model, this, decider, null);
            play.start();

            // Handle capture if the computer formed a mill
            MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
            if (stage.isCaptureMode()) {
                int[] target = decider.chooseCaptureTarget(model.getIdPlayer());
                if (target != null) {
                    tryCapture(target[0], target[1], model.getIdPlayer());
                }
                stage.setCaptureMode(false);
            }
        } else {
            playHumanTurn();
        }
    }

    private void playHumanTurn() {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        Player p = model.getCurrentPlayer();
        boolean done = false;

        while (!done) {
            System.out.print(buildPrompt(p, stage));
            String line = input.nextLine().trim();
            if (line.toLowerCase().contains("stop")) {
                model.stopStage();
                return;
            }
            done = analyseAndPlay(line);
            if (!done) {
                System.out.println("Invalid input. Try again.");
            }
        }
        handleCaptureIfNeeded();
    }

    // builds the prompt text shown to the human player
    private String buildPrompt(Player p, MerelleStageModel stage) {
        int phase = stage.getPhase(model.getIdPlayer());
        if (phase == MerelleStageModel.PHASE_PLACEMENT) {
            return p.getName() + " - place a pawn (e.g. A1) > ";
        }
        if (stage.isFlying(model.getIdPlayer())) {
            return p.getName() + " - fly a pawn (e.g. A1 D4) > ";
        }
        return p.getName() + " - move a pawn (e.g. A1 A4) > ";
    }

    /**
     * Parse the input and play the corresponding action.
     * Returns true if the action was valid and played.
     */
    public boolean analyseAndPlay(String line) {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        int color = model.getIdPlayer();

        if (stage.getPhase(color) == MerelleStageModel.PHASE_PLACEMENT) {
            int[] coords = parseCell(line);
            if (coords == null) return false;
            return tryPlace(coords[0], coords[1], color);
        }

        // movement / flying : "A1 D1"
        String[] parts = line.split("\\s+");
        if (parts.length != 2) return false;
        int[] src = parseCell(parts[0]);
        int[] dst = parseCell(parts[1]);
        if (src == null || dst == null) return false;
        return tryMove(src[0], src[1], dst[0], dst[1], color);
    }

    private boolean tryPlace(int row, int col, int color) {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        MerelleBoard board = stage.getBoard();
        if (!board.isValidIntersection(row, col)) {
            System.out.println("Error: not a valid intersection.");
            return false;
        }
        if (!board.isEmptyAt(row, col)) {
            System.out.println("Error: intersection already occupied.");
            return false;
        }

        MerellePawnPot pot = (color == MerellePawn.PAWN_BLACK) ? stage.getBlackPot() : stage.getWhitePot();
        GameElement pawn = takeFirstPawn(pot);
        if (pawn == null) return false;

        ActionList actions = ActionFactory.generatePutInContainer(model, pawn, "merelleboard", row, col);
        new ActionPlayer(model, this, actions).start();

        if (color == MerellePawn.PAWN_BLACK) stage.decreaseBlackPawnsToPlace();
        else stage.decreaseWhitePawnsToPlace();

        if (board.formsMill(row, col, color)) {
            stage.setCaptureMode(true);
            System.out.println("** Mill ! Capture an opponent pawn. **");
            update();
        }
        return true;
    }

    private boolean tryMove(int rSrc, int cSrc, int rDst, int cDst, int color) {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        MerelleBoard board = stage.getBoard();
        if (!board.isValidIntersection(rSrc, cSrc)) {
            System.out.println("Error: source is not a valid intersection.");
            return false;
        }
        if (!board.isValidIntersection(rDst, cDst)) {
            System.out.println("Error: destination is not a valid intersection.");
            return false;
        }
        if (board.isEmptyAt(rSrc, cSrc)) {
            System.out.println("Error: no pawn at source.");
            return false;
        }
        if (!board.isEmptyAt(rDst, cDst)) {
            System.out.println("Error: destination is not empty.");
            return false;
        }
        if (board.getColorAt(rSrc, cSrc) != color) {
            System.out.println("Error: not your pawn.");
            return false;
        }
        if (!stage.isFlying(color) && !board.areAdjacent(rSrc, cSrc, rDst, cDst)) {
            System.out.println("Error: destination is not adjacent (and you are not flying).");
            return false;
        }

        GameElement pawn = board.getElement(rSrc, cSrc);
        ActionList actions = ActionFactory.generateMoveWithinContainer(model, pawn, rDst, cDst);
        new ActionPlayer(model, this, actions).start();

        if (board.formsMill(rDst, cDst, color)) {
            stage.setCaptureMode(true);
            System.out.println("** Mill ! Capture an opponent pawn. **");
            update();
        }
        return true;
    }

    private boolean tryCapture(int row, int col, int color) {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        MerelleBoard board = stage.getBoard();
        int opponent = 1 - color;
        if (!board.isValidIntersection(row, col)) {
            System.out.println("Error: not a valid intersection.");
            return false;
        }
        if (board.isEmptyAt(row, col)) {
            System.out.println("Error: no pawn to capture.");
            return false;
        }
        if (board.getColorAt(row, col) != opponent) {
            System.out.println("Error: this is your own pawn.");
            return false;
        }
        if (board.isInMill(row, col) && !allOpponentPawnsInMill(opponent)) {
            System.out.println("Error: cannot capture a pawn in a mill unless all opponent pawns are in mills.");
            return false;
        }

        GameElement pawn = board.getElement(row, col);
        ActionList actions = ActionFactory.generateRemoveFromStage(model, pawn);
        new ActionPlayer(model, this, actions).start();

        stage.setCaptureMode(false);
        return true;
    }

    // true if every opponent pawn on the board is part of a mill
    private boolean allOpponentPawnsInMill(int opponentColor) {
        MerelleBoard board = ((MerelleStageModel) model.getGameStage()).getBoard();
        for (int[] inter : MerelleBoard.INTERSECTIONS) {
            if (board.getColorAt(inter[0], inter[1]) == opponentColor && !board.isInMill(inter[0], inter[1])) {
                return false;
            }
        }
        return true;
    }

    // if a mill was formed, ask the human to capture or let the computer choose
    private void handleCaptureIfNeeded() {
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        if (!stage.isCaptureMode()) return;

        Player p = model.getCurrentPlayer();
        if (p.getType() == Player.COMPUTER) {
            // Already handled in playTurn() for computer
            return;
        }

        boolean done = false;
        while (!done) {
            System.out.print(p.getName() + " - capture an opponent pawn (e.g. A1) > ");
            String line = input.nextLine().trim();
            if (line.toLowerCase().contains("stop")) {
                model.stopStage();
                return;
            }
            int[] coords = parseCell(line);
            if (coords != null) done = tryCapture(coords[0], coords[1], model.getIdPlayer());
            if (!done) System.out.println("Invalid capture. Try again.");
        }
    }

    // get the first pawn still in the pot
    private GameElement takeFirstPawn(MerellePawnPot pot) {
        for (int i = 0; i < 9; i++) {
            if (!pot.isEmptyAt(i, 0)) return pot.getElement(i, 0);
        }
        return null;
    }

    // "A1" -> {row=0, col=0}. Returns null if not valid.
    private int[] parseCell(String s) {
        if (s == null || s.length() != 2) return null;
        char letter = Character.toUpperCase(s.charAt(0));
        char digit = s.charAt(1);
        if (letter < 'A' || letter > 'G') return null;
        if (digit < '1' || digit > '7') return null;
        return new int[]{ digit - '1', letter - 'A' };
    }

    @Override
    public void endOfTurn() {
        model.setNextPlayer();
        MerelleStageModel stage = (MerelleStageModel) model.getGameStage();
        stage.getPlayerName().setText(model.getCurrentPlayer().getName());
    }
}
