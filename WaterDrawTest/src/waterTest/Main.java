package waterTest;

/**
 * Testing 2D water flow model based on:
 *
 * Clavet, S., Beaudoin, P., Poulin, P., 2005. Particle-based viscoelastic fluid
 * simulation, in: Proceedings of the 2005 ACM SIGGRAPH/Eurographics Symposium
 * on Computer Animation, SCA ’05. ACM, New York, NY, USA, pp. 219–228.
 *
 *
 * @author geodo
 */
import processing.core.*;

public class Main extends PApplet {

//    PShape square;  // The PShape object
    PImage img;

    int numParticles = 100000;
    Particle[] particles = new Particle[numParticles];
    //use column 0 and 1 for x and y record
    float[][] previousPositions = new float[numParticles][2];

    //test. 0 and 1 for x and y
    float[][] testParticlePosition = new float[numParticles][2];

    PVector gravity;
    PVector v, rUnitVector;

    float interactionRadius = 10;//only interact with particles within this
    //viscosity vars
    float q;//distance of particle, if within radius, normalised to 1.
    float u;//radical velocity cutoff thingyo
    float imp;//impulse
    PVector impVector;//Vector after impulse applied
    float sigma = 0.8f;//linear control
    float beta = 0.0005f;//nonlinear control

    //test
    PFont font;

    @Override
    public void setup() {

        //The default mode (just size(800,800) is more accurate but slower. 
        //Both P2D and P3D use openGL by default
        size(600, 600);

        //set gravity
        //Sticking to screen directions. Positive y numbers are down!
        gravity = new PVector(0, 0.1f);

        //create particles
        for (int i = 0; i < particles.length; i++) {
//            particles[i] = new Particle(new PVector((float) Math.random() * width, 0), new PVector(0, 0));
//            particles[i] = new Particle(new PVector((float) Math.random() * (width / 4), 0), new PVector(0, 0));
            particles[i] = new Particle(new PVector((float) Math.random() * width, (float) Math.random() * height), new PVector(0, 0));

            testParticlePosition[i][0] = (float) Math.random() * width;
            testParticlePosition[i][1] = (float) Math.random() * height;

        }

        noSmooth();

        font = createFont("Arial Bold", 13, false);
//        textAlign(LEFT, BASELINE);
        textFont(font, 13);

        noFill();
        
//        img = loadImage("DoubleCircle.png");
        img = loadImage("1382496437_circle.png");

    }//setup

    @Override
    public void draw() {

        background(255);

        //Breaking down the paper's algorithm into discrete methods
        //Slight problem if I'm trying to link with traer physics - 
        //we'll see if order of forces matters
        //I'm also assuming for now that I'm not altering the timestep.
        //That might change
        //Using an integer for to be able to easily exclude `me'
        //from neighbour checks, with some vague notion it's quicker than doing
        //a class check
        //for (Particle p : particles) {
        for (int i = 0; i < particles.length; i++) {

////            particles[i].position.y += (float) Math.random() * 3;
            particles[i].position.y += 2;
           
            if (particles[i].position.y > height || particles[i].position.y < 0) {

                particles[i].position.x = (float) Math.random() * width;
                particles[i].position.y = (float) Math.random() * height;
                particles[i].velocity.x = 0;
                particles[i].velocity.y = 0;

            }
//            ellipse(particles[i].position.x, particles[i].position.y, 3, 3);
                        tint(255,126);
            image(img, particles[i].position.x, particles[i].position.y);

            
            
//            testParticlePosition[i][1] += (float) Math.random() * 3;
            
//            testParticlePosition[i][1] += 2;
//
//            if (testParticlePosition[i][1] > height || testParticlePosition[i][1] < 0) {
//
//                testParticlePosition[i][0] = (float) Math.random() * width;
//                testParticlePosition[i][1] = (float) Math.random() * height;
////                testParticlePosition[i].velocity.x = 0;
////                testParticlePosition[i].velocity.y = 0;
//
//            }
//
////            point(testParticlePosition[i][0], testParticlePosition[i][1]);
////            ellipse(testParticlePosition[i][0], testParticlePosition[i][1], 3, 3);
////            shape(square, testParticlePosition[i][0], testParticlePosition[i][1]);
//            
//            tint(255,126);
//            image(img, testParticlePosition[i][0], testParticlePosition[i][1]);

        }//for int i

//        drawPositions();
        fill(0);
        text("" + frameRate, 10, 10);
        noFill();

    }

    private void drawPositions() {

        //draw positions
        for (Particle p : particles) {

            ellipse(p.position.x, p.position.y, 3, 3);

        }

    }

    //i is ref of subject particle 
    private void applyViscosity(int i) {

        //apply viscosity.
        //1. Check if neighbour is within interaction radius
        for (int j = 0; j < 1; j++) {
//            for (int j = 0; j < particles.length; j++) {

            //note: article says i < j. I may need to enforce, but let's press on for now.
            //rule out `me'
            if (i != j) {

                //distance of particle, if within radius, normalised to 1.
//                    q = Utils.DistanceBetweenPVectors(particles[i].position,
//                            particles[j].position) / interactionRadius;
                //test no-sqrt in circle method
                q = Utils.DistanceBetweenPVectorsInTest(particles[i].position,
                        particles[j].position, interactionRadius);

                //if particle is within radius
                if (q < 1) {

                    //do inward velocity test
                    v = PVector.sub(particles[i].velocity, particles[j].velocity);

//                        if(v.x > 0) {
//                            System.out.println("ping?");
//                        }
                    //rUnitVector, where r is the (possibly absolute) distance between i and j
                    //found via vector subtraction
                    rUnitVector = v.get();
                    rUnitVector.normalize();

//                        System.out.println("v: " + v.y + "rUnitV: " + rUnitVector.y);
                    u = rUnitVector.dot(v);

                    if (u > 0) {

                        //response of each particle along their current axis of movement
                        imp = (1 - q) * ((sigma * u) + (beta * u * u));

                        //use to change size of the unit vector representing the relative
                        //velocities of the two particles
                        impVector = rUnitVector.get();
                        impVector.mult(imp);

                        impVector.div(2);

                        //Apply equal/opposite force to pair of particles
                        particles[j].addForce(impVector);
//                                    velocity.sub(impVector);
                        impVector.x = -impVector.x;
                        impVector.y = -impVector.y;

                        particles[i].addForce(impVector);

                    }//if u

                }//if distance 

            }//if i!=j

        }//for int j

    }

    public static void main(String[] args) {
        PApplet.main(new String[]{"waterTest.Main"});
    }
}
