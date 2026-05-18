package merelle.view;

import boardifier.model.GameElement;
import boardifier.view.ConsoleColor;
import boardifier.view.ElementLook;
import merelle.model.MerellePawn;

public class MerellePawnLook extends ElementLook {

    public MerellePawnLook(GameElement element) {
        // Pawn look is constituted of a single character, so shape size = 1x1
        super(element, 1, 1);
    }

    protected void render() {
        MerellePawn pawn = (MerellePawn)element;
        // put in shape[0][0] the pawn's letter, with a color depending on the pawn color.
        // black pawn  : white "B" on black background
        // white pawn  : black "W" on white background
        if (pawn.getColor() == MerellePawn.PAWN_BLACK) {
            shape[0][0] = ConsoleColor.WHITE + ConsoleColor.BLACK_BACKGROUND + "B" + ConsoleColor.RESET;
        }
        else {
            shape[0][0] = ConsoleColor.BLACK + ConsoleColor.WHITE_BACKGROUND + "W" + ConsoleColor.RESET;
        }
    }
}
