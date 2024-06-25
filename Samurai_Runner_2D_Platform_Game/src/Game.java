
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.midi.*;
import java.awt.*;
import game2D.*;

// Game demonstrates how we can override the GameCore class
// to create our own 'game'. We usually need to implement at
// least 'draw' and 'update' (not including any local event handling)
// to begin the process. You should also add code to the 'init'
// method that will initialise event handlers etc. 

// Student ID: 2940038

@SuppressWarnings("serial")

public class Game extends GameCore {
	// Useful game constants
	static int screenWidth = 512;
	static int screenHeight = 384;

	// Game constants
	float lift = 0.005f;
	float gravity = 0.0001f;
	float fly = -0.04f;
	float moveSpeed = 0.2f;

	// Enemy Constants
	float wolfMoveSpeed = 0.03f;

	// Game state flags
	boolean right = false;
	boolean left = false;
	boolean jump = false;
	boolean attack = false;
	boolean isAttacking = false;

	// Game state
	boolean gameOver = false;

	// Game resources
	Animation landing;

	// Sprite Initialize
	Sprite player = null;

	Sprite enemy1 = null;
	Sprite enemy2 = null;
	Sprite enemy3 = null;
	Sprite enemy4 = null;

	Sprite flag = null;

	ArrayList<Sprite> clouds = new ArrayList<Sprite>();

	// Layers of background image to provide a parallax effect
	private Image bg1Layer1;
	private Image bg1Layer2;
	private Image bg1Layer3;
	private Image bg1Layer4;
	private Image bg1Layer5;
	private Image bg1Layer6;
	private Image bg1Layer7;
	private Image bg1Layer8;
	private Image bg1LayerAll;

	TileMap tmap = new TileMap(); // Our tile map, note that we load it in init()

	// variables to keep track of game content
	private int levelNumber = 1;
	private int life;
	long total = 0; // The score will be the total time elapsed since a crash

	// the midi background music for the game
	private static Sequencer themeMusic;
	boolean bgmPlaying = false;

	// Animations for the player character
	private Animation pIdle;
	private Animation pRunning;
	private Animation pWalking;
	private Animation pAttacking;
	private Animation pJumping;
	private Animation pDead;
	private Animation pIdleInv;
	private Animation pRunningInv;
	private Animation pWalkingInv;
	private Animation pAttackingInv;
	private Animation pJumpingInv;
	private Animation pDeadInv;

	// enemy animation
	private Animation wolfWalkLeft;
	private Animation wolfWalkRight;
	private Animation wolfDeath;

	// flag animation
	private Animation flagAnim;

	/**
	 * The obligatory main method that creates an instance of our class and starts
	 * it running
	 * 
	 * @param args The list of parameters this program might use (ignored)
	 */
	public static void main(String[] args) {

		Game gct = new Game();
		gct.init("map1.txt");

		// Start in windowed mode with the given screen height and width
		gct.run(false, screenWidth, screenHeight);
	}

	/*
	 * The class plays the theme music of the game using the sequencer
	 * 
	 * @param fileName takes the file name of the music (midi file) and plays it on
	 * loop
	 */
	public static void midiSoundPlayer(String fileName)
			throws InvalidMidiDataException, IOException, MidiUnavailableException {
		// loads the midi file
		Sequence seq = MidiSystem.getSequence(new File(fileName));
		themeMusic = MidiSystem.getSequencer();
		themeMusic.open();
		themeMusic.setSequence(seq);
		// loops the music continuously
		themeMusic.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
		themeMusic.start();
	}

	/**
	 * Initialize the class, e.g. set up variables, load images, create animations,
	 * register event handlers.
	 * 
	 * This shows you the general principles but you should create specific methods
	 * for setting up your game that can be called again when you wish to restart
	 * the game (for example you may only want to load animations once but you could
	 * reset the positions of sprites each time you restart the game).
	 */
	public void init(String map) {
		// plays the bgm if its not already playing
		if (bgmPlaying == false) {
			try {
				midiSoundPlayer("sounds/theme.mid");
			} catch (InvalidMidiDataException | IOException | MidiUnavailableException e) {
				e.printStackTrace();
			}
			// sets the boolean to true to denote that the music is now playing
			bgmPlaying = true;
		}

		Sprite s; // Temporary reference to a sprite

		// Loads the layers of background image
		bg1Layer1 = loadImage("background/bg1_sky.png");
		bg1Layer2 = loadImage("background/bg1_rocks_3.png");
		bg1Layer3 = loadImage("background/bg1_rocks_2.png");
		bg1Layer4 = loadImage("background/bg1_rocks_1.png");
		bg1Layer5 = loadImage("background/bg1_pines.png");
		bg1Layer6 = loadImage("background/bg1_clouds_1.png");
		bg1Layer7 = loadImage("background/bg1_clouds_2.png");
		bg1Layer8 = loadImage("background/bg1_clouds_3.png");
		bg1LayerAll = loadImage("background/bg1_all.png");

		// Load the tile map and print it out so we can check it is valid
		tmap.loadMap("maps", "map1.txt");

		setSize(tmap.getPixelWidth() / 4, tmap.getPixelHeight());
		setVisible(true);

		// Create a set of background sprites that we can
		// rearrange to give the illusion of motion

		// Loads all the animation of the sprites required for the game

		// player animations
		pIdle = new Animation();
		pIdle.loadAnimationFromSheet("images/Idle.png", 6, 1, 60);

		pRunning = new Animation();
		pRunning.loadAnimationFromSheet("images/Run.png", 8, 1, 60);

		pWalking = new Animation();
		pWalking.loadAnimationFromSheet("images/Walk.png", 9, 1, 60);

		pAttacking = new Animation();
		pAttacking.loadAnimationFromSheet("images/Attack.png", 5, 1, 90);

		pJumping = new Animation();
		pJumping.loadAnimationFromSheet("images/Jump.png", 9, 1, 90);

		pDead = new Animation();
		pDead.loadAnimationFromSheet("images/Dead.png", 6, 1, 60);

		pIdleInv = new Animation();
		pIdleInv.loadAnimationFromSheet("images/Idle_Inv.png", 6, 1, 60);

		pRunningInv = new Animation();
		pRunningInv.loadAnimationFromSheet("images/Run_Inv.png", 8, 1, 60);

		pWalkingInv = new Animation();
		pWalkingInv.loadAnimationFromSheet("images/Walk_Inv.png", 9, 1, 60);

		pAttackingInv = new Animation();
		pAttackingInv.loadAnimationFromSheet("images/Attack_Inv.png", 5, 1, 90);

		pJumpingInv = new Animation();
		pJumpingInv.loadAnimationFromSheet("images/Jump_Inv.png", 9, 1, 90);

		pDeadInv = new Animation();
		pDeadInv.loadAnimationFromSheet("images/Dead_Inv.png", 6, 1, 60);

		// enemy animations
		wolfWalkLeft = new Animation();
		wolfWalkLeft.loadAnimationFromSheet("images/Wolf_Walk_Left.png", 21, 1, 60);

		wolfWalkRight = new Animation();
		wolfWalkRight.loadAnimationFromSheet("images/Wolf_Walk_Right.png", 21, 1, 60);

		wolfDeath = new Animation();
		wolfDeath.loadAnimationFromSheet("images/Wolf_Death.png", 17, 1, 60);

		// flag/checkpoint animation
		flagAnim = new Animation();
		flagAnim.loadAnimationFromSheet("images/Flag.png", 5, 1, 30);

		// Initialize the player with an animation
		player = new Sprite(pIdle);

		// Initialize the enemy with an animation
		enemy1 = new Sprite(wolfWalkRight);
		enemy2 = new Sprite(wolfWalkRight);
		enemy3 = new Sprite(wolfWalkRight);
		enemy4 = new Sprite(wolfWalkRight);

		// Initialize the flag/checkpoint with an animation
		flag = new Sprite(flagAnim);

		// Load a single cloud animation
		Animation ca = new Animation();
		ca.addFrame(loadImage("images/cloud.png"), 1000);

		// Create 3 clouds at random positions off the screen
		// to the right
		for (int c = 0; c < 3; c++) {
			s = new Sprite(ca);
			s.setX(screenWidth + (int) (Math.random() * 200.0f));
			s.setY(30 + (int) (Math.random() * 150.0f));
			s.setVelocityX(-0.02f);
			s.show();
			clouds.add(s);
		}

		initialiseGame();

		System.out.println(tmap);
	}

	/**
	 * You will probably want to put code to restart a game in a separate method so
	 * that you can call it when restarting the game when the player loses.
	 */
	public void initialiseGame() {
		// player lives
		life = 5;

		// On the first level it loads the first map and sets the position and velocity
		// of the sprites
		if (levelNumber == 1) {
			tmap.loadMap("maps", "map1.txt");

			player.setPosition(50, 300);
			player.setVelocity(0, 0);
			player.show();

			enemy1.setSpawnX(tmap.getTileXC(22, 15));
			enemy1.setSpawnY(tmap.getTileYC(15, 13));
			enemy1.setMaxPatrol(enemy1.getSpawnX() + 100);
			enemy1.setMinPatrol(enemy1.getSpawnX() - 100);

			enemy2.setSpawnX(tmap.getTileXC(40, 15));
			enemy2.setSpawnY(tmap.getTileYC(15, 13));
			enemy2.setMaxPatrol(enemy2.getSpawnX() + 120);
			enemy2.setMinPatrol(enemy2.getSpawnX() - 120);

			enemy3.setSpawnX(tmap.getTileXC(62, 15));
			enemy3.setSpawnY(tmap.getTileYC(15, 13));
			enemy3.setMaxPatrol(enemy3.getSpawnX() + 100);
			enemy3.setMinPatrol(enemy3.getSpawnX() - 100);

			flag.setPosition(2500, 390);
		}

		// On the second level it loads the second map and sets the position and
		// velocity of the sprites
		if (levelNumber == 2) {
			tmap.loadMap("maps", "map2.txt");

			player.setPosition(50, 300);
			player.setVelocity(0, 0);
			player.show();

			enemy1.setSpawnX(tmap.getTileXC(18, 15));
			enemy1.setSpawnY(tmap.getTileYC(15, 13));
			enemy1.setMaxPatrol(enemy1.getSpawnX() + 100);
			enemy1.setMinPatrol(enemy1.getSpawnX() - 100);

			enemy2.setSpawnX(tmap.getTileXC(34, 15));
			enemy2.setSpawnY(tmap.getTileYC(15, 13));
			enemy2.setMaxPatrol(enemy2.getSpawnX() + 100);
			enemy2.setMinPatrol(enemy2.getSpawnX() - 100);

			enemy3.setSpawnX(tmap.getTileXC(52, 15));
			enemy3.setSpawnY(tmap.getTileYC(15, 13));
			enemy3.setMaxPatrol(enemy3.getSpawnX() + 100);
			enemy3.setMinPatrol(enemy3.getSpawnX() - 100);

			enemy4.setSpawnX(tmap.getTileXC(70, 15));
			enemy4.setSpawnY(tmap.getTileYC(15, 13));
			enemy4.setMaxPatrol(enemy4.getSpawnX() + 100);
			enemy4.setMinPatrol(enemy4.getSpawnX() - 100);

			flag.setPosition(2500, 390);
		}
	}

	/**
	 * Draw the current state of the game.
	 */
	public void draw(Graphics2D g) {
		// Be careful about the order in which you draw objects - you
		// should draw the background first, then work your way 'forward'

		// First work out how much we need to shift the view in order to
		// see where the player is. To do this, we adjust the offset so that
		// it is relative to the player's position along with a shift
		int xo = -(int) player.getX() + 200;
		int yo = 0; // -(int)player.getY() + 200;

		// Maintain the screen view using the offsets
		if (xo >= 0) {
			xo = 0;
		} else if (player.getX() >= 2107) {
			xo = -(int) 1910;
		}

		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());

		// Draw the background image of the game and add a parallax effect by
		// multiplying with offsets
		g.drawImage(bg1Layer1, 0, 0, null);

		g.drawImage(bg1Layer2, (int) (xo * 0.05f), (int) (yo * 0.05f), null);
		g.drawImage(bg1Layer3, (int) (xo * 0.08f), (int) (yo * 0.08f), null);

		g.drawImage(bg1Layer4, (int) (xo * 0.15f), (int) (yo * 0.15f), null);
		g.drawImage(bg1Layer5, (int) (xo * 0.15f), (int) (yo * 0.15f), null);

		g.drawImage(bg1Layer6, (int) (xo * 0.2f), (int) (yo * 0.2f), null);
		g.drawImage(bg1Layer7, (int) (xo * 0.2f), (int) (yo * 0.2f), null);
		g.drawImage(bg1Layer8, (int) (xo * 0.3f), (int) (yo * 0.3f), null);

		// Apply offsets to sprites then draw them
		for (Sprite s : clouds) {
			s.setOffsets(xo, yo);
			s.draw(g);
		}

		// Enemy Sprites in the array list
		ArrayList<Sprite> enemies = new ArrayList<>();
		enemies.add(enemy1);
		enemies.add(enemy2);
		enemies.add(enemy3);
		enemies.add(enemy4);

		// Apply offsets to player, enemies and draw
		for (Sprite e : enemies) {
			e.setOffsets(xo, yo);
			// e.draw(g);
			checkOnScreen(g, e, xo, yo);
		}

		// Apply offsets to tile map and draw it
		tmap.draw(g, xo, yo);

		// Apply offsets to player and draw
		player.setOffsets(xo, yo);
		player.draw(g);

		flag.setOffsets(xo, yo);
		flag.draw(g);

		// Show score and status information
		String score = String.format("Score: %d", total);
		g.setColor(Color.darkGray);
		g.drawString(score, getWidth() - 100, 50);

		// Show lives and status information
		String lives = String.format("Life: %d", life);
		g.setColor(Color.darkGray);
		g.drawString(lives, getWidth() - 100, 70);

		// Show level number and status information
		String level = String.format("Level: %d", levelNumber);
		g.setColor(Color.darkGray);
		g.drawString(level, getWidth() - 100, 90);

		// draw the game over screen when the player dies or completes the game
		if (gameOver) {
			g.setColor(Color.white);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.drawImage(bg1LayerAll, 0, 0, null);

			String msg1 = "GAME OVER";
			String msg2 = "YOUR SCORE: " + total;
			Font font = new Font("Arial", Font.BOLD, 24);
			g.setFont(font);
			g.setColor(Color.black);

			// Enable anti-aliasing for text
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			// checks the font metrics and then places the text on the middle of the screen
			FontMetrics fm = g.getFontMetrics();
			int x1 = (getWidth() - fm.stringWidth(msg1)) / 2;
			int y1 = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
			int x2 = (getWidth() - fm.stringWidth(msg2)) / 2;
			int y2 = y1 + fm.getHeight() + 10;
			g2d.drawString(msg1, x1, y1);
			g2d.drawString(msg2, x2, y2);
			
			// stops the theme music when the game is over
			themeMusic.stop();
		}
	}

	/**
	 * Update any sprites and check for collisions
	 * 
	 * @param elapsed The elapsed time between this call and the previous call of
	 *                elapsed
	 */
	public void update(long elapsed) {

		// checks if the player is dead or the game is completed yet
		if (!gameOver) {
			// Make adjustments to the speed of the sprite due to gravity
			player.setVelocityY(player.getVelocityY() + (gravity * elapsed));

			player.setAnimationSpeed(1.0f);

			if (jump) {
				player.setAnimation(pJumping, false);
				player.setVelocityX(moveSpeed/13);

				// checks if the player is on ground (in checkTileCollision)
				if (gravity == 0) {
					// Sets the y velocity so the player jumps
					gravity = 0.001f;
					player.setVelocityY(-0.6f);

					Sound jumpSound = new Sound("sounds/jump.wav");
					jumpSound.start();
				}
				// Checks if the jump animation has looped once
				if (pJumping.hasLooped() == true) {
					// sets the jump to false after one loop
					jump = false;
				}
			}

			else if (right) {
				player.setVelocityX(0);
				// Set the move speed for the player
				player.setVelocityX(moveSpeed);
				player.setAnimation(pWalking, false);

				// checks if the attack is active
				if (attack) {
					// plays the attack animation
					player.setAnimation(pAttacking, false);

					// checks if the attack animation has looped
					if (pAttacking.hasLooped()) {
						// sets the attack to false after one loop
						attack = false;

						// loads the attack sound
						Sound attackSound = new Sound("sounds/sword.wav");
						attackSound.start();
					}
				}
			}

			else if (left) {
				player.setVelocityX(0);
				// Set the move speed for the player
				player.setVelocityX(-moveSpeed);
				player.setAnimation(pWalkingInv, false);

				// checks if the attack is active
				if (attack) {
					// plays the attack animation
					player.setAnimation(pAttacking, false);

					// checks if the attack animation has looped
					if (pAttacking.hasLooped()) {
						// sets the attack to false after one loop
						attack = false;

						// loads the attack sound
						Sound attackSound = new Sound("sounds/sword.wav");
						attackSound.start();
					}
				}
			}

			else if (attack) {
				player.setAnimation(pAttacking, false);

				// checks if the attack animation has looped
				if (pAttacking.hasLooped()) {
					// sets the attack to false after one loop
					attack = false;

					// loads the attack sound
					Sound attackSound = new Sound("sounds/sword.wav");
					attackSound.start();
				}
			}

			else {
				// Sets the player to idle state
				player.setAnimation(pIdle, true);
				player.setVelocityX(0);

				// sets the loop of attack and jump to false
				pAttacking.setLooped();
				pJumping.setLooped();
			}

			for (Sprite s : clouds)
				s.update(elapsed);

			// Add the enemy sprites to an arraylist
			ArrayList<Sprite> enemies = new ArrayList<>();
			enemies.add(enemy1);
			enemies.add(enemy2);
			enemies.add(enemy3);
			enemies.add(enemy4);

			for (Sprite enemy : enemies) {
				// checks if the enemy sprite exceeds to the patrol range
				if ((enemy.getX() > enemy.getMaxPatrol() && enemy.getDirection())
						|| (enemy.getX() < enemy.getMinPatrol() && !enemy.getDirection())) {
					// Changes the direction of the enemy sprite
					enemy.setDirection(!enemy.getDirection());
				}
				// checks if the direction of the enemy sprite is right
				if (enemy.getDirection()) {
					// sets the velocity of the enemy sprite to make it move right
					enemy.setVelocityX(wolfMoveSpeed);
					// sets the animation of the enemy sprite to left
					enemy.setAnimation(wolfWalkRight);
				}
				// checks if the direction of the enemy sprite is left
				else {
					// sets the velocity of the enemy sprite to make it move left
					enemy.setVelocityX(-wolfMoveSpeed);
					// sets the animation of the enemy sprite to left
					enemy.setAnimation(wolfWalkLeft);
				}
				enemy.update(elapsed);
			}

			// Now update the sprites animation and position
			player.update(elapsed);

			// Then check for any collisions that may have occurred
			handleScreenEdge(player, tmap, elapsed);

			// handles the collision between the player and enemy and player and the
			// checkpoint flag
			handleSpriteCollision();

			// handles the tile collision for the player
			checkTileCollision(player, tmap);
		}
	}

	/**
	 * Checks and handles collisions with the edge of the screen. You should
	 * generally use tile map collisions to prevent the player leaving the game
	 * area. This method is only included as a temporary measure until you have
	 * properly developed your tile maps.
	 * 
	 * @param s       The Sprite to check collisions for
	 * @param tmap    The tile map to check
	 * @param elapsed How much time has gone by since the last call
	 */
	public void handleScreenEdge(Sprite s, TileMap tmap, long elapsed) {
		// This method just checks if the sprite has gone off the bottom screen.
		// Ideally you should use tile collision instead of this approach

		float difference = s.getY() + s.getHeight() - tmap.getPixelHeight();
		if (difference > 0) {
			// Put the player back on the map according to how far over they were
			s.setY(tmap.getPixelHeight() - s.getHeight() - (int) (difference));

			// and make them bounce
			s.setVelocityY(-s.getVelocityY() * 0.75f);
		}
	}

	/**
	 * Method to check if the collision took place between the two sprites using
	 * bounding box
	 * 
	 * @param s1, s2 The two sprites to check the collision for
	 * 
	 * @return true if a collision may have occurred, false if it has not.
	 */
	public boolean boundingBoxCollision(Sprite s1, Sprite s2) {
		return (((s1.getX() + 10) + (s1.getImage().getWidth(null) - 50) > s2.getX())
				&& ((s1.getX() + 10) < ((s2.getX() + s2.getImage().getWidth(null) - 32)))
				&& ((s1.getY() + s1.getImage().getHeight(null) > s2.getY())
						&& (s1.getY() < s2.getY() + s2.getImage().getHeight(null))));
		// return false;
	}

	/**
	 * Method to check if the collision took place between the two sprites using
	 * bounding circles
	 * 
	 * @param s1, s2 The two sprites to check the collision for
	 * 
	 * @return true if a collision may have occurred, false if it has not.
	 */
	public boolean boundingCircleCollision(Sprite s1, Sprite s2) {
		int dx, dy, minimum;

		dx = (int) (s1.getX() - s2.getX());
		dy = (int) (s1.getY() - s2.getY());
		minimum = (int) (s1.getRadius() + s2.getRadius());

		return (((dx * dx) + (dy * dy)) < (minimum * minimum));
	}

	public void handleSpriteCollision() {
		// Adds the enemy sprites to an arraylist
		ArrayList<Sprite> enemies = new ArrayList<>();
		enemies.add(enemy1);
		enemies.add(enemy2);
		enemies.add(enemy3);
		enemies.add(enemy4);

		// A boolean to check if the player got hit by the enemy to decrease his life
		boolean hitDamage = false;

		for (Sprite enemy : enemies) {
			// When the enemy and the player collide with each other
			if (boundingBoxCollision(player, enemy)) {
				// If the attack is active
				if (attack) {
					// Play the death sound of the wolf
					Sound wolfDead = new Sound("sounds/wolf_death.wav");
					wolfDead.start();

					// Hide the enemy
					enemy.hide();

					// Reposition it out of the screen
					enemy.setPosition(0, 0);

					// Play the death animation of the wolf
					enemy.setAnimation(wolfDeath);

					// Add score
					total += 3;
				} else {
					// If the attack is not active then set the boolean hitDamage to true
					hitDamage = true;
				}
			}

			// When the player hits the enemy and the attack is not active
			if (hitDamage) {
				// load the hurt sound effect
				Sound damage = new Sound("sounds/hurt.wav");
				damage.start();

				// if the player is at his last life
				if (life == 1) {
					life--;
					player.setAnimation(pDead);
					player.stop();

					// sets the gamover true
					gameOver = true;
				} else {
					// checks the direction of the player
					if (player.getPlayerDirection()) {
						// pushes the character towards the left
						player.setVelocity(-0.4f, -0.2f);
						// moves the character sprite towards the left
						player.setPosition(player.getX() - 40, player.getY() - 10);
					}
					// if player is moving left
					else if (!player.getPlayerDirection())  {
						// pushes the character towards the right
						player.setVelocity(0.4f, -0.2f);
						// moves the character sprite towards the right
						player.setPosition(player.getX() + 40, player.getY() - 10);
					}
					// take a life
					life--;

					// sets the hitDamage false so the life does not decrement repeatedly on one hit
					hitDamage = false;
				}
			}

			if (levelNumber == 1) {
				// If the character collides with the checkpoint flag
				if ((boundingBoxCollision(player, flag))) {
					// load the level completed sound effect
					Sound levelComp = new Sound("sounds/levelCompleted.wav");
					levelComp.start();
					// change the level to next level
					nextlvl();
				}
			}

			if (levelNumber == 2) {
				// If the character collides with the checkpoint flag
				if ((boundingBoxCollision(player, flag))) {
					// load the level completed sound effect
					Sound levelComp = new Sound("sounds/levelCompleted.wav");
					levelComp.start();
					gameOver = true;
				}
			}
		}
	}

	/**
	 * Check and handles collisions with a tile map for the given sprite 's'. Calls
	 * the other 2 methods which handle the top and bottom & left and right
	 * collisions respectively
	 * 
	 * @param s    The Sprite to check collisions for
	 * @param tmap The tile map to check
	 */
	public void checkTileCollision(Sprite s, TileMap tmap) {
		checkTileCollisionYCoordinate(s, tmap);
		checkTileCollisionXCoordinate(s, tmap);
	}

	/**
	 * Check and handles collisions with a tile map for the given sprite 's'. This
	 * handles the collision of the points with respect to y axis (top and bottom
	 * side collision handling)
	 * 
	 * @param s    The Sprite to check collisions for
	 * @param tmap The tile map to check
	 */
	public void checkTileCollisionYCoordinate(Sprite s, TileMap tmap) {
		// Take a note of a sprite's current position
		float sx = s.getX() + 10;
		float sy = s.getY() + 42; // Adding the values based on the size of the sprite

		// Find out how wide and how tall a tile is
		float tileWidth = tmap.getTileWidth();
		float tileHeight = tmap.getTileHeight();

		// Divide the sprite’s x coordinate by the width of a tile, to get
		// the number of tiles across the x axis that the sprite is positioned at
		// Top left Coordinate
		int xtileTL = (int) (sx / tileWidth);
		int ytileTL = (int) (sy / tileHeight);

		// The character tile at top left coordinate
		char chTL = tmap.getTileChar(xtileTL, ytileTL);

		// Bottom Left Coordinate
		int xtileBL = (int) (sx / tileWidth);
		int ytileBL = (int) ((sy + s.getHeight() - 40) / tileHeight);

		// The character tile at bottom left coordinate
		int chBL = tmap.getTileChar(xtileBL, ytileBL);

		// Top Right Coordinate
		int xtileTR = (int) ((sx + s.getWidth() - 70) / tileWidth);
		int ytileTR = (int) (sy / tileHeight);

		// The character tile at top right coordinate
		int chTR = tmap.getTileChar(xtileTR, ytileTR);

		// Bottom Right Coordinate
		int xtileBR = (int) ((sx + s.getWidth() - 70) / tileWidth);
		int ytileBR = (int) ((sy + s.getHeight() - 40) / tileHeight);

		// The character tile at bottom right coordinate
		int chBR = tmap.getTileChar(xtileBR, ytileBR);

		// If it's not a dot (empty space), handle it
		if ((chTR != '.') || (chTL != '.')) {
			// Here we just stop the sprite and push it downwards a little bit
			s.setY(s.getY() + 1f);
			s.stop();
		}

		// If the bottom left or bottom right are not empty space
		if ((chBR != '.') || (chBL != '.')) {
			if (chBR != 'p' && (chBL != 'p')) {
				// Here we just stop the sprite and push it downwards a little bit so it stays
				// on the
				// ground tiles
				s.setY(s.getY() - 1f);
				// Allows the character to jump in this case since it is touching the ground and
				// can jump
				gravity = 0;
				s.stop();
			}
		}

		// If the bottom left and bottom right character is empty space so jump
		// is active so increase the gravity a bit to make it fall to ground.
		if ((chBR == '.') && (chBL == '.') && (chBR != 'p') && (chBL != 'p') && (chBL != 'w')) {
			gravity = 0.001f;
		}

		// If the bottom right character is a coin then collect the coin
		if (chBR == 'p') {
			// Play the audio when the coin is collected
			Sound coinCollected = new Sound("sounds/coinCollected.wav");
			coinCollected.start();

			// Replace the coin tile with a empty tile
			tmap.setTileChar('.', xtileBR, ytileBR);

			// Add score
			total++;
		}
		// If the bottom left character is a coin then collect the coin
		else if (chBL == 'p') {
			// Play the audio when the coin is collected
			Sound coinCollected = new Sound("sounds/coinCollected.wav");
			coinCollected.start();

			// Replace the coin tile with a empty tile
			tmap.setTileChar('.', xtileBL, ytileBL);

			// Add score
			total++;
		}

		// If the bottom left or bottom right is water then reposition the character to
		// initial position of the map
		if ((chBL == 'w') || (chBR == 'w')) {
			// Play the audio when it hits water
			Sound hurt = new Sound("sounds/hurt.wav");
			hurt.start();

			// Reset the position of the player to default
			player.setPosition(50, 300);
		}
	}

	/**
	 * Check and handles collisions with a tile map for the given sprite 's'. This
	 * handles the collision of the points with respect to x axis (left and right
	 * side collision handling)
	 * 
	 * @param s    The Sprite to check collisions for
	 * @param tmap The tile map to check
	 */
	public void checkTileCollisionXCoordinate(Sprite s, TileMap tmap) {
		// Take a note of a sprite's current position
		float sx = s.getX() + 10;
		float sy = s.getY() + 42;

		// Find out how wide and how tall a tile is
		float tileWidth = tmap.getTileWidth();
		float tileHeight = tmap.getTileHeight();

		// A Coordinate that is little bit above the bottom left coordinate
		int xtileBL = (int) (sx / tileWidth);
		int ytileBL = (int) ((sy + s.getHeight() - 50) / tileHeight);

		// The character tile at that coordinate
		char chBL = tmap.getTileChar(xtileBL, ytileBL);

		// If it's not a dot (empty space)
		if ((chBL != '.') && (chBL != 'p')) {
			// Here we just stop the sprite and push it towards the right
			s.setX(s.getX() + 11);
			s.stop();
		}

		// A Coordinate that is little bit above the bottom right coordinate
		int xtileBR = (int) ((sx + s.getWidth() - 70) / tileWidth);
		int ytileBR = (int) ((sy + s.getHeight() - 50) / tileHeight);
		int chBR = tmap.getTileChar(xtileBR, ytileBR);

		// If it's not empty space
		if ((chBR != '.') && (chBR != 'p')) {
			// Here we just stop the sprite and push it towards the left
			s.setX(s.getX() - 11);
			s.stop();
		}
		// If the bottom left character is a coin then collect the coin
		if ((chBL == 'p')) {
			// Play the audio when the coin is collected
			Sound coinCollected = new Sound("sounds/coinCollected.wav");
			coinCollected.start();

			// Replace the coin tile with a empty tile
			tmap.setTileChar('.', xtileBL, ytileBL);

			// Add score
			total++;
		}
		// If the bottom left character is a coin then collect the coin
		else if (chBR == 'p') {
			// Play the audio when the coin is collected
			Sound coinCollected = new Sound("sounds/coinCollected.wav");
			coinCollected.start();

			// Replace the coin tile with a empty tile
			tmap.setTileChar('.', xtileBR, ytileBR);

			// Add score
			total++;
		}
	}

	/*
	 * This changes the level number and loads the new map
	 */
	public void nextlvl() {
		if (levelNumber == 1) {
			// Increases the level number when the checkpoint is reached
			levelNumber++;

			// Loads the second level
			init("map2.txt");

			// Initialize the game again
			initialiseGame();
		} else if (levelNumber == 2) {
			levelNumber = 1;
		}
	}

	/**
	 *
	 * @param g  the graphics object to draw to
	 * @param s  the current sprite
	 * @param xo the x offset value
	 * @param yo the y offset value
	 */
	public void checkOnScreen(Graphics2D g, Sprite s, int xo, int yo) {
		// Create a rectangle around the edges of the screen
		Rectangle rect = (Rectangle) g.getClip();
		// Variables to register the position of the sprite
		int xc, yc;

		// get the x and y position of the sprite
		xc = (int) (xo + s.getX());
		yc = (int) (yo + s.getY());

		// when the sprite is within the rectangle on the screen
		if (rect.contains(xc, yc)) {
			s.show(); // show the sprite
			s.draw(g); // draw them to the screen
		} else {
			s.hide(); // hide the sprite
		}
	}
	
	/**
	 * Override of the keyPressed event defined in GameCore to catch our own events
	 * 
	 * @param e The event that has been generated
	 */
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();

		switch (key) {
		case KeyEvent.VK_UP:
			jump = true;
			break;
		case KeyEvent.VK_RIGHT:
			right = true;
			break;
		case KeyEvent.VK_LEFT:
			left = true;
			break;
		case KeyEvent.VK_SPACE:
			attack = true;
			break;
		case KeyEvent.VK_ESCAPE:
			stop();
			break;
		default:
			break;
		}
	}

	/**
	 * The event that happens when the key is released
	 * 
	 * @param e the key release event
	 */
	public void keyReleased(KeyEvent e) {

		int key = e.getKeyCode();

		switch (key) {
		case KeyEvent.VK_ESCAPE:
			stop();
			break;
		case KeyEvent.VK_RIGHT:
			right = false;
			break;
		case KeyEvent.VK_LEFT:
			left = false;
			break;
		default:
			break;
		}
	}
	
	/**
	 * The event that happens when the key is released
	 * 
	 * @param e the mouse clicked event
	 */
	public void mouseClicked(MouseEvent e) {
		Sound caw = new Sound("sounds/caw.wav");
		caw.start();
	}
}
