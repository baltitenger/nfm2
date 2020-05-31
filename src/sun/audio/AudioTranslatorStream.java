/*
 * Decompiled with CFR 0.149.
 */
package sun.audio;

import java.io.IOException;
import java.io.InputStream;
import sun.audio.InvalidAudioFormatException;
import sun.audio.NativeAudioStream;

public class AudioTranslatorStream
extends NativeAudioStream {
	private int length = 0;

	public AudioTranslatorStream(InputStream in) throws IOException {
		super(in);
		throw new InvalidAudioFormatException();
	}

	@Override
	public int getLength() {
		return this.length;
	}
}

