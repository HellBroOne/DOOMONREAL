package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.bounding.BoundingBox;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.shape.*;
import com.jme3.material.Material;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import java.util.ArrayList;
import java.util.List;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Box;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.font.BitmapText;
import com.jme3.font.BitmapFont;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.util.SkyFactory;
import com.jme3.input.controls.MouseAxisTrigger;



public class Main extends SimpleApplication implements ActionListener {

    private Node puente;
    private Spatial jugador;
    private boolean adelante, atras, izquierda, derecha;
    private float velocidadJugador = 15f;
    private List<Geometry> balas = new ArrayList<>();
    private List<Node> enemigos = new ArrayList<>();
    private Material materialBala;
    private Material texturaCalle;
    private Material materialEnemigo;
    private boolean salto = false;
    private int puntuacion = 0;
    private BitmapText hudText;
    private BitmapText gameOverText;
    private AudioNode music;
    private boolean gameOver = false;
    private float rotacionJugador = 0;
    private float sensibilidadMouse = 0.01f;
    private Node cameraNode;


    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setTitle("DooMonreal");
        settings.setSettingsDialogImage("Interface/dm_splash.png");
        settings.setResolution(1280, 960);
        settings.setFrameRate(70);
        settings.setFullscreen(true);
        Main app = new Main();
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        inputManager.setCursorVisible(false);
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(15f);
        cam.setLocation(new Vector3f(0, 20, 30));
        cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);

        // Música de fondo
        music = new AudioNode(assetManager, "Sounds/doomonreal_bgm.wav", AudioData.DataType.Stream);
        music.setPositional(false);
        music.setLooping(true);
        music.setVolume(120f);
        music.play();

        configurarControles();
        initHUD();

        // Materiales generales
        materialBala = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialBala.setColor("Color", ColorRGBA.Yellow);

        materialEnemigo = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialEnemigo.setColor("Color", ColorRGBA.Red);
        
        //Skybox
        Spatial sky = SkyFactory.createSky(
            assetManager,
            assetManager.loadTexture("Textures/Sky/top.png"),
            assetManager.loadTexture("Textures/Sky/top.png"),
            assetManager.loadTexture("Textures/Sky/top.png"),
            assetManager.loadTexture("Textures/Sky/top.png"),
            assetManager.loadTexture("Textures/Sky/top.png"),
            assetManager.loadTexture("Textures/Sky/top.png")
        );
        rootNode.attachChild(sky);

        // Luces para mejor visibilidad
        DirectionalLight luzDireccional = new DirectionalLight();
        luzDireccional.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        luzDireccional.setColor(ColorRGBA.White.mult(1.2f));
        rootNode.addLight(luzDireccional);

        AmbientLight luzAmbiente = new AmbientLight();
        luzAmbiente.setColor(ColorRGBA.White.mult(0.3f));
        rootNode.addLight(luzAmbiente);
        
        // Suelo, jugador y puente
        crearSuelo();
        crearPuente();
        crearJugador();

        getStateManager().attach(new GeneradorEnemigos(this));
    }

    private void initHUD() {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        
        // Texto de puntuación
        hudText = new BitmapText(font, false);
        hudText.setSize(font.getCharSet().getRenderedSize());
        hudText.setColor(ColorRGBA.White);
        hudText.setText("Puntuación: 0");
        hudText.setLocalTranslation(10, hudText.getLineHeight() + 10, 0);
        guiNode.attachChild(hudText);
        
        // Texto de Game Over (inicialmente invisible)
        gameOverText = new BitmapText(font, false);
        gameOverText.setSize(font.getCharSet().getRenderedSize() * 10);
        gameOverText.setColor(ColorRGBA.White);
        gameOverText.setText("GAME OVER");
        gameOverText.setLocalTranslation(
            settings.getWidth()/2 - gameOverText.getLineWidth()/2,
            settings.getHeight()/2 + gameOverText.getLineHeight()/2,
            0
        );
        gameOverText.setCullHint(Spatial.CullHint.Always);
        guiNode.attachChild(gameOverText);
    }

    private void configurarControles() {
        inputManager.addMapping("Adelante", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Atras", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Izquierda", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Derecha", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Salto", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Adelante", "Atras", "Izquierda", "Derecha", "Salto");

        inputManager.addMapping("Disparar", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "Disparar");
        
        //listener para la rotacion
        inputManager.addMapping("MouseX", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("MouseY", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addListener(this, "MouseX", "MouseY");
    }

    private void crearSuelo() {
        Box suelo = new Box(200, 0.1f, 50);
        Geometry geomSuelo = new Geometry("Suelo", suelo);
        texturaCalle = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        texturaCalle.setTexture("ColorMap", assetManager.loadTexture("Textures/calle1.jpg"));
        geomSuelo.setMaterial(texturaCalle);
        geomSuelo.setLocalTranslation(0, -0.5f, 0);
        geomSuelo.setModelBound(new BoundingBox());
        geomSuelo.updateModelBound();
        rootNode.attachChild(geomSuelo);
    }

    private void crearPuente() {
        puente = new Node("Puente");

        Box plataforma = new Box(50, 0.5f, 5);
        Geometry geomPlataforma = new Geometry("Plataforma", plataforma);
        Material matPlataforma = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matPlataforma.setColor("Color", new ColorRGBA(0.7f, 0.7f, 0.7f, 1));
        geomPlataforma.setMaterial(matPlataforma);
        geomPlataforma.setLocalTranslation(0, 0, 0);
        puente.attachChild(geomPlataforma);

        Box barrera = new Box(50, 1f, 0.5f);
        Geometry barreraIzq = new Geometry("Barrera Izquierda", barrera);
        Material matBarrera = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matBarrera.setColor("Color", ColorRGBA.Yellow);
        barreraIzq.setMaterial(matBarrera);
        barreraIzq.setLocalTranslation(0, 1, 5.5f);

        Geometry barreraDer = barreraIzq.clone();
        barreraDer.setName("Barrera Derecha");
        barreraDer.setLocalTranslation(0, 1, -5.5f);

        puente.attachChild(barreraIzq);
        puente.attachChild(barreraDer);
        rootNode.attachChild(puente);
    }

    private void crearJugador() {
        jugador = assetManager.loadModel("Models/Ninja.mesh.xml");
        
        jugador.scale(0.03f);
        jugador.setLocalTranslation(0, 0, 0);
        
        jugador.setModelBound(new BoundingBox());
        jugador.updateModelBound();

        rootNode.attachChild(jugador);
        
        // Crear un cameraNode como hijo del jugador
        cameraNode = new Node("CameraNode");
        //jugador.attachChild(cameraNode);
        cameraNode.setLocalTranslation(0, 5, 10); // posición detrás y arriba del jugador
    }

    private void disparar() {
        if (gameOver) return; 
        
        Sphere balaShape = new Sphere(20, 20, 0.3f);
        Geometry bala = new Geometry("Bala", balaShape);
        bala.setMaterial(materialBala);
        Vector3f posicionBala = jugador.getLocalTranslation().add(0, 0.87f, 0);
        bala.setLocalTranslation(posicionBala);
        bala.setModelBound(new BoundingBox());
        bala.updateModelBound();
        rootNode.attachChild(bala);
        balas.add(bala);
    }
    
    @Override
    public void onAction(String nombre, boolean estaPresionado, float tpf) {
        if (gameOver) return; 
        
        if (nombre.equals("Adelante")) adelante = estaPresionado;
        if (nombre.equals("Atras")) atras = estaPresionado;
        if (nombre.equals("Izquierda")) izquierda = estaPresionado;
        if (nombre.equals("Derecha")) derecha = estaPresionado;
        if (nombre.equals("Disparar") && estaPresionado) disparar();
    }

    @Override
    public void simpleUpdate(float tpf) {
        
        Vector3f dir = new Vector3f();
        if (adelante) dir.z -= 1;
        if (atras) dir.z += 1;
        if (izquierda) dir.x -= 1;
        if (derecha) dir.x += 1;
        if (dir.lengthSquared() > 0) {
            dir.normalizeLocal().multLocal(velocidadJugador * tpf);
            jugador.move(dir);
        }
        
        if (gameOver) return;

        Vector3f jugadorPos = jugador.getLocalTranslation();
        cam.setLocation(jugadorPos.add(0, 10, 20));
        cam.lookAt(jugadorPos, Vector3f.UNIT_Y);
        /**
        NMMS ESTE CODIGO NO JALO
       
        // Mueve la cámara SIEMPRE
        //Vector3f jugadorPos = jugador.getLocalTranslation();
        // Sigue al jugador
        cam.setLocation(cameraNode.getWorldTranslation());
        // Mira al jugador
        cam.lookAt(jugador.getWorldTranslation().add(0, 2, 0), Vector3f.UNIT_Y);

        // Solo actualiza lógica del juego si no es Game Over
        if (gameOver) return;

        // Movimiento del jugador
        Vector3f dir = new Vector3f();
        if (adelante) dir.z -= 1;
        if (atras) dir.z += 1;
        if (izquierda) dir.x -= 1;
        if (derecha) dir.x += 1;

        if (dir.lengthSquared() > 0) {
            dir.normalizeLocal().multLocal(velocidadJugador * tpf);
            dir = jugador.getLocalRotation().mult(dir);
            jugador.move(dir);
        }
        **/
        
        System.out.println("Mouse Pos: " + inputManager.getCursorPosition());

        for (int i = balas.size() - 1; i >= 0; i--) {
            Geometry bala = balas.get(i);
            bala.move(0, 0, -100 * tpf); 

            for (int j = enemigos.size() - 1; j >= 0; j--) {
                Node enemigo = enemigos.get(j);
                
                if (bala.getWorldBound().intersects(enemigo.getWorldBound())) {
                    rootNode.detachChild(enemigo);
                    enemigos.remove(j);
                    AudioNode kill = new AudioNode(assetManager, "Sounds/enemydefeat.wav", AudioData.DataType.Buffer);
                    kill.setPositional(true); 
                    kill.playInstance();  
                    rootNode.detachChild(bala);
                    balas.remove(i);
                    puntuacion++;
                    hudText.setText("Puntuación: " + puntuacion);
                    System.out.println("¡Enemigo eliminado! Puntuación: " + puntuacion);
                    break;
                }
            }

            if (bala.getLocalTranslation().z < -100) {
                rootNode.detachChild(bala);
                balas.remove(i);
            }
        }

        for (Node enemigo : enemigos) {
            Vector3f dirEnemigo = jugador.getLocalTranslation().subtract(enemigo.getLocalTranslation()).normalize().mult(tpf * 3f);
            enemigo.move(dirEnemigo);
            enemigo.updateModelBound();
            
            if (jugador.getWorldBound().intersects(enemigo.getWorldBound())) {
                gameOver();
                break;
            }
        }
    }

    private void gameOver() {
        gameOver = true;
        gameOverText.setCullHint(Spatial.CullHint.Never); 
        
        //AudioNode music = (AudioNode) rootNode.getChild("BackgroundMusic");
        if (music != null) {
            music.stop();
        }
        
        AudioNode gameOverSound = new AudioNode(assetManager, "Sounds/gameover.wav", AudioData.DataType.Buffer);
        gameOverSound.setPositional(false);
        gameOverSound.setLooping(false);
        gameOverSound.play();
        
        //este audio nomas por los loles, lo pueden quitar si quieren 
        AudioNode gmOv = new AudioNode(assetManager, "Sounds/gameover_mus.wav", AudioData.DataType.Stream);
        gmOv.setPositional(false);
        gmOv.play();
        
        System.out.println("¡Game Over! Puntuación final: " + puntuacion);
    }

    public void crearEnemigo() {
        if (gameOver) return; 
        
        Node enemigo = new Node("Enemigo");

        Spatial cabeza = assetManager.loadModel("Models/MonkeyHead.mesh.xml");
        cabeza.scale(1.1f);
        cabeza.setLocalTranslation(0, 1.5f, 0);
        enemigo.attachChild(cabeza);

        Node cartelNode = new Node("Cartel");

        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText texto = new BitmapText(font, false);
        texto.setSize(0.5f);
        texto.setText("¡¡NO AL SEGUNDO PISO!!");
        texto.setColor(ColorRGBA.Red);
        texto.updateLogicalState(0);
        texto.updateGeometricState();

        float textoWidth = texto.getLineWidth();
        float textoHeight = texto.getLineHeight();
        float padding = 0.3f;

        Quad fondo = new Quad(textoWidth + padding * 2, textoHeight + padding * 2);
        Geometry fondoGeom = new Geometry("Fondo", fondo);
        Material fondoMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fondoMat.setColor("Color", ColorRGBA.White);
        fondoGeom.setMaterial(fondoMat);

        fondoGeom.setLocalTranslation(-fondo.getWidth() / 2f, -fondo.getHeight() / 2f, 0);
        texto.setLocalTranslation(-textoWidth / 2f, -textoHeight / 2f + 0.25f, 0.01f);

        cartelNode.attachChild(fondoGeom);
        cartelNode.attachChild(texto);
        cartelNode.setLocalTranslation(0, 3.2f, 0);
        enemigo.attachChild(cartelNode);

        Vector3f jugadorPos = jugador.getLocalTranslation();
        float x = jugadorPos.x + (FastMath.nextRandomFloat() * 40f - 20f);
        float z = jugadorPos.z - 20f - (FastMath.nextRandomFloat() * 10f);
        enemigo.setLocalTranslation(new Vector3f(x, 1f, z));

        enemigo.setModelBound(new BoundingBox());
        enemigo.updateModelBound();

        enemigos.add(enemigo);
        rootNode.attachChild(enemigo);
    }
}
