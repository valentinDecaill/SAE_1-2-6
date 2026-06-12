package merelle.control;

import boardifier.control.StageFactory;
import boardifier.model.GameException;
import boardifier.model.Model;
import boardifier.view.RootPane;
import boardifier.view.View;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Optional;

public class MerelleGraphical extends Application {

    private static final int FRAME_GAP_MS = 30;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showMenu();
    }

    private void showMenu() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Label title = new Label("Merelle - Nine Men's Morris");
        title.setFont(Font.font(24));

        Button newGameBtn = new Button("New Game");
        Button rulesBtn = new Button("Rules");
        Button quitBtn = new Button("Quit");

        newGameBtn.setOnAction(e -> showGameDialog());
        rulesBtn.setOnAction(e -> showRules());
        quitBtn.setOnAction(e -> Platform.exit());

        root.getChildren().addAll(title, newGameBtn, rulesBtn, quitBtn);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Merelle");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showGameDialog() {
        Dialog<GameConfig> dialog = new Dialog<>();
        dialog.setTitle("New Game");
        dialog.setHeaderText("Game Settings");

        ButtonType startBtn = new ButtonType("Start", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(startBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> modeBox = new ComboBox<>();
        modeBox.getItems().addAll("2 Humans", "Human vs Computer", "2 Computers");
        modeBox.setValue("2 Humans");

        TextField name1Field = new TextField("player1");
        TextField name2Field = new TextField("player2");

        modeBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            int mode = modeBox.getSelectionModel().getSelectedIndex();
            if (mode == 0) {
                name1Field.setText("player1");
                name2Field.setText("player2");
            } else if (mode == 1) {
                name1Field.setText("player1");
                name2Field.setText("computer");
            } else {
                name1Field.setText("computer1");
                name2Field.setText("computer2");
            }
        });

        ComboBox<String> strategyBox = new ComboBox<>();
        strategyBox.getItems().addAll("Random", "Smart");
        strategyBox.setValue("Smart");

        grid.add(new Label("Mode:"), 0, 0);
        grid.add(modeBox, 1, 0);
        grid.add(new Label("Player 1:"), 0, 1);
        grid.add(name1Field, 1, 1);
        grid.add(new Label("Player 2:"), 0, 2);
        grid.add(name2Field, 1, 2);
        grid.add(new Label("AI Strategy:"), 0, 3);
        grid.add(strategyBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == startBtn) {
                int mode = modeBox.getSelectionModel().getSelectedIndex();
                int strategy = strategyBox.getSelectionModel().getSelectedIndex() == 0
                        ? MerelleDecider.STRATEGY_RANDOM
                        : MerelleDecider.STRATEGY_HEURISTIC;
                return new GameConfig(mode, name1Field.getText(), name2Field.getText(), strategy);
            }
            return null;
        });

        Optional<GameConfig> result = dialog.showAndWait();
        result.ifPresent(this::startGame);
    }

    private void showRules() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rules");
        alert.setHeaderText("Nine Men's Morris - Rules");
        alert.setContentText(
                "Nine Men's Morris is a strategy board game for 2 players.\n\n" +
                "Material:\n" +
                "- A board with 24 intersections\n" +
                "- 9 black pawns and 9 white pawns\n\n" +
                "Phase 1 - Placement:\n" +
                "Players take turns placing one pawn on a free intersection.\n" +
                "If a player forms a mill (3 pawns in a row), he captures one opponent pawn.\n" +
                "Pawns in a mill cannot be captured unless all opponent pawns are in mills.\n\n" +
                "Phase 2 - Movement:\n" +
                "Players move one pawn to an adjacent free intersection.\n" +
                "The mill rule also applies.\n\n" +
                "Phase 3 - Flying:\n" +
                "When a player has only 3 pawns left, he can move a pawn to any free intersection.\n\n" +
                "Victory:\n" +
                "A player wins if the opponent has fewer than 3 pawns,\n" +
                "or if the opponent cannot move."
        );
        alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void startGame(GameConfig config) {
        Model model = new Model(FRAME_GAP_MS * 1000000L);
        setupPlayers(model, config);

        StageFactory.registerModelAndView("merelle",
                "merelle.model.MerelleStageModel",
                "merelle.view.MerelleStageView");

        View view = new View(model, primaryStage, new RootPane());

        MerelleController control = new MerelleController(model, view);
        if (config.mode == 2) {
            // 2 Computers : computer1 = Smart, computer2 = Random
            control.setAiStrategy(0, MerelleDecider.STRATEGY_HEURISTIC);
            control.setAiStrategy(1, MerelleDecider.STRATEGY_RANDOM);
        } else {
            // Human vs Computer : use the chosen strategy for the AI player
            control.setAiStrategy(config.strategy);
        }
        control.setFirstStageName("merelle");

        Platform.runLater(() -> {
            try {
                control.startGame();
                primaryStage.setResizable(true);
                primaryStage.setMinWidth(700);
                primaryStage.setMinHeight(550);
                Thread gameThread = new Thread(() -> {
                    control.stageLoop();
                    Platform.runLater(() -> showEndGame(model));
                }, "GameLoop");
                gameThread.setDaemon(true);
                gameThread.start();
            } catch (GameException e) {
                e.printStackTrace();
            }
        });
    }

    private void setupPlayers(Model model, GameConfig config) {
        String name1 = config.name1.isEmpty() ? "player1" : config.name1;
        String name2 = config.name2.isEmpty() ? "player2" : config.name2;

        if (config.mode == 0) {
            model.addHumanPlayer(name1);
            model.addHumanPlayer(name2);
        } else if (config.mode == 1) {
            model.addHumanPlayer(name1);
            model.addComputerPlayer(name2.isEmpty() ? "computer" : name2);
        } else {
            model.addComputerPlayer(name1.isEmpty() ? "computer1" : name1);
            model.addComputerPlayer(name2.isEmpty() ? "computer2" : name2);
        }

        java.util.Random random = new java.util.Random();
        model.setIdPlayer(random.nextInt(2));
    }

    private void showEndGame(Model model) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game finished");
        if (model.getIdWinner() != -1) {
            alert.setHeaderText(model.getPlayers().get(model.getIdWinner()).getName() + " wins!");
        } else {
            alert.setHeaderText("Draw game");
        }

        ButtonType menuBtn = new ButtonType("Back to Menu");
        ButtonType quitBtn = new ButtonType("Quit");
        alert.getButtonTypes().setAll(menuBtn, quitBtn);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == menuBtn) {
            showMenu();
        } else {
            Platform.exit();
        }
    }

    private static class GameConfig {
        int mode;
        String name1;
        String name2;
        int strategy;

        GameConfig(int mode, String name1, String name2, int strategy) {
            this.mode = mode;
            this.name1 = name1;
            this.name2 = name2;
            this.strategy = strategy;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
