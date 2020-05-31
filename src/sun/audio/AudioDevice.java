/*
 * Decompiled with CFR 0.149.
 */
package sun.audio;

import com.sun.media.sound.DataPusher;
import com.sun.media.sound.Toolkit;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import sun.audio.AudioDataStream;
import sun.audio.AudioStream;
import sun.audio.ContinuousAudioDataStream;

public class AudioDevice {
	private boolean DEBUG = false;
	private Hashtable clipStreams = new Hashtable();
	private Vector infos = new Vector();
	private boolean playing = false;
	private Mixer mixer = null;
	public static final AudioDevice device = new AudioDevice();

	private AudioDevice() {
	}

	private synchronized void startSampled(AudioInputStream as, InputStream in) throws UnsupportedAudioFileException, LineUnavailableException {
		Info info = null;
		DataPusher datapusher = null;
		DataLine.Info lineinfo = null;
		SourceDataLine sourcedataline = null;
		if ((as = Toolkit.getPCMConvertedAudioInputStream(as)) == null) {
			return;
		}
		lineinfo = new DataLine.Info(SourceDataLine.class, as.getFormat());
		if (!AudioSystem.isLineSupported(lineinfo)) {
			return;
		}
		sourcedataline = (SourceDataLine)AudioSystem.getLine(lineinfo);
		datapusher = new DataPusher(sourcedataline, as);
		info = new Info(null, in, datapusher);
		this.infos.addElement(info);
		datapusher.start();
	}

	private synchronized void startMidi(InputStream bis, InputStream in) throws InvalidMidiDataException, MidiUnavailableException {
		Sequencer sequencer = null;
		Info info = null;
		sequencer = MidiSystem.getSequencer();
		sequencer.open();
		try {
			sequencer.setSequence(bis);
		}
		catch (IOException e) {
			throw new InvalidMidiDataException(e.getMessage());
		}
		info = new Info(sequencer, in, null);
		this.infos.addElement(info);
		sequencer.addMetaEventListener(info);
		sequencer.start();
	}

	public synchronized void openChannel(InputStream in) {
		if (this.DEBUG) {
			System.out.println("AudioDevice: openChannel");
			System.out.println("input stream =" + in);
		}
		Info info = null;
		for (int i = 0; i < this.infos.size(); ++i) {
			info = (Info)this.infos.elementAt(i);
			if (info.in != in) continue;
			return;
		}
		AudioInputStream as = null;
		if (in instanceof AudioStream) {
			if (((AudioStream)in).midiformat != null) {
				try {
					this.startMidi(((AudioStream)in).stream, in);
				}
				catch (Exception e) {
					return;
				}
			} else if (((AudioStream)in).ais != null) {
				try {
					this.startSampled(((AudioStream)in).ais, in);
				}
				catch (Exception e) {
					return;
				}
			}
		} else if (in instanceof AudioDataStream) {
			if (in instanceof ContinuousAudioDataStream) {
				try {
					AudioInputStream ais = new AudioInputStream(in, ((AudioDataStream)in).getAudioData().format, -1L);
					this.startSampled(ais, in);
				}
				catch (Exception e) {
					return;
				}
			} else {
				try {
					AudioInputStream ais = new AudioInputStream(in, ((AudioDataStream)in).getAudioData().format, ((AudioDataStream)in).getAudioData().buffer.length);
					this.startSampled(ais, in);
				}
				catch (Exception e) {
					return;
				}
			}
		} else {
			BufferedInputStream bis = new BufferedInputStream(in, 1024);
			try {
				try {
					as = AudioSystem.getAudioInputStream(bis);
				}
				catch (IOException ioe) {
					return;
				}
				this.startSampled(as, in);
			}
			catch (UnsupportedAudioFileException e) {
				try {
					try {
						MidiFileFormat midiFileFormat = MidiSystem.getMidiFileFormat(bis);
					}
					catch (IOException ioe1) {
						return;
					}
					this.startMidi(bis, in);
				}
				catch (InvalidMidiDataException e1) {
					AudioFormat defformat = new AudioFormat(AudioFormat.Encoding.ULAW, 8000.0f, 8, 1, 1, 8000.0f, true);
					try {
						AudioInputStream defaif = new AudioInputStream(bis, defformat, -1L);
						this.startSampled(defaif, in);
					}
					catch (UnsupportedAudioFileException es) {
						return;
					}
					catch (LineUnavailableException es2) {
						return;
					}
				}
				catch (MidiUnavailableException e2) {
					return;
				}
			}
			catch (LineUnavailableException e) {
				return;
			}
		}
		this.notify();
	}

	public synchronized void closeChannel(InputStream in) {
		if (this.DEBUG) {
			System.out.println("AudioDevice.closeChannel");
		}
		if (in == null) {
			return;
		}
		for (int i = 0; i < this.infos.size(); ++i) {
			Info info = (Info)this.infos.elementAt(i);
			if (info.in != in) continue;
			if (info.sequencer != null) {
				info.sequencer.stop();
				this.infos.removeElement(info);
				continue;
			}
			if (info.datapusher == null) continue;
			info.datapusher.stop();
			this.infos.removeElement(info);
		}
		this.notify();
	}

	public synchronized void open() {
	}

	public synchronized void close() {
	}

	public void play() {
		if (this.DEBUG) {
			System.out.println("exiting play()");
		}
	}

	public synchronized void closeStreams() {
		for (int i = 0; i < this.infos.size(); ++i) {
			Info info = (Info)this.infos.elementAt(i);
			if (info.sequencer != null) {
				info.sequencer.stop();
				info.sequencer.close();
				this.infos.removeElement(info);
				continue;
			}
			if (info.datapusher == null) continue;
			info.datapusher.stop();
			this.infos.removeElement(info);
		}
		if (this.DEBUG) {
			System.err.println("Audio Device: Streams all closed.");
		}
		this.clipStreams = new Hashtable();
		this.infos = new Vector();
	}

	public int openChannels() {
		return this.infos.size();
	}

	void setVerbose(boolean v) {
		this.DEBUG = v;
	}

	class Info
	implements MetaEventListener {
		Sequencer sequencer;
		InputStream in;
		DataPusher datapusher;

		Info(Sequencer sequencer, InputStream in, DataPusher datapusher) {
			this.sequencer = sequencer;
			this.in = in;
			this.datapusher = datapusher;
		}

		@Override
		public void meta(MetaMessage event) {
			if (event.getType() == 47 && this.sequencer != null) {
				this.sequencer.close();
			}
		}
	}
}

