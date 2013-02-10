/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package event;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import org.lwjgl.*;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.opengl.GL11.*;
import character.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.UnicodeFont;
import sound.Sound;
import sound.SoundManager;

/**
 *
 * @author Andy
 */
public class EventTest {

    static float width;
    static float height;
    static long lastFrame = 0;
    static long lastFrameBottleNeck = 0;
    static Hud h;
    public static Player p;
    static Terrain ter;
    static ArrayList<Entity> e;
    static ArrayList<DisplayableEntity> de;
    static ArrayList<PhysicalEntity> pe;
    static PhysicalWorld w;
    static boolean paused = false;

    private static boolean pauseDebounce;
    private static boolean debugDebounce;
    private static boolean muteDebounce;
    private static boolean respawnDebounce;
    private static boolean activateDebounce;
    private static boolean activate;
    private static boolean inConversation;
    private static float dx, dy;


    public static void main(String[] argv) {
        if (argv == null || argv.length != 2) {
            create(1024, 768);
        } else {
            int width = Integer.parseInt(argv[0]);
            int height = Integer.parseInt(argv[1]);
            create(width, height);
        }
    }

    public static void create(float width, float height) {

        EventTest.width = width;
        EventTest.height = height;
        init();
        run();
        destroy();
    }

    private static void init() {

        try {

            //Display
            Display.setDisplayMode(new DisplayMode((int) width, (int) height));
            Display.setFullscreen(true);
            Display.setVSyncEnabled(true);
            Display.setTitle("Engine");
            Display.create();

	    // Keyboard
	    Keyboard.create();
	    //Mouse
            Mouse.setGrabbed(true);
            Mouse.create();

            //OpenAL
            SoundManager.create();
            Sound loading = SoundManager.createSound("res/jazz.wav");
            loading.repeat();
            //loading.play();

            //OpenGL
            glViewport(0, 0, (int) width, (int) height);

            glClearColor(0f, 0f, 0f, 1f);

            glEnable(GL_TEXTURE_2D);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


            glMatrixMode(GL_PROJECTION);
            glOrtho(0, width, height, 0, 1, -1);
            glMatrixMode(GL_MODELVIEW);
            (new HudGraphic(null, "LOADING", HudGraphic.loadFont("Arial", 48, java.awt.Color.white), 300, 100)).draw();
            Display.update();


            glEnable(GL_DEPTH_TEST);


            glEnable(GL_CULL_FACE);

            glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST);

            glShadeModel(GL_SMOOTH);

            glDisable(GL_DITHER);

            glEnable(GL_LIGHTING);
            glEnable(GL_LIGHT0);
            glLightModel(GL_LIGHT_MODEL_AMBIENT, asFloatBuffer(new float[]{0.2f, 0.2f, 0.15f, 1f}));
            glLight(GL_LIGHT0, GL_DIFFUSE, asFloatBuffer(new float[]{.35f, .35f, .35f, 1}));
            glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(new float[]{0f, 200f, -300f, 1}));
            glEnable(GL_LIGHT1);
            glLight(GL_LIGHT1, GL_DIFFUSE, asFloatBuffer(new float[]{.7f, .7f, .7f, 1}));
            glLight(GL_LIGHT1, GL_POSITION, asFloatBuffer(new float[]{0f, 20f, 0f, 1}));
            glEnable(GL_COLOR_MATERIAL);
            glColorMaterial(GL_FRONT, GL_DIFFUSE);
            //float specReflection[] = {0.3f, 0.3f, 0.3f, 1f};
            //glMaterial(GL_FRONT, GL_SPECULAR, asFloatBuffer(specReflection));
            //glMateriali(GL_FRONT, GL_SHININESS, 30);

            glFogi(GL_FOG_MODE, GL_LINEAR);        // Fog Mode
            glFog(GL_FOG_COLOR, asFloatBuffer(new float[]{0.45f, 0.5f, 0.6f, 1f}));            // Set Fog Color
            glFogf(GL_FOG_DENSITY, 0.35f);              // How Dense Will The Fog Be
            glHint(GL_FOG_HINT, GL_DONT_CARE);          // Fog Hint Value
            glFogf(GL_FOG_START, 100f);             // Fog Start Depth
            glFogf(GL_FOG_END, 200f);               // Fog End Depth
            glEnable(GL_FOG);                   // Enables GL_FOG

            //Misc

            e = new ArrayList<Entity>();
            de = new ArrayList<DisplayableEntity>();
            pe = new ArrayList<PhysicalEntity>();


            p = new Player();
            p.init();
            p.setPosition(new Vector3f(0, 10, 0));
            p.fg.add(new ForceGenerator() {

		    @Override
			public Vector3f getForce(PhysicalEntity e) {
			return new Vector3f(0, -e.getMass() * 20, 0);
		    }
		});
            System.out.println("Start Terrain");
            ter = new Terrain(p);
            System.out.println("DONE");
            SkyDome sky = new SkyDome();
            //Vector3f waterPosition = ter.planes.boundary.getMin();
            //waterPosition.setY(-5);
            //Water water = new Water(waterPosition, ter.planes.boundary.getWidth(), ter.planes.boundary.getDepth(), 10, 200, 200);
            //Vector3f waterPosition = new Vector3f(-50, 3, -50);
            //Water water = new Water(waterPosition, 50, 50, 2, 100, 100);
            h = new Hud(width, height);

            e.add(sky);
            e.add(ter);
            e.add(p);
            //e.add(water);
            e.add(h);

            de.add(sky);
            de.add(ter);
            //de.add(water);
            de.add(h);

            pe.add(p);

	    /*
	    {
		int i = 0;
		for (; i < 1; i++) {
		    Person per = new Person();
		    per.b.setPosition(new Vector3f(0, 10*(i+1), 0));
		    per.fg.add(new ForceGenerator() {
			    
			    @Override
				public Vector3f getForce(PhysicalEntity e) {
				return new Vector3f(0, -e.getMass() * 20, 0);
			    }
			});
		    e.add(per);
		    de.add(per);
		    pe.add(per);
		}
	    }
	    */

            w = new PhysicalWorld(ter, pe);

            //Timer
            getDelta();
            getDeltaBottleNeck();

            DebugMessages.setShow(true);
            loading.stop();
	    updateThread = new UpdateThread();
	    updateThread.start();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private static void run() {

        Sound music = SoundManager.createSound("res/looping.wav");
        music.repeat();
        music.setVolume(0.4f);
        music.play();
        while (!Display.isCloseRequested() && KeyboardWrapper.get(Keyboard.KEY_ESCAPE).isUp()) {
	    
		getDeltaBottleNeck();

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


		processInputs();
		//p.update(delta);
		//System.out.println("Total " + delta);
            int delta = getDelta();

            DebugMessages.addMessage("FPS", "" + 1000f / delta);
            DebugMessages.addMessage("Conv", "" + inConversation);
            DebugMessages.addMessage("Acti", "" + activate);
            //System.out.println("Total " + delta);

		//processInputs();
		/*
		  for (int i = 0; i < 3; i++) {
		  update(delta / 3);
		  }
		*/

		render();
		SoundManager.update(delta);
		Display.update();
		Display.sync(30);



	}
    }

    private static void destroy() {
	isRunning = false;
	try { updateThread.join(); } catch (Exception e) { }
	Display.destroy();
	Keyboard.destroy();
	Mouse.destroy();
	SoundManager.destroy();
	System.exit(0);


    }

    public static boolean isRunning() {
	return isRunning;
    }

    public static long getTime() {

	return (Sys.getTime() * 1000) / Sys.getTimerResolution();

    }

    private static int getDelta() {

	long currentTime = getTime();
	int delta = (int) (currentTime - lastFrame);
	lastFrame = getTime();
	return delta;

    }

    public static int getDeltaBottleNeck() {

	long currentTime = getTime();
	int delta = (int) (currentTime - lastFrameBottleNeck);
	lastFrameBottleNeck = getTime();
	return delta;

    }

    public static void processInputs() {

	processKeyboard();
	processMouse();
	hasProcessedInputs = true;

    }

    private static void processKeyboard() {
	if (KeyboardWrapper.put(Keyboard.KEY_P).isPressed()) {
	    paused = !paused;
	    h.setPause(paused);
	    Mouse.setGrabbed(!paused);
	}
        
	if (KeyboardWrapper.put(Keyboard.KEY_F).isPressed()) {
	    DebugMessages.setShow(!DebugMessages.getShow());
	}
        
	if (KeyboardWrapper.put(Keyboard.KEY_M).isPressed()) {
	    SoundManager.mute(!SoundManager.isMuted());
	}

	KeyboardWrapper.put(Keyboard.KEY_W);
	KeyboardWrapper.put(Keyboard.KEY_A);
	KeyboardWrapper.put(Keyboard.KEY_S);
	KeyboardWrapper.put(Keyboard.KEY_D);
	KeyboardWrapper.put(Keyboard.KEY_UP);
	KeyboardWrapper.put(Keyboard.KEY_DOWN);
	KeyboardWrapper.put(Keyboard.KEY_LEFT);
	KeyboardWrapper.put(Keyboard.KEY_RIGHT);
	KeyboardWrapper.put(Keyboard.KEY_SPACE);
	KeyboardWrapper.put(Keyboard.KEY_LSHIFT);
	KeyboardWrapper.put(Keyboard.KEY_ESCAPE);

        if (KeyboardWrapper.put(Keyboard.KEY_R).isPressed()) {
            p.b.setPosition(new Vector3f(0, 20, 0));
            MessageCenter.addMessage("RESPAWNED " + (int) (Math.random() * 100));
        }

	if (KeyboardWrapper.put(Keyboard.KEY_E).isPressed()) {
            activate = true;
        } else {
            activate = false;
        }
    }

    private static void processMouse() {
	dx = Mouse.getDX();
	dy = Mouse.getDY();
    }

    public static void update(int delta) {

        setConversation(!ConversationDisplay.isFinished());
        if (inConversation && isActivate()) {
            ConversationDisplay.advance();
        }

        if (paused) {
            h.update(delta);
        } else {
            for (Entity ee : e) {
                ee.update(delta);
            }
            w.update(delta);
        }

    }

    private static void render() {

	//renderCode
	p.adjust();
	for (DisplayableEntity ent : de) {
	    ent.draw();
	}
    }

    public static FloatBuffer asFloatBuffer(float... values) {

	FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
	buffer.put(values);
	buffer.flip();
	return buffer;

    }

    public static float getDx() {
	return dx;
    }

    public static float getDy() {
	return dy;
    }

    public static void addEntity(Entity ent) {
	e.add(ent);
    }

    public static void addDisplayableEntity(DisplayableEntity dEnt) {
	de.add(dEnt);
    }

    public static void addPhysicalEntity(PhysicalEntity pEnt) {
	pe.add(pEnt);
    }

    public static void addAbstractEntity(AbstractEntity aEnt) {
        addEntity(aEnt);
        addDisplayableEntity(aEnt);
        addPhysicalEntity(aEnt);
    }

    public static boolean isActivate() {
        if (activate) {
            activate = false;
            return true;
        }
        return false;
    }

    public static void setConversation(boolean set) {
        inConversation = set;
        Hud.setShowConversation(set);
    }

    public static void toggleConversation() {
        setConversation(!inConversation);
    }
}