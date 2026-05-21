package merelle.control;

import boardifier.control.StageFactory;
import boardifier.model.GameException;
import boardifier.model.Model;
import boardifier.view.View;

/**
 * Main entry point of the Merelle game in text mode.
 *
 * Usage : java MerelleConsole [mode] [strategy]
 *   mode = 0 : 2 humans (default)
 *   mode = 1 : human vs computer
 *   mode = 2 : 2 computers
 *
 *   strategy = "random" : random AI (default)
 *   strategy = "smart"  : heuristic AI (forms mills, blocks opponent, ...)
 */
public class MerelleConsole {

    public static void main(String[] args) throws GameException {
        int mode = 0;
        if (args.length >= 1 && args[0].matches("[0-2]")) {
            mode = Integer.parseInt(args[0]);
        }
        int strategy = MerelleDecider.STRATEGY_RANDOM;
        if (args.length >= 2 && args[1].equalsIgnoreCase("smart")) {
            strategy = MerelleDecider.STRATEGY_HEURISTIC;
        }

        Model model = new Model();
        if (mode == 0) {
            model.addHumanPlayer("player1");
            model.addHumanPlayer("player2");
        } else if (mode == 1) {
            model.addHumanPlayer("player1");
            model.addComputerPlayer("computer");
        } else {
            model.addComputerPlayer("computer1");
            model.addComputerPlayer("computer2");
        }

        StageFactory.registerModelAndView("merelle",
                "merelle.model.MerelleStageModel",
                "merelle.view.MerelleStageView");

        View view = new View(model);
        MerelleController control = new MerelleController(model, view);
        control.setAiStrategy(strategy);
        control.setFirstStageName("merelle");

        control.startGame();
        control.stageLoop();
    }
}
