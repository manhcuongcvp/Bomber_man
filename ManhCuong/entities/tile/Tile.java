package ManhCuong.entities.tile;

import ManhCuong.entities.Entity;
import ManhCuong.level.Coordinates;
import ManhCuong.graphics.Screen;
import ManhCuong.graphics.Sprite;

public abstract class Tile extends Entity {
	
	
	public Tile(int x, int y, Sprite sprite) {
		_x = x;
		_y = y;
		_sprite = sprite;
	}
	
	@Override
	public boolean collide(Entity e) {
		return false;
	}
	
	@Override
	public void render(Screen screen) {
		screen.renderEntity( Coordinates.tileToPixel(_x), Coordinates.tileToPixel(_y), this);
	}
	
	@Override
	public void update() {}
}
