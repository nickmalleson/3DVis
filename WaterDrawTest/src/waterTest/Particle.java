/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package waterTest;

import processing.core.PVector;

/**
 * Working from http://natureofcode.com/book/chapter-2-forces/
 *
 * @author geodo
 */
public class Particle {

    PVector position;
    PVector velocity;
    PVector acceleration;
    //Mass not included in the paper

    public Particle(PVector position, PVector velocity) {

        this.position = position;
        this.velocity = velocity;

        acceleration = new PVector(0, 0);

    }

    public void addForce(PVector f) {

        acceleration.add(f);

    }

    public void applyForces() {

        velocity.add(acceleration);
        position.add(velocity);
        //reset acceleration to zero for next force application
        acceleration.mult(0);

    }

}
