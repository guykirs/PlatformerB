package com.guillaumesoft.platformerb;

import com.badlogic.androidgames.framework.FileIO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Settings
{
    public static boolean soundEnabled = true;
    public static boolean musicEnabled = true;

    public final static int[] highscores = new int[] { 100, 80, 50, 30, 10 };
    public final static String file = ".superjumper";

    public static void load(FileIO files)
    {
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(files.readFile(file)));
            soundEnabled = Boolean.parseBoolean(in.readLine());
            musicEnabled = Boolean.parseBoolean(in.readLine());

            for(int i = 0; i < 5; i++)
            {
                highscores[i] = Integer.parseInt(in.readLine());
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NumberFormatException e)
        {
            // :/ It's ok, defaults save our day
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (in != null)
                    in.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void save(FileIO files)
    {
        BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(new OutputStreamWriter(files.writeFile(file)));

            out.write(Boolean.toString(soundEnabled));
            out.write("\n");
            out.write(Boolean.toString(musicEnabled));
            out.write("\n");

            for(int i = 0; i < 5; i++)
            {
                out.write(Integer.toString(highscores[i]));
                out.write("\n");
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (out != null)
                    out.close();
            }
            catch (IOException e)
            {
            }
        }
    }

    public static void addScore(int score)
    {
        for(int i=0; i < 5; i++)
        {
            if(highscores[i] < score)
            {
                for(int j= 4; j > i; j--)
                    highscores[j] = highscores[j-1];
                highscores[i] = score;
                break;
            }
        }
    }
}

