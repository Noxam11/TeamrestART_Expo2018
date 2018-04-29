package com.example.nutri_000.testinggauge;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Amon on 2/21/2018.
 */


//each movement recorded has its own variable set of everything listed below. can be accessed by movements.get(index).variable
class Movement {
    File path;
    String filename;
    String name;
    int counter;

    int percent=100;
    int reps=2;
    int levels=10;
    int error=10;
    int diff;

    //ArrayList<ArrayList<Float>> Hand = new ArrayList<ArrayList<Float>>(3);
    //ArrayList<ArrayList<Float>> LowerArm = new ArrayList<ArrayList<Float>>(3);
    //ArrayList<ArrayList<Float>> UpperArm = new ArrayList<ArrayList<Float>>(3);
    //ArrayList<ArrayList<Float>> Back = new ArrayList<ArrayList<Float>>(3);
    ArrayList<Float> Handx = new ArrayList();
    ArrayList<Float> Handy = new ArrayList();
    ArrayList<Float> Handz = new ArrayList();
    ArrayList<Float> LowerArmx = new ArrayList();
    ArrayList<Float> LowerArmy = new ArrayList();
    ArrayList<Float> LowerArmz = new ArrayList();
    ArrayList<Float> UpperArmx = new ArrayList();
    ArrayList<Float> UpperArmy = new ArrayList();
    ArrayList<Float> UpperArmz = new ArrayList();
    ArrayList<Float> Backx = new ArrayList();
    ArrayList<Float> Backy = new ArrayList();
    ArrayList<Float> Backz = new ArrayList();
    ArrayList<Long> HandTime = new ArrayList();
    ArrayList<Long> LowerArmTime = new ArrayList();
    ArrayList<Long> UpperArmTime = new ArrayList();
    ArrayList<Long> BackTime = new ArrayList();




}