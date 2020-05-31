/*
 * Decompiled with CFR 0.149.
 */
package sun.audio;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import sun.audio.AudioData;

public class AudioStream
extends FilterInputStream {
	protected AudioInputStream ais = null;
	protected AudioFormat format = null;
	protected MidiFileFormat midiformat = null;
	protected InputStream stream = null;

	public AudioStream(InputStream in) throws IOException {
		super(in);
		this.stream = in;
		if (!in.markSupported()) {
			this.stream = new BufferedInputStream(in, 1024);
		}
		try {
			this.ais = AudioSystem.getAudioInputStream(this.stream);
			this.format = this.ais.getFormat();
			this.in = this.ais;
		}
		catch (UnsupportedAudioFileException e) {
			try {
				this.midiformat = MidiSystem.getMidiFileFormat(this.stream);
			}
			catch (InvalidMidiDataException e1) {
				throw new IOException("could not create audio stream from input stream");
			}
		}
	}

	public AudioData getData() throws IOException {
		int length = this.getLength();
		if (length < 0x100000) {
			byte[] buffer = new byte[length];
			try {
				this.ais.read(buffer, 0, length);
			}
			catch (IOException ex) {
				throw new IOException("Could not create AudioData Object");
			}
			return new AudioData(this.format, buffer);
		}
		throw new IOException("could not create AudioData object");
	}

	public int getLength() {
		if (this.ais != null && this.format != null) {
			return (int)(this.ais.getFrameLength() * (long)this.ais.getFormat().getFrameSize());
		}
		if (this.midiformat != null) {
			return this.midiformat.getByteLength();
		}
		return -1;
	}
}

