package merelle.view;

import boardifier.model.ContainerElement;
import boardifier.view.TableLook;

/**
 * Pawn pot inherits from TableLook, using the constructor for
 * flexible cell sizes and a visible border. It implies that if there is no element in a cell
 * it has a zero size and thus, is not displayed. This is why during the game,
 * the pot will reduce in size because pawns are removed from the pot to be placed
 * on the main board. At the end, it will totally disappear.
 */
public class MerellePawnPotLook extends TableLook {

    public MerellePawnPotLook(ContainerElement containerElement) {
        super(containerElement, -1, 1);
    }
}
