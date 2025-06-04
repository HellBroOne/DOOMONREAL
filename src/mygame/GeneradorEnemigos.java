package mygame;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.material.Material;
import com.jme3.scene.shape.Box;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

public class GeneradorEnemigos extends BaseAppState {

    private Main mainApp;
    private float tiempoAcumulado = 0f;
    private float intervaloGeneracion = 3f;

    public GeneradorEnemigos(Main app) {
        this.mainApp = app;
    }

    @Override
    protected void initialize(Application app) {}

    @Override
    protected void cleanup(Application app) {}

    @Override
    protected void onEnable() {}

    @Override
    protected void onDisable() {}

    @Override
    public void update(float tpf) {
        tiempoAcumulado += tpf;
        if (tiempoAcumulado >= intervaloGeneracion) {
            tiempoAcumulado = 0;
            mainApp.crearEnemigo();
        }
    }
}
