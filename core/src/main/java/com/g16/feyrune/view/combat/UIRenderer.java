package com.g16.feyrune.view.combat;

import com.g16.feyrune.model.Player;
import com.g16.feyrune.model.overworld.encounter.Encounter;

public class UIRenderer {
    private HealthBar fHealthBar, eHealthBar;
    private ChoiceRenderer choiceRenderer;
    public UIRenderer(Encounter encounter, Player player){
        choiceRenderer = new ChoiceRenderer();

        eHealthBar = new HealthBar((int)encounter.getEnemyCreature().getHP());
        fHealthBar = new HealthBar((int)player.getCreature().getHP());
    }

    public void render(){
        eHealthBar.render();
        fHealthBar.render();

    }

}
