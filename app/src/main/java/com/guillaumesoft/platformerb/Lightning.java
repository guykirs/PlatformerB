package com.guillaumesoft.platformerb;

import com.badlogic.androidgames.framework.DynamicGameObject;
import com.badlogic.androidgames.framework.GameObject;
import com.badlogic.androidgames.framework.gl.SpriteBatcher;
import com.badlogic.androidgames.framework.math.Vector2;

/// <summary>
///  THIS CLASS DRAWS THE GEM TO THE SCREEN
///  THIS PROVIDE THE PLAY POINTS AND HEALTH
///  OCTOBER 23, 2014
///  GUILLAUME SWOLFS
///  GUILLAUMESOFT
/// </summary>
class Lightning extends DynamicGameObject
{
    //////////////////////////////////////////////
    // CLASS STATIC VARAIBLES
    public static float GEM_WIDTH  = 40.0f;
    public static float GEM_HEIGHT = 40.0f;

    //////////////////////////////////////////////
    // CLASS VARAIBLES
    public final int PointValue = 30;
    float stateTime;
    private float bounce;

    /// <summary>
    /// Constructs a new gem.
    /// </summary>
    public Lightning(float x, float y)
    {
        super(x, y, GEM_WIDTH, GEM_HEIGHT);
    }

    /// <summary>
    /// Called when this gem has been collected by a player and removed from the level.
    /// </summary>
    /// <param name="collectedBy">
    /// The player who collected this gem. Although currently not used, this parameter would be
    /// useful for creating special powerup gems. For example, a gem could make the player invincible.
    /// </param>
    public void OnCollected(Player collectedBy)
    {
        Assets.playSound(Assets.gemCollected);
    }

    public void Update(float deltaTime)
    {
        stateTime += deltaTime;

        final float BounceHeight = 0.30f;
        final float BounceRate   = 3.0f;
        final float BounceSync   = -0.75f;

        double t = stateTime * BounceRate + position.x * BounceSync;
        bounce = (float)Math.sin(t) * BounceHeight * GEM_HEIGHT;
    }

    /// <summary>
    /// Draws a gem in the appropriate color.
    /// </summary>
    public void Draw(SpriteBatcher batcher)
    {
        batcher.beginBatch(Assets.tile);

        batcher.drawSprite(position.x, position.y + bounce, GEM_WIDTH, GEM_HEIGHT, Assets.gemRegion);

        batcher.endBatch();
    }
}


