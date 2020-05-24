package com.dpycb.mario.sprites.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.dpycb.mario.MarioGame;
import com.dpycb.mario.screens.PlayScreen;
import com.dpycb.mario.sprites.Mario;


public class Turtle extends Enemy {
    public static final int KICK_LEFT_SPEED = -2;
    public static final int KICK_RIGHT_SPEED = 2;

    public enum State {WALKING, STANDING_SHELL, MOVING_SHELL, DEAD}
    public State currentState;
    public State previousState;
    private float stateTimer;
    private Animation walkAnimation;
    private Array<TextureRegion> frames;
    private TextureRegion shell;
    private float deadRotationDegrees;

    private boolean setToDestroy;
    private boolean destroyed;

    public Turtle(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        frames = new Array<>();
        frames.add(new TextureRegion(screen.getTextureAtlas().findRegion("turtle"), 0, 0, 16, 24));
        frames.add(new TextureRegion(screen.getTextureAtlas().findRegion("turtle"), 16, 0, 16, 24));
        shell = new TextureRegion(screen.getTextureAtlas().findRegion("turtle"), 64, 0, 16, 24);

        walkAnimation = new Animation(0.2f, frames);
        currentState = previousState = State.WALKING;
        setBounds(getX(), getY(), 16/ MarioGame.PPM, 24/MarioGame.PPM);
        deadRotationDegrees = 0;
    }

    @Override
    public void draw(Batch batch) {
        if (!destroyed) {
            super.draw(batch);
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
        fixtureDef.restitution = 1.5f;
        fixtureDef.filter.categoryBits = MarioGame.ENEMY_HEAD_BIT;
        b2Body.createFixture(fixtureDef).setUserData(this);
    }

    @Override
    public void update(float deltaTime) {
        setRegion(getFrame(deltaTime));
        if (currentState == State.STANDING_SHELL && stateTimer > 5) {
            currentState = State.WALKING;
            velocity.x = 1;
        }
        setPosition(b2Body.getPosition().x - getWidth()/2, b2Body.getPosition().y - 8/MarioGame.PPM);
        if (currentState == State.DEAD) {
            deadRotationDegrees += 3;
            rotate(deadRotationDegrees);
            if (stateTimer > 5 && !destroyed) {
                world.destroyBody(b2Body);
                destroyed = true;
            }
        }
        else {
            b2Body.setLinearVelocity(velocity);
        }
    }

    public TextureRegion getFrame(float deltaTime) {
        TextureRegion region;
        if (currentState == State.MOVING_SHELL || currentState == State.STANDING_SHELL) {
            region = shell;
        }
        else {
            region = (TextureRegion) walkAnimation.getKeyFrame(stateTimer, true);
        }

        if (velocity.x > 0 && !region.isFlipX()) {
            region.flip(true, false);
        }
        if (velocity.x < 0 && region.isFlipX()) {
            region.flip(true, false);
        }

        stateTimer = currentState == previousState ? stateTimer + deltaTime : 0;
        previousState = currentState;
        return region;
    }

    @Override
    public void hitOnHead(Mario mario) {
        if (currentState != State.STANDING_SHELL) {
            currentState = State.STANDING_SHELL;
            velocity.x = 0;
        }
        else {
            kick(mario.getX() <= this.getX() ? KICK_RIGHT_SPEED : KICK_LEFT_SPEED);
        }
    }

    public void kick(int speed) {
        velocity.x = speed;
        currentState = State.MOVING_SHELL;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void killed() {
        currentState = State.DEAD;
        Filter filter = new Filter();
        filter.maskBits = MarioGame.NOTHING_BIT;

        for (Fixture fix : b2Body.getFixtureList()) {
            fix.setFilterData(filter);
        }
        b2Body.applyLinearImpulse(new Vector2(0, 5f), b2Body.getWorldCenter(), true);
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        if (enemy instanceof Turtle) {
            if (((Turtle) enemy).currentState == State.MOVING_SHELL && currentState != State.MOVING_SHELL) {
                killed();
            }
            else if (currentState == State.MOVING_SHELL && ((Turtle) enemy).currentState == State.WALKING) {
                return;
            }
            else {
                reverseVelocity(true, false);
            }
        }
        else if (!(currentState == State.MOVING_SHELL)) {
            reverseVelocity(true, false);
        }
    }
}
