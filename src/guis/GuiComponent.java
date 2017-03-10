package guis;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;

import renderEngine.Loader;

public abstract class GuiComponent {
	
	protected ArrayList<GuiComponent> components = new ArrayList<GuiComponent>();
	protected GuiTexture texture;
	protected boolean removed = false;
	
	public GuiComponent(Loader loader, String texture, Vector2f position, Vector2f size) {
		this.texture = new GuiTexture(loader.loadTexture(texture), position, size);
	}
	
	public void remove() {
		this.removed = true;
	}
	
	public boolean isRemoved() {
		return removed;
	}
	
	public void add(GuiComponent c) { // NOTE: Adding a component to another component is position relative!
		float cX = c.getPosition().x;
		float cY = c.getPosition().y;
		float tX = this.getPosition().x - this.getSize().x;
		float tY = this.getPosition().y - this.getSize().y;
		c.setPosition(new Vector2f(tX + cX, tY - cY));
		components.add(c);
		System.out.println(c.getPosition().x + ":" + c.getPosition().y);
	}
	
	public void remove(GuiComponent c) {
		components.remove(c);
	}
	
	public Vector2f getPosition() {
		return texture.getPosition();
	}
	
	public Vector2f getSize() {
		return texture.getScale();
	}
	
	public void setPosition(Vector2f position) {
		texture.setPosition(position);
	}
	
	public void setSize(Vector2f size) {
		texture.setScale(size);
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
