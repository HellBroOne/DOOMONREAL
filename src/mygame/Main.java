package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.shape.*;
import com.jme3.material.Material;
import java.util.ArrayList;
import java.util.List;

public class Main extends SimpleApplication implements ActionListener {

    private Node puente;
    private Geometry jugador;
    private boolean adelante, atras, izquierda, derecha;
    private float velocidadJugador = 15f;
    private List<Geometry> balas = new ArrayList<>();
    private List<Geometry> enemigos = new ArrayList<>();
    private Material materialBala;
    private Material materialEnemigo;
    private int puntuacion = 0;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        cam.setLocation(new Vector3f(0, 20, 30));
        cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);

        configurarControles();

        materialBala = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialBala.setColor("Color", ColorRGBA.Yellow);

        materialEnemigo = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialEnemigo.setColor("Color", ColorRGBA.Red);

        crearSuelo();
        crearPuente();
        crearJugador();

        getStateManager().attach(new GeneradorEnemigos(this));
    }

    private void configurarControles() {
        inputManager.addMapping("Adelante", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Atras", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Izquierda", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Derecha", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addListener(this, "Adelante", "Atras", "Izquierda", "Derecha");

        inputManager.addMapping("Disparar", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "Disparar");
    }

    private void crearSuelo() {
        Box suelo = new Box(200, 0.1f, 200);
        Geometry geomSuelo = new Geometry("Suelo", suelo);
        Material matSuelo = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matSuelo.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.3f, 1));
        geomSuelo.setMaterial(matSuelo);
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
        geomPlataforma.setModelBound(new BoundingBox());
        geomPlataforma.updateModelBound();
        puente.attachChild(geomPlataforma);

        Box barrera = new Box(50, 1f, 0.5f);
        Geometry barreraIzq = new Geometry("Barrera Izquierda", barrera);
        Material matBarrera = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matBarrera.setColor("Color", ColorRGBA.Yellow);
        barreraIzq.setMaterial(matBarrera);
        barreraIzq.setLocalTranslation(0, 1, 5.5f);
        barreraIzq.setModelBound(new BoundingBox());
        barreraIzq.updateModelBound();

        Geometry barreraDer = new Geometry("Barrera Derecha", barrera);
        barreraDer.setMaterial(matBarrera);
        barreraDer.setLocalTranslation(0, 1, -5.5f);
        barreraDer.setModelBound(new BoundingBox());
        barreraDer.updateModelBound();

        puente.attachChild(barreraIzq);
        puente.attachChild(barreraDer);
        rootNode.attachChild(puente);
    }

    private void crearJugador() {
        Box jugadorBox = new Box(0.8f, 1.5f, 0.8f);
        jugador = new Geometry("Jugador", jugadorBox);
        Material matJugador = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matJugador.setColor("Color", new ColorRGBA(0, 0.5f, 1, 1));
        jugador.setMaterial(matJugador);
        jugador.setLocalTranslation(0, 1.5f, 0);
        jugador.setModelBound(new BoundingBox());
        jugador.updateModelBound();
        rootNode.attachChild(jugador);
    }

    private void disparar() {
        Sphere balaShape = new Sphere(20, 20, 0.3f);
        Geometry bala = new Geometry("Bala", balaShape);
        bala.setMaterial(materialBala);
        Vector3f posicionBala = jugador.getLocalTranslation().add(0, 1.5f, 0);
        bala.setLocalTranslation(posicionBala);
        bala.setModelBound(new BoundingBox());
        bala.updateModelBound();
        rootNode.attachChild(bala);
        balas.add(bala);
    }

    @Override
    public void onAction(String nombre, boolean estaPresionado, float tpf) {
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

        Vector3f jugadorPos = jugador.getLocalTranslation();
        cam.setLocation(jugadorPos.add(0, 10, 20));
        cam.lookAt(jugadorPos, Vector3f.UNIT_Y);

        for (int i = balas.size() - 1; i >= 0; i--) {
            Geometry bala = balas.get(i);
            bala.move(0, 0, -100 * tpf); 

            bala.updateModelBound();

            boolean colisionDetectada = false;
            for (int j = enemigos.size() - 1; j >= 0; j--) {
                Geometry enemigo = enemigos.get(j);

                enemigo.updateModelBound();

                if (bala.getWorldBound().intersects(enemigo.getWorldBound())) {
                    System.out.println("Intersección detectada entre bala y enemigo!");
                    rootNode.detachChild(enemigo);
                    enemigos.remove(j);
                    rootNode.detachChild(bala);
                    balas.remove(i);
                    puntuacion++;
                    System.out.println("¡Enemigo eliminado! Puntuación: " + puntuacion);
                    colisionDetectada = true;
                    break;
                }
            }

            if (!colisionDetectada && bala.getLocalTranslation().z < -100) {
                rootNode.detachChild(bala);
                balas.remove(i);
            }
        }

        for (Geometry enemigo : enemigos) {
            Vector3f dirEnemigo = jugador.getLocalTranslation().subtract(enemigo.getLocalTranslation()).normalize().mult(tpf * 3f);
            enemigo.move(dirEnemigo);
            enemigo.updateModelBound();
        }

        rootNode.updateGeometricState();
    }

    public void crearEnemigo() {
        Box enemigoBox = new Box(1f, 1f, 1f);
        Geometry enemigo = new Geometry("Enemigo", enemigoBox);
        enemigo.setMaterial(materialEnemigo);
        enemigo.setLocalTranslation((float) (Math.random() * 40 - 20), 1f, -50);
        enemigo.setModelBound(new BoundingBox());
        enemigo.updateModelBound();
        rootNode.attachChild(enemigo);
        enemigos.add(enemigo);
    }

}
