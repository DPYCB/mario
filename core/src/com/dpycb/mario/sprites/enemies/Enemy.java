package com.dpycb.mario.sprites.enemies;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.dpycb.mario.screens.PlayScreen;
import com.dpycb.mario.sprites.Mario;

public abstract class Enemy extends Sprite {
    protected PlayScreen screen;
    protected World world;
    public Body b2Body;
    public Vector2 velocity;

    public Enemy(PlayScreen screen, float x, float y) {
        this.world = screen.getWorld();
        this.screen = screen;
        setPosition(x, y);;
        defineEnemy();
        velocity = new Vector2(1, 0);
        b2Body.setActive(false);
    }

    protected abstract void defineEnemy();
    public abstract  void update(float deltaTime);
    public abstract void hitOnHead(Mario mario);
    //when enemy collides with an object he must go in the opposite direction. This method helps
    public void reverseVelocity(boolean x, boolean y) {
        if (x) {
            velocity.x = -velocity.x;
        }
        if (y) {
            velocity.y = -velocity.y;
        }
    }
    public abstract void onEnemyHit(Enemy enemy);

}
