package guis;

import org.lwjgl.util.vector.Vector2f;

import renderEngine.Loader;

public class GuiPanel extends GuiComponent {

	public GuiPanel(Loader loader, String texture, Vector2f position, Vector2f size) {
		super(loader, texture, position, size);
	}

}
