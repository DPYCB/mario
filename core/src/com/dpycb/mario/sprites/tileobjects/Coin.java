package com.dpycb.mario.sprites.tileobjects;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.dpycb.mario.MarioGame;
import com.dpycb.mario.scenes.Hud;
import com.dpycb.mario.screens.PlayScreen;
import com.dpycb.mario.sprites.Mario;
import com.dpycb.mario.sprites.items.Item;
import com.dpycb.mario.sprites.items.ItemDefinition;
import com.dpycb.mario.sprites.items.Mushroom;


public class Coin extends InteractiveTileObject {
    private static TiledMapTileSet tileSet;
    //talen from Tiled Empty Coin texture ID (it is 27, but TileSet starts count from 1)
    private final int BLANK_COIN = 28;

    public Coin(PlayScreen screen, MapObject object) {
        super(screen, object);
        tileSet = map.getTileSets().getTileSet("tileset_gutter");
        fixture.setUserData(this);
        setCategoryFilter(MarioGame.COIN_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        if (getCell().getTile().getId() == BLANK_COIN) {
            MarioGame.assetManager.get("audio/sounds/bump.wav", Sound.class).play();
        }
        else {
            if (object.getProperties().containsKey("mushroom")) {
                MarioGame.assetManager.get("audio/sounds/powerup_spawn.wav", Sound.class).play();
                screen.spawnItem(new ItemDefinition(new Vector2(body.getPosition().x, body.getPosition().y + 16 / MarioGame.PPM), Mushroom.class));
                getCell().setTile(tileSet.getTile(BLANK_COIN));
                Hud.addScore(400);
            }
            else {
                MarioGame.assetManager.get("audio/sounds/coin.wav", Sound.class).play();
                getCell().setTile(tileSet.getTile(BLANK_COIN));
            }
        }
    }
}
