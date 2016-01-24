package io.tokra.audio.mp3;

public class PlayMP3 {

	public PlayMP3() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MP3 player = new MP3("file:///S:/Stenchman - Gypsy Tart.mp3");
		player.start();
		
	}
}
