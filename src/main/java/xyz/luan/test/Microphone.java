package xyz.luan.test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.function.Consumer;

public class Microphone {

	boolean closed;
	private int sampleRate;
	private Consumer<byte[]> consumer;
	private TargetDataLine targetLine;

	public Microphone(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void listen(Consumer<byte[]> byteConsumer) {
		this.consumer = byteConsumer;
	}

	public void close() {
		closed = true;
		targetLine.close();
	}

	public void start() {
		AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
		DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);

		try {
			targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
			targetLine.open(format);
			targetLine.start();

			int numBytesRead;
			byte[] targetData = new byte[targetLine.getBufferSize() / 5];

			while (!closed) {
				numBytesRead = targetLine.read(targetData, 0, targetData.length);
				if (numBytesRead == -1) {
					break;
				}
				consumer.accept(targetData);
			}
		} catch (Exception e) {
			if (!closed) {
				throw new RuntimeException(e);
			}
		}
	}
}
