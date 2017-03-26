package particles;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import engineTester.DisplayManager;
import entities.Camera;
import entities.Player;
import terrains.Terrain;

public class Particle {

	private Vector3f position;
	private Vector3f velocity;
	private float gravityEffect;
	private float lifeLength;
	private float rotation;
	private float scale;
	
	private ParticleTexture texture;
	
	private Vector2f texOffset1 = new Vector2f();
	private Vector2f texOffset2 = new Vector2f();
	private float blend;
	
	private float elapsedTime = 0;
	private float distance;
	
	private boolean alive = false;
	private boolean collideable = false;
	
	private Vector3f reusableChange = new Vector3f();

	public Particle(ParticleTexture texture, Vector3f position, Vector3f velocity, float gravityEffect, float lifeLength, float rotation,
			float scale, boolean collideable) {
		activate(texture, position, velocity, gravityEffect, lifeLength, rotation, scale, collideable);
	}

	public void activate(ParticleTexture texture, Vector3f position, Vector3f velocity, float gravityEffect, float lifeLength, float rotation,
			float scale, boolean collideable) {
		alive = true;
		this.texture = texture;
		this.position = position;
		this.velocity = velocity;
		this.gravityEffect = gravityEffect;
		this.lifeLength = lifeLength;
		this.rotation = rotation;
		this.scale = scale;
		this.collideable = collideable;
		ParticleMaster.addParticles(this, ParticleMaster.particles);
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	public void remove() {
		alive = false;
	}
	
	public float getDistance() {
		return distance;
	}
	
	public ParticleTexture getTexture() {
		return texture;
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getRotation() {
		return rotation;
	}

	public float getScale() {
		return scale;
	}
	
	public float getBlend() {
		return blend;
	}
	
	public Vector2f getTexOffset1() {
		return texOffset1;
	}
	
	public Vector2f getTexOffset2() {
		return texOffset2;
	}
	
	public boolean update(Camera camera, Terrain terrain) {
		float y = terrain.getHeightOfTerrain(position.x, position.z);
		if (position.y <= y && collideable) position.y = y;
		else {
			velocity.y += Player.GRAVITY * gravityEffect * DisplayManager.getFrameTimeSeconds();
			reusableChange.set(velocity);
			reusableChange.scale(DisplayManager.getFrameTimeSeconds());
			Vector3f.add(reusableChange, position, position);
			distance = Vector3f.sub(camera.getPosition(), position, null).lengthSquared();
			updateTextureCoordInfo();
		}
		elapsedTime += DisplayManager.getFrameTimeSeconds();
		return elapsedTime < lifeLength;
	}
	
	public void updateTextureCoordInfo() {
		float lifeFactor = elapsedTime / lifeLength;
		int stageCount = texture.getNumberOFRows() * texture.getNumberOFRows();
		float atlasProgression = lifeFactor * stageCount;
		int index1 = (int) Math.floor(atlasProgression);
		int index2 = index1 < stageCount ? index1 + 1 : index1;
		this.blend = atlasProgression % 1;
		setTextureOffset(texOffset1, index1);
		setTextureOffset(texOffset2, index2);
		
	}
	
	private void setTextureOffset(Vector2f offset, int index) {
		int column = index % texture.getNumberOFRows();
		int row = index / texture.getNumberOFRows();
		offset.x = (float)column / texture.getNumberOFRows();
		offset.y = (float) row / texture.getNumberOFRows();
	}
	
}
