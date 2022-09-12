package com.g16.feyrune;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.g16.feyrune.model.Parser.map.Map;
import com.g16.feyrune.view.textureMap.MapDrawer;
import com.g16.feyrune.view.textureMap.TextureMap;
import com.g16.feyrune.view.textureMap.TextureMapParser;

public class Feyrune extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;

	TextureMap textureMap;
	Map map;

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		textureMap = TextureMapParser.parseMapFile("assets/maps/dungeon/dungeon1.tmx");
		map = Map.getGlobalMap();
	}

	@Override
	public void render () {
		ScreenUtils.clear(textureMap.getBackgroundColor());
		batch.begin();
		MapDrawer.drawMap(batch, map, textureMap.getTilesets());
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
