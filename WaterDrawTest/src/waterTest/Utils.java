package waterTest;


import processing.core.PVector;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author geodo
 */
public class Utils {
    
    private static float px, py;
    
    
//    private static PVector p;
    
    public static float DistanceBetweenPVectors(PVector a, PVector b) {
        
        px = a.x - b.x;
        py = a.y - b.y;
        
        return (float) Math.sqrt(px * px + py * py);
        
    }
    
    
    //returns < 1 if in. No square root so hopefully faster
    public static float DistanceBetweenPVectorsInTest(PVector a, PVector b, float r) {
        
        px = a.x - b.x;
        py = a.y - b.y;
        
        return (float) ((px * px) + (py * py)) / (r * r);
        
    }
    
    
    
    
    
    public static PVector unitVectorFromTwoVectors(PVector a, PVector b) {
        
        px = a.x - b.x;
        py = a.y - b.y;
        
        PVector p = new PVector(px, py);
        
        p.normalize();
        
        return p;        
        
    }
    
}
