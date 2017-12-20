package com.example.rock.todo;

import android.graphics.Color;

import java.util.HashMap;

/**
 * Created by rock on 2017. 12. 20..
 */

public class DrawerColor {
    private HashMap<String, String> colorMap;

    public DrawerColor() {

        colorMap = new HashMap<>();
        setColors();
    }

    private void setColors() {

        colorMap.put("0", "#000000");//검
        colorMap.put("1", "#ff0000");//빨
        colorMap.put("2", "#ffec3b");//노
        colorMap.put("3", "#4caf4f");//초
        colorMap.put("4", "#2296f3");//파
        colorMap.put("5", "#7d4bcc");//보
        colorMap.put("6", "#ffffff");//흰
    }


    public int getColor(int position) {
        System.out.println("pos : "+position);
        System.out.println("df: "+Color.parseColor(colorMap.get(position+"")));
        return Color.parseColor(colorMap.get(position+""));
    }
}
