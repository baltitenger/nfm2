/*
 * Decompiled with CFR 0.149.
 */
package sun.audio;

import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.audio.AudioDevice;

public class AudioPlayer
extends Thread {
	private AudioDevice devAudio;
	private static boolean DEBUG = false;
	public static final AudioPlayer player = AudioPlayer.getAudioPlayer();

	private static ThreadGroup getAudioThreadGroup() {
		if (DEBUG) {
			System.out.println("AudioPlayer.getAudioThreadGroup()");
		}
		ThreadGroup g = AudioPlayer.currentThread().getThreadGroup();
		while (g.getParent() != null && g.getParent().getParent() != null) {
			g = g.getParent();
		}
		return g;
	}

	private static AudioPlayer getAudioPlayer() {
		if (DEBUG) {
			System.out.println("> AudioPlayer.getAudioPlayer()");
		}
		PrivilegedAction action = new PrivilegedAction(){

			public Object run() {
				AudioPlayer t = new AudioPlayer();
				t.setPriority(10);
				t.setDaemon(true);
				t.start();
				return t;
			}
		};
		AudioPlayer audioPlayer = (AudioPlayer)AccessController.doPrivileged(action);
		return audioPlayer;
	}

	private AudioPlayer() {
		super(AudioPlayer.getAudioThreadGroup(), "Audio Player");
		if (DEBUG) {
			System.out.println("> AudioPlayer private constructor");
		}
		this.devAudio = AudioDevice.device;
		this.devAudio.open();
		if (DEBUG) {
			System.out.println("< AudioPlayer private constructor completed");
		}
	}

	public synchronized void start(InputStream in) {
		if (DEBUG) {
			System.out.println("> AudioPlayer.start");
			System.out.println("  InputStream = " + in);
		}
		this.devAudio.openChannel(in);
		this.notify();
		if (DEBUG) {
			System.out.println("< AudioPlayer.start completed");
		}
	}

	public synchronized void stop(InputStream in) {
		if (DEBUG) {
			System.out.println("> AudioPlayer.stop");
		}
		this.devAudio.closeChannel(in);
		if (DEBUG) {
			System.out.println("< AudioPlayer.stop completed");
		}
	}

	@Override
	public void run() {
		this.devAudio.play();
		if (DEBUG) {
			System.out.println("AudioPlayer mixing loop.");
		}
		try {
			do {
				Thread.sleep(5000L);
			} while (true);
		}
		catch (Exception e) {
			if (DEBUG) {
				System.out.println("AudioPlayer exited.");
			}
			return;
		}
	}
}

