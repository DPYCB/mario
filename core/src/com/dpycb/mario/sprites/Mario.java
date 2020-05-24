package com.dpycb.mario.sprites;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.dpycb.mario.MarioGame;
import com.dpycb.mario.screens.PlayScreen;
import com.dpycb.mario.sprites.enemies.Enemy;
import com.dpycb.mario.sprites.enemies.Turtle;


public class Mario extends Sprite {
    public enum State { FALLING, JUMPING, STANDING, RUNNING, GROWING, DEAD }
    public State currentState;
    public State previousState;

    public World world;
    public Body b2Body;

    private TextureRegion marioStand;
    private TextureRegion marioJump;
    private TextureRegion marioDead;
    private Animation marioRun;
    private boolean runningRight;
    private float stateTimer;

    private boolean marioIsBig;
    private boolean runGrowAnim;
    private boolean timeToDefineBigMario;
    private boolean timeToRedefineMario;
    private boolean marioIsDead;

    private TextureRegion bigMarioStand;
    private TextureRegion bigMarioJump;
    private Animation bigMarioRun;
    private Animation growMario;


    public Mario (PlayScreen screen) {
        this.world = screen.getWorld();
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0f;

        runningRight = true;
        marioIsBig = false;
        runGrowAnim = false;
        timeToRedefineMario = false;

        Array<TextureRegion> frames = new Array<>();

        //Running Mario animation. 1 and 4 numbers (and all others), as well as  i*16 is taken from Mario_and_enemies.png, order of Mario states-images
        for (int i = 1; i < 4; i ++) {
            frames.add(new TextureRegion(screen.getTextureAtlas().findRegion("little_mario"), i * 16, 0, 16, 16));
        }
        marioRun = new Animation(0.1f, frames);
        frames.clear();

        for (int i = 1; i < 4; i ++) {
            frames.add(new TextureRegion(screen.getTextureAtlas().findRegion("big_mario"), i * 16, 0, 16, 32));
        }
        bigMarioRun = new Animation(0.1f, frames);

        //animation for Mario growth. First he becomes big, then small , then big, then small/ x, y, width and height are taken from the texture pack
        frames.add(new TextureRegion(screen.getTextureAtlas().findRegion("big_mario"), 240, 0, 16, 32));
        frames.add(new TextureRegion(screen.getTextureAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        frames.add(new TextureRegion(screen.getTextureAtlas().findRegion("big_mario"), 240, 0, 16, 32));
        frames.add(new TextureRegion(screen.getTextureAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        growMario = new Animation(0.2f, frames);

        //jumping mario animation
        marioJump = new TextureRegion(screen.getTextureAtlas().findRegion("little_mario"), 80, 0, 16, 16);
        bigMarioJump = new TextureRegion(screen.getTextureAtlas().findRegion("big_mario"), 80, 0, 16, 32);
        frames.clear();

        //getting texture for the standing mario from texture pack. Texture is 16x16 and starts at left upper corner - 0x0
        marioStand = new TextureRegion(screen.getTextureAtlas().findRegion("little_mario"), 0, 0, 16, 16);
        bigMarioStand = new TextureRegion(screen.getTextureAtlas().findRegion("big_mario"), 0, 0, 16, 32);

        marioDead = new TextureRegion(screen.getTextureAtlas().findRegion("little_mario"), 96, 0, 16, 16);

        defineMario();
        setBounds(0, 0, 16 / MarioGame.PPM, 16 / MarioGame.PPM);
        setRegion(marioStand);

    }

    public void update(float deltaTime) {
        //positioning the Mario texture to its shape
        if (marioIsBig) {
            setPosition(b2Body.getPosition().x - getWidth() / 2, b2Body.getPosition().y - getHeight() / 2 - 6 /MarioGame.PPM);
        }
        else {
            setPosition(b2Body.getPosition().x - getWidth() / 2, b2Body.getPosition().y - getHeight() / 2);
        }
        setRegion(getFrame(deltaTime));
        if (timeToDefineBigMario) {
            defineBigMario();
        }
        else if (timeToRedefineMario) {
            redefineMario();
        }
    }

    //WTF IS GOING ON?!?
    public TextureRegion getFrame(float deltaTime) {
        currentState = getState();
        TextureRegion region;
        if (currentState.equals((State.DEAD))) {
            region = marioDead;
        }
        else if (currentState.equals((State.GROWING))) {
            region = (TextureRegion) growMario.getKeyFrame(stateTimer);
            if (growMario.isAnimationFinished(stateTimer)) {
                runGrowAnim = false;
            }
        }
        else if (currentState.equals(State.JUMPING)) {
            region =marioIsBig ? bigMarioJump : marioJump;
        }
        else if (currentState.equals(State.RUNNING)) {
            region = marioIsBig ? (TextureRegion) bigMarioRun.getKeyFrame(stateTimer, true)
                    : (TextureRegion) marioRun.getKeyFrame(stateTimer, true);
        }
        else region = marioIsBig ? bigMarioStand : marioStand;

        if ((b2Body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
            region.flip(true, false);
            runningRight = false;
        }
        else if ((b2Body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
            region.flip(true, false);
            runningRight = true;
        }

        stateTimer = currentState == previousState ? stateTimer + deltaTime : 0;
        previousState = currentState;

        return region;
    }

    public State getState() {
        if (marioIsDead) {
            return State.DEAD;
        }
        else if (runGrowAnim) {
            return State.GROWING;
        }
        else if (b2Body.getLinearVelocity().y > 0 || (b2Body.getLinearVelocity().y < 0 && previousState == State.JUMPING)) {
            return State.JUMPING;
        }
        else if (b2Body.getLinearVelocity().y < 0) {
            return State.FALLING;
        }
        else if (b2Body.getLinearVelocity().x != 0) {
            return State.RUNNING;
        }
        else return State.STANDING;
    }

    private void redefineMario() {
        Vector2 currentPosition = b2Body.getPosition();
        world.destroyBody(b2Body);
        //creates Mario body
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(currentPosition);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        b2Body = world.createBody(bodyDef);

        //creates Mario fixture
        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioGame.PPM);
        //define Mario's own bit and bits which he can collide with
        fixtureDef.filter.categoryBits = MarioGame.MARIO_BIT;
        fixtureDef.filter.maskBits = MarioGame.GROUND_BIT
                | MarioGame.COIN_BIT | MarioGame.BRICK_BIT
                | MarioGame.OBJECT_BIT | MarioGame.ENEMY_BIT
                | MarioGame.ENEMY_HEAD_BIT | MarioGame.ITEM_BIT;

        fixtureDef.shape = shape;
        b2Body.createFixture(fixtureDef).setUserData(this);

        //creating sensor on Mario head to detect collision with ContactListener
        EdgeShape head = new EdgeShape();
        //coordinates of EdgeShape (line) beginning and ending are taken as per Mario's body center
        head.set(new Vector2(-2 / MarioGame.PPM, 6 / MarioGame.PPM), new Vector2(2 / MarioGame.PPM, 6 / MarioGame.PPM));
        fixtureDef.shape = head;
        fixtureDef.filter.categoryBits = MarioGame.MARIO_HEAD_BIT;
        fixtureDef.isSensor = true;
        b2Body.createFixture(fixtureDef).setUserData(this);

        timeToRedefineMario = false;
    }

    private void defineBigMario() {
        Vector2 currentPosition = b2Body.getPosition();
        world.destroyBody(b2Body);

        //creates Mario body
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(currentPosition.add(0, 10/MarioGame.PPM));
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        b2Body = world.createBody(bodyDef);

        //creates Mario fixture
        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioGame.PPM);
        //define Mario's own bit and bits which he can collide with
        fixtureDef.filter.categoryBits = MarioGame.MARIO_BIT;
        fixtureDef.filter.maskBits = MarioGame.GROUND_BIT
                | MarioGame.COIN_BIT | MarioGame.BRICK_BIT
                | MarioGame.OBJECT_BIT | MarioGame.ENEMY_BIT
                | MarioGame.ENEMY_HEAD_BIT | MarioGame.ITEM_BIT;

        fixtureDef.shape = shape;
        b2Body.createFixture(fixtureDef);
        shape.setPosition(new Vector2(0, -14 / MarioGame.PPM));
        b2Body.createFixture(fixtureDef).setUserData(this);

        //creating sensor on Mario head to detect collision with ContactListener
        EdgeShape head = new EdgeShape();
        //coordinates of EdgeShape (line) beginning and ending are taken as per Mario's body center
        head.set(new Vector2(-2 / MarioGame.PPM, 6 / MarioGame.PPM), new Vector2(2 / MarioGame.PPM, 6 / MarioGame.PPM));
        fixtureDef.shape = head;
        fixtureDef.filter.categoryBits = MarioGame.MARIO_HEAD_BIT;
        fixtureDef.isSensor = true;

        b2Body.createFixture(fixtureDef).setUserData(this);
        timeToDefineBigMario = false;
    }

    private void defineMario() {
        //creates Mario body
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(32 / MarioGame.PPM,32 / MarioGame.PPM);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        b2Body = world.createBody(bodyDef);

        //creates Mario fixture
        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioGame.PPM);
        //define Mario's own bit and bits which he can collide with
        fixtureDef.filter.categoryBits = MarioGame.MARIO_BIT;
        fixtureDef.filter.maskBits = MarioGame.GROUND_BIT
                | MarioGame.COIN_BIT | MarioGame.BRICK_BIT
                | MarioGame.OBJECT_BIT | MarioGame.ENEMY_BIT
                | MarioGame.ENEMY_HEAD_BIT | MarioGame.ITEM_BIT;

        fixtureDef.shape = shape;
        b2Body.createFixture(fixtureDef).setUserData(this);

        //creating sensor on Mario head to detect collision with ContactListener
        EdgeShape head = new EdgeShape();
        //coordinates of EdgeShape (line) beginning and ending are taken as per Mario's body center
        head.set(new Vector2(-2 / MarioGame.PPM, 6 / MarioGame.PPM), new Vector2(2 / MarioGame.PPM, 6 / MarioGame.PPM));
        fixtureDef.shape = head;
        fixtureDef.filter.categoryBits = MarioGame.MARIO_HEAD_BIT;
        fixtureDef.isSensor = true;
        b2Body.createFixture(fixtureDef).setUserData(this);
    }

    public void grow() {
        if (!isBig()) {
            runGrowAnim = true;
            marioIsBig = true;
            timeToDefineBigMario = true;
            setBounds(getX(), getY(), getWidth(), getHeight() * 2);
            MarioGame.assetManager.get("audio/sounds/powerup.wav", Sound.class).play();
        }
    }

    public void hit(Enemy enemy) {
        if (enemy instanceof Turtle && ((Turtle) enemy).getCurrentState() == Turtle.State.STANDING_SHELL) {
            ((Turtle) enemy).kick(this.getX() <= enemy.getX() ? Turtle.KICK_RIGHT_SPEED : Turtle.KICK_LEFT_SPEED);
        }
        else {
            if (marioIsBig) {
                marioIsBig = false;
                timeToRedefineMario = true;
                setBounds(getX(), getY(), getWidth(), getHeight() / 2);
                MarioGame.assetManager.get("audio/sounds/powerup.wav", Sound.class).play();
            } else {
                MarioGame.assetManager.get("audio/music/level1-loop.ogg", Music.class).stop();
                MarioGame.assetManager.get("audio/sounds/mariodie.wav", Sound.class).play();
                marioIsDead = true;
                //creating new filter, so that Mario will be able to collide with nothing (NOTHING BIT)
                Filter filter = new Filter();
                filter.maskBits = MarioGame.NOTHING_BIT;
                for (Fixture fixture : b2Body.getFixtureList()) {
                    fixture.setFilterData(filter);
                    b2Body.applyLinearImpulse(new Vector2(0, 2f), b2Body.getWorldCenter(), true);
                }
            }
        }
    }

    public boolean isBig() {
        return marioIsBig;
    }

    public boolean isDead() {
        return marioIsDead;
    }

    public float getStateTimer() {
        return stateTimer;
    }
}
