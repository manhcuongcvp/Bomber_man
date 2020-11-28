package ManhCuong.entities.mob;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ManhCuong.Game;
import ManhCuong.Board;
import ManhCuong.entities.Entity;
import ManhCuong.entities.Message;
import ManhCuong.audio.Audio;
import ManhCuong.entities.bomb.Bomb;
import ManhCuong.entities.bomb.DirectionalExplosion;
import ManhCuong.entities.mob.enemy.Enemy;
import ManhCuong.entities.tile.powerup.Powerup;
import ManhCuong.graphics.Screen;
import ManhCuong.graphics.Sprite;
import ManhCuong.input.Keyboard;
import ManhCuong.level.Coordinates;

public class Player extends Mob {

	private List<Bomb> _bombs;
	protected Keyboard _input;
	protected Audio _audio = new Audio();
	protected int _timeBetweenPutBombs = 0;

	public static List<Powerup> _powerups = new ArrayList<Powerup>();

	/** Shield 1.0
	 *
 	 * @time 11.11
	 * @date 11/6/2020
	 * @arthur Manh Cuong
	 */
	protected Shield _shield = new Shield(0,0,0,0,_board,this);
	public void set_shield(Shield _shield){
		this._shield = _shield;
	}

	public Shield get_shield() {
		return _shield;
	}

	protected void DetectSetShield(){
		if (_input.R){
			int xt = Coordinates.pixelToTile(_x + _sprite.getSize() / 2f);
			int yt = Coordinates.pixelToTile( (_y + _sprite.getSize() / 2f) - _sprite.getSize() ); //subtract half player height and minus 1 y position
			placeShield(xt,yt);
		}
	}

	protected void placeShield(int x, int y){
		if (_shield.getCdSkill() <= 0){
			_shield = new Shield(x,y,300,1800,_board,this);
			_shield.setActive(true);
			//_board.addEntitie(0,_shield);
		}
	}





	public Player(int x, int y, Board board) {
		super(x, y, board);
		_bombs = _board.getBombs();
		_input = _board.getInput();
		_sprite = Sprite.player_right;
	}


	/*
	|--------------------------------------------------------------------------
	| Update & Render
	|--------------------------------------------------------------------------
	 */
	@Override
	public void update() {
		clearBombs();
		if(_alive == false) {
			afterKill();
			return;
		}

		if(_timeBetweenPutBombs < -7500) _timeBetweenPutBombs = 0; else _timeBetweenPutBombs--; //dont let this get tooo big

		animate();

		DetectSetShield();


		calculateMove();

		detectPlaceBomb();
		_shield.update();
	}

	@Override
	public void render(Screen screen) {
		calculateXOffset();

		if(_alive)
			chooseSprite();
		else
			_sprite = Sprite.movingSprite(Sprite.player_dead1, Sprite.player_dead2, _animate, 20);
					//Sprite.player_dead1;

		screen.renderEntity((int)_x, (int)_y - _sprite.SIZE, this);
		_shield.render(screen);
	}

	public void calculateXOffset() {
		int xScroll = Screen.calculateXOffset(_board, this);
		Screen.setOffset(xScroll, 0);
	}


	/*
	|--------------------------------------------------------------------------
	| Mob Unique
	|--------------------------------------------------------------------------
	 */
	private void detectPlaceBomb() {
		if(_input.space && Game.getBombRate() > 0 && _timeBetweenPutBombs < 0) {

			int xt = Coordinates.pixelToTile(_x + _sprite.getSize() / 2);
			int yt = Coordinates.pixelToTile( (_y + _sprite.getSize() / 2) - _sprite.getSize() ); //subtract half player height and minus 1 y position

			placeBomb(xt,yt);
			Game.addBombRate(-1);

			_timeBetweenPutBombs = 30;
		}
	}

	protected void placeBomb(int x, int y) {
		_audio.playSound("res/sounds/place_bomb.wav",0);
		Bomb b = new Bomb(x, y, _board);
		_board.addBomb(b);
	}

	private void clearBombs() {
		Iterator<Bomb> bs = _bombs.iterator();

		Bomb b;
		while(bs.hasNext()) {
			b = bs.next();
			if(b.isRemoved())  {
				bs.remove();
				Game.addBombRate(1);
			}
		}

	}

	/*
	|--------------------------------------------------------------------------
	| Mob Colide & Kill
	|--------------------------------------------------------------------------
	 */
	@Override
	public void kill() {
		if (!_shield.getActive()){
			if(!_alive) return;

			_alive = false;

			_board.addLives(-1);
			_audio.playSound("res/sounds/dead.wav",0);
			Message msg = new Message("-1 LIVE", getXMessage(), getYMessage(), 2, Color.white, 14);
			_board.addMessage(msg);
		}
	}

	@Override
	protected void afterKill() {
		if(_timeAfter > 0) --_timeAfter;
		else {
			if(_bombs.size() == 0) {

				if(_board.getLives() == 0)
					_board.endGame();
				else
					_board.restartLevel();
			}
		}
	}

	/*
	|--------------------------------------------------------------------------
	| Mob Movement
	|--------------------------------------------------------------------------
	 */
	@Override
	protected void calculateMove() {
		int xa = 0, ya = 0;
		if(_input.up) ya--;
		if(_input.down) ya++;
		if(_input.left) xa--;
		if(_input.right) xa++;

		if(xa != 0 || ya != 0)  {
			move(xa * Game.getPlayerSpeed(), ya * Game.getPlayerSpeed());
			_moving = true;
		} else {
			_moving = false;
		}

	}

	@Override
	public boolean canMove(double x, double y) {
		for (int c = 0; c < 4; c++) { //colision detection for each corner of the player
			double xt = ((_x + x) + c % 2 * 11) / Game.TILES_SIZE; //divide with tiles size to pass to tile coordinate
			double yt = ((_y + y) + c / 2 * 12 - 13) / Game.TILES_SIZE; //these values are the best from multiple tests

			Entity a = _board.getEntity(xt, yt, this);

			if(!a.collide(this))
				return false;
		}

		return true;
	}

	@Override
	public void move(double xa, double ya) {
		if(xa > 0) _direction = 1;
		if(xa < 0) _direction = 3;
		if(ya > 0) _direction = 2;
		if(ya < 0) _direction = 0;

		if(canMove(0, ya)) { //separate the moves for the player can slide when is colliding
			_y += ya;
		}

		if(canMove(xa, 0)) {
			_x += xa;
		}
	}


	/**
	 * Xử lý va chạm cho người chơi
	 * @param e
	 * @return
	 */
	@Override
	public boolean collide(Entity e) {
		if(e instanceof DirectionalExplosion) {
			kill();
			return false;
		}

		// Nếu có khiên thì húc được kẻ địch chết
		if(e instanceof Enemy) {
			if (_shield.getActive()){
				((Enemy) e).kill();
			}
			else {
				kill();
				return true;
			}
		}

		return true;
	}

	/*
	|--------------------------------------------------------------------------
	| Powerups
	|--------------------------------------------------------------------------
	 */
	public void addPowerup(Powerup p) {
		if(p.isRemoved()) return;
		_audio.playSound("res/sounds/power_up.wav",0);
		_powerups.add(p);

		p.setValues();
	}

	public void clearUsedPowerups() {
		Powerup p;
		for (int i = 0; i < _powerups.size(); i++) {
			p = _powerups.get(i);
			if(p.isActive() == false)
				_powerups.remove(i);
		}
	}

	public void removePowerups() {
		for (int i = 0; i < _powerups.size(); i++) {
				_powerups.remove(i);
		}
	}

	/*
	|--------------------------------------------------------------------------
	| Mob Sprite
	|--------------------------------------------------------------------------
	 */
	private void chooseSprite() {
		switch(_direction) {
		case 0:
			_sprite = Sprite.player_up;
			if(_moving) {
				_sprite = Sprite.movingSprite(Sprite.player_up_1, Sprite.player_up_2, _animate, 20);
			}
			break;
		case 1:
			_sprite = Sprite.player_right;
			if(_moving) {
				_sprite = Sprite.movingSprite(Sprite.player_right_1, Sprite.player_right_2, _animate, 20);
			}
			break;
		case 2:
			_sprite = Sprite.player_down;
			if(_moving) {
				_sprite = Sprite.movingSprite(Sprite.player_down_1, Sprite.player_down_2, _animate, 20);
			}
			break;
		case 3:
			_sprite = Sprite.player_left;
			if(_moving) {
				_sprite = Sprite.movingSprite(Sprite.player_left_1, Sprite.player_left_2, _animate, 20);
			}
			break;
		default:
			_sprite = Sprite.player_right;
			if(_moving) {
				_sprite = Sprite.movingSprite(Sprite.player_right_1, Sprite.player_right_2, _animate, 20);
			}
			break;
		}
	}
}
