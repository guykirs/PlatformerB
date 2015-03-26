package com.guillaumesoft.platformerb;

import com.badlogic.androidgames.framework.GameObject;
import com.badlogic.androidgames.framework.gl.Animation;
import com.badlogic.androidgames.framework.gl.SpriteBatcher;
import com.badlogic.androidgames.framework.gl.TextureRegion;

/// <summary>
///  THIS CLASS DRAWS THE GEM TO THE SCREEN
///  THIS PROVIDE THE PLAY POINTS AND HEALTH
///  OCTOBER 23, 2014
///  GUILLAUME SWOLFS
///  GUILLAUMESOFT
/// </summary>
class Torch extends GameObject
{
    //////////////////////////////////////////////
    // CLASS STATIC VARAIBLES
    public static float TORCH_WIDTH  = 30.0f;
    public static float TORCH_HEIGHT = 30.0f;

    //////////////////////////////////////////////
    // CLASS VARAIBLES
    float stateTime;

    /// <summary>
    /// Constructs a new gem.
    /// </summary>
    public Torch(float x, float y)
    {
        super(x, y, TORCH_WIDTH, TORCH_HEIGHT);
    }

    public void Update(float deltaTime)
    {
        stateTime += deltaTime;
    }

    /// <summary>
    /// Draws a gem in the appropriate color.
    /// </summary>
    public void Draw(SpriteBatcher batcher)
    {
       /* TextureRegion keyFrame;
        keyFrame = Assets.torch.getKeyFrame(stateTime, Animation.ANIMATION_LOOPING);
        batcher.beginBatch(Assets.items);
           batcher.drawSprite(position.x, position.y, TORCH_WIDTH, TORCH_HEIGHT, keyFrame);
        batcher.endBatch();*/
    }
}
