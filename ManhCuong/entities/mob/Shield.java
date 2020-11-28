package ManhCuong.entities.mob;

import ManhCuong.Board;
import ManhCuong.entities.Entity;
import ManhCuong.graphics.Screen;
import ManhCuong.graphics.Sprite;

public class Shield extends Mob{
    private boolean isActive = false;
    private double timeRemaining = 0;
    private double cdSkill = 0;
    protected Board _board;
    private Player player;

    public Shield(int x, int y, double timeRemaining, double cdSkill, Board board, Player player) {
        super(x,y,board);
        this._x = x;
        this._y = y;
        _sprite = Sprite.shield;
        _board = board;
        this.timeRemaining = timeRemaining; //Tồn tại 5s
        this.cdSkill = cdSkill; //30s để hồi skill
        this.player = player;
    }


    public void setPosition(double x,double y){
        this._x  = x;
        this._y  = y;
    }


    public boolean  getActive(){
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public double getCdSkill() {
        return cdSkill;
    }

    public void setTimeRemaining(double timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    @Override
    public void update() {
        if (cdSkill > 0) {
            cdSkill--;
        }
//        System.out.println(cdSkill);

        if (timeRemaining > 0){
            timeRemaining--;
            this._x = player.getX();
            this._y = player.getY();
            //System.out.println(_x + "+" + _y);
        }
        else if (isActive)
            {
            this.setActive(false);
            remove();

        }

    }

//    @Override
//    public void render(Screen screen) {
//        this._x = player.getX();
//        this._y = player.getY();
//        screen.renderEntity((int)_x, (int)_y, this);
//    }

    // Cuong
    @Override
    public void render(Screen screen) {
//        calculateXOffset();

//        if(_alive)
//            chooseSprite();
//        else
//            _sprite = Sprite.player_dead1;

//            this._x = player.getX();
//            this._y = player.getY();
//            //System.out.println(_x + "+" + _y);

        if (timeRemaining > 0)
        screen.renderEntity((int)_x, (int)_y - _sprite.SIZE, this);
    }

    @Override
    protected void calculateMove() {

    }

    @Override
    protected void move(double xa, double ya) {

    }

    @Override
    public void kill() {

    }

    @Override
    protected void afterKill() {

    }

    @Override
    protected boolean canMove(double x, double y) {
        return false;
    }

    @Override
    public boolean collide(Entity e) {
        return false;
    }
}
