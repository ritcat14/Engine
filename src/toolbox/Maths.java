package toolbox;

import java.awt.Rectangle;
import java.awt.Toolkit;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;

public class Maths {
	
	public static Matrix4f createTransformationMatrix(Vector2f translation, Vector2f scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.scale(new Vector3f(scale.x, scale.y, 1f), matrix, matrix);
		return matrix;
	}

	public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry,
			float rz, float scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1,0,0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0,1,0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0,0,1), matrix, matrix);
		Matrix4f.scale(new Vector3f(scale,scale,scale), matrix, matrix);
		return matrix;
	}
	
	public static float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {
		float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
		float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
		float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
		float l3 = 1.0f - l1 - l2;
		return l1 * p1.y + l2 * p2.y + l3 * p3.y;
	}
	
	public static Matrix4f createViewMatrix(Camera camera) {
		Matrix4f viewMatrix = new Matrix4f();
		viewMatrix.setIdentity();
		Matrix4f.rotate((float) Math.toRadians(camera.getPitch()), new Vector3f(1, 0, 0), viewMatrix,
				viewMatrix);
		Matrix4f.rotate((float) Math.toRadians(camera.getYaw()), new Vector3f(0, 1, 0), viewMatrix, viewMatrix);
		Vector3f cameraPos = camera.getPosition();
		Vector3f negativeCameraPos = new Vector3f(-cameraPos.x,-cameraPos.y,-cameraPos.z);
		Matrix4f.translate(negativeCameraPos, viewMatrix, viewMatrix);
		return viewMatrix;
	}
	
	public static Vector2f convertToJavaSize(Vector2f size) {
		int jWidth = (int) (0.02f / ((size.x / 100) * Display.getWidth()));
		int jHeight = (int) (0.02f / ((size.y / 100) * Display.getHeight()));
		return new Vector2f(jWidth * 2, jHeight * 2);
	}
	
	public static Vector2f convertToJavaCoordinates(Vector2f position, Vector2f size) {
		int xPos = (int) ((((0.02f) / ((position.x / 100) * Display.getWidth())) + 1) - size.x);
		int yPos = (int) ((((0.02f) / ((position.y / 100) * Display.getHeight())) + 1) - size.y);
		return new Vector2f(xPos, yPos);
	}
	
	public static Vector2f convertToOpenGLSize(int width, int height) {
		float glWidth = 0.02f * ((width * 100) / Display.getWidth());
		float glHeight = 0.02f * ((height * 100) / Display.getHeight());
		return new Vector2f(glWidth/2, glHeight/2);
	}
	
	public static Vector2f convertToOpenGLCoordinates(int x, int y, float xOffset, float yOffset) {
		float xPos = (((0.02f) * ((x * 100) / Display.getWidth())) - 1) + xOffset;
		float yPos = (1 - ((0.02f) * ((y * 100) / Display.getHeight()))) - yOffset;
		return new Vector2f(xPos, yPos);
	}
	
	public static boolean inBounds(int pointX, int pointY, Vector2f position, Vector2f size) {
		Rectangle r = new Rectangle((int)position.x, (int)position.y, (int)size.x, (int)size.y);
		return (r.contains(pointX, pointY));
	}
	
	public static Vector2f getScaledVector(float width) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		int aspectX = tk.getScreenSize().width / 100;
		int aspectY = tk.getScreenSize().height / 100;
		float height = (width / aspectY) * aspectX;
		return new Vector2f(width, height);
	}

}
