package com.guillaumesoft.platformerb;

import com.badlogic.androidgames.framework.DynamicGameObject;
import com.badlogic.androidgames.framework.gl.Animation;
import com.badlogic.androidgames.framework.gl.SpriteBatcher;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.math.Vector2;
import tv.ouya.console.api.OuyaController;

class Fire extends DynamicGameObject
{
    ////////////////////////////////////////////////
    // CLASS FINAL VARAIBLES
    ////////////////////////////////////////////////

    private static final int FIRE_WIDTH   = 90;
    private static final int FIRE_HEIGHT  = 100;

    private Level level;
    private float stateTime;

    /////////////////////////////////////////////////
    // PUBLIC CLASS VARAIBLES

    /// <summary>
    /// Constructors a new player.
    /// </summary>
    public Fire(float x, float y)
    {
        super(x, y, FIRE_WIDTH, FIRE_HEIGHT);

        // PASS THE LEVEL INSTANCE TO THIS CLASS
        this.level = level;
    }


    /// <summary>
    /// Handles input, performs physics, and animates the player sprite.
    /// </summary>
    /// <remarks>
    /// We pass in all of the input states so that our game is only polling the hardware
    /// once per frame. We also pass the game's orientation because when using the accelerometer,
    /// we need to reverse our motion when the orientation is in the LandscapeRight orientation.
    /// </remarks>
    public void Update(float deltaTime)
    {
        stateTime += deltaTime;

    }

    /// <summary>
    /// Draws the animated player.
    /// </summary>
    public void Draw(SpriteBatcher batcher)
    {
        TextureRegion keyFrame;

        keyFrame = Assets.fire.getKeyFrame(stateTime, Animation.ANIMATION_LOOPING);


        batcher.beginBatch(Assets.objectsAnimation);

            batcher.drawSprite(position.x, position.y, FIRE_WIDTH, FIRE_HEIGHT, keyFrame);

        batcher.endBatch();

    }
}
