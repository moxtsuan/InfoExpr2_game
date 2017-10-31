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

  private static final int JCOUNT = 5;	// ‘Ø‹óŠÔ
	private static final int matchP = 3;	// ƒZƒbƒgƒ|ƒCƒ“ƒg

  public enum State { // ó‘Ô
    DEAD,							// Õ“Ë
    ALIVE,						// ’Êí
    JUMP;							// ƒWƒƒƒ“ƒv
  }
  public enum Dir { // •ûŒü
    N,
    E,
    W,
    S;
  }
	public enum MODE {	// çŠ¶æ…‹
		TITLE,
		GAME,
		END,
		THREE,	// ã‚¢ãƒ‹ãƒ¡ãƒ¼ã‚·ãƒ§ãƒ³ç”¨
		TWO,
		ONE;
	}

  class Point {	// g‚¢‚É‚­‚¢‚Ì‚Åg‚Á‚Ä‚Ü‚¹‚ñ
    public int x;
    public int y;
    private void Point(int ix, int iy) {
      this.x = ix;
      this.y = iy;
    }
  }

  class Bike {	// ©‹@
    private State state;	// ó‘Ô
    public Dir dir;				// •ûŒü
    protected String jiki;	// ƒ_ƒ~[‚Å‚·
    protected int px, py; // ‘O‚ÌÀ•W
    protected int x, y;		// À•W
    private int jumpcount;	// ‘Ø‹óŠÔ
    private int speed;		// ƒXƒs[ƒh(‚Ù‚Úƒ_ƒ~[)

    State getState() {	// ó‘Ô‚ğæ“¾
      return this.state;
    }
    void setState(State s) {	// ó‘Ô‚ğƒZƒbƒg
      this.state = s;
    }
    void jump() {	// ƒWƒƒƒ“ƒv
      if ( this.state != State.ALIVE ) { return; }	// ’Êíó‘ÔˆÈŠO‚Å‚Í‰½‚à‚µ‚È‚¢
			this.state = State.JUMP;	// ƒWƒƒƒ“ƒv‚·‚é
      this.jumpcount = JCOUNT;	// ‘Ø‹óŠÔ‚ğƒZƒbƒg
    }
    void update() {	// ‘Ø‹óŠÔ‚ÌŒ¸­
      if ( this.state == State.JUMP ) {
        this.jumpcount--;
        if ( this.jumpcount <= 0 ) {
          this.state = State.ALIVE;
        }
      }
    }
    void move() {	// ˆÚ“®
			if ( this.state == State.ALIVE ) {	// ƒyƒCƒ“ƒg—p
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
    Bike(int ix, int iy, int p) {	// ƒRƒ“ƒXƒgƒ‰ƒNƒ^
      this.state = State.ALIVE;
      this.x = ix;  this.px = this.x;
      this.y = iy;	this.py = this.y;
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

  class Field {	// ƒtƒB[ƒ‹ƒh
    protected Color map[][];	// F
    protected int xSize, ySize;	// ƒTƒCƒY

    void init() {	// ‰Šú‰»
      int i, j;
      for ( j=0; j<ySize; j++ ) {
        this.map[0][j] = map[xSize-1][j] = Color.BLACK;
      }
      for ( i=1; i<xSize-1; i++ ) {
        this.map[i][0] = map[i][ySize-1] = Color.BLACK;
        for ( j=1; j<ySize-1; j++ ) {
          this.map[i][j] = Color.WHITE;
        }
      }
    }
		void setColor(int x, int y, Color c) {
			this.map[x][y] = c;
		}
		Color getColor(int x, int y) {
			return map[x][y];
		}
    Field(int x, int y) {	// ƒRƒ“ƒXƒgƒ‰ƒNƒ^
      this.xSize = x;
      this.ySize = y;
      this.map = new Color[x][y];
    }
  }

  private Field field;
  private Bike p1, p2;

	private int block;
	private Thread thread;
	private String message;
	private String pointmes;	// x vs y
	private Font font;

	private int width, height;
<<<<<<< HEAD
	private int countR, countL;
	private boolean gameset;	// ƒQ[ƒ€ƒZƒbƒg”»’è
=======
	private int countR, countL;	// å¾—ç‚¹
	private boolean gameset;	// ã‚²ãƒ¼ãƒ ã‚»ãƒƒãƒˆåˆ¤å®š
	private MODE mode;				// ãƒ¢ãƒ¼ãƒ‰
>>>>>>> 72ef1b0c43a48f4df712fbc9aecd4538f62befdd

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

		//‚±‚Ì•Ó‚ÉOP•`‰æ
		
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
		// ‘S‘Ì‚ğ”wŒiF‚Å“h‚è‚Â‚Ô‚·B
		g.clearRect(0, 0, width, height);

<<<<<<< HEAD
		 // ˆê’UA•Ê‚Ì‰æ‘œiƒIƒtƒXƒNƒŠ[ƒ“j‚É‘‚«‚Ş
		int i, j;
		for (i = 0; i < field.xSize; i++) {
			for (j = 0; j < field.ySize; j++) {
				g.setColor(field.getColor(i, j));
				g.fillRect(i * block, j * block, block, block);
=======
		if ( mode == MODE.GAME || mode == MODE.END ) {
			int i, j;
			for (i = 0; i < field.xSize; i++) {
				for (j = 0; j < field.ySize; j++) {
					g.setColor(field.getColor(i, j));
					g.fillRect(i * block, j * block, block, block);
				}
>>>>>>> 72ef1b0c43a48f4df712fbc9aecd4538f62befdd
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
		AudioClip clip;
		Thread thisThread = Thread.currentThread();
		//‚±‚Ì•Ó‚ÅƒL[“ü—Í”»’è‚Å”²‚¯o‚¹‚éƒ‹[ƒv‚ÅOP•`‰æ
		
		
		while (thisThread == thread) {
			initialize();
<<<<<<< HEAD
			clip = Applet.newAudioClip(getClass().getResource("test.wav"));
		    clip.loop();
			if ( gameset ) {
=======
			requestFocus();
			if ( gameset && mode == MODE.GAME ) {
>>>>>>> 72ef1b0c43a48f4df712fbc9aecd4538f62befdd
				countR = 0;
				countL = 0;
				gameset = false;
				message = "Game started!";
				repaint();
			}

			while (mode == MODE.GAME && p1.state != State.DEAD && p2.state != State.DEAD) {
				int i;
				p1.update();
				p2.update();
				for ( i=0; p1.state!=State.DEAD&&i<p1.speed; i++ ) {
					p1.move();
					if ( p1.state == State.ALIVE && field.map[p1.x][p1.y] != Color.WHITE ) {
						p1.setState(State.DEAD);
					} else if ( p1.state == State.JUMP && field.map[p1.x][p1.y] == Color.BLACK ) {
						p1.setState(State.DEAD);
					} else {
						field.setColor(p1.px, p1.py, Color.RED);
						field.setColor(p1.x, p1.y, Color.ORANGE);
					}
				}
				for ( i=0; p2.state!=State.DEAD&&i<p2.speed; i++ ) {
					p2.move();
				if ( p2.state == State.ALIVE && field.map[p2.x][p2.y] != Color.WHITE) {
						p2.state = State.DEAD;
						if(p1.x == p2.x && p1.y == p2.y) {
							p1.setState(State.DEAD);
							field.setColor(p1.x, p1.y, Color.MAGENTA.darker());
						}
					} else if ( p2.state == State.JUMP && field.map[p2.x][p2.y] == Color.BLACK ) {
						p2.setState(State.DEAD);
					} else {
						field.setColor(p2.px, p2.py, Color.BLUE);
						field.setColor(p2.x, p2.y, Color.CYAN);
					}
				}
				if (p1.state == State.DEAD) {
					if (p2.state == State.DEAD) {
						message = "Draw!";
						clip.stop();
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
<<<<<<< HEAD
			/* ƒ^ƒCƒgƒ‹ƒo[‚É•\¦‚·‚é•¶š—ñ‚ğw’è‚Å‚«‚é */
			JFrame frame = new JFrame("‰¼‘è");	// Œˆ‚ß‚Ä‚­‚¾‚³‚¢
=======
			/* ã‚¿ã‚¤ãƒˆãƒ«ãƒãƒ¼ã«è¡¨ç¤ºã™ã‚‹æ–‡å­—åˆ—ã‚’æŒ‡å®šã§ãã‚‹ */
			JFrame frame = new JFrame("Tour De Aji");	// æ±ºã‚ã¦ãã ã•ã„
>>>>>>> 72ef1b0c43a48f4df712fbc9aecd4538f62befdd
			frame.add(new Oct31());
			frame.pack();
			frame.setVisible(true);
			/* ~ƒ{ƒ^ƒ“‚ğ‰Ÿ‚µ‚½‚Æ‚«‚Ì“®ì‚ğw’è‚·‚é */
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
	}
}
