package engineTester;

import java.awt.Toolkit;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.PixelFormat;

public class DisplayManager {

	private static final Toolkit tk = Toolkit.getDefaultToolkit();
	private static final int WIDTH = (int) tk.getScreenSize().getWidth();
	private static final int HEIGHT = (int) tk.getScreenSize().getHeight();
	private static final int FPS_CAP = 60;
	
	private static long lastFrameTime;
	private static float delta;
	
	public static void createDisplay(){		
		ContextAttribs attribs = new ContextAttribs(3,3)
		.withForwardCompatible(true)
		.withProfileCore(true);

		System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
				
		try {
			Display.setDisplayMode(new DisplayMode(WIDTH,HEIGHT));
			Display.create(new PixelFormat().withDepthBits(24), attribs);
			Display.setTitle("Broken Universe");
			GL11.glEnable(GL13.GL_MULTISAMPLE);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		
		GL11.glViewport(0,0, WIDTH, HEIGHT);
		lastFrameTime = getCurrentTime();
	}
	
	public static void updateDisplay(){
		Display.sync(FPS_CAP);
		Display.update();
		long currentFrameTime = getCurrentTime();
		delta = (currentFrameTime - lastFrameTime)/1000f;
		lastFrameTime = currentFrameTime;
	}
	
	public static float getFrameTimeSeconds(){
		return delta;
	}
	
	public static void closeDisplay(){
		Display.destroy();
	}
	
	private static long getCurrentTime(){
		return Sys.getTime()*1000/Sys.getTimerResolution();
	}
	
	
	

}
