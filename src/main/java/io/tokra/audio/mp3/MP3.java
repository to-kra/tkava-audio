package io.tokra.audio.mp3;

import java.net.URL;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;

public class MP3 extends Thread {

	public MP3() {
	}

	private URL url;
	private MediaLocator mediaLocator;
	private Player playMP3;

	public MP3(String mp3) {
		try {
			this.url = new URL(mp3);
		} catch (java.net.MalformedURLException e) {
			System.out.println(e.getMessage());
		}
	}

	public void run() {

		try {
			mediaLocator = new MediaLocator(url);
			playMP3 = Manager.createPlayer(mediaLocator);
		} catch (java.io.IOException e) {
			System.out.println(e.getMessage());
		} catch (javax.media.NoPlayerException e) {
			System.out.println(e.getMessage());
		}

		playMP3.addControllerListener(new ControllerListener() {
			public void controllerUpdate(ControllerEvent e) {
				if (e instanceof EndOfMediaEvent) {
					playMP3.stop();
					playMP3.close();
				}
			}
		});
		playMP3.realize();
		playMP3.start();
	}
}
