package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Box;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication {
    
    private Geometry enemy;
    private Node playerNode;
    private Geometry playerGeom;
    private float moveSpeed = 10f;
    private float verticalAngle = 0;
    private final float maxVerticalAngle = FastMath.HALF_PI - 0.1f;
    private float mouseDeltaX = 0;
    private float mouseDeltaY = 0;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        inputManager.setCursorVisible(false);
        flyCam.setEnabled(false);
        
        //Jugador
        playerNode = new Node("Player");

        Box playerBox = new Box(0.5f, 1f, 0.5f);
        playerGeom = new Geometry("PlayerGeom", playerBox);

        Material playerMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        playerMat.setColor("Color", ColorRGBA.Green);
        playerGeom.setMaterial(playerMat);

        playerNode.attachChild(playerGeom);
        playerNode.setLocalTranslation(0, 1f, 0); // Leve altura para que no esté clavado en el suelo

        rootNode.attachChild(playerNode);

        // Cámara detrás del jugador
        cam.setLocation(new Vector3f(0, 5, 10));
        cam.lookAt(playerNode.getLocalTranslation(), Vector3f.UNIT_Y);

        // Activamos el input
        initKeys();
        
        Box b = new Box(100, 0.2f, 100);
        Geometry geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Gray);
        geom.setMaterial(mat);
        geom.move(0, -10, 0);
        
        Box segundoPiso = new Box(0.5f, 1f, 0.5f);
        Geometry tower = new Geometry("Tower", segundoPiso);

        Material towerMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        towerMat.setColor("Color", ColorRGBA.Blue);
        tower.setMaterial(towerMat);
        tower.setLocalTranslation(0, 0, 0); // Centro del mapa

        rootNode.attachChild(tower);
        
        Box enemyBox = new Box(0.3f, 0.3f, 0.3f);
        Geometry enemy = new Geometry("Enemy", enemyBox);

        Material enemyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        enemyMat.setColor("Color", ColorRGBA.Red);
        enemy.setMaterial(enemyMat);
        enemy.setLocalTranslation(-10, 0.3f, 0); // Punto de partida

        rootNode.attachChild(enemy);
        
        this.enemy = enemy;

        rootNode.attachChild(geom);
        
        //reticula
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        BitmapText crosshair = new BitmapText(font, false);
        crosshair.setSize(font.getCharSet().getRenderedSize() * 2);
        crosshair.setText("+");
        crosshair.setColor(ColorRGBA.White);

        // Centrar el texto en pantalla
        float x = settings.getWidth() / 2f - crosshair.getLineWidth() / 2f;
        float y = settings.getHeight() / 2f + crosshair.getLineHeight() / 2f;
        crosshair.setLocalTranslation(x, y, 0);

        guiNode.attachChild(crosshair);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (enemy != null) {
            enemy.move(tpf * 2, 0, 0); // Se mueve en X a 2 unidades por segundo
        }

        float sensitivity = 0.0025f;

        // Rotación horizontal del personaje
        playerNode.rotate(0, -mouseDeltaX * sensitivity, 0);

        // Rotación vertical de la cámara
        verticalAngle += mouseDeltaY * sensitivity;
        verticalAngle = FastMath.clamp(verticalAngle, -FastMath.HALF_PI + 0.1f, FastMath.HALF_PI - 0.1f);

        // Calcula la posición de la cámara detrás del jugador
        Vector3f baseOffset = new Vector3f(0, 3, 6);
        Vector3f rotatedOffset = playerNode.getLocalRotation().mult(baseOffset);
        Quaternion verticalRot = new Quaternion().fromAngleAxis(verticalAngle, Vector3f.UNIT_X);
        Vector3f finalOffset = verticalRot.mult(rotatedOffset);

        cam.setLocation(playerNode.getLocalTranslation().add(finalOffset));
        cam.lookAt(playerNode.getLocalTranslation().add(0, 1f, 0), Vector3f.UNIT_Y);

        // Reinicia los valores del mouse para el siguiente frame
        mouseDeltaX = 0;
        mouseDeltaY = 0;
    }


    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    private void initKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));

        inputManager.addListener(analogListener, "Left", "Right", "Forward", "Backward");
        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "Shoot");
        
        inputManager.addRawInputListener(rawMouseListener);

        
    }
    
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            Vector3f dir = cam.getDirection().clone().setY(0).normalizeLocal();
            Vector3f left = cam.getLeft().clone().setY(0).normalizeLocal();
            float sensitivity = 2.5f;

            if (name.equals("Forward")) {
                playerNode.move(dir.mult(tpf * moveSpeed));
            }
            if (name.equals("Backward")) {
                playerNode.move(dir.negate().mult(tpf * moveSpeed));
            }
            if (name.equals("Left")) {
                playerNode.move(left.mult(tpf * moveSpeed));
            }
            if (name.equals("Right")) {
                playerNode.move(left.negate().mult(tpf * moveSpeed));
            }

        }
    };

    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Shoot") && isPressed) {
                shootProjectile();
            }
        }
    };
    
    private RawInputListener rawMouseListener = new RawInputListener() {
        public void onMouseMotionEvent(MouseMotionEvent evt) {
            mouseDeltaX += evt.getDX();
            mouseDeltaY += evt.getDY();
        }

        public void beginInput() {}
        public void endInput() {}
        public void onJoyAxisEvent(com.jme3.input.event.JoyAxisEvent evt) {}
        public void onJoyButtonEvent(com.jme3.input.event.JoyButtonEvent evt) {}
        public void onMouseButtonEvent(com.jme3.input.event.MouseButtonEvent evt) {}
        public void onKeyEvent(com.jme3.input.event.KeyInputEvent evt) {}
        public void onTouchEvent(com.jme3.input.event.TouchEvent evt) {}
    };


    private void shootProjectile() {
        Box bulletBox = new Box(0.1f, 0.1f, 0.3f);
        Geometry bullet = new Geometry("Bullet", bulletBox);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Yellow);
        bullet.setMaterial(mat);

        Vector3f start = playerNode.getLocalTranslation().clone().add(0, 1f, 0);
        bullet.setLocalTranslation(start);
        rootNode.attachChild(bullet);

        // Movimiento en la dirección de la cámara
        Vector3f direction = cam.getDirection().clone().normalizeLocal();
        bullet.addControl(new BulletControl(direction.mult(30))); // Control personalizado
    }

}

class BulletControl extends AbstractControl {
    private Vector3f velocity;

    public BulletControl(Vector3f velocity) {
        this.velocity = velocity;
    }

    @Override
    protected void controlUpdate(float tpf) {
        spatial.move(velocity.mult(tpf));
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // Nada aquí
    }
}

