package io.tokra.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioFormatConverter {

	public AudioFormatConverter(AudioInputStream ais) {
		logger.debug("FormatConverter()");
		setOriginalAIS(ais);
		setOriginalFormat(getOriginalAIS().getFormat());
		setAlaw(checkWhetherAlaw(getOriginalFormat()));
		setUlaw(checkWhetherUlaw(getOriginalFormat()));
		setPCM_SIGNED(checkWhetherPCM_SIGNED(getOriginalFormat()));
		setPCM_UNSIGNED(checkWhetherPCM_UNSIGNED(getOriginalFormat()));
		logger.info(toStringOriginalFormat());
	}

	private AudioFormat originalFormat, newFormat;
	private AudioInputStream originalAIS, convertedAIS;
	private static final Logger logger = LoggerFactory
			.getLogger(AudioFormatConverter.class);
	private boolean Alaw, Ulaw, PCM_SIGNED, PCM_UNSIGNED;

	public boolean checkWhetherAlaw(AudioFormat format) {
		return (format.getEncoding() == AudioFormat.Encoding.ALAW) ? true
				: false;
	}

	public boolean checkWhetherUlaw(AudioFormat format) {
		return (format.getEncoding() == AudioFormat.Encoding.ULAW) ? true
				: false;
	}

	public boolean checkWhetherPCM_SIGNED(AudioFormat format) {
		return (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) ? true
				: false;
	}

	public boolean checkWhetherPCM_UNSIGNED(AudioFormat format) {
		return (format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED) ? true
				: false;
	}

	public void convertAlawUlawToPCM() {

		if (this.isAlaw() == true || this.isUlaw() == true) {

			AudioFormat newFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED, this.getOriginalFormat()
							.getSampleRate(), this.getOriginalFormat()
							.getSampleSizeInBits() * 2, this
							.getOriginalFormat().getChannels(), this
							.getOriginalFormat().getFrameSize() * 2, this
							.getOriginalFormat().getFrameRate(), true);
			this.setNewFormat(newFormat);

			try {
				logger.debug("\nConversion from:\n\t{}\nto:\n\t{}\n",
						this.getOriginalFormat(), this.getNewFormat());
				this.setConvertedAIS(this.getOriginalAIS());
				this.setConvertedAIS(AudioSystem.getAudioInputStream(
						this.getNewFormat(), this.getOriginalAIS()));

			} catch (IllegalArgumentException e) {
				logger.error("Unsupported conversion: {}", e.getMessage());
				return;
			}
		}
		logger.debug("Conversion done !");
		return;
	}

	public boolean convertAnyToPCM() {

		if (this.isPCM_SIGNED() == false || this.isPCM_UNSIGNED() == false) {

			AudioFormat newFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED, this.getOriginalFormat()
							.getSampleRate(), 16, this.getOriginalFormat()
							.getChannels(), this.getOriginalFormat()
							.getChannels() * 2, this.getOriginalFormat()
							.getSampleRate(), this.getOriginalFormat()
							.isBigEndian());
			this.setNewFormat(newFormat);

			try {
				logger.debug("\nConversion from:\n\t{}\nto:\n\t{}\n",
						this.getOriginalFormat(), this.getNewFormat());
				this.setConvertedAIS(this.getOriginalAIS());
				this.setConvertedAIS(AudioSystem.getAudioInputStream(
						this.getNewFormat(), this.getOriginalAIS()));

			} catch (IllegalArgumentException e) {
				logger.error("Unsupported conversion: {}", e.getMessage());
				return false;
			}
			logger.debug("Conversion done !");
			return true;
		}
		return false;
	}

	public AudioFormat getOriginalFormat() {
		return originalFormat;
	}

	private void setOriginalFormat(AudioFormat originalFormat) {
		this.originalFormat = originalFormat;
	}

	public AudioInputStream getOriginalAIS() {
		return originalAIS;
	}

	private void setOriginalAIS(AudioInputStream originalAIS) {
		this.originalAIS = originalAIS;
	}

	public AudioFormat getNewFormat() {
		return newFormat;
	}

	public void setNewFormat(AudioFormat newFormat) {
		this.newFormat = newFormat;
	}

	public AudioInputStream getNewAIS() {
		return convertedAIS;
	}

	public void setConvertedAIS(AudioInputStream newAIS) {
		this.convertedAIS = newAIS;
	}

	public boolean isAlaw() {
		return Alaw;
	}

	private void setAlaw(boolean alaw) {
		Alaw = alaw;
	}

	public boolean isUlaw() {
		return Ulaw;
	}

	private void setUlaw(boolean ulaw) {
		Ulaw = ulaw;
	}

	public boolean isPCM_SIGNED() {
		return PCM_SIGNED;
	}

	private void setPCM_SIGNED(boolean pCM_SIGNED) {
		PCM_SIGNED = pCM_SIGNED;
	}

	public boolean isPCM_UNSIGNED() {
		return PCM_UNSIGNED;
	}

	private void setPCM_UNSIGNED(boolean pCM_UNSIGNED) {
		PCM_UNSIGNED = pCM_UNSIGNED;
	}

	public String toStringOriginalFormat() {
		StringBuffer sb = new StringBuffer();
		sb.append("Original format:");
		sb.append(this.getOriginalFormat().toString());
		return sb.toString();
	}

	public String toStringNewFormat() {
		StringBuffer sb = new StringBuffer();
		sb.append("New format:");
		sb.append(this.getNewFormat().toString());
		return sb.toString();
	}
}
