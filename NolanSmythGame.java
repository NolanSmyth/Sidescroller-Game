import java.awt.event.KeyEvent;
import java.util.Random;
import sun.audio.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;


public class NolanSmythGame
{
  
  // set to false to use your code
  private static final boolean DEMO = false;           
  public MattGame dg;
  
  // Game window should be wider than tall:   H_DIM < W_DIM   
  // (more effectively using space)
  private static final int H_DIM = 6;   // # of cells vertically by default: height of game
  private static final int W_DIM = 10;  // # of cells horizontally by default: width of game
  private static final int U_ROW = 0;
  
  private Grid grid;
  private Grid grid1;
  private int userRow;
  private int msElapsed;
  private int countScroll;
  private int timesGet;
  private int timesAvoid;
  private boolean startGame = false;
  private static String musicLoc = "music.aif";
  private static String userGif = "user.gif";
  private static String background = "background.png";
  private static String introBackground = "introbackground.png";
  private static final int introHeight = 12;
  private static final int introWidth = 13;
  private static String[] getGifs = {"g1.png", "g2.png", "g3.png"};
  private static String[] avoidGifs = {"a1.png", "a2.png", "a3.png"};
  
  private boolean pauseGame = false;
  private Random rand = new Random();
  
  private int pauseTime = 100;
  
  public NolanSmythGame()
  {
    init(H_DIM, W_DIM, U_ROW);
  }
  
  public NolanSmythGame(int hdim, int wdim, int uRow)
  {
    if (hdim < wdim)
      init(hdim, wdim, uRow);
    else
      System.out.println("Invalid dimensions. Height must be smaller than width.");
  }
  
  private void init(int hdim, int wdim, int uRow) {  
    grid = new Grid(introBackground, introHeight, introWidth);
    while (!startGame) {
      handleKeyPress();
    }
    //grid = new Grid(hdim, wdim, Color.BLUE);   
    grid = new Grid(background, hdim, wdim);
    
    //other Grid constructor of interest: 
    //    comment the line above; uncomment the one below 
    //You can adjust colors by creating Color objects: look at the Grid constructors
    //grid = new Grid(hdim, wdim, Color.MAGENTA);   
    ///////////////////////////////////////////////
    userRow = uRow;
    msElapsed = 0;
    timesGet = 0;
    timesAvoid = 0;
    updateTitle();
    grid.setImage(new Location(userRow, 0), userGif);
  }
  
  public void play()
  {  
    while (!isGameOver())
    {
      grid.pause(pauseTime);
      handleKeyPress();
        handleKeyPress();
      if (countScroll % (3 * pauseTime) == 0 && !pauseGame)
      {
        scrollLeft();
        populateRightEdge();
      }
      updateTitle();
      msElapsed += pauseTime;
      countScroll += pauseTime;
    }
  }
  
  public void handleKeyPress()
  {
    
    int key = grid.checkLastKeyPressed();
    
    //use Java constant names for key presses
    //http://docs.oracle.com/javase/7/docs/api/constant-values.html#java.awt.event.KeyEvent.VK_DOWN
    if (key == KeyEvent.VK_Q)
      System.exit(0);
    
    else if (key == KeyEvent.VK_UP && userRow > 0) {
      handleCollision(new Location(userRow-1, 0));
      grid.setImage(new Location(userRow, 0), null);
      userRow --;
      grid.setImage(new Location(userRow, 0), userGif);
    }
    
    else if (key == KeyEvent.VK_DOWN && userRow < grid.getNumRows() - 1){
      handleCollision(new Location(userRow+1, 0));
      grid.setImage(new Location(userRow, 0), null);
      userRow ++;
      grid.setImage(new Location(userRow, 0), userGif);
    }
    
    else if (key == KeyEvent.VK_COMMA && pauseTime < 150) {
      pauseTime = pauseTime + 20;
      countScroll = 0;
    }
    
    else if (key == KeyEvent.VK_PERIOD && pauseTime > 50) {
      pauseTime = pauseTime - 20;
      countScroll = 0;
    }
    
    else if (key == KeyEvent.VK_P && !pauseGame)
      pauseGame = true;
    
    else if (key == KeyEvent.VK_P && pauseGame)
      pauseGame = false; 
    
    else if (key == KeyEvent.VK_S)
      startGame = true;
    
    // to help you with step 9  --> explore to understand how to tune your game speed
    else if (key == KeyEvent.VK_T) 
    {
      boolean interval = (msElapsed % (3 * pauseTime) == 0);
      System.out.println("pauseTime " + pauseTime + " msElapsed reset " + msElapsed 
                        + " interval " + interval);
    }
  }
  
  
  public void populateRightEdge(){
    for (int i = 0; i < rand.nextInt(grid.getNumRows()); i++) { 
      int rowGen = rand.nextInt(grid.getNumRows());
      
      if (rand.nextInt(2) == 1) { //50% chance of generating anything
        if(rand.nextInt(2) == 1) { //after generation is confirmed, 50% chance of generating get
          grid.setImage(new Location(rowGen, grid.getNumCols()-1), getGifs[rand.nextInt(3)]);
        }
        else { //after generation is confirmed, 50% chance of generating avoid
          grid.setImage(new Location(rowGen, grid.getNumCols()-1), avoidGifs[rand.nextInt(3)]);
        }
      }
    }
  }
  
  public void scrollLeft(){
    if (grid.getImage(new Location(userRow, 1)) != null) {
        handleCollision(new Location(userRow, 1));
    }
    for (int i = 0; i < grid.getNumRows(); i++) {
      if (grid.getImage(new Location(i,0)) != null 
            && !grid.getImage(new Location(i,0)).equals(userGif))
        grid.setImage(new Location(i,0), null);
      scrollRowLeft(i);
    }
    populateRightEdge();
  }
  
  public void scrollRowLeft(int row) {
    
    for (int i = 1; i < grid.getNumCols(); i++) {
      String img = grid.getImage(new Location(row, i)); 
      String img2replace = grid.getImage(new Location(row, i-1));      
      if (img == null || img.substring(0,1).equals("g") || img.substring(0,1).equals("a")) {
        if (img2replace != null && img2replace.equals(userGif)) {
          handleCollision(new Location(row, i-1));
          continue;
        }
        grid.setImage(new Location(row, i-1), img);
        grid.setImage(new Location(row, i), null);
      }                     
    }
  }
  
  
  public void handleCollision(Location loc){
    if (grid.getImage(loc) == null)
      return;
    else if (grid.getImage(loc).substring(0,1).equals("g"))
      timesGet ++;
    else if (grid.getImage(loc).substring(0,1).equals("a"))
      timesAvoid ++;
  }
  
  public int getScore(){
  int score = timesGet * 10;
  return score;
  }
  
  public void updateTitle()
  {
    grid.setTitle("Game:  " + getScore());
  }
  
  public boolean isGameOver()
  {
    return getScore() > 290 || timesAvoid > 19;
  }
  
  public static void test()
  {
    if (DEMO) {       // reference game: 
                      //   - play and observe first the mechanism of the demo to understand the basic game 
                      //   - go back to the demo anytime you don't know what your next step is
                      //     or details about it are not concrete
                      //         figure out according to the game play 
                      //         (the sequence of display and action) how the functionality
                      //         you are implementing next is supposed to operate
                      // It's critical to have a plan for each piece of code: follow, understand
                      // and study the assignment description details; and explore the basic game. 
                      // You should always know what you are doing (your current, small goal) before
                      // implementing that piece or talk to us. 

      System.out.println("Running the demo: DEMO=" + DEMO);
      //default constructor   (4 by 10)
      MattGame game = new MattGame();
      //other constructor: client adjusts game window size   TRY IT
      // MattGame game = new MattGame(10, 20, 0);
      game.play();
    
    } else {
      System.out.println("Running student game: DEMO=" + DEMO);
      // !DEMO   -> your code should execute those lines when you are
      // implementing your game
      
      //test 1: with parameterless constructor
      NolanSmythGame game = new NolanSmythGame();
      
      //test 2: with constructor specifying grid size    IT SHOULD ALSO WORK as long as height < width
      //NolanSmythGame game = new NolanSmythGame(10, 20, 4);
      game.play();
    }
  }
  
  
  public static void main(String[] args)
  {
    music();
    test();
  }
  
  public static void music() 
    {       
        AudioPlayer MGP = AudioPlayer.player;
        AudioStream BGM;
        AudioData MD;

        ContinuousAudioDataStream loop = null;

        try
        {
            InputStream test = new FileInputStream(musicLoc);
            BGM = new AudioStream(test);
            AudioPlayer.player.start(BGM);
           // MD = BGM.getData();
           // loop = new ContinuousAudioDataStream(MD);

        }
        catch(FileNotFoundException e){
            System.out.print(e.toString());
        }
        catch(IOException error)
        {
            System.out.print(error.toString());
        }
        MGP.start(loop);
    }

}
