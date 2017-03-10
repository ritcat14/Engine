package engineTester;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiButton;
import guis.GuiComponent;
import guis.GuiPanel;
import guis.GuiRenderer;
import models.TexturedModel;
import objConverter.OBJFileLoader;
import particles.ParticleMaster;
import postProcessing.Fbo;
import postProcessing.PostProcessing;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.Maths;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;

public class World {
	
	private Loader loader;
	private MasterRenderer renderer;
	
	private Fbo multisampleFbo;
	private Fbo outputFbo;
	private Fbo outputFbo2;
	
	public static final float gradient = 5.0f;
	public static final float density = 0.003f;
	
	private ArrayList<Entity> entities;
	private ArrayList<Entity> normalMapEntities;
	private ArrayList<Light> lights;
	
	private ArrayList<Entity> entitiesToRender;
	private ArrayList<Entity> normalMapEntitiesToRender;
	private ArrayList<Light> lightsToRender;
	
	private ArrayList<GuiComponent> components;
	
	private Light sun;
	private Camera camera;
	private Player player;
	private Terrain terrain;
	private WaterTile water;
	private WaterShader waterShader;
	private GuiRenderer guiRenderer;
	private WaterFrameBuffers buffers;
	private WaterRenderer waterRenderer;
	
	public World(Loader loader, MasterRenderer renderer, Camera camera, Player player) {
		this.loader = loader;
		this.renderer = renderer;
		this.camera = camera;
		this.player = player;
		entities = new ArrayList<Entity>();
		normalMapEntities = new ArrayList<Entity>();
		lights = new ArrayList<Light>();
		entitiesToRender = new ArrayList<Entity>();
		normalMapEntitiesToRender = new ArrayList<Entity>();
		lightsToRender = new ArrayList<Light>();
		components = new ArrayList<GuiComponent>();
		init();
	}
	
	public void init() {
		guiRenderer = new GuiRenderer(loader);
		
		multisampleFbo = new Fbo(Display.getWidth(), Display.getHeight());
		outputFbo = new Fbo(Display.getWidth(), Display.getHeight(), Fbo.DEPTH_TEXTURE);
		outputFbo2 = new Fbo(Display.getWidth(), Display.getHeight(), Fbo.DEPTH_TEXTURE);

		// *********TERRAIN TEXTURE STUFF**********
		
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy2"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));
		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture,
				gTexture, bTexture);
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));

		terrain = new Terrain(0, -1, loader, texturePack, blendMap, "heightmap");

		// *****************************************
		
		String[] entityData = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("res/entityList.txt"));
			entityData = reader.readLine().split(";");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		float x = 735;
		float z = -485;
		float y = terrain.getHeightOfTerrain(x, z);
		
		for (String s : entityData) {
			TexturedModel model = new TexturedModel(OBJFileLoader.loadOBJ(s + "/model", loader),
					new ModelTexture(loader.loadTexture(s + "/diffuse")));
			
			String line;
			int index = 0;
			String[] data = new String[10];
			try {
				reader = new BufferedReader(new FileReader("res/" + s + "/configs.txt"));
				while((line = reader.readLine()) != null) {
					data[index] = line;
					index++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				boolean hasExtraMap = Boolean.parseBoolean(data[0].split(";")[1]);
				boolean hasTransparency = Boolean.parseBoolean(data[1].split(";")[1]);
				boolean hasReflection = Boolean.parseBoolean(data[2].split(";")[1]);
				float reflection = 0;
				if (hasReflection) reflection = Float.parseFloat(data[3].split(";")[1]);
				
				if (hasExtraMap) model.getTexture().setExtraInfoMap(loader.loadTexture(s + "/extra"));
				model.getTexture().setHasTransparency(hasTransparency);
				model.getTexture().setReflectivity(reflection);
				
				entities.add(new Entity(model, new Vector3f(x, y, z), 0, -70, 0, 8f));
			} catch (Exception e) {
				System.out.println(s);
				e.printStackTrace();
			}
		}
		
		//*******************OTHER SETUP***************

		sun = new Light(new Vector3f(100000, 100000, -100000), new Vector3f(0f, 0f, 0f));
		lightsToRender.add(sun);
		
		entitiesToRender.add(player);
	
		//**********Water Renderer Set-up************************
	
		buffers = new WaterFrameBuffers();
		waterShader = new WaterShader();
		waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), buffers);
		water = new WaterTile(Terrain.SIZE, -Terrain.SIZE, Terrain.SIZE, -20);
		
		TexturedModel tree1 = new TexturedModel(OBJFileLoader.loadOBJ("tree", loader), new ModelTexture(loader.loadTexture("texture")));
		
		Random random = new Random(5666778);
		float waterHeight = water.getHeight();
		for (int i = 0; i < Terrain.SIZE/5; i++) {
			if (i % 3 == 0) {
				x = (random.nextFloat() * Terrain.SIZE);
				z = (random.nextFloat() * -Terrain.SIZE);
				if (!((x > 50 && x < 100) || (z < -50 && z > -100))) {
					y = terrain.getHeightOfTerrain(x, z);
					if (y < waterHeight) continue;
					entities.add(new Entity(tree1, 3, new Vector3f(x, y, z), 0,
							random.nextFloat() * 360, 0, 10f));
				}
			}
		}
		
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
		PostProcessing.init(loader);
		Vector2f size =  Maths.convertToOpenGLSize(Display.getWidth(), Display.getHeight());
		//GuiPanel main = new GuiPanel(loader, "blank", new Vector2f(0,0), size);
		
		size = Maths.convertToOpenGLSize(90, Display.getHeight());
		components.add(new GuiPanel(loader, "test", Maths.convertToOpenGLCoordinates(0, 0, size.x, size.y), size));
		size = Maths.convertToOpenGLSize(Display.getWidth() - 90, 90);
		components.add(new GuiPanel(loader, "test", Maths.convertToOpenGLCoordinates(90, 0, size.x, size.y), size));
		/*main.add(new GuiPanel(loader, "testCorner", new Vector2f(0.08f, -0.08f), size));
		main.add(new GuiButton(loader, "inventory", new Vector2f(0,0), new Vector2f(0.1f, 0.1f)) {
			@Override
			public void onMousePressed() {
				float xPos = ((0.02f) * ((Mouse.getX() * 100) / Display.getWidth())) - 1;
				float zPos = 1 - ((0.02f) * ((Mouse.getY() * 100) / Display.getHeight()));
				if (Mouse.isButtonDown(0) && inBounds(xPos, zPos, getPosition().x, getPosition().y, getSize().x, getSize().y)) {
					System.out.println("Inventory pressed");
				}
			}
		});
		components.add(main);*/
		checkLists();
	}
	
	private boolean inBounds(float x0, float y0, float x1, float y1, float sizeX, float sizeY) {
		return (x0 > (x1 - sizeX) && x0 < (x1 + sizeX) && y0 > (y1 - sizeY) && y0 < (y1 + sizeY));
	}
	
	private Vector2f getScaledVector(float width) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		int aspectX = tk.getScreenSize().width / 100;
		int aspectY = tk.getScreenSize().height / 100;
		float height = (width / aspectY) * aspectX;
		return new Vector2f(width, height);
	}
	
	private int i = 0;
	public void update() {
		i++;
		water.setHeight(-20 + (i / 5000));
		
		if (player.isMoving()) checkLists();
		
		ParticleMaster.update(camera);
		player.move(terrain);
		camera.move();
		renderer.renderShadowMap(entitiesToRender, sun);
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
		
		//render reflection teture
		buffers.bindReflectionFrameBuffer();
		float distance = 2 * (camera.getPosition().y - water.getHeight());
		camera.getPosition().y -= distance;
		camera.invertPitch();
		renderer.renderScene(entitiesToRender, normalMapEntitiesToRender, terrain, lightsToRender, camera, new Vector4f(0, 1, 0, -water.getHeight()+1));
		camera.getPosition().y += distance;
		camera.invertPitch();
		
		//render refraction texture
		buffers.bindRefractionFrameBuffer();
		renderer.renderScene(entitiesToRender, normalMapEntitiesToRender, terrain, lightsToRender, camera, new Vector4f(0, -1, 0, water.getHeight()));
		
		//render to screen
		GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
		buffers.unbindCurrentFrameBuffer();	
		multisampleFbo.bindFrameBuffer();
		renderer.renderScene(entitiesToRender, normalMapEntitiesToRender, terrain, lightsToRender, camera, new Vector4f(0, -1, 0, 100000));	
		waterRenderer.render(water, camera, sun);
		ParticleMaster.renderParticles(camera);
		multisampleFbo.unbindFrameBuffer();
		multisampleFbo.resolveToFbo(GL30.GL_COLOR_ATTACHMENT0, outputFbo);
		multisampleFbo.resolveToFbo(GL30.GL_COLOR_ATTACHMENT1, outputFbo2);
		PostProcessing.doPostProcessing(outputFbo.getColourTexture(), outputFbo2.getColourTexture());

		ArrayList<GuiComponent> componentsToRemove = new ArrayList<GuiComponent>();
		for (GuiComponent c : components) {
			if (c.isRemoved()) componentsToRemove.add(c);
		}
		components.removeAll(componentsToRemove);
		for (GuiComponent c : components) c.render(guiRenderer);
	}
	
	private void checkLists() {
		ArrayList<Entity> entitiesToRemove = new ArrayList<Entity>();
		for (Entity e : entities) {
			if (e instanceof Player) continue;
			if (e.isRemoved()) {
				entitiesToRemove.add(e);
				continue;
			}
			boolean inBounds = player.inBounds(e.getPosition().x, e.getPosition().z);
			if (inBounds) {
				if (!entitiesToRender.contains(e)) entitiesToRender.add(e);
			} else {
				if (entitiesToRender.contains(e)) entitiesToRender.remove(e);
			}
		}
		entities.removeAll(entitiesToRemove);
		
		ArrayList<Entity> normalMapEntitiesToRemove = new ArrayList<Entity>();
		for (Entity e : normalMapEntities) {
			if (e instanceof Player) continue;
			if (e.isRemoved()) {
				normalMapEntitiesToRemove.add(e);
				continue;
			}
			boolean inBounds = player.inBounds(e.getPosition().x, e.getPosition().z);
			if (inBounds) {
				if (!normalMapEntitiesToRender.contains(e)) normalMapEntitiesToRender.add(e);
			} else {
				if (normalMapEntitiesToRender.contains(e)) normalMapEntitiesToRender.remove(e);
			}
		}
		normalMapEntities.removeAll(normalMapEntitiesToRemove);
		
		ArrayList<Light> lightsToRemove = new ArrayList<Light>();
		for (Light l : lights) {
			if (l.equals(sun)) continue;
			boolean inBounds = player.inBounds(l.getPosition().x, l.getPosition().z);
			if (inBounds) {
				if (!lightsToRender.contains(l)) lightsToRender.add(l);
			} else {
				if (lightsToRender.contains(l)) lightsToRender.remove(l);
			}
		}
		lights.removeAll(lightsToRemove);
	}

	public void cleanUp() {
		PostProcessing.cleanUp();
		outputFbo.cleanUp();
		outputFbo2.cleanUp();
		multisampleFbo.cleanUp();
		ParticleMaster.cleanUp();
		buffers.cleanUp();
		waterShader.cleanUp();
		guiRenderer.cleanUp();
	}
	
}
