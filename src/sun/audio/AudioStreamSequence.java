/*
 * Decompiled with CFR 0.149.
 */
package sun.audio;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Enumeration;

public class AudioStreamSequence
extends SequenceInputStream {
	Enumeration e;
	InputStream in;

	public AudioStreamSequence(Enumeration e) {
		super(e);
	}
}

