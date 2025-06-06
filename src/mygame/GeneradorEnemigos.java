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
    private float intervaloGeneracion = 3f; // Initial spawn interval (seconds)
    private float tiempoJuego = 0f; // Total game time tracker
    private float minIntervalo = 0.5f; // Minimum spawn interval
    
    // Difficulty curve parameters
    private final float dificultadIncrementoTiempo = 30f; // Every 30 seconds
    private final float dificultadReduccionIntervalo = 0.2f; // Reduce spawn interval by this amount
    private final float dificultadMinimaReduccion = 0.05f; // Minimum reduction per step
    
    private int enemyCount = 0;
    private final int BOSS_INTERVAL = 20;

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
        tiempoJuego += tpf;
        
        // Spawn enemy if interval has passed
        if (tiempoAcumulado >= intervaloGeneracion) {
            tiempoAcumulado = 0;
            enemyCount++;
            if (enemyCount % BOSS_INTERVAL == 0) {
                mainApp.crearEnemigo(true);
            } else {
                mainApp.crearEnemigo(false);
            }
        }
        
        // Gradually increase difficulty over time
        if (tiempoJuego >= dificultadIncrementoTiempo) {
            tiempoJuego = 0; // Reset timer
            
            // Calculate new spawn interval (but don't go below minimum)
            float nuevoIntervalo = intervaloGeneracion - dificultadReduccionIntervalo;
            intervaloGeneracion = Math.max(nuevoIntervalo, minIntervalo);
            
            // Optional: Make sure we don't reduce too much at once
            if (intervaloGeneracion - nuevoIntervalo > dificultadMinimaReduccion) {
                intervaloGeneracion = intervaloGeneracion - dificultadMinimaReduccion;
            }
        }
    }
}