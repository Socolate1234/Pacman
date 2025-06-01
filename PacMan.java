import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;


public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Squareblock {
        int x, y, width, height;
        int startX, startY, velocityX = 0, velocityY = 0;

        char path = 'U'; // U D L R
        Image image;

        Squareblock(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updatepath(char newpath) {
            char prevpath = this.path;
            updatePosition(newpath);
            if (isStandingAgainstWalls()) {
                revertPosition();
                revertpath(prevpath);
            }
        }
        
        void updatePosition(char newpath) {
            this.path = newpath;
            updateVelocity();
        
            this.x += this.velocityX;
            this.y += this.velocityY;
        }
        
        void revertPosition() {
            this.x -= this.velocityX;
            this.y -= this.velocityY;
        }
        
        void revertpath(char prevpath) {
            this.path = prevpath;
            updateVelocity();
        }
        
        boolean isStandingAgainstWalls() {
            for (Squareblock wall : walls) {
                if (collision(this, wall)) {
                    return true;
                }
            }
            return false;
        }
        
        void updateVelocity() {
            switch (this.path) {
                case 'U':
                    this.velocityX = 0;
                    this.velocityY = -tileSize / 4;
                    break;
                case 'D':
                    this.velocityX = 0;
                    this.velocityY = tileSize / 4;
                    break;
                case 'L':
                    this.velocityX = -tileSize / 4;
                    this.velocityY = 0;
                    break;
                case 'R':
                    this.velocityX = tileSize / 4;
                    this.velocityY = 0;
                    break;
                default:
                    this.velocityX = 0;
                    this.velocityY = 0; // Optional: Handle unexpected paths
                    break;
            }
        }
        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21, columnCount = 19, tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage, inkyGhostImage, clydeGhostImage, pinkyGhostImage, blinkyGhostImage;
    private Image moveUpImage, moveDownImage, moveLeftImage, moveRightImage, gameIcon, cherryFood, appleFood, clockMagic;

    //X = wall, O = skip, P = pac man, ' ' = food
    //Ghosts: b = inky, o = clyde, p = pinky, r = blinky, c = cherry, a= apple 
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X   c   X     c   X",
        "X XX XXX X XXX XX X",
        "X   a             X",
        "X XX X XXXXX X XX X",
        "X    X      X     X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "X       bpo       X",
        "XXXX X XX XX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X   c    X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXX   X   XXXX X",
        "X        c l      X",
        "XXXXXXXXXXXXXXXXXXX" 
    };


    HashSet<Squareblock> walls, foods, ghosts, cherries, apples, clocks;
    Squareblock pacman;

    Timer gameLoop;
    char[] paths = {'U', 'D', 'L', 'R'}; //up down left right
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        //load images
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        inkyGhostImage = new ImageIcon(getClass().getResource("./inkyGhost.png")).getImage();
        clydeGhostImage = new ImageIcon(getClass().getResource("./clydeGhost.gif")).getImage();
        pinkyGhostImage = new ImageIcon(getClass().getResource("./pinkyGhost.png")).getImage();
        blinkyGhostImage = new ImageIcon(getClass().getResource("./blinkyGhost.png")).getImage();

        moveUpImage = new ImageIcon(getClass().getResource("./up.gif")).getImage();
        moveDownImage = new ImageIcon(getClass().getResource("./down.gif")).getImage();
        moveLeftImage = new ImageIcon(getClass().getResource("./left.gif")).getImage();
        moveRightImage = new ImageIcon(getClass().getResource("./right.gif")).getImage();
        gameIcon = new ImageIcon(getClass().getResource("./right3.png")).getImage();
        cherryFood = new ImageIcon(getClass().getResource("./cherry.png")).getImage();
        appleFood = new ImageIcon(getClass().getResource("./apple.png")).getImage();
        clockMagic = new ImageIcon(getClass().getResource("./clock.png")).getImage();

        loadMap();
        for (Squareblock ghost : ghosts) {
            char newpath = paths[random.nextInt(4)];
            ghost.updatepath(newpath);
        }
        //how long it takes to start timer, milliseconds gone between frames
        gameLoop = new Timer(50, this); //20fps (1000/50)
        gameLoop.start();

    }

    public void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();
        cherries = new HashSet<>();
        apples = new HashSet<>();
        clocks = new HashSet<>();

        for (int r = 0; r < rowCount; r++) {
            String row = tileMap[r];
            for (int c = 0; c < columnCount; c++) {
                char tileChar = row.charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;
    
                processTile(tileChar, x, y);
            }
        }
    }
    
    private void processTile(char tileChar, int x, int y) {
        switch (tileChar) {
            case 'X': // Wall
                walls.add(new Squareblock(wallImage, x, y, tileSize, tileSize));
                break;
            case 'b': // inky ghost
                addGhost(inkyGhostImage, x, y);
                break;
            case 'o': // clyde ghost
                addGhost(clydeGhostImage, x, y);
                break;
            case 'p': // pinky ghost
                addGhost(pinkyGhostImage, x, y);
                break;
            case 'r': // blinky ghost
                addGhost(blinkyGhostImage, x, y);
                break;
            case 'P': // Pacman
                pacman = new Squareblock(moveRightImage, x, y, tileSize, tileSize);
                break;
            case ' ': // Food
                foods.add(new Squareblock(null, x + 14, y + 14, 4, 4));
                break;
            case 'c': // Cherry
                cherries.add(new Squareblock(cherryFood, x + 5, y + 5, 25, 25));
                break;
            case 'a': // Apple
                apples.add(new Squareblock(appleFood, x + 5, y + 5, 25, 25));
                break;
            case 'l': // clock
                clocks.add(new Squareblock(clockMagic, x + 5, y + 5, 25, 25));
                break;
            default:
                // Optional: Handle unexpected characters
                break;
        }
    }
    
    private void addGhost(Image ghostImage, int x, int y) {
        ghosts.add(new Squareblock(ghostImage, x, y, tileSize, tileSize));
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Squareblock ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Squareblock wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }
        g.setColor(Color.WHITE);
        for (Squareblock food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }
        for (Squareblock cherry : cherries) {
            g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
        }
        for (Squareblock apple : apples){
            g.drawImage(apple.image, apple.x, apple.y, apple.width, apple.height, null);
        }
        for (Squareblock clock : clocks){
            g.drawImage(clock.image, clock.x, clock.y, clock.width, clock.height, null);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 24));
if (gameOver) {
    g.drawString("Game Over: " + String.valueOf(score), tileSize -6, tileSize - 2);
} else {
    // Draw pacmanLeft images based on the number of lives
    for (int j = 0; j < lives; j++) {
        g.drawImage(gameIcon, j * (tileSize  ), tileSize / 4 , this);
    }
    
    // Combine lives and score into one string and draw it
    g.drawString("                Score: " + score, tileSize - 6 , tileSize - 2 );
    }
}
    public void move() {
        movePacman();
        handleGhosts();
        handleFoodCollision();
    
        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }
    
    private void movePacman() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;
    
        // Check for wall collisions
        if (checkCollisionAgainstWalls(pacman)) {
            pacman.x -= pacman.velocityX;
            pacman.y -= pacman.velocityY;
        }
    }
    
    private void handleGhosts() {
        for (Squareblock ghost : ghosts) {
            // Check for collision with Pacman
            if (collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    return;
                }
                resetPositions();
            }
    
            moveGhost(ghost);
        }
    }
    
    private void moveGhost(Squareblock ghost) {
        if (ghost.y == tileSize * 9 && ghost.path != 'U' && ghost.path != 'D') {
            ghost.updatepath('U');
        }
    
        ghost.x += ghost.velocityX;
        ghost.y += ghost.velocityY;
    
        // Check for wall or boundary collisions
        if (checkCollisionAgainstWalls(ghost) || isOutOfBounds(ghost)) {
            ghost.x -= ghost.velocityX;
            ghost.y -= ghost.velocityY;
    
            char newpath = paths[random.nextInt(4)];
            ghost.updatepath(newpath);
        }
    }
    
    private void handleFoodCollision() {
        Squareblock foodEaten = null;
        Squareblock cherryEaten = null;
        Squareblock appleEaten = null;
        Squareblock clockEaten = null;
        // Check if Pacman eats regular food
        for (Squareblock food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 20; // Regular food adds 20 points
                break; // Pacman can only eat one food at a time
            }
        }
    
        // Remove the eaten regular food
        foods.remove(foodEaten);
    
        // Check if Pacman eats a cherry
        for (Squareblock cherry : cherries) {
            if (collision(pacman, cherry)) {
                cherryEaten = cherry; // Mark the cherry as eaten
                score += 333;
             // Cherry adds 333 points
                break; // Pacman can only eat one cherry at a time
            }
        }
        cherries.remove(cherryEaten);
        // Remove the eaten cherry
        

        for (Squareblock apple : apples) {
            if (collision(pacman, apple)) {
                appleEaten = apple; // Mark the cherry as eaten
               // resetGhosts();
                lives = 3;
                //score += 1000; // Cherry adds 333 points
                break; // Pacman can only eat one cherry at a time
            }
        }
        apples.remove(appleEaten);
        
        for (Squareblock clock : clocks) {
            if (collision(pacman, clock)) {
                clockEaten = clock; // Mark the cherry as eaten
                resetGhosts();
                //lives = 3;
                //score += 1000; 
                break; // Pacman can only eat one cherry at a time
            }
        }
        clocks.remove(clockEaten);
    }
    
    private boolean checkCollisionAgainstWalls(Squareblock Squareblock) {
        return walls.stream().anyMatch(wall -> collision(Squareblock, wall));
    }
    
    private boolean isOutOfBounds(Squareblock Squareblock) {
        return Squareblock.x <= 0 || Squareblock.x + Squareblock.width >= boardWidth;
    }
    
    public boolean collision(Squareblock a, Squareblock b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }
    
    public void resetPositions() {
        resetPacman();
        resetGhosts();
    }
    
    private void resetPacman() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
    }
    
    private void resetGhosts() {
        for (Squareblock ghost : ghosts) {
            ghost.reset();
            char newpath = paths[random.nextInt(4)];
            ghost.updatepath(newpath);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
        // System.out.println("KeyEvent: " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updatepath('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updatepath('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updatepath('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updatepath('R');
        }

        if (pacman.path == 'U') {
            pacman.image = moveUpImage;
        }
        else if (pacman.path == 'D') {
            pacman.image = moveDownImage;
        }
        else if (pacman.path == 'L') {
            pacman.image = moveLeftImage;
        }
        else if (pacman.path == 'R') {
            pacman.image = moveRightImage;
        }
    }
}