package engineTester;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Player;
import models.RawModel;
import models.TexturedModel;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import textures.ModelTexture;

public class MainGameLoop {

	public static void main(String[] args) {

		DisplayManager.createDisplay();
		Loader loader = new Loader();

		RawModel bunnyModel = OBJLoader.loadObjModel("person", loader);
		TexturedModel stanfordBunny = new TexturedModel(bunnyModel, new ModelTexture(
				loader.loadTexture("playerTexture")));

		Player player = new Player(stanfordBunny, new Vector3f(75, 5, -75), 0, 100, 0, 0.6f);
		Camera camera = new Camera(player);
		
		MasterRenderer renderer = new MasterRenderer(loader, camera);
		
		World world = new World(loader, renderer, camera, player);
		
		//****************Game Loop Below*********************

		while (!Display.isCloseRequested()) {
			world.update();
			DisplayManager.updateDisplay();
		}

		//*********Clean Up Below**************
		world.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}
}
