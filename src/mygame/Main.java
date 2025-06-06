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


import com.jme3.material.RenderState.BlendMode;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.control.BillboardControl;

public class Main extends SimpleApplication implements ActionListener {

    private Node puente;
    private Spatial jugador;
    private boolean adelante, atras, izquierda, derecha;
    private float velocidadJugador = 15f;
    private List<Geometry> balas = new ArrayList<>();
    private List<Node> enemigos = new ArrayList<>();
    private List<Node> jefes = new ArrayList<>();
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
        flyCam.setEnabled(false);
        flyCam.setMoveSpeed(15f);
        cam.setLocation(new Vector3f(0, 25, 40));
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
        gameOverText.setSize(font.getCharSet().getRenderedSize() * 7);
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
        // Crear un Quad plano que represente al jugador
        Quad quad = new Quad(9f, 10f); // Tamaño del sprite. Ajusta según tu imagen
        Geometry jugadorGeom = new Geometry("Jugador", quad);

        // Crear un material con transparencia
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Textures/monrealon.png")); // Ruta a tu PNG
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha); // Para respetar transparencia
        jugadorGeom.setQueueBucket(RenderQueue.Bucket.Transparent); // Para renderizar con alpha

        jugadorGeom.setMaterial(mat);

        jugadorGeom.setLocalTranslation(0, 0, 0);

        jugadorGeom.rotate(0, FastMath.PI, 0); 

        jugador = jugadorGeom;

        rootNode.attachChild(jugador);
        
        // Crear un cameraNode como hijo del jugador
        cameraNode = new Node("CameraNode");
        //jugador.attachChild(cameraNode);
        cameraNode.setLocalTranslation(0, 5, 10); // posición detrás y arriba del jugador
    }


    private void disparar() {
    if (gameOver) return; 

    // Crear un Quad plano para la bala
    Quad quad = new Quad(3f, 2f); 
    Geometry bala = new Geometry("Bala", quad);

    // Crear un material que use el PNG con transparencia
    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setTexture("ColorMap", assetManager.loadTexture("Textures/benito.png"));

    bala.setMaterial(mat);

    // Posicionar la bala
    Vector3f posicionBala = jugador.getLocalTranslation().add(0, 0.87f, 0);
    bala.setLocalTranslation(posicionBala);

    // Orientación opcional (por ejemplo, mirando hacia el frente del jugador)
    bala.rotate(0, jugador.getLocalRotation().toAngles(null)[1], 0);

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
        if (gameOver) {
            return;
        }

        if (jugador != null) {
            jugador.lookAt(cam.getLocation(), Vector3f.UNIT_Y);
        }

        // Player movement (unchanged)
        Vector3f dir = new Vector3f();
        if (adelante) {
            dir.z -= 1;
        }
        if (atras) {
            dir.z += 1;
        }
        if (izquierda) {
            dir.x -= 1;
        }
        if (derecha) {
            dir.x += 1;
        }
        if (dir.lengthSquared() > 0) {
            dir.normalizeLocal().multLocal(velocidadJugador * tpf);
            jugador.move(dir);
        }

        // Camera follow - modified to be higher
        Vector3f jugadorPos = jugador.getLocalTranslation();
        cam.setLocation(jugadorPos.add(0, 15, 25));
        cam.lookAt(jugadorPos.add(0, 3, 0), Vector3f.UNIT_Y);
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

        // Bullet movement and collision
        for (int i = balas.size() - 1; i >= 0; i--) {
            Geometry bala = balas.get(i);
            bala.move(0, 0, -100 * tpf);

            // Check collision with regular enemies
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

            // Check collision with bosses (only if bullet still exists)
            if (i < balas.size()) {  // Make sure bullet wasn't removed by enemy collision
                for (int j = jefes.size() - 1; j >= 0; j--) {
                    Node boss = jefes.get(j);

                    if (bala.getWorldBound().intersects(boss.getWorldBound())) {
                        damageBoss(boss, 1); // Deal 1 damage
                        rootNode.detachChild(bala);
                        balas.remove(i);

                        // Play different sound for boss hit
                        AudioNode hit = new AudioNode(assetManager, "Sounds/Filter_hurt.wav", AudioData.DataType.Buffer);
                        hit.setPositional(true);
                        hit.playInstance();

                        break;
                    }
                }
            }

            // Remove bullets that go too far
            if (i < balas.size() && bala.getLocalTranslation().z < -100) {
                rootNode.detachChild(bala);
                balas.remove(i);
            }
        }
        
        // Enemy movement (unchanged)
        for (Node enemigo : enemigos) {
            Vector3f dirEnemigo = jugador.getLocalTranslation().subtract(enemigo.getLocalTranslation())
                    .normalize().mult(tpf * 3f);
            enemigo.move(dirEnemigo);
            enemigo.updateModelBound();

            if (jugador.getWorldBound().intersects(enemigo.getWorldBound())) {
                gameOver();
                break;
            }
        }

        // Boss movement (similar to enemies but maybe different speed)
        for (Node boss : jefes) {
            Vector3f dirBoss = jugador.getLocalTranslation().subtract(boss.getLocalTranslation())
                    .normalize().mult(tpf * 2f); // Boss might move slower
            boss.move(dirBoss);
            boss.updateModelBound();

            if (jugador.getWorldBound().intersects(boss.getWorldBound())) {
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

    public void crearEnemigo(boolean isBoss) {
        if (gameOver) {
            return;
        }

        Node enemigo = new Node(isBoss ? "Boss" : "Enemigo");

        // Create enemy/boss sprite
        Material enemyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        enemyMat.setTexture("ColorMap", assetManager.loadTexture(
                isBoss ? "Textures/amlo2.png" : "Textures/enemy.png"));
        enemyMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

        float size = 4f; // Boss is bigger
        Quad enemyQuad = new Quad(size, size);
        Geometry enemyGeom = new Geometry(isBoss ? "BossSprite" : "EnemySprite", enemyQuad);
        enemyGeom.setMaterial(enemyMat);
        enemyGeom.setLocalTranslation(0, size / 2, 0); // Center vertically

        // Make the sprite always face the camera
        BillboardControl billboard = new BillboardControl();
        enemyGeom.addControl(billboard);
        enemigo.attachChild(enemyGeom);

        // Create health bar for boss
        if (isBoss) {
            // Health bar background (red)
            Quad healthBarBg = new Quad(size, 0.5f);
            Geometry bgGeom = new Geometry("HealthBarBG", healthBarBg);
            Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            bgMat.setColor("Color", ColorRGBA.Red);
            bgGeom.setMaterial(bgMat);
            bgGeom.setLocalTranslation(-size / 2, size + 0.5f, 0);

            // Actual health bar (green)
            Quad healthBar = new Quad(size, 0.5f);
            Geometry healthGeom = new Geometry("HealthBar", healthBar);
            Material healthMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            healthMat.setColor("Color", ColorRGBA.Green);
            healthGeom.setMaterial(healthMat);
            healthGeom.setLocalTranslation(-size / 2, size + 0.5f, 0.01f);

            // Store health bar reference in user data
            enemigo.setUserData("health", 100); // Boss has 10 health
            enemigo.setUserData("maxHealth", 100);
            enemigo.setUserData("healthBar", healthGeom);

            enemigo.attachChild(bgGeom);
            enemigo.attachChild(healthGeom);
        }

        // Create sign
        Node cartelNode = new Node("Cartel");
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText texto = new BitmapText(font, false);
        texto.setSize(0.5f);
        texto.setText(isBoss ? "¡¡NO ACABARAS CON LA CUARTA TRANSFORMACION!!" : "¡¡NO AL SEGUNDO PISO!!");
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
        cartelNode.setLocalTranslation(0, size + 2f, 0); // Position above enemy/boss
        enemigo.attachChild(cartelNode);

        // Position the enemy/boss
        Vector3f jugadorPos = jugador.getLocalTranslation();
        float x = jugadorPos.x + (FastMath.nextRandomFloat() * (isBoss ? 30f : 40f) - (isBoss ? 15f : 20f));
        float z = jugadorPos.z - (isBoss ? 30f : 20f) - (FastMath.nextRandomFloat() * (isBoss ? 5f : 10f));
        enemigo.setLocalTranslation(new Vector3f(x, 1f, z));

        enemigo.setModelBound(new BoundingBox());
        enemigo.updateModelBound();
        
        if (isBoss) {
            // Add to boss list if you have one
            jefes.add(enemigo);
        } else {
            enemigos.add(enemigo);
        }
        rootNode.attachChild(enemigo);
    }
    
    public void damageBoss(Node boss, int damage) {
        if (boss == null || !boss.getName().equals("Boss")) {
            return;
        }

        int currentHealth = boss.getUserData("health");
        int maxHealth = boss.getUserData("maxHealth");
        Geometry healthBar = boss.getUserData("healthBar");

        currentHealth = Math.max(0, currentHealth - damage);
        boss.setUserData("health", currentHealth);

        // Update health bar
        float healthPercentage = (float) currentHealth / maxHealth;
        float originalWidth = ((Quad) healthBar.getMesh()).getWidth(); // Get original width
        healthBar.setLocalScale(healthPercentage, 1f, 1f);

        // Calculate new position - center the scaled health bar
        healthBar.setLocalTranslation(
                -originalWidth * healthPercentage / 2f, // Adjusted X position
                healthBar.getLocalTranslation().y, // Keep original Y
                0.01f // Keep original Z
        );

        if (currentHealth <= 0) {
            boss.removeFromParent();
            jefes.remove(boss);
            AudioNode kill = new AudioNode(assetManager, "Sounds/Isaac_dies_new_1.wav", AudioData.DataType.Buffer);
            kill.setPositional(true);
            kill.playInstance();
        }
    }
}
