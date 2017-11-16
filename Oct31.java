import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.applet.*;
import java.net.*;

public class Oct31 extends JPanel implements Runnable, KeyListener {
  private static final long serialVersionUID = 1L;

  private static final int JCOUNT = 5;  // 滞空時間
  private static final int matchP = 3;  // セットポイント

  public enum State {  // 状態
    DEAD,              // 衝突
    ALIVE,             // 通常
    JUMP;              // ジャンプ
  }
  public enum Dir {     // 方向
    N,
    E,
    W,
    S;
  }
  public enum MODE {  // 状態
    TITLE,
    GAME,
    END,
    THREE,  // アニメーション用
    TWO,
    ONE;
  }


  class Point {  // 面倒なので使いません
    public int x;
    public int y;
    private void Point(int ix, int iy) {
      this.x = ix;
      this.y = iy;
    }
  }

  class Bike {  // 自機
    private State state;        // 状態
    public Dir dir;             // 方向
    protected String jiki;      // ダミー
    protected int px, py;       // 前の座標
    protected int x, y;         // 座標
    private int jumpcount;      // 滞空時間
    private int speed;          // 速度

    State getState() {  // 状態を取り出す
      return this.state;
    }
    void setState(State s) {  // 状態をセット
      this.state = s;
    }
    void jump() {  // ジャンプ
      if ( this.state != State.ALIVE ) { return; }  // 通常状態でなければ何もしない
      this.state = State.JUMP;  // ジャンプ状態に
      this.jumpcount = JCOUNT;  // 滞空時間を設定
    }
    void update() {  // ジャンプ中の処理
      if ( this.state == State.JUMP ) {
        this.jumpcount--;
        if ( this.jumpcount <= 0 ) {
          this.state = State.ALIVE;
        }
      }
    }

    void move() {  // 移動
      if ( this.state == State.ALIVE ) {  // 空中にいない
        this.py = this.y; this.px = this.x;
      }
      switch ( this.dir ) {
      case N:
        this.y--;
        break;
      case E:
        this.x++;
        break;
      case W:
        this.x--;
        break;
      case S:
        this.y++;
        break;
      }
    }
    void brake() {
      this.speed = 0;
    }
    Bike(int ix, int iy, int p) {
      this.state = State.ALIVE;
      this.x = ix;  this.px = this.x;
      this.y = iy;  this.py = this.y;
      if ( p == 1 ) {
        this.jiki = "L";
        this.dir = Dir.S;
      } else if ( p == 2 ) {
        this.jiki = "R";
        this.dir = Dir.N;
      }
      this.jumpcount = 0;
      this.speed = 3;
    }
  }

  class Field {                  // フィールド
    protected Color map[][];     // 色
    protected int xSize, ySize;  // サイズ

    void init() {   // 初期化
      int i, j, k;
      int x, y;     // ランダムな座標
      int n = 10;   // 障害物の個数
      int m = 3;    // 障害物の大きさ
      for ( j=0; j<ySize; j++ ) {
        this.map[0][j] = map[xSize-1][j] = Color.BLACK;
      }
      for ( i=1; i<xSize-1; i++ ) {
        this.map[i][0] = map[i][ySize-1] = Color.BLACK;
        for ( j=1; j<ySize-1; j++ ) {
          this.map[i][j] = Color.LIGHT_GRAY;
        }
      }
      for( k = 0; k < n; k++) {
        x = (int)(Math.random() * (xSize-m))+1;
        y = (int)(Math.random() * (ySize-m))+1;
        for(i = 0; i < m; i++) {
          for(j = 0; j < m; j++) {
            this.map[i+x][j+y] = Color.GRAY;
          }
        }
      }
    }
    void setColor(int x, int y, Color c) {
      this.map[x][y] = c;
    }
    Color getColor(int x, int y) {
      return map[x][y];
    }

    Field(int x, int y) {
      this.xSize = x;
      this.ySize = y;
      this.map = new Color[x][y];
    }
  }

  private Field field;
  private Bike p1, p2;

  private int block;
  private Thread thread;
  private String message;       // メッセージ
  private String pointmes;      // x vs y
  private Font font;            // フォント

  private int width, height;

  private int countR, countL;   // 得点
  private boolean gameset;      // ゲームセット判定
  private MODE mode;            // モード

  private AudioClip clip;       // 音声ファイル

  private void initialize() {
    field.init();
    p1 = new Bike(2, 2, 1);
    p2 = new Bike(field.xSize - 3, field.ySize - 3, 2);
  }

  public Oct31() {


    setPreferredSize(new Dimension(320, 380));

    block = 4;
    field = new Field(80, 80);
    gameset = true;
    pointmes = "0 vs 0";
    mode = MODE.TITLE;
    font = new Font("Monospaced", Font.PLAIN, 12);
    setFocusable(true);
    addKeyListener(this);
    Dimension size = getPreferredSize();
    width = size.width; height = size.height;

    startThread();


  }

  public void startThread() {
    if (thread == null) {
      thread = new Thread(this);
      thread.start();
    }
  }

  public void stopThread() {
    if (thread != null) {
      thread = null;
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    g.clearRect(0, 0, width, height);

    if ( mode == MODE.GAME || mode == MODE.END ) {
      int i, j;
      for (i = 0; i < field.xSize; i++) {
        for (j = 0; j < field.ySize; j++) {
          g.setColor(field.getColor(i, j));
          g.fillRect(i * block, j * block, block, block);
        }
      }
      g.setFont(font);
      g.setColor(Color.GREEN.darker());
      g.drawString(message, 2 * block, block * (field.ySize+3));
      g.drawString(pointmes, 2 * block, block * (field.ySize+6));
      g.setColor(Color.RED.darker());
      g.drawString("Left:  A(L), S(D), D(U), F(R), W(JUMP)", 2 * block, block * (field.ySize + 9));
      g.setColor(Color.BLUE.darker());
      g.drawString("Right: H(L), J(D), K(U), L(R), O(JUMP)", 2 * block, block * (field.ySize + 12));
    } else if ( mode == MODE.TITLE ) {
      g.setFont(font);
      g.setColor(Color.GREEN.darker());
      g.drawString("Tour De Aji", (field.xSize / 2) * block - 32, block * (field.ySize / 2));
      g.drawString("Push any button!", (field.xSize / 2) * block - 32, block * (field.ySize / 2) + 12);
    }
  }

  public void run() {
    Thread thisThread = Thread.currentThread();

    clip = Applet.newAudioClip(getClass().getResource("test.wav"));


    while (thisThread == thread) {
      initialize();
      requestFocus();
      if ( gameset && mode == MODE.GAME ) {
        countR = 0;
        countL = 0;
        gameset = false;
        message = "Game started!";
        repaint();
      }
      if ( mode == MODE.GAME ) {
        clip.loop();
      }

      while (mode == MODE.GAME && p1.state != State.DEAD && p2.state != State.DEAD) {
        int i;
        p1.update();
        p2.update();
        for ( i=0; p1.state!=State.DEAD&&i<p1.speed; i++ ) {
          p1.move();
          if ( p1.state == State.ALIVE && field.map[p1.x][p1.y] != Color.LIGHT_GRAY ) {
            p1.setState(State.DEAD);
          } else if ( p1.state == State.JUMP && field.map[p1.x][p1.y] == Color.BLACK ) {
            p1.setState(State.DEAD);
          } else {
            field.setColor(p1.px, p1.py, Color.ORANGE);
            field.setColor(p1.x, p1.y, Color.RED);
          }
        }
        for ( i=0; p2.state!=State.DEAD&&i<p2.speed; i++ ) {
          p2.move();
        if ( p2.state == State.ALIVE && field.map[p2.x][p2.y] != Color.LIGHT_GRAY) {
            p2.state = State.DEAD;
            if(p1.x == p2.x && p1.y == p2.y) {
              p1.setState(State.DEAD);
              field.setColor(p1.x, p1.y, Color.MAGENTA.darker());
            }
          } else if ( p2.state == State.JUMP && field.map[p2.x][p2.y] == Color.BLACK ) {
            p2.setState(State.DEAD);
          } else {
            field.setColor(p2.px, p2.py, Color.CYAN);
            field.setColor(p2.x, p2.y, Color.BLUE);
          }
        }
        if (p1.state == State.DEAD) {
          clip.stop();
          if (p2.state == State.DEAD) {
            message = "Draw!";
          } else {
            countR++;
            if ( countR >= matchP ) {
              message = "Gameset! R won!";
              mode = MODE.END;
              gameset = true;
            } else {
              message = "R won!";
            }
          }
        } else if (p2.state == State.DEAD) {
          clip.stop();
          countL++;
          if ( countL >= matchP ) {
            message = "Gameset L won!";
            mode = MODE.END;
            gameset = true;
          } else {
            message = "L won!";
          }
        }
        pointmes = String.valueOf(countR) + " vs " + String.valueOf(countL);
        repaint();
        try{
          Thread.sleep(250);
        } catch(InterruptedException e) {}
      }
      try{
        Thread.sleep(1750);
      } catch(InterruptedException e) {}
    }
  }

  public void keyPressed(KeyEvent e) {
    int key = e.getKeyCode();
    switch (key) {
    case 'A': if ( p1.state != State.JUMP && p1.dir != Dir.E ) { p1.dir = Dir.W; } break;
    case 'S': if ( p1.state != State.JUMP && p1.dir != Dir.N ) { p1.dir = Dir.S; } break;
    case 'D': if ( p1.state != State.JUMP && p1.dir != Dir.S ) { p1.dir = Dir.N; } break;
    case 'F': if ( p1.state != State.JUMP && p1.dir != Dir.W ) { p1.dir = Dir.E; } break;
    case 'W': if ( p1.state == State.ALIVE ) { p1.jump(); } break;
    case 'H': if ( p2.state != State.JUMP && p2.dir != Dir.E ) { p2.dir = Dir.W; } break;
    case 'J': if ( p2.state != State.JUMP && p2.dir != Dir.N ) { p2.dir = Dir.S; } break;
    case 'K': if ( p2.state != State.JUMP && p2.dir != Dir.S ) { p2.dir = Dir.N; } break;
    case 'L': if ( p2.state != State.JUMP && p2.dir != Dir.W ) { p2.dir = Dir.E; } break;
    case 'O': if ( p2.state == State.ALIVE ) { p2.jump(); } break;
    }
  }

  public void keyReleased(KeyEvent e) {}
  public void keyTyped(KeyEvent e) { if ( mode == MODE.TITLE || mode == MODE.END ) { mode = MODE.GAME; } }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      JFrame frame = new JFrame("Tour De Aji");  // 決めてください
      frame.add(new Oct31());
      frame.pack();
      frame.setVisible(true);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    });
  }
}
