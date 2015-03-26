package com.guillaumesoft.platformerb;

import com.badlogic.androidgames.framework.DynamicGameObject;
import com.badlogic.androidgames.framework.gl.TextureRegion;
import com.badlogic.androidgames.framework.math.Vector2;

enum TileCollision
{
    Passable(0), Impassable(1), Platform(2), Ladder(3), Checkpoint(4);

    final int numTab;

    TileCollision(int num)
    {
        this.numTab = num;
    }

    public int getValue()
    {
        return this.numTab;
    }
}

/// <summary>
/// Stores the appearance and collision behavior of a tile.
/// </summary>
class Tile
{
    ////////////////////////////////////////
    // PUBLIC CLASS VARAIBLES
    public TextureRegion texture;
    public TileCollision Collision;

    public static final int Width  = 42;
    public static final int Height = 32;
    public static final Vector2 Size = new Vector2(Width, Height);
    public static final int Center = Width /2;

    /////////////////////////////////////////
    /// Constructs a new tile.
    /// </summary>
    public Tile(TextureRegion texture, TileCollision collision)
    {

        this.texture = texture;
        Collision    = collision;
    }
}
