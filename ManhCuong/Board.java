package ManhCuong;

import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ManhCuong.entities.mob.Mob;
import ManhCuong.entities.mob.Player;
import ManhCuong.entities.Entity;
import ManhCuong.entities.Message;
import ManhCuong.entities.bomb.Bomb;
import ManhCuong.entities.bomb.Explosion;
import ManhCuong.entities.tile.powerup.Powerup;
import ManhCuong.exceptions.LoadLevelException;
import ManhCuong.graphics.IRender;
import ManhCuong.graphics.Screen;
import ManhCuong.input.Keyboard;
import ManhCuong.level.FileLevel;
import ManhCuong.level.Level;
import ManhCuong.audio.Audio;

public class Board implements IRender {

	private final List<Message> _messages = new ArrayList<>();
	public int _width, _height;
	/**
	 * Thực thể không di chuyển
	 */
	public Entity[] _entities;
//    public List<Shield> _shield = new ArrayList<>();
	/**
	 * Thực thể di chuyển
	 */
	public List<Mob> _mobs = new ArrayList<>();/**
	 /**
	 * Màn chơi
	 */
	protected Level _level;
	/**
	 */
	protected Game _game;
	/**
	 * Input từ keyboard
	 */
	protected Keyboard _input;
	protected Screen _screen;
	/**
	 * Bomb
	 */
	protected List<Bomb> _bombs = new ArrayList<>();
	private int _screenToShow = -1; //1:endgame, 2:changelevel, 3:paused
	/**
	 * Thời gian
	 */
	private int _time = Game.TIME;
	/**
	 * Điểm
	 */
	private int _points = Game.POINTS;
	/**
	 * Mạng
	 */
	private int _lives = Game.LIVES;
	/**
	 * Audio
	 */
	protected Audio _audio = new Audio();
	protected boolean checkAceCall = false;
	protected boolean checkFbCall = false;

	public Board(Game game, Keyboard input, Screen screen) {
		_game = game;
		_input = input;
		_screen = screen;

		changeLevel(1); //start in level 1
	}


	/**
	 |--------------------------------------------------------------------------
	 | Render & Update
	 |--------------------------------------------------------------------------

	 */
	/**
	 * Update được gọi liên tục trong mỗi khung hình
	 *
	 */
	@Override
	public void update() {
		if( _game.isPaused() ) return;

		updateEntities();
		updateMobs();
		updateBombs();
		updateMessages();
		detectEndGame();

		if (!checkAceCall) AceNotify();
		if (!checkFbCall) FirstBloodNotify();

		for (int i = 0; i < _mobs.size(); i++) {
			Mob a = _mobs.get(i);
			if (a.isRemoved()) _mobs.remove(i);
		}
	}

	/**
	 * Render xuất hình ảnh ra màn hình
	 *
	 */
	@Override
	public void render(Screen screen) {
		if( _game.isPaused() ) return;

		//only render the visible part of screen
		int x0 = Screen.xOffset >> 4; //tile precision, -> left X
		int x1 = (Screen.xOffset + screen.getWidth() + Game.TILES_SIZE) / Game.TILES_SIZE; // -> right X
		int y0 = Screen.yOffset >> 4;
		int y1 = (Screen.yOffset + screen.getHeight()) / Game.TILES_SIZE; //render one tile plus to fix black margins

		for (int y = y0; y < y1; y++) {
			for (int x = x0; x < x1; x++) {
				_entities[x + y * _level.getWidth()].render(screen);
			}
		}

		renderBombs(screen);
		renderMobs(screen);
	}


	/**
	 |--------------------------------------------------------------------------
	 | ChangeLevel
	 |--------------------------------------------------------------------------
	 */

	public void newGame() {
		resetProperties();
		changeLevel(1);
	}

	@SuppressWarnings("static-access")
	private void resetProperties() {
		_points = Game.POINTS;
		_lives = Game.LIVES;
		Player._powerups.clear();
		checkFbCall = false;
		checkAceCall = false;
		_game.playerSpeed = 1.0;
		_game.bombRadius = 1;
		_game.bombRate = 1;

	}

	@SuppressWarnings("static-access")
	private void resetPoint() {
		_points = Game.POINTS;
		Player._powerups.clear();
		checkFbCall = false;
		checkAceCall = false;
		_game.playerSpeed = 1.0;
		_game.bombRadius = 1;
		_game.bombRate = 1;
	}


	public void restartLevel() {
		changeLevel(_level.getLevel());
		resetPoint();
	}
	/**
	 * Hack nextLevel
	 */
	public void nextLevel() {
		if (_level.getLevel() <= 5){changeLevel(_level.getLevel() + 1);}
		else{
			endGame();
		}
	}
	/**
	 * Hack mạng
	 */

	public void setNeverDie(){
		_lives = 1000000;
	}
	/**
	 * Hack prevLevel
	 */
	public void prevLevel() {
		if (_level.getLevel() > 1){
			changeLevel(_level.getLevel() - 1);
		}
		else{
			restartLevel();
		}
	}


	/**
	 * Audio
	 */


	public void changeLevel(int level) {
		_time = Game.TIME;
		_screenToShow = 2;
		_game.resetScreenDelay();
		_game.pause();
		_mobs.clear();
		_bombs.clear();
		_messages.clear();
		checkFbCall = false;
		checkAceCall = false;

		try {
			_level = new FileLevel("levels/Level" + level + ".txt", this);
			_entities = new Entity[_level.getHeight() * _level.getWidth()];

			_level.createEntities();
		} catch (LoadLevelException e) {
			endGame(); //failed to load.. so.. no more levels?
		}
	}

	// Thay đổi level bằng code cheat
	public void changeLevelByCode(String str) {
		int i = _level.validCode(str);

		if (i != -1) changeLevel(i + 1);
	}

	public boolean isPowerupUsed(int x, int y, int level) {
		Powerup p;
		for (int i = 0; i < Player._powerups.size(); i++) {
			p = Player._powerups.get(i);
			if(p.getX() == x && p.getY() == y && level == p.getLevel())
				return true;
		}

		return false;
	}

	/**
	 |--------------------------------------------------------------------------
	 | Detections
	 |--------------------------------------------------------------------------
	 */

	/**
	 * Kiểm tra thời gian
	 */
	protected void detectEndGame() {
		if(_time <= 0)
			restartLevel();
	}

	/**
	 * GameOver
	 */
	public void endGame() {
		_screenToShow = 1;
		_game.resetScreenDelay();
		_game.pause();
	}

	/**
	 * Kiểm tra kẻ địch trên sàn
	 */
	public boolean detectNoEnemies() {
		int total = 0;
		for (Mob mob : _mobs) {
			if (!(mob instanceof Player))
				++total;
		}
		return total == 0;
	}

	public void AceNotify(){
		if (!detectNoEnemies()) {
		}
		else {
			_audio.playSound("res/sounds/mk.wav",0);
			checkAceCall = true;
		}
//		return checkAceCall;
	}

	public void FirstBloodNotify(){
		if (!detectFirstBlood()) {
		}
		else {
			_audio.playSound("res/sounds/firstblood.wav",0);
			checkFbCall = true;
		}
//		return checkFbCall;
	}

	public boolean  detectFirstBlood(){
		int total = 0;
		for (Mob mob : _mobs) {
			if (!(mob instanceof Player))
				if (mob.isAlive())
					++total;
		}
		return total == _mobs.size() - 2;
	}


	/**
	 |--------------------------------------------------------------------------
	 | Pause & Resume
	 |--------------------------------------------------------------------------
	 */
	public void gamePause() {

		_game.resetScreenDelay();
		if(_screenToShow <= 0)
			_screenToShow = 3;
		_game.pause();
	}

	public void gameResume() {
		_game.resetScreenDelay();
		_screenToShow = -1;
		_game.run();
	}

	/**
	 |--------------------------------------------------------------------------
	 | Screens
	 |--------------------------------------------------------------------------
	 */

	public void drawScreen(Graphics g) {
		switch (_screenToShow) {
			case 1:
				_screen.drawEndGame(g, _points, _level.getActualCode());
				break;
			case 2:
				_screen.drawChangeLevel(g, _level.getLevel());
				break;
			case 3:
				_screen.drawPaused(g);
				break;
		}
	}

	/**
	 |--------------------------------------------------------------------------
	 | Getters And Setters
	 |--------------------------------------------------------------------------
	 */

	public Entity getEntity(double x, double y, Mob m) {

		Entity res;

		res = getExplosionAt((int) x, (int) y);
		if (res != null) return res;

		res = getBombAt(x, y);
		if (res != null) return res;

		res = getMobAtExcluding((int) x, (int) y, m);
		if (res != null) return res;

		res = getEntityAt((int)x, (int)y);

		return res;
	}

	public List<Bomb> getBombs() {
		return _bombs;
	}

	public Bomb getBombAt(double x, double y) {
		Iterator<Bomb> bs = _bombs.iterator();
		Bomb b;
		while(bs.hasNext()) {
			b = bs.next();
			if(b.getX() == (int)x && b.getY() == (int)y)
				return b;
		}

		return null;
	}

	public Mob getMobAt(double x, double y) {
		Iterator<Mob> itr = _mobs.iterator();

		Mob cur;
		while(itr.hasNext()) {
			cur = itr.next();

			if(cur.getXTile() == x && cur.getYTile() == y)
				return cur;
		}

		return null;
	}

	public Player getPlayer() {
		Iterator<Mob> itr = _mobs.iterator();

		Mob cur;
		while(itr.hasNext()) {
			cur = itr.next();

			if(cur instanceof Player)
				return (Player) cur;
		}

		return null;
	}

	public Mob getMobAtExcluding(int x, int y, Mob a) {
		Iterator<Mob> itr = _mobs.iterator();

		Mob cur;
		while(itr.hasNext()) {
			cur = itr.next();
			if(cur == a) {
				continue;
			}

			if(cur.getXTile() == x && cur.getYTile() == y) {
				return cur;
			}

		}

		return null;
	}

	public Explosion getExplosionAt(int x, int y) {
		Iterator<Bomb> bs = _bombs.iterator();
		Bomb b;
		while(bs.hasNext()) {
			b = bs.next();

			Explosion e = b.explosionAt(x, y);
			if(e != null) {
				return e;
			}

		}

		return null;
	}

	public Entity getEntityAt(double x, double y) {
		return _entities[(int)x + (int)y * _level.getWidth()];
	}

	/**
	 |--------------------------------------------------------------------------
	 | Adds and Removes
	 |--------------------------------------------------------------------------
	 */
	public void addEntitie(int pos, Entity e) {
		_entities[pos] = e;
	}

//	public void addShield(Shield s){
//		_shield.add(s);
//	}

	public void addMob(Mob e) {
		_mobs.add(e);
	}

	public void addBomb(Bomb e) {
		_bombs.add(e);
	}

	public void addMessage(Message e) {
		_messages.add(e);
	}

	/**
	 |--------------------------------------------------------------------------
	 | Renders
	 |--------------------------------------------------------------------------
	 */
	protected void renderEntities(Screen screen) {
		for (Entity entity : _entities) {
			entity.render(screen);
		}
	}

	protected void renderMobs(Screen screen) {

		for (Mob mob : _mobs) mob.render(screen);
	}

	protected void renderBombs(Screen screen) {

		for (Bomb bomb : _bombs) bomb.render(screen);
	}

	public void renderMessages(Graphics g) {
		Message m;
		for (Message message : _messages) {
			m = message;

			g.setFont(new Font("Arial", Font.PLAIN, m.getSize()));
			g.setColor(m.getColor());
			g.drawString(m.getMessage(), (int) m.getX() - Screen.xOffset * Game.SCALE, (int) m.getY());
		}
	}

	/**
	 |--------------------------------------------------------------------------
	 | Updates
	 |--------------------------------------------------------------------------
	 */
	protected void updateEntities() {
		if (_game.isPaused()) return;
		for (Entity entity : _entities) {
			entity.update();
		}
	}

	protected void updateMobs() {
		if(_game.isPaused()) return;
		Iterator<Mob> itr = _mobs.iterator();

		while(itr.hasNext() && !_game.isPaused())
			itr.next().update();
	}

	//	protected void updateShield() {
//		if (_game.isPaused()) return;
//		Iterator<Shield> itr = _shield.iterator();
//
//		while(itr.hasNext() && !_game.isPaused())
//			itr.next().update();
//	}
	protected void updateBombs() {
		if (_game.isPaused()) return;

		for (Bomb bomb : _bombs) bomb.update();
	}

	protected void updateMessages() {
		if (_game.isPaused()) return;
		Message m;
		int left;
		for (int i = 0; i < _messages.size(); i++) {
			m = _messages.get(i);
			left = m.getDuration();

			if (left > 0)
				m.setDuration(--left);
			else
				_messages.remove(i);
		}
	}

	/**
	 |--------------------------------------------------------------------------
	 | Getters & Setters
	 |--------------------------------------------------------------------------
	 */
	public Keyboard getInput() {
		return _input;
	}

	public Level getLevel() {
		return _level;
	}

	public Game getGame() {
		return _game;
	}

	public int getShow() {
		return _screenToShow;
	}

	public void setShow(int i) {
		_screenToShow = i;
	}

	public int getTime() {
		return _time;
	}

	public int getLives() {
		return _lives;
	}

	public int subtractTime() {
		if(_game.isPaused())
			return this._time;
		else
			return this._time--;
	}

	public int getPoints() {
		return _points;
	}

	public void addPoints(int points) {
		this._points += points;
	}

	public void addLives(int lives) {
		this._lives += lives;
	}

	public int getWidth() {
		return _level.getWidth();
	}

	public int getHeight() {
		return _level.getHeight();
	}

}
