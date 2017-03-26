package particles;

public class ParticleTexture {
	
	private int textureID;
	private int numberOFRows;
	private boolean additive;
	
	public ParticleTexture(int textureID, int numberOFRows, boolean additive) {
		this.additive = additive;
		this.textureID = textureID;
		this.numberOFRows = numberOFRows;
	}
	
	public boolean isAdditive() {
		return additive;
	}
	
	public int getTextureID() {
		return textureID;
	}
	
	public int getNumberOFRows() {
		return numberOFRows;
	}

}