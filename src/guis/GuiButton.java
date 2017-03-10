package guis;

import org.lwjgl.util.vector.Vector2f;

import renderEngine.Loader;

public abstract class GuiButton extends GuiComponent {

	public GuiButton(Loader loader, String texture, Vector2f position, Vector2f size) {
		super(loader, texture, position, size);
	}
	
	public abstract void onMousePressed();
	
	@Override
	public void render(GuiRenderer renderer) {
		super.render(renderer);
		onMousePressed();
	}

}
