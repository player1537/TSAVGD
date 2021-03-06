package event;

import java.util.ArrayList;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Andy
 */
public abstract class PhysicalEntity implements Entity {

    public BoundingBox b;
    public Vector3f velocity;
    public Vector3f acceleration;
    public float invMass;
    public Vector3f forceAccum;
    public ArrayList<ForceGenerator> fg;
    public float drag = 0.999f;
    public boolean isAwake;
    public float vAvg;
    public boolean collidable;

    public PhysicalEntity() {
        this(new BoundingBox());
    }

    public PhysicalEntity(BoundingBox b) {

        this.b = b;
        velocity = new Vector3f();
        acceleration = new Vector3f();
        forceAccum = new Vector3f();
        invMass = 1f / 10f;
        fg = new ArrayList<ForceGenerator>();
        isAwake = true;
        vAvg = 100;
        collidable = true;

    }

    public abstract void collide(ArrayList<Triangle> cols);

    public abstract void collide(PhysicalEntity col);

    public void integrate(float time) {
        Vector3f vCopy = (Vector3f) new Vector3f(velocity).scale(time);
        b.translate(vCopy);
        acceleration = (Vector3f) forceAccum.scale(invMass);
        Vector3f.add(velocity, (Vector3f) acceleration.scale(time), velocity);
        velocity.scale(drag);

    }

    public void setPosition(Vector3f p) {

        b.setPosition(p);

    }

    public void moveFromWorld(float x, float y, float z) {

        b.translate(x, y, z);

    }

    public Vector3f[] getVertexes() {

        return b.getVertexes();

    }

    public void setVelocity(Vector3f v) {
        this.velocity = v;
    }

    public void addForce(Vector3f f) {
        Vector3f.add(f, forceAccum, forceAccum);
    }

    public float getMass() {
        return 1f / invMass;
    }

    void updateForces() {
        for (ForceGenerator f : fg) {
            Vector3f.add(forceAccum, f.getForce(this), forceAccum);
        }
    }

    public Vector3f getMiddle() {
        return b.getCenter();
    }

    public void setAwake(boolean value) {
        isAwake = value;
    }

    public boolean canSleep() {
        vAvg += velocity.lengthSquared() * 0.7;
        vAvg /= 2.3;
        if (this instanceof Player) {
            DebugMessages.addMessage("VAVG", "" + vAvg);
        }
        return vAvg < 0.1;
    }

    public boolean isCollidable() {
        return collidable;
    }

    void setBounds(BoundingBox bounds) {
        this.b = bounds;
    }
}
