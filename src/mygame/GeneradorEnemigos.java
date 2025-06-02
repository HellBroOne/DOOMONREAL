package mygame;

import com.jme3.app.state.BaseAppState;
import com.jme3.app.Application;

public class GeneradorEnemigos extends BaseAppState {

    private Main juego;
    private float temporizador = 0;
    private float intervaloGeneracion = 2f;

    public GeneradorEnemigos(Main juego) {
        this.juego = juego;
    }

    @Override
    protected void initialize(Application app) {}

    @Override
    public void update(float tpf) {
        temporizador += tpf;
        
        if (temporizador >= intervaloGeneracion) {
            juego.crearEnemigo();
            temporizador = 0;
            
            intervaloGeneracion = Math.max(0.5f, intervaloGeneracion * 0.95f);
        }
    }

    @Override
    protected void cleanup(Application app) {}

    @Override
    protected void onEnable() {}

    @Override
    protected void onDisable() {}
}