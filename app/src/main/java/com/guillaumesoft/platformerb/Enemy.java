package com.guillaumesoft.platformerb;

import com.badlogic.androidgames.framework.DynamicGameObject;
import com.badlogic.androidgames.framework.gl.Animation;
import com.badlogic.androidgames.framework.gl.SpriteBatcher;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.math.Clamp;
import com.badlogic.androidgames.framework.math.Vector2;

/// <summary>
/// A monster who is impeding the progress of our fearless adventurer.
/// </summary>
class Enemy extends DynamicGameObject
{
    /////////////////////////////////////////
    // CLASS CONSTANTS
    private static final float ENEMY_WIDTH  = 90f;
    private static final float ENEMY_HEIGHT = 100f;

    private static final int   MONSTER_STATE_WALK    = 0;
    private static final int   MONSTER_STATE_IDLE    = 1;
    private static final int   MONSTER_STATE_KILLED  = 2;

    //////////////////////////////////////////
    // CLASS VARAIBLES
    public  int     state;
    private float   elapsed;
    public boolean isAlive;

    /// <summary>
    /// How long this enemy has been waiting before turning around.
    /// </summary>
    private float waitTime;
    private int side = 0;
    private Level level;

    /// <summary>
    /// Constructs a new Enemy.
    /// </summary>
    public Enemy(Level level, Vector2 position)
    {
        super(position.x, position.y, ENEMY_WIDTH, ENEMY_HEIGHT);

        this.level = level;
        this.position = position;

        state = MONSTER_STATE_IDLE;

        // RESET PLAYER VELOCITY
        velocity = new Vector2(Vector2.Zero());

        isAlive = true;

        side = -1;
    }

    /// <summary>
    /// Paces back and forth along a platform, waiting at either end.
    /// </summary>
    public void Update(float deltaTime)
    {
        final float MaxWaitTime = 0.5f;
        final float MoveSpeed = 0.9f;
        final float MaxMoveSpeed = 1.5f;

        elapsed += deltaTime;

        // KEEP TRAK OF THE BOUNDS
        bounds.lowerLeft.set(position).sub(bounds.width / 2, bounds.height / 2);

        // Calculate tile position based on the side we are walking towards.
        float posX = position.x + bounds.width / 2 * side;
        int tileX  = (int)Math.floor(posX / Tile.Width) - side;
        int tileY  = (int)Math.floor(position.y / Tile.Height);

        if(isAlive)
        {
            if (waitTime > 0)
            {
                // Wait for some amount of time.
                waitTime = Math.max(0.0f, waitTime - deltaTime);

                state = MONSTER_STATE_IDLE;

                if (waitTime <= 0.0f)
                {
                    if(side == 1)
                        side = -1;
                    else
                        side = 1;
                }
            }
            else
            {
                if(side == 1)
                {

                    //If we are about to run into a wall or off a cliff, start waiting.
                    if ((level.GetCollision(tileX + side, tileY - 1) == TileCollision.Impassable)||(level.GetCollision(tileX + side, tileY - 1) == TileCollision.Checkpoint))
                    {
                        state = MONSTER_STATE_WALK;

                        // Move in the current direction.
                        Vector2 velocity = new Vector2(side * MoveSpeed * elapsed, 0.0f);
                        velocity.x = Clamp.clamp(velocity.x, -MaxMoveSpeed, MaxMoveSpeed);
                        position.add(velocity);
                    }
                    else
                    {
                        waitTime = MaxWaitTime;
                    }
                }
                else
                {
                    //If we are about to run into a wall or off a cliff, start waiting.
                    if ((level.GetCollision(tileX, tileY - 1) == TileCollision.Impassable)||(level.GetCollision(tileX, tileY - 1) == TileCollision.Checkpoint))
                    {
                        state = MONSTER_STATE_WALK;

                        // Move in the current direction.
                        Vector2 velocity = new Vector2(side * MoveSpeed * elapsed, 0.0f);
                        velocity.x = Clamp.clamp(velocity.x, -MaxMoveSpeed, MaxMoveSpeed);
                        position.add(velocity);
                    }
                    else
                    {
                        waitTime = MaxWaitTime;
                    }
                }

                // DID THE ENEMY FIND THE PLAY
                float chase  = position.x - level.player.position.x;
                float diff  = position.y - level.player.position.y;

                if((chase <=  120)&&(diff < 54.0)&&(diff > 0.0))
                {
                    if(position.x > level.player.position.x)
                    {
                        side = -1;
                    }
                    else
                    {
                        side = 1;
                    }
                }

                // HAVE WE HI THE SIDE OF THE SCREEN
                if((position.x < Tile.Width * 3) ||(position.x > 1920 - Tile.Width * 3))
                {
                    if(side == 1)
                        side = -1;
                    else
                        side = 1;
                }
            }
        }
    }

    public void OnKilled()
    {
        state = MONSTER_STATE_KILLED;

        Assets.playSound(Assets.monsterkilled);

        isAlive = false;
    }

    /// <summary>
    /// Draws the animated enemy.
    /// </summary>
    public void Draw( SpriteBatcher batcher)
    {
        TextureRegion keyFrame;
        switch (state)
        {
            case MONSTER_STATE_IDLE:
                keyFrame = Assets.monsterIdleA.getKeyFrame(elapsed, Animation.ANIMATION_LOOPING);
                break;
            case MONSTER_STATE_WALK:
                keyFrame = Assets.monsterRunA.getKeyFrame(elapsed, Animation.ANIMATION_LOOPING);
                break;
            case MONSTER_STATE_KILLED:
                keyFrame = Assets.monsterIdleA.getKeyFrame(elapsed, Animation.ANIMATION_NONLOOPING);
                break;
            default:
                keyFrame = Assets.monsterIdleA.getKeyFrame(elapsed, Animation.ANIMATION_NONLOOPING);
                break;
        }

        batcher.beginBatch(Assets.monsterA);

           batcher.drawSprite(position.x + Tile.Height, position.y, side * ENEMY_WIDTH, ENEMY_HEIGHT, keyFrame);

        batcher.endBatch();
    }
}


