package com.dpycb.mario.tools;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.dpycb.mario.MarioGame;
import com.dpycb.mario.sprites.Mario;
import com.dpycb.mario.sprites.enemies.Enemy;
import com.dpycb.mario.sprites.items.Item;
import com.dpycb.mario.sprites.tileobjects.InteractiveTileObject;

public class WorldContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        int collisionDefinition = fixtureA.getFilterData().categoryBits | fixtureB.getFilterData().categoryBits;

        //collisions between enemies and etc (1st - is Mario jumping on enemy head, 2nd - enemy changes direction when bumps into smth)
        if (collisionDefinition == (MarioGame.ENEMY_HEAD_BIT | MarioGame.MARIO_BIT)) {
            if (fixtureA.getFilterData().categoryBits == MarioGame.ENEMY_HEAD_BIT) {
                ((Enemy)fixtureA.getUserData()).hitOnHead((Mario) fixtureB.getUserData());
            }
            else {
                ((Enemy)fixtureB.getUserData()).hitOnHead((Mario) fixtureA.getUserData());
            }
        }
        else if (collisionDefinition == (MarioGame.MARIO_HEAD_BIT | MarioGame.BRICK_BIT)) {
            if (fixtureA.getFilterData().categoryBits == MarioGame.MARIO_HEAD_BIT) {
                ((InteractiveTileObject) fixtureB.getUserData()).onHeadHit((Mario) fixtureA.getUserData());
            }
            else {
                ((InteractiveTileObject) fixtureA.getUserData()).onHeadHit((Mario) fixtureB.getUserData());
            }
        }
        else if (collisionDefinition == (MarioGame.MARIO_HEAD_BIT | MarioGame.COIN_BIT)) {
            if (fixtureA.getFilterData().categoryBits == MarioGame.MARIO_HEAD_BIT) {
                ((InteractiveTileObject) fixtureB.getUserData()).onHeadHit((Mario) fixtureA.getUserData());
            }
            else {
                ((InteractiveTileObject) fixtureA.getUserData()).onHeadHit((Mario) fixtureB.getUserData());
            }
        }
        else if (collisionDefinition == (MarioGame.MARIO_BIT | MarioGame.ENEMY_BIT)) {
            if (fixtureA.getFilterData().categoryBits == MarioGame.MARIO_BIT) {
                ((Mario) fixtureA.getUserData()).hit((Enemy)fixtureB.getUserData());
            }
            else {
                ((Mario) fixtureB.getUserData()).hit((Enemy)fixtureA.getUserData());
            }
        }
        else if (collisionDefinition == (MarioGame.ENEMY_BIT | MarioGame.OBJECT_BIT)) {
            if (fixtureA.getFilterData().categoryBits == MarioGame.ENEMY_HEAD_BIT) {
                ((Enemy)fixtureA.getUserData()).reverseVelocity(true, false);
            }
            else {
                ((Enemy)fixtureB.getUserData()).reverseVelocity(true, false);
            }
        }
        else if (collisionDefinition == (MarioGame.ENEMY_BIT | MarioGame.ENEMY_BIT)) {
            ((Enemy)fixtureA.getUserData()).onEnemyHit((Enemy)fixtureB.getUserData());
            ((Enemy)fixtureB.getUserData()).onEnemyHit((Enemy)fixtureA.getUserData());
        }
        else if (collisionDefinition == (MarioGame.ITEM_BIT | MarioGame.OBJECT_BIT)) {
            if (fixtureA.getFilterData().categoryBits == MarioGame.ITEM_BIT) {
                ((Item)fixtureA.getUserData()).reverseVelocity(true, false);
            }
            else {
                ((Item)fixtureB.getUserData()).reverseVelocity(true, false);
            }
        }
        else if (collisionDefinition == (MarioGame.ITEM_BIT | MarioGame.MARIO_BIT)) {
            if (fixtureA.getFilterData().categoryBits == MarioGame.ITEM_BIT) {
                ((Item)fixtureA.getUserData()).use((Mario) fixtureB.getUserData());
            }
            else {
                ((Item)fixtureB.getUserData()).use((Mario) fixtureA.getUserData());
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if (fixtureA.getUserData() == "head" || fixtureB.getUserData() == "head") {
            Fixture head = fixtureA.getUserData() == "head" ? fixtureA : fixtureB;
            Fixture object = head == fixtureA ? fixtureB : fixtureA;
        }

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
