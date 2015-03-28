package com.guillaumesoft.platformerb;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;
import com.badlogic.androidgames.framework.math.Clamp;

import com.badlogic.androidgames.framework.Disposable;
import com.badlogic.androidgames.framework.gl.SpriteBatcher;
import com.badlogic.androidgames.framework.gl.Texture;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.math.OverlapTester;
import com.badlogic.androidgames.framework.math.Rectangle;
import com.badlogic.androidgames.framework.math.Vector2;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import tv.ouya.console.api.OuyaController;


/**
 * Created by guillaume on 02/07/14.
 * Guillaumesoft
 * Level.java
 * Feb 19, 2015
 */
public class Level implements Disposable
{
    ///////////////////////////////////////////////////////////
    // FINAL VARAIBLES TYPES
    private static final Vector2 InvalidPosition = new Vector2(-1, -1);

    ////////////////////////////////////////////////////
    // CLASS PRIVATE VARAIBLES

    // Arbitrary, but constant seed
    private Random random = new Random(354668);

    // VALUE TO HOLD LOCATION OF EXIT
    private Vector2 exit;

    // RECTANGLE EXIT VARAIBLES
    private Rectangle exitbounds= null;

    // CURRENT GAME LEVEL VALUE
    private int index;

    // CREATE THE SPRITE BATCH FOR THE LEVEL CLASS
    private SpriteBatcher spritebatcher;

    // The layer which entities are drawn on top of.
    private final int EntityLayer = 2;

    // VALUE TO HOLD CONTROLLER STATUS
    private boolean isConnected = true;

    private Texture[]       textures;
    private TextureRegion[] layerRegion;

    ////////////////////////////////////////////////////
    // CLASS PUBLIC VARAIBLES

    public long time_start;
    public Tile[][] tiles;

    public double TimeRemaing = 120.0;

    public float TimeStatus;

    public boolean reachedExit = false;

    // KEEP TRACK OF GAME SCORE
    public int score;

    ////////////////////////////////////////////////////
    // CLASS INSTANCES
    private ArrayList<String>   sInfo = new ArrayList<>();
    private List<Gem>           gems;
    private List<Enemy>         enamies;
    private List<Axe>           axe;
    private List<Bowl>          bowl;
    private List<BullSkull>     bullskull;
    private List<Dragon>        dragon;
    private List<Food>          food;
    private List<Skull>         skull;
    private List<Spear>         spear;
    private List<Stars>         stars;
    private List<Armour>        armour;
    private List<Barrel>        barrel;
    private List<Fire>          fire;

    // CREATE A PLAYER INSTANCE
    public Player player;


    ////////////////////////////////////////////////////////////
    // CLASS FUNCTIONS

    public Level(InputStream fileStream, int levelindex, SpriteBatcher sp)
    {
        // INITIALIZE CLASS AND OBJECTS
        this.exit         = new Vector2();
        this.gems         = new ArrayList<>();
        this.enamies      = new ArrayList<>();
        this.axe          = new ArrayList<>();
        this.bowl         = new ArrayList<>();
        this.bullskull    = new ArrayList<>();
        this.dragon       = new ArrayList<>();
        this.food         = new ArrayList<>();
        this.skull        = new ArrayList<>();
        this.spear        = new ArrayList<>();
        this.stars        = new ArrayList<>();
        this.armour       = new ArrayList<>();
        this.barrel       = new ArrayList<>();
        this.fire       = new ArrayList<>();

        // LOAD THE TILES
        LoadTiles(fileStream);

        // CURRENT LEVEL
        index = levelindex;

        // GET THE SPRITE BATCH FORM THE GAME SCREEN
        this.spritebatcher = sp;

        textures = new Texture[3];
        layerRegion  = new TextureRegion[3];

        for (int i = 0; i <= 2; i++)
        {
            switch (i) {
                case 0:
                    textures[i] = Assets.layer00;
                    layerRegion[i] = Assets.layer00Region;
                    break;
                case 1:
                    textures[i] = Assets.layer10;
                    layerRegion[i] = Assets.layer10Region;
                    break;
                case 2:
                    textures[i] = Assets.layer20;
                    layerRegion[i] = Assets.layer20Region;
                    break;
            }
        }
    }

    //////////////////////////////////////////////////////////
    // LOAD THE GAME LEVELS
    //////////////////////////////////////////////////////////
    private void LoadTiles(InputStream fileStream)
    {
        String line;
        String[] saLineElements;

        // Load the level and ensure all of the lines are the same length.
        BufferedReader bufferreader = new BufferedReader(new InputStreamReader(fileStream));

        try
        {
            while ((line = bufferreader.readLine()) != null)
            {
                //The information is split into segments and stored into the array
                saLineElements = line.split(" ");
                sInfo.add(saLineElements[0]);
            }
            bufferreader.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // REVERSE THE ORDER OF THE ARRAY
        Collections.reverse(sInfo);

        // INITIALIZE THE TILE ARRAY
        tiles = new Tile[48][33];

        for(int y=0; y < 33; y++)
        {

            for (int x = 0; x < 48; x++)//29
            {
                // to load each tile.
                char tileType = sInfo.get(y).charAt(x);

                tiles[x][y] = LoadTile(tileType, x, y);
            }
        }

        // Verify that the level has a beginning and an end.
        if (player == null)
            throw new UnsupportedOperationException("A level must have a starting point.");
        if (exit == InvalidPosition)
            throw new UnsupportedOperationException("A level must have an exit.");
    }

    //////////////////////////////////////////////////////
    // TRANSLATE CHAR TO TO TILES THEN LOAD THE TILES
    //////////////////////////////////////////////////////
    private Tile LoadTile(char tileType, int x, int y)
    {
        switch (tileType)
        {
            // Blank space
            case '.':
                return new Tile(null, TileCollision.Passable);

            case 'A':
                // LOAD ALL THE ENEMY
                return LoadEnemiesTile(x * Tile.Width, y * Tile.Height + 30, 0);

            case 'B':
                // LOAD ALL THE ENEMY
                return LoadEnemiesTile(x * Tile.Width, y * Tile.Height + 30, 1);

            case 'C':
                // LOAD ALL THE ENEMY
                return LoadEnemiesTile(x * Tile.Width, y * Tile.Height + 30, 2);

            case 'G':
                // LOAD ALL THE GEMS
                return LoadGemTile(x * Tile.Width, y * Tile.Height);

            case '#':
                TextureRegion block = Assets.BlockARegion;
                switch(random.nextInt(4) + 1)
                {
                    case 1:
                        block = Assets.BlockARegion;
                        break;
                    case 2:
                        block = Assets.BlockBRegion;
                        break;
                    case 3:
                        block = Assets.BlockCRegion;
                        break;
                    case 4:
                        block = Assets.BlockDRegion;
                        break;
                    case 5:
                        block = Assets.BlockERegion;
                        break;
                    case 6:
                        block = Assets.BlockFRegion;
                        break;
                    case 7:
                        block = Assets.BlockGRegion;
                        break;
                }
                return LoadTile(block, TileCollision.Impassable);

            case 'X':
                return LoadExitTile(x * Tile.Width, y * Tile.Height);

            case '1':
                return LoadStartTile(x * Tile.Width, y * Tile.Height);

            //Ladder
            case 'H':
                return LoadTile(Assets.ladder, TileCollision.Ladder);

            case 'P':
                return LoadTile(Assets.BlockARegion, TileCollision.Checkpoint);

            case 'E':
               // LOAD ALL THE AXE
               return LoadAxeTile(x * Tile.Width, y * Tile.Height);

            case 'F':
                // LOAD ALL THE BOWL
                return LoadBowlTile(x * Tile.Width, y * Tile.Height);

            case 'I':
                // LOAD ALL THE BULLSKULL
                return LoadBullskullTile(x * Tile.Width, y * Tile.Height);

            case 'O':
                // LOAD ALL THE DRAGON
                return LoadDragonTile(x * Tile.Width, y * Tile.Height);

            case 'J':
                // LOAD ALL THE FOOD
                return LoadFoodTile(x * Tile.Width, y * Tile.Height);

            case 'K':
                // LOAD ALL THE SKULL
                return LoadSkullTile(x * Tile.Width, y * Tile.Height);

            case 'L':
                // LOAD ALL THE SPEAR
                return LoadSpearTile(x * Tile.Width, y * Tile.Height);

            case 'N':
                // LOAD ALL THE STARS
                return LoadStarsTile(x * Tile.Width, y * Tile.Height);

            case 'Q':
                // LOAD ALL THE STARS
                return LoadArmourTile(x * Tile.Width, y * Tile.Height);

            case 'R':
                // LOAD ALL THE STARS
                return LoadBarrelTile(x * Tile.Width, y * Tile.Height);

            case 'T':
                // LOAD THE FIRE
               return LoadFireTile(x * Tile.Width, y * Tile.Height);

            default:
                throw new UnsupportedOperationException("Unsupported tile type character");
        }
    }


    private Tile LoadTile(TextureRegion region, TileCollision collision)
    {
        return new Tile(region, collision);
    }

    /// <summary>
    /// Remembers the location of the level's exit.
    /// </summary>
    private Tile LoadExitTile(int x, int y)
    {
        exitbounds = new Rectangle(x , y, 0.6f, 0.6f);

        return LoadTile(Assets.ExitRegion, TileCollision.Passable );
    }

    /// <summary>
    /// Instantiates a player, puts him in the level, and remembers where to put him when he is resurrected.
    /// </summary>
    private Tile LoadStartTile(int x, int y)
    {
        if (player != null)
            throw new IllegalArgumentException("A level may only have one starting point.");

        // INITIALIZE THE PLAYER
        player = new Player(this, new Vector2(x, y));

        // RETURN TILE VALUE
        return new Tile(null, TileCollision.Passable );
    }

    /// <summary>
    /// Instantiates an enemy A and puts him in the level.
    /// </summary>
    private Tile LoadEnemiesTile(int x, int y, int iType)
    {
        Enemy ENEMY = new Enemy(this, new Vector2(x, y), iType);
        this.enamies.add(ENEMY);

        return new Tile(null, TileCollision.Passable);
    }

    /// <summary>
    /// Instantiates a gem and put it in the level.
    /// </summary>
    private Tile LoadFireTile(int x, int y)
    {
        Fire FIRE = new Fire(x, y);
        this.fire.add(FIRE);

        return new Tile(null, TileCollision.Passable);
    }

    /// <summary>
    /// Instantiates a gem and put it in the level.
    /// </summary>
    private Tile LoadGemTile(int x, int y)
    {
        Gem GEM = new Gem(x, y);
        this.gems.add(GEM);

        return new Tile(null, TileCollision.Passable);
    }

    /// <summary>
    /// Instantiates a armour and put it in the level.
    /// </summary>
    private Tile LoadArmourTile(int x, int y)
    {
        Armour ARMOUR = new Armour(x, y);
        this.armour.add(ARMOUR);

        return new Tile(null, TileCollision.Passable);
    }

    /// <summary>
    /// Instantiates a axe and put it in the level.
    /// </summary>
    private Tile LoadAxeTile(int x, int y)
    {
        Axe AXE = new Axe(x, y);
        this.axe.add(AXE);

        return new Tile(null, TileCollision.Passable);
    }

    /// <summary>
    /// Instantiates a axe and put it in the level.
    /// </summary>
    private Tile LoadBarrelTile(int x, int y)
    {
        Barrel BARREL = new Barrel(x, y);
        this.barrel.add(BARREL);

        return new Tile(null, TileCollision.Passable);
    }

    /// <summary>
    /// Instantiates a bowl and put it in the level.
    /// </summary>
    private Tile LoadBowlTile(int x, int y)
    {
        Bowl BOWL = new Bowl(x, y);
        this.bowl.add(BOWL);

        return new Tile(null, TileCollision.Passable);
    }

    /// <summary>
    /// Instantiates a bullskull and put it in the level.
    /// </summary>
    private Tile LoadBullskullTile(int x, int y)
    {
        BullSkull BULLSKULL = new BullSkull(x, y);
        this.bullskull.add(BULLSKULL);

        return new Tile(null, TileCollision.Passable);
    }

    /// <summary>
    /// Instantiates a dragon and put it in the level.
    /// </summary>
    private Tile LoadDragonTile(int x, int y)
    {
        Dragon DRAGON = new Dragon(x, y);
        this.dragon.add(DRAGON);

        return new Tile(null, TileCollision.Passable);
    }

    /// <summary>
    /// Instantiates a food and put it in the level.
    /// </summary>
    private Tile LoadFoodTile(int x, int y)
    {
        Food FOOD = new Food(x, y);
        this.food.add(FOOD);

        return new Tile(null, TileCollision.Passable);
    }

    /// <summary>
    /// Instantiates a food and put it in the level.
    /// </summary>
    private Tile LoadSkullTile(int x, int y)
    {
        Skull SKULL = new Skull(x, y);
        this.skull.add(SKULL);

        return new Tile(null, TileCollision.Passable);
    }

    /// <summary>
    /// Instantiates a food and put it in the level.
    /// </summary>
    private Tile LoadSpearTile(int x, int y)
    {
        Spear SPEAR = new Spear( x, y);
        this.spear.add(SPEAR);

        return new Tile(null, TileCollision.Passable);
    }

    /// <summary>
    /// Instantiates a food and put it in the level.
    /// </summary>
    private Tile LoadStarsTile(int x, int y)
    {
        Stars STAR = new Stars( x, y);
        this.stars.add(STAR);

        return new Tile(null, TileCollision.Passable);
    }



    // HAS THE PLAYER COLLIDED WITH ANY GAME OBJECTS
    private void updateObjectCollisions()
    {
        int i = 0;

        // GEM
        Iterator<Gem> GEM = gems.iterator();

        while(GEM.hasNext())
        {
            Gem obj = GEM.next();

            if (OverlapTester.overlapRectangles(player.bounds, obj.bounds))
            {
                if(Settings.soundEnabled)
                    Assets.playSound(Assets.gemCollected);

                gems.remove(i);

                OnGemCollected(obj, player);

                break;
            }
            i++;
        }

        i = 0;

        // barrel
        Iterator<Barrel> BARREL = barrel.iterator();

        while(BARREL.hasNext())
        {
            Barrel obj = BARREL.next();

            if (OverlapTester.overlapRectangles(player.bounds, obj.bounds))
            {
                if(Settings.soundEnabled)
                    Assets.playSound(Assets.gemCollected);

                barrel.remove(i);

                OnBarrelCollected(obj, player);

                break;
            }
            i++;
        }

        i = 0;

        // axe
        Iterator<Axe> AXE = axe.iterator();

        while(AXE.hasNext())
        {
            Axe obj = AXE.next();

            if (OverlapTester.overlapRectangles(player.bounds, obj.bounds))
            {
                if(Settings.soundEnabled)
                    Assets.playSound(Assets.gemCollected);

                axe.remove(i);

                OnAxeCollected(obj, player);

                break;
            }
            i++;
        }

        i = 0;

        // bowl
        Iterator<Bowl> BOWL = bowl.iterator();

        while(BOWL.hasNext())
        {
            Bowl obj = BOWL.next();

            if (OverlapTester.overlapRectangles(player.bounds, obj.bounds))
            {
                if(Settings.soundEnabled)
                    Assets.playSound(Assets.gemCollected);

                bowl.remove(i);

                OnBowlCollected(obj, player);

                break;
            }
            i++;
        }

        i = 0;

        // bullskull
        Iterator<BullSkull> BULLSKULL = bullskull.iterator();

        while(BULLSKULL.hasNext())
        {
            BullSkull obj = BULLSKULL.next();

            if (OverlapTester.overlapRectangles(player.bounds, obj.bounds))
            {
                if(Settings.soundEnabled)
                    Assets.playSound(Assets.gemCollected);

                bullskull.remove(i);

                OnBullskullCollected(obj, player);

                break;
            }
            i++;
        }

        i = 0;

        // dragon
        Iterator<Dragon> DRAGON = dragon.iterator();

        while(DRAGON.hasNext())
        {
            Dragon obj = DRAGON.next();

            if (OverlapTester.overlapRectangles(player.bounds, obj.bounds))
            {
                if(Settings.soundEnabled)
                    Assets.playSound(Assets.gemCollected);

                dragon.remove(i);

                OnDragonCollected(obj, player);

                break;
            }
            i++;
        }

        i = 0;

        // dragon
        Iterator<Food> FOOD = food.iterator();

        while(FOOD.hasNext())
        {
            Food obj = FOOD.next();

            if (OverlapTester.overlapRectangles(player.bounds, obj.bounds))
            {
                if(Settings.soundEnabled)
                    Assets.playSound(Assets.gemCollected);

                food.remove(i);

                OnFoodCollected(obj, player);

                break;
            }
            i++;
        }

        i = 0;

        // dragon
        Iterator<Skull> SKULL = skull.iterator();

        while(SKULL.hasNext())
        {
            Skull obj = SKULL.next();

            if (OverlapTester.overlapRectangles(player.bounds, obj.bounds))
            {
                if(Settings.soundEnabled)
                    Assets.playSound(Assets.gemCollected);

                skull.remove(i);

                OnSkullCollected(obj, player);

                break;
            }
            i++;
        }

        i = 0;

        // pickup spear
        Iterator<Spear> SPEAR = spear.iterator();

        while(SPEAR.hasNext())
        {
            Spear obj = SPEAR.next();

            if (OverlapTester.overlapRectangles(player.bounds, obj.bounds))
            {
                if(Settings.soundEnabled)
                    Assets.playSound(Assets.gemCollected);

                spear.remove(i);

                OnSpearCollected(obj, player);

                break;
            }
            i++;
        }

        i = 0;

        // pickup stars
        Iterator<Stars> STARS = stars.iterator();

        while(STARS.hasNext())
        {
            Stars obj = STARS.next();

            if (OverlapTester.overlapRectangles(player.bounds, obj.bounds))
            {
                if(Settings.soundEnabled)
                    Assets.playSound(Assets.gemCollected);

                stars.remove(i);

                OnStarsCollected(obj, player);

                break;
            }
            i++;
        }
    }

    /////////////////////////////////////////////
    // UPDATE THE THE GEM TILE
    /////////////////////////////////////////////
    public void updateGem(float deltaTime)
    {
        int len = this.gems.size();
        for(int i = 0; i < len; i++)
        {
            Gem GEM = this.gems.get(i);
            GEM.Update(deltaTime);
        }
    }

    /////////////////////////////////////////////
    // UPDATE THE THE AXE TILE
    /////////////////////////////////////////////
    public void updateAxe(float deltaTime)
    {
        int len = this.axe.size();
        for(int i = 0; i < len; i++)
        {
            Axe AXE = this.axe.get(i);
            AXE.Update(deltaTime);
        }
    }

    /////////////////////////////////////////////
    // UPDATE THE THE BARREL TILE
    /////////////////////////////////////////////
    public void updateBarrel(float deltaTime)
    {
        int len = this.barrel.size();
        for(int i = 0; i < len; i++)
        {
            Barrel BARREL = this.barrel.get(i);
            BARREL.Update(deltaTime);
        }
    }

    /////////////////////////////////////////////
    // UPDATE THE THE BOWL TILE
    /////////////////////////////////////////////
    public void updateBowl(float deltaTime)
    {
        int len = this.bowl.size();
        for(int i = 0; i < len; i++)
        {
            Bowl BOWL = this.bowl.get(i);
            BOWL.Update(deltaTime);
        }
    }

    /////////////////////////////////////////////
    // UPDATE THE THE BOWL TILE
    /////////////////////////////////////////////
    public void updateBullskull(float deltaTime)
    {
        int len = this.bullskull.size();
        for(int i = 0; i < len; i++)
        {
            BullSkull BULLSKULL = this.bullskull.get(i);
            BULLSKULL.Update(deltaTime);
        }
    }

    /////////////////////////////////////////////
    // UPDATE THE THE DRAGON TILE
    /////////////////////////////////////////////
    public void updateDragon(float deltaTime)
    {
        int len = this.dragon.size();
        for(int i = 0; i < len; i++)
        {
            Dragon DRAGON = this.dragon.get(i);
            DRAGON.Update(deltaTime);
        }
    }


    /////////////////////////////////////////////
    // UPDATE THE THE FOOD TILE
    /////////////////////////////////////////////
    public void updateFood(float deltaTime)
    {
        int len = this.food.size();
        for(int i = 0; i < len; i++)
        {
            Food FOOD = this.food.get(i);
            FOOD.Update(deltaTime);
        }
    }

    /////////////////////////////////////////////
    // UPDATE THE THE SKULL TILE
    /////////////////////////////////////////////
    public void updateSkull(float deltaTime)
    {
        int len = this.skull.size();
        for(int i = 0; i < len; i++)
        {
            Skull SKULL = this.skull.get(i);
            SKULL.Update(deltaTime);
        }
    }

    /////////////////////////////////////////////
    // UPDATE THE THE SPEAR TILE
    /////////////////////////////////////////////
    public void updateSpear(float deltaTime)
    {
        int len = this.spear.size();
        for(int i = 0; i < len; i++)
        {
            Spear SPEAR = this.spear.get(i);
            SPEAR.Update(deltaTime);
        }
    }

    /////////////////////////////////////////////
    // UPDATE THE THE SPEAR TILE
    /////////////////////////////////////////////
    public void updateStars(float deltaTime)
    {
        int len = this.stars.size();
        for(int i = 0; i < len; i++)
        {
            Stars STARS = this.stars.get(i);
            STARS.Update(deltaTime);
        }
    }

    /////////////////////////////////////////////
    // UPDATE THE THE SPEAR TILE
    /////////////////////////////////////////////
    public void updateArmour(float deltaTime)
    {
        int len = this.armour.size();
        for(int i = 0; i < len; i++)
        {
            Armour ARMOUR = this.armour.get(i);
            ARMOUR.Update(deltaTime);
        }
    }

    /////////////////////////////////////////////////////////
    // HAS THE  PLAYER COLLIDED WITH THE EXIT TILE
    /////////////////////////////////////////////////////////
    private void OnExitReached()
    {
        player.OnReachedExit();
        Assets.playSound(Assets.exitReached);
        reachedExit = true;
    }

    ///////////////////////////////////////////////
    // UPDATE THE MONSTER A
    ///////////////////////////////////////////////
    public void updateEnemies(float deltaTime)
    {
        int len = this.enamies.size();
        for(int i = 0; i < len; i++)
        {
            Enemy ENEMY = this.enamies.get(i);
            ENEMY.Update(deltaTime);

            if((player.isAlive)&&(ENEMY.isAlive))
            {
                if (OverlapTester.overlapRectangles(player.bounds, ENEMY.bounds))
                {
                    //  IF THE PLAYER IS ATTACKING AND THE ENEMY IS ALIVE THEN KILL THE ENEMY
                    if (player.isAttacking)
                    {
                        ENEMY.OnKilled();
                    }
                    else
                    {
                        player.OnKilled();

                        if (player.lives == 0)
                        {
                            OnPlayerKilled();
                        }
                    }
                }
            }
        }
    }

    /// <summary>
    /// Called when a axe is collected.
    /// </summary>
    /// <param name="gem">The gem that was collected.</param>
    /// <param name="collectedBy">The player who collected this gem.</param>
    private void OnGemCollected(Gem gem, Player collectedBy)
    {
        score += gem.PointValue;

        gem.OnCollected(collectedBy);
    }

    /// <summary>
    /// Called when a gem is collected.
    /// </summary>
    /// <param name="barrel">The barrel that was collected.</param>
    /// <param name="collectedBy">The player who collected this gem.</param>
    private void OnBarrelCollected(Barrel barrel, Player collectedBy)
    {
        score += barrel.PointValue;

        barrel.OnCollected(collectedBy);
    }

    /// <summary>
    /// Called when a axe is collected.
    /// </summary>
    /// <param name="axe">The axe that was collected.</param>
    /// <param name="collectedBy">The player who collected this gem.</param>
    private void OnAxeCollected(Axe axe, Player collectedBy)
    {
        score += axe.PointValue;

        axe.OnCollected(collectedBy);
    }

    /// <summary>
    /// Called when a bowl is collected.
    /// </summary>
    /// <param name="bowl">The bowl that was collected.</param>
    /// <param name="collectedBy">The player who collected this gem.</param>
    private void OnBowlCollected(Bowl bowl, Player collectedBy)
    {
        score += bowl.PointValue;
        bowl.OnCollected(collectedBy);
    }


    /// <summary>
    /// Called when a bullskull is collected.
    /// </summary>
    /// <param name="bullskull">The bullskull that was collected.</param>
    /// <param name="collectedBy">The player who collected this gem.</param>
    private void OnBullskullCollected(BullSkull bullskull, Player collectedBy)
    {
        score += bullskull.PointValue;

        bullskull.OnCollected(collectedBy);
    }

    /// <summary>
    /// Called when a dragon is collected.
    /// </summary>
    /// <param name="dragon">The dragon that was collected.</param>
    /// <param name="collectedBy">The player who collected this gem.</param>
    private void OnDragonCollected(Dragon dragon, Player collectedBy)
    {
        score += dragon.PointValue;

        dragon.OnCollected(collectedBy);
    }

    /// <summary>
    /// Called when a food is collected.
    /// </summary>
    /// <param name="food">The food that was collected.</param>
    /// <param name="collectedBy">The player who collected this gem.</param>
    private void OnFoodCollected(Food food, Player collectedBy)
    {
        score += food.PointValue;

        food.OnCollected(collectedBy);
    }

    /// <summary>
    /// Called when a skull is collected.
    /// </summary>
    /// <param name="skull">The skull that was collected.</param>
    /// <param name="collectedBy">The player who collected this gem.</param>
    private void OnSkullCollected(Skull skull, Player collectedBy)
    {
        score += skull.PointValue;

        skull.OnCollected(collectedBy);
    }

    /// <summary>
    /// Called when a spear is collected.
    /// </summary>
    /// <param name="spear">The spear that was collected.</param>
    /// <param name="collectedBy">The player who collected this gem.</param>
    private void OnSpearCollected(Spear spear, Player collectedBy)
    {
        score += spear.PointValue;

        spear.OnCollected(collectedBy);
    }

    /// <summary>
    /// Called when a spear is collected.
    /// </summary>
    /// <param name="spear">The spear that was collected.</param>
    /// <param name="collectedBy">The player who collected this gem.</param>
    private void OnStarsCollected(Stars star, Player collectedBy)
    {
        score += star.PointValue;

        star.OnCollected(collectedBy);
    }


    /// <summary>
    /// Called when the player is killed.
    /// </summary>
    /// <param name="killedBy">
    /// The enemy who killed the player. This is null if the player was not killed by an
    /// enemy, such as when a player falls into a hole.
    /// </param>
    private void OnPlayerKilled()
    {
        player.OnKilled();
    }

    /////////////////////////////////////////////
    // UPDATE THE THE FIRE TILE
    /////////////////////////////////////////////
    public void updateFire(float deltaTime)
    {
        int len = this.fire.size();
        for(int i = 0; i < len; i++)
        {
            Fire FIRE = this.fire.get(i);
            FIRE.Update(deltaTime);
        }
    }

    /////////////////////////////////////////////////////////////
    // LEVEL UPDATE FUNCTION
    public void update(float deltaTime,  OuyaController  gamePadState)
    {
        TimeStatus =+ deltaTime;

        final int PointsPerSecond = 5;

        // Pause while the player is dead or time is expired.
        if (!player.isAlive || TimeRemaing == 0.0f)
        {
            // Still want to perform physics on the player.
            player.ApplyPhysics(deltaTime);
        }
        else if (reachedExit)
        {
            // Animate the time being converted into points.
            int seconds = (int)Math.round(deltaTime * 100.0f);
            seconds = Math.min(seconds, (int) Math.ceil(deltaTime));
            TimeRemaing -= seconds;
            score += seconds * PointsPerSecond;
        }
        else
        {
            TimeRemaing -= deltaTime;
            player.Update(deltaTime, gamePadState);
            updateGem(deltaTime);
            updateObjectCollisions();
            updateEnemies(deltaTime);

            updateAxe(deltaTime);
            updateBowl(deltaTime);
            updateBullskull(deltaTime);
            updateDragon(deltaTime);
            updateFood(deltaTime);
            updateSkull(deltaTime);
            updateSpear(deltaTime);
            updateStars(deltaTime);
            updateArmour(deltaTime);
            updateBarrel(deltaTime);
            updateFire(deltaTime);

            // DID THE USER HIT THE EXIT
            if (OverlapTester.overlapRectangles(exitbounds, player.bounds))
                OnExitReached();

            if(player.position.y < 0.0f)
                player.OnKilled();

            // Falling off the bottom of the level kills the player.
            //if (player.BoundingRectangle().top >= Height() * Tiles.Height)
               //OnPlayerKilled(null);

            // The player has reached the exit if they are standing on the ground and
            // his bounding rectangle contains the center of the exit tile. They can only
            // exit when they have collected all of the gems.
            //if (player.isAlive && player.isOnGround && player.BoundingRectangle().Contains(exit))
            //{
            //OnExitReached();
            //}
        }

        // Clamp the time remaining at zero.
        if (TimeRemaing < 0.0f)
            TimeRemaing = 0.0f;
    }

    // DISPLAY THE RE-CONNECT MESSAGE TO THE USER IF THE CONTROLLER BECOMES DISCONNECTED
    private void presentController()
    {
        this.spritebatcher.beginBatch(Assets.RedFont);

           Assets.redfont.drawText(this.spritebatcher, "Please re-connect your controller", 1920 / 2 - 400, 1080 / 2, 20.0f, 20.0f);

        this.spritebatcher.endBatch();
    }

    // DISPLAY THE LEVEL FINISHED TO THE USER
   /* private void presentLevelEnd()
    {
        this.spritebatcher.beginBatch(Assets.messages);

           this.spritebatcher.drawSprite(1920 /2, 1080 /2, 300, 150, Assets.winRegion);

        this.spritebatcher.endBatch();
    }*/

    // DISPLAY THE PLAYER READY MESSAGE
    private void presentReady()
    {
        this.spritebatcher.beginBatch(Assets.items);

           this.spritebatcher.drawSprite(1920 /2, 1080 /2, 170, 50, Assets.ready);

        this.spritebatcher.endBatch();
    }

    public void Draw()
    {
        //WindowManager wm = (WindowManager) ScreenManager.getConext().getSystemService(ScreenManager.getConext().WINDOW_SERVICE);
        //Display display = wm.getDefaultDisplay();
        //ScrollCamera(display);

        ///////////////////////////////////////////////////////////////////
        // DRAW THE BACKGROUND
        for (int i = 0; i <= EntityLayer; ++i)
        {
            this.spritebatcher.beginBatch(textures[i]);

               this.spritebatcher.drawSprite(1920 /2,  1080 /2, 1920, 1080, layerRegion[i]);

            this.spritebatcher.endBatch();

        }

        // DRAW THE TILES TO THE SCREEN
        DrawTiles();

        // DRAW THE GEMS ON THE SCREEN
       int  len = this.gems.size();
        for(int i = 0; i < len; i++)
        {
            Gem GEM = this.gems.get(i);
            GEM.Draw( this.spritebatcher);
        }

        // DRAW MONSTER A TO THE SCREEN
        len = this.enamies.size();
        for(int i = 0; i < len; i++)
        {
            Enemy ENEMY = this.enamies.get(i);
            ENEMY.Draw(this.spritebatcher);
        }

        len = this.axe.size();
        for(int i = 0; i < len; i++)
        {
            Axe AXE = this.axe.get(i);
            AXE.Draw(this.spritebatcher);
        }

        len = this.bowl.size();
        for(int i = 0; i < len; i++)
        {
            Bowl BOWL = this.bowl.get(i);
            BOWL.Draw(this.spritebatcher);
        }

        len = this.bullskull.size();
        for(int i = 0; i < len; i++)
        {
            BullSkull BULLSKULL = this.bullskull.get(i);
            BULLSKULL.Draw(this.spritebatcher);
        }

        len = this.dragon.size();
        for(int i = 0; i < len; i++)
        {
            Dragon DRAGON = this.dragon.get(i);
            DRAGON.Draw(this.spritebatcher);
        }

        len = this.food.size();
        for(int i = 0; i < len; i++)
        {
            Food FOOD = this.food.get(i);
            FOOD.Draw(this.spritebatcher);
        }

        len = this.skull.size();
        for(int i = 0; i < len; i++)
        {
            Skull SKULL = this.skull.get(i);
            SKULL.Draw(this.spritebatcher);
        }

        len = this.spear.size();
        for(int i = 0; i < len; i++)
        {
            Spear SPEAR = this.spear.get(i);
            SPEAR.Draw(this.spritebatcher);
        }

        len = this.stars.size();
        for(int i = 0; i < len; i++)
        {
            Stars STARS = this.stars.get(i);
            STARS.Draw(this.spritebatcher);
        }

        len = this.armour.size();
        for(int i = 0; i < len; i++)
        {
            Armour ARMOUR = this.armour.get(i);
            ARMOUR.Draw(this.spritebatcher);
        }

        len = this.barrel.size();
        for(int i = 0; i < len; i++)
        {
            Barrel BARREL = this.barrel.get(i);
            BARREL.Draw(this.spritebatcher);
        }

        len = this.fire.size();
        for(int i = 0; i < len; i++)
        {
            Fire FIRE = this.fire.get(i);
            FIRE.Draw(this.spritebatcher);
        }

        // DRAW THE PLAYER
        player.Draw(this.spritebatcher);

        /*switch(ScreenManager.STATE)
        {
            case ScreenManager.GAME_READY:
                presentReady();
                break;
            case ScreenManager.GAME_OVER:
                presentGameOver();
                break;
            case ScreenManager.GAME_LEVEL_END:
                presentLevelEnd();
                break;
        }

        // IS THE CONTROLLER STILL CONNECTED
        // IF ITS NOT TELL THE USER
        if(!isConnected)
            presentController();*/
    }

    /// <summary>
    /// Draws each tile in the level.
    /// </summary>
    private void DrawTiles()
    {
        // For each tile position
        for(int y=0; y < 33; y++)
        {
            for (int x = 0; x < 48; x++)//29
            {
                // If there is a visible tile in that position
                TextureRegion texture = tiles[x][y].texture;

                if (texture != null)
                {
                    // Draw it in screen space.
                    Vector2 position = new Vector2(x * Tile.Width, y * Tile.Height);// * tile.size();

                    this.spritebatcher.beginBatch(Assets.tile);

                       this.spritebatcher.drawSprite(position.x, position.y, Tile.Width, Tile.Height, texture);

                    this.spritebatcher.endBatch();
                }
            }
        }
    }

    public TileCollision GetCollision(int x, int y)
    {
        // Prevent escaping past the level ends.
        if (x < 0 || x >= 1920)
            return TileCollision.Impassable;
        // Allow jumping past the level top and falling through the bottom.
        if (y < 0 || y >= 1080)
            return TileCollision.Impassable;

        return tiles[x][y].Collision;
    }

    /// <summary>
    /// Restores the player to the starting point to try the level again.
    /// </summary>
    public void StartNewLife()
    {
        player.Reset(new Vector2(player.start));
    }

    public void dispose()  {   }

}