package com.dpycb.mario.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dpycb.mario.MarioGame;
import com.dpycb.mario.scenes.Hud;
import com.dpycb.mario.sprites.enemies.Enemy;
import com.dpycb.mario.sprites.enemies.Goomba;
import com.dpycb.mario.sprites.Mario;
import com.dpycb.mario.sprites.items.Item;
import com.dpycb.mario.sprites.items.ItemDefinition;
import com.dpycb.mario.sprites.items.Mushroom;
import com.dpycb.mario.tools.B2WorldCreator;
import com.dpycb.mario.tools.WorldContactListener;

import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PlayScreen implements Screen {
    private MarioGame game;
    //FOR BIG GAMES - libgdx assets manager
    private TextureAtlas textureAtlas;

    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private Hud hud;

    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    //box2d
    private World world;
    //graphics for bodies and fixtures inside world
    private Box2DDebugRenderer b2dr;
    private B2WorldCreator worldCreator;

    private Mario player;

    private Music music;

    private Array<Item> items;
    private LinkedBlockingQueue<ItemDefinition> itemsToSpawn;

    public PlayScreen (MarioGame game) {
        textureAtlas = new TextureAtlas("Mario_and_enemies.pack");

        this.game = game;
        //creates Character camera
        gameCamera = new OrthographicCamera();
        //sets screen scaling logic
        gameViewport = new FitViewport(MarioGame.V_WIDTH / MarioGame.PPM, MarioGame.V_HEIGHT / MarioGame.PPM, gameCamera);
        //creates HUD
        hud = new Hud(game.batch);
        //define and load map from assets folder
        mapLoader = new TmxMapLoader();
        map = mapLoader.load("level1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / MarioGame.PPM);

        gameCamera.position.set(gameViewport.getWorldWidth() / 2, gameViewport.getWorldHeight() / 2, 0);

        //define box2d world. new vector2 defines gravity
        world = new World(new Vector2(0, -10 ), true);
        b2dr = new Box2DDebugRenderer();

        worldCreator = new B2WorldCreator(this);

        //create Mario and enemies
        player = new Mario(this);

        world.setContactListener(new WorldContactListener());

        music = MarioGame.assetManager.get("audio/music/level1-loop.ogg", Music.class);
        music.setLooping(true);
        music.setVolume(0.3f);
        music.play();

        items = new Array<>();
        itemsToSpawn = new LinkedBlockingQueue<>();
    }

    public void spawnItem (ItemDefinition itemDef) {
        itemsToSpawn.add(itemDef);
    }

    public void handleSpawningItems() {
        if (!itemsToSpawn.isEmpty()) {
            ItemDefinition itemDefinition = itemsToSpawn.poll();
            if (itemDefinition.type == Mushroom.class) {
                items.add(new Mushroom(this, itemDefinition.position.x, itemDefinition.position.y));
            }
        }
    }

    public boolean gameOver() {
        if ((player.currentState == Mario.State.DEAD) && (player.getStateTimer() > 3)) {
            return true;
        }
        else return false;
    }

    public TiledMap getMap() {
        return map;
    }

    public World getWorld() {
        return world;
    }

    public TextureAtlas getTextureAtlas() {
        return textureAtlas;
    }

    @Override
    public void show() {

    }

    public void update (float deltaTime) {
        hanldeInput(deltaTime);
        handleSpawningItems();

        //TO READ AND REFRACTOr!!!!!!!!!
        world.step(1/60f, 6, 2);

        player.update(deltaTime);

        for (Enemy enemy : worldCreator.getEnemies()) {
            enemy.update(deltaTime);
            if (enemy.getX() < (player.getX()+224/MarioGame.PPM)) {
                enemy.b2Body.setActive(true);
            }
        }

        for (Item item : items) {
            item.update(deltaTime);
        }

        hud.update(deltaTime);
        if (player.currentState != Mario.State.DEAD) {
            gameCamera.position.x = player.b2Body.getPosition().x;
        }

        gameCamera.update();
        renderer.setView(gameCamera);
    }

    private void hanldeInput(float deltaTime) {
        if (player.currentState != Mario.State.DEAD) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && (player.b2Body.getLinearVelocity().y == 0)) {
                player.b2Body.applyLinearImpulse(new Vector2(0, 4f), player.b2Body.getWorldCenter(), true);
            }

            if ((Gdx.input.isKeyPressed(Input.Keys.RIGHT)) && (player.b2Body.getLinearVelocity().x <= 2)) {
                //player.b2Body.applyForceToCenter(new Vector2(10f, 0), true);
                player.b2Body.applyLinearImpulse(new Vector2(0.1f, 0), player.b2Body.getWorldCenter(), true);
            }

            if ((Gdx.input.isKeyPressed(Input.Keys.LEFT)) && (player.b2Body.getLinearVelocity().x >= -2)) {
                //player.b2Body.applyForceToCenter(new Vector2(-10f, 0), true);
                player.b2Body.applyLinearImpulse(new Vector2(-0.1f, 0), player.b2Body.getWorldCenter(), true);
            }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        //clear screen to Black
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //render the game
        renderer.render();

        //renderer Box2DDebugLines
        b2dr.render(world, gameCamera.combined);

        //draw Mario and enemies
        game.batch.setProjectionMatrix(gameCamera.combined);
        game.batch.begin();
        player.draw(game.batch);
        for (Enemy enemy : worldCreator.getEnemies()) {
            enemy.draw(game.batch);
        }

        for(Item item : items) {
            item.draw(game.batch);
        }

        game.batch.end();

        //draw hud for the game
        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();

        if (gameOver()) {
            game.setScreen(new GameOverScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
    }
}
