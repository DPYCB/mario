package com.dpycb.mario.tools;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.dpycb.mario.MarioGame;
import com.dpycb.mario.screens.PlayScreen;
import com.dpycb.mario.sprites.enemies.Enemy;
import com.dpycb.mario.sprites.enemies.Turtle;
import com.dpycb.mario.sprites.tileobjects.Brick;
import com.dpycb.mario.sprites.tileobjects.Coin;
import com.dpycb.mario.sprites.enemies.Goomba;

public class B2WorldCreator {
    private Array<Goomba> goombas;
    private Array<Turtle> turtles;

    public B2WorldCreator(PlayScreen screen) {
        World world = screen.getWorld();
        TiledMap map = screen.getMap();

        BodyDef bodyDef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;

        //create bodies/fixtures ground layer of the tilemap
        for (MapObject object : map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set((rectangle.getX() + rectangle.getWidth() / 2) / MarioGame.PPM, (rectangle.getY() + rectangle.getHeight() / 2) / MarioGame.PPM);

            body = world.createBody(bodyDef);

            shape.setAsBox((rectangle.getWidth() / 2) / MarioGame.PPM, (rectangle.getHeight() / 2) / MarioGame.PPM);
            fixtureDef.shape = shape;

            body.createFixture(fixtureDef);
        }

        //create bodies/fixtures pipes layer of the tilemap
        for (MapObject object : map.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set((rectangle.getX() + rectangle.getWidth() / 2) / MarioGame.PPM, (rectangle.getY() + rectangle.getHeight() / 2) / MarioGame.PPM);

            body = world.createBody(bodyDef);

            shape.setAsBox((rectangle.getWidth() / 2) / MarioGame.PPM, (rectangle.getHeight() / 2) / MarioGame.PPM);
            fixtureDef.shape = shape;
            fixtureDef.filter.categoryBits = MarioGame.OBJECT_BIT;

            body.createFixture(fixtureDef);
        }

        //create bodies/fixtures coins layer of the tilemap
        for (MapObject object : map.getLayers().get(4).getObjects().getByType(RectangleMapObject.class)) {
            new Coin(screen, object);
        }

        //create bodies/fixtures bricks layer of the tilemap
        for (MapObject object : map.getLayers().get(5).getObjects().getByType(RectangleMapObject.class)) {
            new Brick(screen, object);
        }

        //create goombas
        goombas = new Array<>();
        for (MapObject object : map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            goombas.add(new Goomba(screen, rectangle.getX() / MarioGame.PPM, rectangle.getY() / MarioGame.PPM));
        }

        //create turtles
        turtles = new Array<>();
        for (MapObject object : map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            turtles.add(new Turtle(screen, rectangle.getX() / MarioGame.PPM, rectangle.getY() / MarioGame.PPM));
        }
    }

    public Array<Goomba> getGoombas() {
        return goombas;
    }
    public Array<Enemy> getEnemies() {
        Array<Enemy> enemies = new Array<>();
        enemies.addAll(goombas);
        enemies.addAll(turtles);
        return enemies;
    }
}
