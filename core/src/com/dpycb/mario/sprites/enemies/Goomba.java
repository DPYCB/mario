package com.dpycb.mario.sprites.enemies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.dpycb.mario.MarioGame;
import com.dpycb.mario.screens.PlayScreen;
import com.dpycb.mario.sprites.Mario;

public class Goomba extends Enemy {
    private float stateTime;
    private Animation walkAnimation;
    private Array<TextureRegion> frames;
    private boolean setToDestroy;
    private boolean destroyed;

    public Goomba(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        frames = new Array<>();
        for (int i = 0; i < 2; i++) {
            frames.add(new TextureRegion(screen.getTextureAtlas().findRegion("goomba"), i * 16, 0, 16, 16));
        }
        walkAnimation = new Animation(0.4f, frames);
        stateTime = 0;
        setBounds(getX(), getY(), 16 / MarioGame.PPM, 16 / MarioGame.PPM);
        setToDestroy = false;
        destroyed = false;
    }

    public void update (float deltaTime) {
        stateTime += deltaTime;
        if (setToDestroy && !destroyed) {
            world.destroyBody(b2Body);
            destroyed = true;
            setRegion(new TextureRegion(screen.getTextureAtlas().findRegion("goomba"), 32, 0, 16, 16));
            stateTime = 0;

        }
        else if (!destroyed) {
            b2Body.setLinearVelocity(velocity);
            setPosition(b2Body.getPosition().x - getWidth() / 2, b2Body.getPosition().y - getHeight() / 2);
            setRegion((TextureRegion) walkAnimation.getKeyFrame(stateTime, true));
        }
    }

    @Override
    protected void defineEnemy() {
        //creates Enemy body
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX(), getY());
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        b2Body = world.createBody(bodyDef);

        //creates Enemy fixture
        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioGame.PPM);
        //define Enemy's own bit and bits which he can collide with
        fixtureDef.filter.categoryBits = MarioGame.ENEMY_BIT;
        fixtureDef.filter.maskBits = MarioGame.GROUND_BIT
                | MarioGame.COIN_BIT | MarioGame.BRICK_BIT
                | MarioGame.ENEMY_BIT | MarioGame.OBJECT_BIT
                | MarioGame.MARIO_BIT;

        fixtureDef.shape = shape;
        b2Body.createFixture(fixtureDef).setUserData(this);

        //creatingHead for Collision and by restitution creating bounce effect
        PolygonShape head = new PolygonShape();
        Vector2[] vertice = new Vector2[4];
        vertice[0] = new Vector2(-5, 8).scl(1 / MarioGame.PPM);
        vertice[1] = new Vector2(5, 8).scl(1 / MarioGame.PPM);
        vertice[2] = new Vector2(-3, 3).scl(1 / MarioGame.PPM);
        vertice[3] = new Vector2(3, 3).scl(1 / MarioGame.PPM);
        head.set(vertice);

        fixtureDef.shape = head;
        fixtureDef.restitution = 0.5f;
        fixtureDef.filter.categoryBits = MarioGame.ENEMY_HEAD_BIT;
        b2Body.createFixture(fixtureDef).setUserData(this);
    }

    @Override
    public void draw(Batch batch) {
        if (!destroyed || stateTime < 1) {
            super.draw(batch);
        }
    }

    @Override
    public void hitOnHead(Mario mario) {
       setToDestroy = true;
        MarioGame.assetManager.get("audio/sounds/stomp.wav", Sound.class).play();

    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        if (enemy instanceof Turtle && (((Turtle) enemy).currentState == Turtle.State.MOVING_SHELL)) {
            setToDestroy = true;
        }
        else {
            reverseVelocity(true, false);
        }
    }
}
