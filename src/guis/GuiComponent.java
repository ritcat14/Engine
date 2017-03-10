package guis;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;

import renderEngine.Loader;

public abstract class GuiComponent {
	
	protected ArrayList<GuiComponent> components = new ArrayList<GuiComponent>();
	protected Vector2f position, size;
	protected GuiTexture texture;
	protected boolean removed = false;
	
	public GuiComponent(Loader loader, String texture, Vector2f position, Vector2f size) {
		this.texture = new GuiTexture(loader.loadTexture(texture), position, size);
		this.position = position;
		this.size = size;
	}
	
	public void remove() {
		this.removed = true;
	}
	
	public boolean isRemoved() {
		return removed;
	}
	
	public void add(GuiComponent c) {
		components.add(c);
	}
	
	public void remove(GuiComponent c) {
		components.remove(c);
	}
	
	public Vector2f getPosition() {
		return position;
	}
	
	public Vector2f getSize() {
		return size;
	}
	
	public void setPosition(Vector2f position) {
		this.position = position;
	}
	
	public void setSize(Vector2f size) {
		this.size = size;
	}
	
	public GuiTexture getTexture() {
		return texture;
	}
	
	public void setTexture(GuiTexture texture) {
		this.texture = texture;
	}
	
	public void render(GuiRenderer renderer) {
		if (removed) return;
		ArrayList<GuiComponent> temp = new ArrayList<GuiComponent>();
		temp.add(this);
		temp.addAll(components);
		renderer.render(temp);
	}

}
