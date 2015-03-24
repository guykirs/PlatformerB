package com.guillaumesoft.platformerb;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.gl.Camera2D;
import com.badlogic.androidgames.framework.gl.SpriteBatcher;
import com.badlogic.androidgames.framework.impl.GLScreen;
import java.io.IOException;
import javax.microedition.khronos.opengles.GL10;
import tv.ouya.console.api.OuyaController;

/// <summary>
/// This is the main type for your game
/// </summary>
public class GameScreen extends GLScreen
{
    // Global content.
    // Meta-level game state.
    private int levelIndex = -1;
    private Level level;
    private boolean wasContinuePressed;

    // We store our input states so that we only poll once per frame,
    // then we use the same input state wherever needed
    // private OuyaController gamePadState;
    private SpriteBatcher batcher;
    private Camera2D      guiCam;

    // The number of levels in the Levels directory of our content. We assume that
    // levels in our content are 0-based and that all numbers under this constant
    // have a level file present. This allows us to not need to check for the file
    // or handle exceptions, both of which can add unnecessary time to level loading.
    //private final int numberOfLevels = 3;

    OuyaController gamePadState;

    // When the time remaining is less than the warning time, it blinks on the hud
    //private static TimeSpan WarningTime = TimeSpan.FromSeconds(30);

    public GameScreen(Game game)
    {
        super(game);

        // SETUP  THE CAMERA AND GRAPHICS
        this.batcher = new SpriteBatcher(glGraphics, 1000);
        guiCam       = new Camera2D(glGraphics, 1920, 1080);

        LoadNextLevel();
    }

    /// <summary>
    /// Allows the game to run logic such as updating the world,
    /// checking for collisions, gathering input, and playing audio.
    /// </summary>
    /// <param name="gameTime">Provides a snapshot of timing values.</param>
    @Override
    public void update(float gameTime)
    {
        // Handle polling for our input and handling high-level input
        HandleInput();

        // update our level, passing down the GameTime along with all of our input states
        level.update(gameTime, gamePadState);
    }

    private void HandleInput()
    {
        // get all of our input states
        gamePadState = OuyaController.getControllerByPlayer(0);

        boolean continuePressed = gamePadState.getButton(OuyaController.BUTTON_O);

        // Perform the appropriate action to advance the game and
        // to get the player back to playing.
        if (!wasContinuePressed && continuePressed)
        {
            if (!level.player.isAlive)
            {
                level.StartNewLife();
            }
            else if (level.TimeRemaing == 0.0f)
            {
                if (level.reachedExit)
                    LoadNextLevel();
                else
                    ReloadCurrentLevel();
            }
        }

        wasContinuePressed = continuePressed;
    }

    private void LoadNextLevel()
    {
        final int numberOfLevels = 8;

        // move to the next level
        levelIndex = (levelIndex + 1) % numberOfLevels;

        // Unloads the content for the current level before loading the next one.
        if (level != null)
            level.dispose();

        String fileName = Integer.toString(levelIndex) + ".txt";

        try
        {
            level = new Level(game.getFileIO().readAsset(fileName), levelIndex, this.batcher);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void ReloadCurrentLevel()
    {
        --levelIndex;
        LoadNextLevel();
    }

    /// <summary>
    /// Draws the game from background to foreground.
    /// </summary>
    /// <param name="gameTime">Provides a snapshot of timing values.</param>
    @Override
    public void present(float deltaTime)
    {
        GL10 gl = glGraphics.getGL();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        guiCam.setViewportAndMatrices();

        gl.glEnable(GL10.GL_TEXTURE_2D);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

           // GAME DRAWING
           level.Draw();//(deltaTime, batcher);
           drawHud();

        gl.glDisable(GL10.GL_BLEND);
    }

    public void drawHud()
    {
        String times = String.format("%.2f", level.TimeRemaing / 60);

        batcher.beginBatch(Assets.RedFont);

           Assets.redfont.drawText(batcher, "Time:" + times, 600, 1080 - 100, 20.0f, 20.0f);
           Assets.redfont.drawText(batcher, "Score:" + Integer.toString(level.score),  1100,  1080 - 100, 20.0f, 20.0f);

        batcher.endBatch();

        if(level.TimeRemaing == 0.0)
        {
            if (level.reachedExit)
            {
                batcher.beginBatch(Assets.messages);

                   batcher.drawSprite(640 /2, 480 /2, 300, 150, Assets.winRegion);

                batcher.endBatch();
            }
            else
            {
                batcher.beginBatch(Assets.messages);

                   batcher.drawSprite(640 /2, 480 /2, 300, 150, Assets.loseRegion);

                batcher.endBatch();
            }
        }
    }

    @Override
    public void pause() {   }

    @Override
    public void resume() {  }

    @Override
    public void dispose()  {  }
}

