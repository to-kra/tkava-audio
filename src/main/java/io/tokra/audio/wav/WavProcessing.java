package io.tokra.audio.wav;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.tokra.audio.wav.WavInfo.FMT;
import io.tokra.audio.wav.WavInfo.RIFF;

/**
 * @author ToKra
 */
public class WavProcessing {
	
	private static final Logger logger = LoggerFactory.getLogger(WavProcessing.class);
	
	public static void saveInputStreamToWav(InputStream samples, String location) {
		try {
			/* File f = new File("C:/outFile.wav"); */
			File f = new File(location);
			f.createNewFile();
			OutputStream out = new FileOutputStream(f);
			byte buf[] = new byte[10000];
			int len;
			while ((len = samples.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			out.close();
			logger.debug("\n\tWav File : '{}' ...was created", location);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @author Tomas Kramaric
	 * @since Feb 24, 2015
	 * @param wavFile
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static WavInfo readWavFileSamples(File wavFile) throws IOException, URISyntaxException {
		StopWatch sw = new StopWatch();
		sw.start();
		long length = getFileLenght(wavFile);
		
		WavInfo wav = null;
		RIFF riff = null;
		FMT fmt = null;
	
		ByteBuffer buf = ByteBuffer.allocate((int)length);
		FileInputStream fis = new FileInputStream(wavFile);
		FileChannel fileChannel = fis.getChannel();
		while (fileChannel.read(buf) > 0) {
			buf.flip();
			while (buf.hasRemaining()) {
				String headerChunkId = getText(buf, 4);
				int headerChunkSize = getNumber(buf, 4);
				logger.trace("Header_ChunkId: '{}', Header_ChunkSize: '{}'", headerChunkId, headerChunkSize);

				switch (headerChunkId) {
				case "RIFF":
					riff = new RIFF(buf);
//					logger.debug("{}", riff);
					break;
				case "fmt ":
					fmt = WavInfo.FMT.getFmtChunk(buf);
//					logger.debug("{}", fmt);
					break;
				case "data":
					wav = processDataChunk(buf, headerChunkSize);
					wav.setRiff(riff);
					wav.setFmt(fmt);
					logger.debug("{}", wav);
					break;
				default:
					getNumber(buf, headerChunkSize);
					break;
				}
			}
			buf.clear();
		}
		fis.close();
		sw.stop();
		logger.info("ReadWavFileSamples... Runtime: '{}' ms", sw.getTime());
		return wav;
	}

	/**
	 * @author Tomas Kramaric
	 * @since Feb 24, 2015
	 * @param buf
	 * @param headerChunkSize
	 * @return
	 * @throws IOException
	 */
	protected static WavInfo processDataChunk(ByteBuffer buf, int headerChunkSize) throws IOException {
		StopWatch sw = new StopWatch();
		sw.start();
		short[] lowerBits = new short[headerChunkSize];
		short[] upperBits = new short[headerChunkSize];
		readBits(buf, lowerBits, upperBits);
		short[] samples = decodePCM16bit(lowerBits, upperBits);
		sw.stop();
		logger.info("Processing Data Chunk... Runtime: '{}' ms", sw.getTime());
		{ /* store results */
			WavInfo wav = new WavInfo();
			wav.setSamplesLowerBits(lowerBits);
			wav.setSamplesUpperBits(upperBits);
			wav.setDecodedSamples(samples);
			return wav;
		}
	}
	
	/**
	 * 
	 * @author ToKra
	 * @since Feb 24, 2015 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static long getFileLenght(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		try {
			int length = is.available();
			logger.debug("File path: '{}', Length: '{}'", file.getAbsolutePath(), length);
			return length;
		} finally {
			is.close();
		}
	}
	
	/**
	 * 
	 * @author ToKra
	 * @since Feb 23, 2015 
	 * @param byteBuffer
	 * @param arrayLength
	 * @return
	 * @throws IOException
	 */
	public static int getNumber(ByteBuffer byteBuffer, int arrayLength) throws IOException{
		int number = 0;
		for (int i = 0; i < arrayLength; i++) {
			byte read = byteBuffer.get();
			int readUnsigned = getUnsignedByte(read);
			number += (int) (Math.pow(2, i * 8) * readUnsigned);
		}
		return number;
	}
	
	/**
	 * 
	 * @author ToKra
	 * @since Feb 23, 2015 
	 * @param byteBuffer
	 * @param arrayLenght
	 * @return
	 * @throws IOException
	 */
	public static String getText(ByteBuffer byteBuffer, int arrayLenght) throws IOException{
		char[] array = new char[arrayLenght];
		for (int i = 0; i < arrayLenght; i++){
			char c = (char) byteBuffer.get();
			array[i] = c;
		}
		return new String(array);
	}
	
	/**
	 * 
	 * @author ToKra
	 * @since Feb 23, 2015 
	 * @param param
	 * @return
	 */
	public static short getUnsignedByte(byte param) {
	    return (short) (param & 0xFF);
	}
	
	/**
	 * 
	 * @author ToKra
	 * @since Feb 23, 2015 
	 * @param byteBuffer
	 * @param lowerBits
	 * @param upperBits
	 * @throws IOException
	 */
	public static void readBits(ByteBuffer byteBuffer, short[] lowerBits, short[] upperBits) throws IOException {
		StopWatch sw = new StopWatch();
		sw.start();
		for (int i = 0; i < lowerBits.length; i++) {
			if(byteBuffer.hasRemaining()){
				byte readLower = byteBuffer.get();
				byte readUpper = byteBuffer.get();
				short readLowerUnsigned = getUnsignedByte(readLower);
				short readUpperUnsigned = getUnsignedByte(readUpper);
				lowerBits[i] = readLowerUnsigned;
				upperBits[i] = readUpperUnsigned;
			} else {
				break;
			}
		}
		logger.trace("Read LowerBits: '{}', Read UpperBits: '{}'", lowerBits.length, upperBits.length);
		sw.stop();
		logger.debug("Reading data bits... Runtime: '{}' ms", sw.getTime());
	}
	
	/**
	 * Decodes PCM data to samples
	 * 
	 * @author ToKra
	 * @since Feb 23, 2015 
	 * @param lower
	 * @param upper
	 * @return
	 */
	public static short[] decodePCM16bit(short[] lower, short[] upper){
		StopWatch sw = new StopWatch();
		sw.start();
		short[] samples = new short[lower.length];
		for(int i = 0; i < lower.length; i++){
			short sampleLittle = (short) ((lower[i] & 0xFF) | (upper[i] << 8)); //http://www.jsresources.org/faq_audio.html#reconstruct_samples
			samples[i] = sampleLittle;
//			logger.trace("Sample: '{}', Value: '{}'", i, sampleLittle);
		}
		sw.stop();
		logger.debug("Decode PCM... Runtime: '{}' ms", sw.getTime());
		return samples;
	}

}
