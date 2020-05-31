/*
 * Decompiled with CFR 0.149.
 */
package sun.audio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioData {
	private static final AudioFormat DEFAULT_FORMAT = new AudioFormat(AudioFormat.Encoding.ULAW, 8000.0f, 8, 1, 1, 8000.0f, true);
	AudioFormat format;
	byte[] buffer;

	public AudioData(byte[] buffer) {
		this.buffer = buffer;
		this.format = DEFAULT_FORMAT;
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(buffer));
			this.format = ais.getFormat();
			ais.close();
		}
		catch (IOException iOException) {
		}
		catch (UnsupportedAudioFileException unsupportedAudioFileException) {
			// empty catch block
		}
	}

	AudioData(AudioFormat format, byte[] buffer) {
		this.format = format;
		this.buffer = buffer;
	}
}

