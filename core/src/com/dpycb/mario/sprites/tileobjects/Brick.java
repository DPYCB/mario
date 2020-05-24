package com.dpycb.mario.sprites.tileobjects;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Rectangle;
import com.dpycb.mario.MarioGame;
import com.dpycb.mario.scenes.Hud;
import com.dpycb.mario.screens.PlayScreen;
import com.dpycb.mario.sprites.Mario;

public class Brick extends InteractiveTileObject {
    public Brick(PlayScreen screen, MapObject object) {
        super(screen, object);
        fixture.setUserData(this);
        setCategoryFilter(MarioGame.BRICK_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        if (mario.isBig()) {
            setCategoryFilter(MarioGame.DESTROYED_BIT);
            getCell().setTile(null);
            Hud.addScore(200);
            MarioGame.assetManager.get("audio/sounds/breakblock.wav", Sound.class).play();
        }
        else {
            MarioGame.assetManager.get("audio/sounds/bump.wav", Sound.class).play();
        }
    }
}
