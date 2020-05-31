/*
 * Decompiled with CFR 0.149.
 */
package sun.audio;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NativeAudioStream
extends FilterInputStream {
	public NativeAudioStream(InputStream in) throws IOException {
		super(in);
	}

	public int getLength() {
		return 0;
	}
}

