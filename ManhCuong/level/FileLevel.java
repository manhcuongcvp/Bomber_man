package ManhCuong.level;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;


import ManhCuong.Board;
import ManhCuong.Game;
import ManhCuong.entities.mob.Player;
import ManhCuong.entities.LayeredEntity;
import ManhCuong.entities.mob.enemy.Balloom;
import ManhCuong.entities.mob.enemy.Doll;
import ManhCuong.entities.mob.enemy.Kondoria;
import ManhCuong.entities.mob.enemy.Minvo;
import ManhCuong.entities.mob.enemy.Oneal;
import ManhCuong.entities.tile.GrassTile;
import ManhCuong.entities.tile.PortalTile;
import ManhCuong.entities.tile.WallTile;
import ManhCuong.entities.tile.destroyable.BrickTile;
import ManhCuong.entities.tile.powerup.PowerupBombs;
import ManhCuong.entities.tile.powerup.PowerupFlames;
import ManhCuong.entities.tile.powerup.PowerupSpeed;
import ManhCuong.exceptions.LoadLevelException;
import ManhCuong.graphics.Screen;
import ManhCuong.graphics.Sprite;

public class FileLevel extends Level {
	
	public FileLevel(String path, Board board) throws LoadLevelException {
		super(path, board);
	}
	
	@Override
	public void loadLevel(String path) throws LoadLevelException {
		try {
			URL absPath = FileLevel.class.getResource("/" + path);
			
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(absPath.openStream()));

			String data = in.readLine();
			StringTokenizer tokens = new StringTokenizer(data);
			
			_level = Integer.parseInt(tokens.nextToken());
			_height = Integer.parseInt(tokens.nextToken());
			_width = Integer.parseInt(tokens.nextToken());

			_lineTiles = new String[_height];
			
			for(int i = 0; i < _height; ++i) {
				_lineTiles[i] = in.readLine().substring(0, _width);
 			}
			
			in.close();
		} catch (IOException e) {
			throw new LoadLevelException("Error loading level " + path, e);
		}
	}
	
	@Override
	public void createEntities() {
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				addLevelEntity( _lineTiles[y].charAt(x), x, y );
			}
		}
	}
	
	public void addLevelEntity(char c, int x, int y) {
		int pos = x + y * getWidth();
		
		switch(c) { // TODO: minimize this method
			case '#': 
				_board.addEntitie(pos, new WallTile(x, y, Sprite.wall));  
				break;
			case 'b': 
				LayeredEntity layer = new LayeredEntity(x, y, 
						new GrassTile(x ,y, Sprite.grass), 
						new BrickTile(x ,y, Sprite.brick));
				
				if(_board.isPowerupUsed(x, y, _level) == false) {
					layer.addBeforeTop(new PowerupBombs(x, y, _level, Sprite.powerup_bombs));
				}
				
				_board.addEntitie(pos, layer);
				break;
			case 's':
				layer = new LayeredEntity(x, y, 
						new GrassTile(x ,y, Sprite.grass), 
						new BrickTile(x ,y, Sprite.brick));
				
				if(_board.isPowerupUsed(x, y, _level) == false) {
					layer.addBeforeTop(new PowerupSpeed(x, y, _level, Sprite.powerup_speed));
				}
				
				_board.addEntitie(pos, layer);
				break;
			case 'f': 
				layer = new LayeredEntity(x, y, 
						new GrassTile(x ,y, Sprite.grass), 
						new BrickTile(x ,y, Sprite.brick));
				
				if(_board.isPowerupUsed(x, y, _level) == false) {
					layer.addBeforeTop(new PowerupFlames(x, y, _level, Sprite.powerup_flames));
				}
				
				_board.addEntitie(pos, layer);
				break;
			case '*': 
				_board.addEntitie(pos, new LayeredEntity(x, y, 
						new GrassTile(x ,y, Sprite.grass), 
						new BrickTile(x ,y, Sprite.brick)) );
				break;
			case 'x': 
				_board.addEntitie(pos, new LayeredEntity(x, y, 
						new GrassTile(x ,y, Sprite.grass), 
						new PortalTile(x ,y, _board, Sprite.portal), 
						new BrickTile(x ,y, Sprite.brick)) );
				break;
			case ' ': 
				_board.addEntitie(pos, new GrassTile(x, y, Sprite.grass) );
				break;
			case 'p': 
				_board.addMob( new Player(Coordinates.tileToPixel(x), Coordinates.tileToPixel(y) + Game.TILES_SIZE, _board) );
				Screen.setOffset(0, 0);
				
				_board.addEntitie(pos, new GrassTile(x, y, Sprite.grass) );
				break;
			//Enemies
			case '1':
				_board.addMob( new Balloom(Coordinates.tileToPixel(x), Coordinates.tileToPixel(y) + Game.TILES_SIZE, _board));
				_board.addEntitie(pos, new GrassTile(x, y, Sprite.grass) );
				break;
			case '2':
				_board.addMob( new Oneal(Coordinates.tileToPixel(x), Coordinates.tileToPixel(y) + Game.TILES_SIZE, _board));
				_board.addEntitie(pos, new GrassTile(x, y, Sprite.grass) );
				break;
			case '3':
				_board.addMob( new Doll(Coordinates.tileToPixel(x), Coordinates.tileToPixel(y) + Game.TILES_SIZE, _board));
				_board.addEntitie(pos, new GrassTile(x, y, Sprite.grass) );
				break;
			case '4':
				_board.addMob( new Minvo(Coordinates.tileToPixel(x), Coordinates.tileToPixel(y) + Game.TILES_SIZE, _board));
				_board.addEntitie(pos, new GrassTile(x, y, Sprite.grass) );
				break;
			case '5':
				_board.addMob( new Kondoria(Coordinates.tileToPixel(x), Coordinates.tileToPixel(y) + Game.TILES_SIZE, _board));
				_board.addEntitie(pos, new GrassTile(x, y, Sprite.grass) );
				break;
			default: 
				_board.addEntitie(pos, new GrassTile(x, y, Sprite.grass) );
				break;
			}
	}
	
}
