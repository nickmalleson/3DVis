package spacetime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import processing.core.*;
import peasy.*;
import de.fhpotsdam.unfolding.*;
import de.fhpotsdam.unfolding.data.MarkerFactory;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.*;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.*;
import de.fhpotsdam.unfolding.providers.*;
import java.util.Arrays;

public class Main extends PApplet {

    PImage backgroundMap;        // OpenStreetMap.
    
    // Corners of the data
    PVector tlCorner, brCorner;

    PeasyCam cam;

    List<PVector> screenCoords = new ArrayList<PVector>();
    List<Location> latloncoords = new ArrayList<Location>();
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
        
        cam = new PeasyCam(this,
                400,        // look at x
                400,        // look at y
                0,          // look at z
                400         // distance from centre
        ); 
        cam.setMinimumDistance(10);
        cam.setMaximumDistance(1000);
        
        /* *************Set up the map and read data. ***************
        Order is important: the map needs to be initialised
        first, so that I can compare between screen and map coordinates. Then data
        needs to be read (so we know starting position)
        Then map can be zoomed and panned.
        *****************************/
        
        map = new UnfoldingMap(
                this,
                1,        // x position (lon) (these coords don't matter, pan later)
                1,        // y position (lat)
                800f,                         // width
                800f,                         // height
                new Microsoft.RoadProvider() // (see providers here: http://unfoldingmaps.org/tutorials/mapprovider-and-tiles.html#)
                //new Google.GoogleMapProvider()
                //new Microsoft.AerialProvider()
        );

        // Read the GPS data
        try {
            readData();
        } catch (Exception ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
        // Now move the map. Note some confusion over x,y and lon,lat.
        Location startLocation = latloncoords.get(0);

        System.out.println("Sart location: "+startLocation.toString()+
                " - ("+startLocation.x+","+startLocation.y+")");
        
        // Need to pan here because the constructor takes screen coordinates, but want to give latlon coords
        map.zoomTo(15);
        map.panTo(startLocation);
        
        
        
        // Add markers
        try {
            MarkerFactory mf = new MarkerFactory();
            Marker m;
            for (Location p:latloncoords) {
                m = mf.createMarker(new PointFeature(p));
                map.addMarker(m);
            }
        } catch (Exception e) {
            System.err.println("Error creating markers: "+e.toString());
            
        }
        
        // Automatically zoom - don't want this because otherwise map zooms
        // when we try to zoom in and out of data
        //MapUtils.createDefaultEventDispatcher(this, map);
        
    }//setup

    @Override
    public void draw() {

        background(255);

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
        
        // Draw a scale bar (from https://code.google.com/p/unfolding/source/browse/trunk/unfolding/examples/de/fhpotsdam/unfolding/examples/MapWithBarScaleApp.java?r=140 )
        drawBarScale(20, map.mapDisplay.getHeight() - 20);


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
            latloncoords.add(new Location(lat,lon));
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
        for (Location geo:latloncoords) {
                    //screenCoords.add(geoToScreen(bngcoord));
//            screenCoords.add(geoToScreen(geo));
            System.out.println("GEO:" +geo);
            screenCoords.add(map.getScreenPosition(geo));
            System.out.println(geo+" -- " + map.getScreenPosition(geo));
            // Convert to BNG and add to lists:
//            xcors.add(Float.parseFloat(lineSplit[2]));
//            ycors.add(Float.parseFloat(lineSplit[3]));
        }



    }

//    PVector geoToScreen(PVector geo) {
//        return new PVector(
//                map(geo.x, tlCorner.x, brCorner.x, 0, width),
//                map(geo.y, tlCorner.y, brCorner.y, 0, height));
//    }

    public static void main(String[] args) {
        PApplet.main(new String[]{"spacetime.Main"});
    }
    
    
    
    
    
    
    // STUFF FOR A SCALE BAR, FROM: https://code.google.com/p/unfolding/source/browse/trunk/unfolding/examples/de/fhpotsdam/unfolding/examples/MapWithBarScaleApp.java?r=140
    
    
    
    private static final List<Float> DISPLAY_DISTANCES = Arrays.asList(0.01f, 0.02f, 0.05f, 0.1f, 0.2f, 0.5f, 1f, 2f,
                        5f, 10f, 20f, 50f, 100f, 200f, 500f, 1000f, 2000f, 5000f);
        private static final float MAX_DISPLAY_DISTANCE = 5000;

    
        /**
         * Draws a bar scale at given position according to current zoom level.
         *
         * Calculates distance at equator (scale is dependent on Latitude). Uses a distance to display
         * from fixed set of distance numbers, so length of bar may vary.
         *
         * @param x
         *            Position to display bar scale
         * @param y
         *            Position to display bar scale
         */
        public void drawBarScale(float x, float y) {

                // Distance in km, appropriate to current zoom
                float distance = MAX_DISPLAY_DISTANCE / map.getZoom();
                distance = getClosestDistance(distance);

                // Gets destLocation (world center, on equator, with calculated distance)
                Location startLocation = new Location(0, 0);
                Location destLocation = GeoUtils.getDestinationLocation(startLocation, 90f, distance);

                // Calculates distance between both locations in screen coordinates
                float[] destXY = map.getScreenPositionFromLocation(destLocation);
                float[] startXY = map.getScreenPositionFromLocation(startLocation);
                float dx = destXY[0] - startXY[0];

                // Display
                stroke(30);
                strokeWeight(1);
                line(x, y - 3, x, y + 3);
                line(x, y, x + dx, y);
                line(x + dx, y - 3, x + dx, y + 3);
                fill(30);
                text(nfs(distance, 0, 0) + " km", x + dx + 3, y + 4);
        }

        /**
         * Returns the nearest distance to display as well as to use for calculation.
         *
         * @param distance
         *            The original distance
         * @return A distance from the set of {@link DISPLAY_DISTANCES}
         */
        public float getClosestDistance(float distance) {
                return closest(distance, DISPLAY_DISTANCES);
        }

        public float closest(float of, List<Float> in) {
                float min = Float.MAX_VALUE;
                float closest = of;

                for (float v : in) {
                        final float diff = Math.abs(v - of);

                        if (diff < min) {
                                min = diff;
                                closest = v;
                        }
                }

                return closest;
        }

}
