package com.guillaumesoft.platformerb;

import com.badlogic.androidgames.framework.DynamicGameObject;
import com.badlogic.androidgames.framework.gl.Animation;
import com.badlogic.androidgames.framework.gl.SpriteBatcher;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.math.Clamp;
import com.badlogic.androidgames.framework.math.OverlapTester;
import com.badlogic.androidgames.framework.math.Rectangle;
import com.badlogic.androidgames.framework.math.Vector2;
import tv.ouya.console.api.OuyaController;

class Player extends DynamicGameObject
{
    ////////////////////////////////////////////////
    // CLASS FINAL VARAIBLES
    ////////////////////////////////////////////////
    private static final int   PLAYER_STATE_JUMP       = 0;
    private static final int   PLAYER_STATE_RUN        = 1;
    private static final int   PLAYER_STATE_DIE        = 2;
    private static final int   PLAYER_STATE_IDLE       = 3;
    private static final int   PLAYER_STATE_ATTACK     = 4;
    private static final int   PLAYER_STATE_CELEBRATE  = 5;

    private static final int PLAYER_WIDTH   = 90;
    private static final int PLAYER_HEIGHT  = 100;

    private static float MaxFallSpeed = 650.0f;

    /////////////////////////////////////////////////
    // PRIVATE CLASS VARAIBLES
    private float   stateTime;
    private float   side;

    // Jumping state
    private boolean isJumping;
    private boolean wasJumping = false;
    private float   jumpTime;
    private int     state;
    private boolean isOnGround;
    // Attacking state
    private final   float   MaxAttackTime = 3.0f;
    private int     numberOfJumps = 0;
    private Vector2 velocity;

    private final float MoveAcceleration   = 13000.0f;

    private Vector2 movement;
    private Level level;

    /////////////////////////////////////////////////
    // PUBLIC CLASS VARAIBLES

    // GET THE PLAYER ATTACK STATE
    public  boolean isAttacking = false;
    public  float   AttackTime;

    //GET THE PLAYER LIFE STATE
    public boolean isAlive;
    /// <summary>
    /// KEEP TRACK OF PLAYER LIVES
    /// </summary>
    public int lives = 3;

    // PLAYER START POSITION
    public Vector2 start = null;

    /// <summary>
    /// Constructors a new player.
    /// </summary>
    public Player(Level level, Vector2 newPosition)
    {
        super(newPosition.x, newPosition.y, PLAYER_WIDTH, PLAYER_HEIGHT);

        // SET THE PLAYER STATE TO IDLE
        this.state = PLAYER_STATE_IDLE;

        if(start == null)
            start = position;

        // RESET PLAYER PARAMETER
        Reset(newPosition);

        // PASS THE LEVEL INSTANCE TO THIS CLASS
        this.level = level;

        // INITIALIZE MOVEMENT
        movement = new Vector2();
    }

    /// <summary>
    /// Resets the player to life.
    /// </summary>
    /// <param name="position">The position to come to life at.</param>
    public void Reset(Vector2 pos)
    {
        // START OFF WITH THE PLAYER IDLE
        this.state = PLAYER_STATE_IDLE;

        // RESET PLAYER VELOCITY
        this.velocity = new Vector2(Vector2.Zero());

        // SET PLAYER STATUS TO ALIVE
        this.isAlive = true;

        position = pos;
    }

    /// <summary>
    /// Handles input, performs physics, and animates the player sprite.
    /// </summary>
    /// <remarks>
    /// We pass in all of the input states so that our game is only polling the hardware
    /// once per frame. We also pass the game's orientation because when using the accelerometer,
    /// we need to reverse our motion when the orientation is in the LandscapeRight orientation.
    /// </remarks>
    public void Update(float deltaTime, OuyaController gamePadState)
    {
        // forwardAmount.add(ScreenManager.movement);
        GetInput(gamePadState);

        DoAttack(deltaTime);

        ApplyPhysics(deltaTime);

        bounds.lowerLeft.set(position).sub(bounds.width / 2, bounds.height / 2);

        if(isAlive)
        {
            if (isOnGround)
            {
                if(isAttacking)
                {
                    state = PLAYER_STATE_ATTACK;
                }
                else
                {
                    // IS THE PLAYER WALKING
                    if (Math.abs(velocity.x)- 0.02f  > 0)
                    {
                        state = PLAYER_STATE_RUN;
                    }
                    else
                    {
                        // THE PLAYER IS NOT MOVING
                        state = PLAYER_STATE_IDLE;
                    }
                }
            }

            //Reset our variables every frame
            movement = Vector2.Zero();

            // Clear input.
            isJumping = false;

            if (isOnGround)
                numberOfJumps = 0;
        }
    }


    /// <summary>
    /// Updates the player's velocity and position based on input, gravity, etc.
    /// </summary>
    public void ApplyPhysics(float deltaTime)
    {
        final float GravityAcceleration = 3400.0f;

        final float MaxMoveSpeed       = 1750.0f;
        final float GroundDragFactor   = 0.48f;
        final float AirDragFactor      = 0.58f;

        this.stateTime += deltaTime;

        //Vector2 previousPosition = position;
        velocity.y = Clamp.clamp( velocity.y + GravityAcceleration * deltaTime, -MaxFallSpeed, MaxFallSpeed);

        velocity.x += movement.x  * MoveAcceleration * deltaTime;

        velocity.y = DoJump(velocity.y, deltaTime);

        // POINT THE PLAYER IN THE RIGHT DIRECTION
        side = velocity.x > 0? -1: 1;

        // Apply pseudo-drag horizontally.
        if (isOnGround)
            velocity.x *= GroundDragFactor;
        else
            velocity.x *= AirDragFactor;

        // Prevent the player from running faster than his top speed.
        velocity.x = Clamp.clamp(velocity.x, -MaxMoveSpeed, MaxMoveSpeed);

        HandleCollisions();

        position.x += velocity.x * deltaTime;
        position.y -= velocity.y * deltaTime;

        if(position.y < 0.0f)
            OnKilled();//(true);
    }

    /// <summary>
    /// Gets player horizontal movement and jump commands from input.
    /// </summary>
    private void GetInput(OuyaController gamePadState)
    {
        // Get analog horizontal movement.
        //movement.x = gamePadState.getAxisValue(OuyaController.AXIS_LS_X )* 1.0f;

        float axisX = gamePadState.getAxisValue(OuyaController.AXIS_LS_X);
        float axisY = gamePadState.getAxisValue(OuyaController.AXIS_LS_Y);

        if (axisX * axisX + axisY * axisY < OuyaController.STICK_DEADZONE * OuyaController.STICK_DEADZONE)
        {
            axisX = axisY = 0.0f;
        }

        movement.x = axisX;

        // Ignore small movements to prevent running in place.
        //if (Math.abs(movement.x) < 0.5f)
            //movement.x = 0.0f;

        // If any digital horizontal movement input is found, override the analog movement.
        if (gamePadState.getButton(OuyaController.BUTTON_DPAD_LEFT))
        {
            movement.x = -1.0f;
        }
        else if (gamePadState.getButton(OuyaController.BUTTON_DPAD_RIGHT))
        {
            movement.x = 1.0f;
        }


        // Check if the player wants to jump.
        isJumping = gamePadState.getButton(OuyaController.BUTTON_O);

        if(gamePadState.getButton(OuyaController.BUTTON_U))
        {
            isAttacking = gamePadState.getButton(OuyaController.BUTTON_U);

            if (AttackTime != MaxAttackTime)
            {
                isAttacking = true;
                AttackTime = MaxAttackTime;
            }
        }
    }

    public void HandleCollisions()
    {
        //Rectangle tileBounds;
        // Reset flag to search for ground collision.
        isOnGround = false;

        // HAS THE PLAY REACHED THE SIDE OF THE SCREENS
        if((position.x < Tile.Width * 3)&&(side == 1))
        {
            velocity.x = 0;
            state = PLAYER_STATE_IDLE;
        }

        if((position.x > 1920 - Tile.Width * 3)&&(side == -1))
        {
            velocity.x = 0;
            state = PLAYER_STATE_IDLE;
        }

        float posX = position.x + bounds.width / 2 * side;
        float posY = position.y - PLAYER_HEIGHT /2 + 12.0f;

        int tileX  = (int)Math.floor(posX / Tile.Width);
        int tileY  = (int)Math.floor(posY / Tile.Height);

        if (!isJumping)
        {
            // HAS THE PLAYER LANDED ON A IMPASSABLE TILE
            if (this.level.GetCollision(tileX, tileY) == TileCollision.Impassable)
            {
                isOnGround = true;
                isJumping  = false;
                velocity.y = 0;
                numberOfJumps = 0;
            }
        }
    }

    private void DoAttack(float deltaTime)
    {
        // If the player wants to attack
        if (isAttacking)
        {
            // Begin or continue an attack
            if (AttackTime > 0.0f)
            {
                AttackTime -= deltaTime;
                state = PLAYER_STATE_ATTACK;
            }
            else
            {
                isAttacking = false;
            }
        }
        else
        {
            //Continues not attack or cancels an attack in progress
            AttackTime = 0.0f;
        }
    }

    /// <summary>
    /// Calculates the Y velocity accounting for jumping and
    /// animates accordingly.
    /// </summary>
    /// <remarks>
    /// During the accent of a jump, the Y velocity is completely
    /// overridden by a power curve. During the decent, gravity takes
    /// over. The jump velocity is controlled by the jumpTime field
    /// which measures time into the accent of the current jump.
    /// </remarks>
    /// <param name="velocityY">
    /// The player's current velocity along the Y axis.
    /// </param>
    /// <returns>
    /// A new Y velocity if beginning or continuing a jump.
    /// Otherwise, the existing Y velocity.
    /// </returns>
    private float DoJump(float velocityY, float deltaTime)
    {
        final float MaxJumpTime        =  1.0f;
        final float JumpLaunchVelocity = -3500.0f;
        final float JumpControlPower   =  0.14f;

        // DID THE USER PRESS THE JUMP BUTTON
        if (isJumping)
        {
            // Begin or continue a jump
            if ((!wasJumping && isOnGround)|| jumpTime > 0.0f)
            {
                if (jumpTime == 0.0f)
                    Assets.playSound(Assets.playerjump);

                jumpTime += deltaTime;
                this.state = PLAYER_STATE_JUMP;
            }

            // If we are in the ascent of the jump
            if (jumpTime > 0.0f && jumpTime <= MaxJumpTime)
            {
                // Fully override the vertical velocity with a power curve that
                //gives players more control over the top of the jump
                velocityY = JumpLaunchVelocity * (1.0f - (float)Math.pow(jumpTime / MaxJumpTime, JumpControlPower));
            }
            else
            {
                // Fully override the vertical velocity with a power curve that
                //gives players more control over the top of the jump
                // Reached the apex of the jump and has double jumps
                if (velocityY > -MaxFallSpeed * 0.5f && !wasJumping && numberOfJumps < 1)
                {
                    velocityY =  JumpLaunchVelocity * (0.5f - (float)Math.pow(jumpTime / MaxJumpTime, JumpControlPower));
                    jumpTime += deltaTime;
                    numberOfJumps++;
                }
                else
                {
                    jumpTime = 0.0f;
                }
            }
        }
        else
        {
            // Continues not jumping or cancels a jump in progress
            jumpTime = 0.0f;
            wasJumping = false;
            isJumping = false;
        }

        wasJumping = isJumping;

        return velocityY;

    }

    /// <summary>
    /// Called when the player has been killed.
    /// </summary>
    /// <param name="killedBy">
    /// The enemy who killed the player. This parameter is null if the player was
    /// not killed by an enemy (fell into a hole).
    /// </param>
    public void OnKilled()//(boolean bfalling)
    {
        if(isAlive)
        {
            lives--;

            isAlive = false;

           /* if (!bfalling)
                Assets.playSound(Assets.playerfall);
            else*/
                Assets.playSound(Assets.playerKilledSound);

            // SHOW PLAYER ANIMATION
            state = PLAYER_STATE_DIE;

            //ScreenManager.STATE = ScreenManager.GAME_READY;
        }
    }

    /// <summary>
    /// Called when this player reaches the ScreenManager.level's exit.
    /// </summary>
    public void OnReachedExit()
    {
        state = PLAYER_STATE_CELEBRATE;
    }

    /// <summary>
    /// Draws the animated player.
    /// </summary>
    public void Draw(SpriteBatcher batcher)
    {
        TextureRegion keyFrame;

        switch (state)
        {
            case PLAYER_STATE_JUMP:
                keyFrame = Assets.playerJump.getKeyFrame(stateTime, Animation.ANIMATION_NONLOOPING);
                break;
            case PLAYER_STATE_RUN:
                keyFrame = Assets.playerWalk.getKeyFrame(stateTime, Animation.ANIMATION_LOOPING);
                break;
            case PLAYER_STATE_DIE:
                keyFrame = Assets.playerKilled.getKeyFrame(stateTime, Animation.ANIMATION_NONLOOPING);
                break;
            case PLAYER_STATE_IDLE:
                keyFrame = Assets.playerIdle.getKeyFrame(stateTime, Animation.ANIMATION_LOOPING);
                break;
            case PLAYER_STATE_ATTACK:
                keyFrame = Assets.playerAttack.getKeyFrame(stateTime, Animation.ANIMATION_LOOPING);
                break;
            case PLAYER_STATE_CELEBRATE:
                keyFrame = Assets.playerCelebrate.getKeyFrame(stateTime, Animation.ANIMATION_LOOPING);
                break;
            default:
                keyFrame = Assets.playerIdle.getKeyFrame(stateTime, Animation.ANIMATION_LOOPING);
        }

        batcher.beginBatch(Assets.player);

           batcher.drawSprite(position.x, position.y, side * PLAYER_WIDTH, PLAYER_HEIGHT, keyFrame);

        batcher.endBatch();

    }
}
