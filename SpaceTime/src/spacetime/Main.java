package spacetime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gicentre.utils.spatial.OSGB;
import processing.core.*;
import peasy.*;
import de.fhpotsdam.unfolding.*;
import de.fhpotsdam.unfolding.data.MarkerFactory;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.*;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.*;
import de.fhpotsdam.unfolding.providers.*;

public class Main extends PApplet {

    PImage backgroundMap;        // OpenStreetMap.
//    PVector tlCorner, brCorner;   // Corners of map in WebMercator coordinates.
//    OSGB bng = new OSGB(); // For converting from lat/lon to bng
    
    // Corners of the data
    PVector tlCorner, brCorner;
    //PVector tlCorner = bng.transformCoords(new PVector(-1.646491f, 53.868911f));
    //PVector brCorner = bng.transformCoords(new PVector(-1.630948f, 53.843498f));
    
//    PVector centre; // Centre of the map
//    PVector screenCentre; // Centre converted to screen pixels
    PeasyCam cam;
//    List<Float> xcors = new ArrayList<Float>();
//    List<Float> ycors = new ArrayList<Float>();
    
    //List<PVector> bngcoords = new ArrayList<PVector>();
    List<PVector> screenCoords = new ArrayList<PVector>();
    List<PVector> latloncoords = new ArrayList<PVector>();
    List<Float> times = new ArrayList<Float>();
    
    UnfoldingMap map;

    @Override
    public void setup() {

        System.out.println("SETTING UP");
//        System.out.println("TLC:"+tlCorner.toString());
//        System.out.println("BRC:"+brCorner.toString());

        //The default mode (just size(800,800) is more accurate but slower. 
        //Both P2D and P3D use openGL by default
        size(800, 800, OPENGL);
        
        // Read the GPS data
        try {
            readData();
        } catch (Exception ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        }

        // FInd out where to look (centre of base map)
//        centre = new PVector(brCorner.x - tlCorner.x, tlCorner.y - brCorner.x);
//        screenCentre = geoToScreen(centre);
//        assert centre.x > 0 && centre.y > 0;

//        cam = new PeasyCam(this, screenCentre.x,screenCentre.y,0,1000);
        cam = new PeasyCam(this,
                400,        // look at x
                400,        // look at y
                0,          // look at z
                400         // distance from centre
        ); 
        cam.setMinimumDistance(10);
        cam.setMaximumDistance(1000);
        
        // Set up the map. Note some confusion over x,y and lon,lat.
        // PVectors have (x,y), but Locations have (y,x)
        Location startLocation = new Location(
                latloncoords.get(0).y,      // y position (lat)
                latloncoords.get(0).x);     // x position (lon)
        System.out.println("Sart location: "+startLocation.toString()+
                " - ("+startLocation.x+","+startLocation.y+")");
        
        map = new UnfoldingMap(
                this,
                startLocation.x,        // x position (lon)
                startLocation.y,        // y position (lat)
                800f,                         // width
                800f,                         // height
                new Microsoft.RoadProvider() // (see providers here: http://unfoldingmaps.org/tutorials/mapprovider-and-tiles.html#)
                //new Google.GoogleMapProvider()
                //new Microsoft.AerialProvider()
        );
        
        map.zoomAndPanTo(startLocation, 10);
        
        
        // Add markers
        try {
            MarkerFactory mf = new MarkerFactory();
            Marker m;
            for (PVector p:latloncoords) {
                m = mf.createMarker(new PointFeature(new Location(p.y,p.x)));
                map.addMarker(m);
            }
        } catch (Exception e) {
            System.err.println("Error creating markers: "+e.toString());
            
        }
        
        // Automatically zoom - don't want this because otherwise map zooms
        // when we try to zoom in and out of data
        //MapUtils.createDefaultEventDispatcher(this, map);
        
//        sphereDetail(5);



    }//setup

    @Override
    public void draw() {

        background(255);


        //fill(0);
        //text("" + frameRate, 10, 10);
        //noFill();

        // Draw the rectangle
//        pushMatrix();
////        translate (screenCentre.x, screenCentre.y);
//        translate(-1500, -1000);
//        fill(100, 100, 100);
//        noStroke();
//        rect(0, 0, 3000, 2000);
//        popMatrix();
        
        

        // start drawing the 3d track
        noFill();
        stroke(150,50,50,150);
        strokeWeight(6);
        beginShape();
        for (int i=0; i<screenCoords.size(); i++) {
            // (Note - divide z coordinate to shrink temporarily)
            vertex(screenCoords.get(i).x,screenCoords.get(i).y, times.get(i)/5);  
//            System.out.println(screenCoords.get(i).x+" - "+screenCoords.get(i).y);
        }
        endShape();
        
        
        map.draw();
        
//        for (int i=0; i<coords.size(); i++) {
//            pushMatrix();
//            translate(screenCoords.get(i).x, screenCoords.get(i).y, times.get(i));
//            sphere(1);
//            popMatrix();
//        }
        
        
//        sphere(100);
//        rotateX(-0.5f);
//        rotateY(-0.5f);
//        fill(255, 0, 0);
//        box(30);
//        pushMatrix();
//        translate(0, 0, 20);
//        fill(0, 0, 255);
//        box(5);
//        popMatrix();

    }

    public void readData() throws Exception {



        BufferedReader br = new BufferedReader(new FileReader(new File("data/csv/test.csv")));
        String line = br.readLine(); // Skip first line
        String[] lineSplit;
        int counter = 0;

        Float lat, lon, 
                minlat = Float.MAX_VALUE, minlon = Float.MAX_VALUE,
                maxlat = -Float.MAX_VALUE, maxlon = -Float.MAX_VALUE;
        
        // First pass - read data
        while ((line = br.readLine()) != null) {

//            System.out.println(line);
            lineSplit = line.split(",");
            // Read lat/lon coordinates
            lat = (Float.parseFloat(lineSplit[2])); // y, e.g. 53.8
            lon = (Float.parseFloat(lineSplit[3])); // x, e.g. -1.63
            PVector geo = new PVector(lon, lat);
            latloncoords.add(geo);
            //bngcoord = bng.transformCoords(geo);
            //bngcoords.add(bngcoord);
            times.add((float) counter++);
            
            if (lon < minlon) minlon = lon;
            if (lon > maxlon) maxlon = lon;
            if (lat < minlat) minlat = lat;
            if (lat > maxlat) maxlat = lat;

        }
        
        // Set top and bottom corners in the data
        
        brCorner = new PVector(maxlon,minlat);
        tlCorner = new PVector(minlon,maxlat);
        System.out.println("TL and BR corners: "+tlCorner.toString()+" , "+brCorner.toString());
        
        // Second pass - work out the screen coordinates (need min/max lat lon first)
        for (PVector geo:latloncoords) {
                    //screenCoords.add(geoToScreen(bngcoord));
            screenCoords.add(geoToScreen(geo));
            // Convert to BNG and add to lists:
//            xcors.add(Float.parseFloat(lineSplit[2]));
//            ycors.add(Float.parseFloat(lineSplit[3]));
        }



    }

    PVector geoToScreen(PVector geo) {
        return new PVector(
                map(geo.x, tlCorner.x, brCorner.x, 0, width),
                map(geo.y, tlCorner.y, brCorner.y, 0, height));
    }

    public static void main(String[] args) {
        PApplet.main(new String[]{"spacetime.Main"});
    }
}
