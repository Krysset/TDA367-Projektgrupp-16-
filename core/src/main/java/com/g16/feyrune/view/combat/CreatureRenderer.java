package com.g16.feyrune.view.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.g16.feyrune.model.combat.creatures.CombatCreature;
import com.g16.feyrune.view.utils.AnimationUtils;

public class CreatureRenderer {
    private CombatCreature creature;
    private String spritePath = "assets/entities/bandit/bandit.png"; //TODO: temp
    private float width,height,posX,posY;
    private Texture texture;
    private TextureRegion[] animationRegion;
    private float stateTime = 0;
    private boolean flip;
    
    public CreatureRenderer(CombatCreature creature, float posX, float posY, boolean flip){
        this.creature = creature;
        this.posX = posX;
        this.posY = posY;
        this.flip = flip;

        texture = new Texture(Gdx.files.internal(spritePath));
        animationRegion = AnimationUtils.getAnimationFrames(texture,8,6,4,flip? 4: 0);

        width = (texture.getWidth() + texture.getWidth() * creature.getPower()/100);
        height = (texture.getHeight() + texture.getHeight() * creature.getPower()/100);
    }



    public void render(Batch batch){

        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time
        Animation<TextureRegion> animation = animate(animationRegion);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);

        batch.draw(currentFrame, posX - (flip? width : 0), posY, width , height);


        //TODO: NOT IMPLEMENTED
    }

    private Animation<TextureRegion> animate(TextureRegion[] animation){
        return new Animation<>(0.15f, animation);
    }


}
