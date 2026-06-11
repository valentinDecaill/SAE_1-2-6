package merelle.model;

import boardifier.model.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MerellePawnTest {

    @Test
    public void testGetColor() {
        Model model = new Model();
        MerelleStageModel stage = new MerelleStageModel("test", model);
        MerellePawn black = new MerellePawn(MerellePawn.PAWN_BLACK, stage);
        MerellePawn white = new MerellePawn(MerellePawn.PAWN_WHITE, stage);
        assertEquals(MerellePawn.PAWN_BLACK, black.getColor());
        assertEquals(MerellePawn.PAWN_WHITE, white.getColor());
    }
}
