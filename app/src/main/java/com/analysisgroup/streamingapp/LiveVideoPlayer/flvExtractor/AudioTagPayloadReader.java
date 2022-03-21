package com.analysisgroup.streamingapp.LiveVideoPlayer.flvExtractor;

import android.util.Pair;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.extractor.TrackOutput;
import com.google.android.exoplayer2.util.CodecSpecificDataUtil;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.ParsableByteArray;

import java.util.Collections;

/**
 * Parses audio tags from an FLV stream and extracts AAC frames.
 */
/* package */ final class AudioTagPayloadReader extends TagPayloadReader {

  private static final int AUDIO_FORMAT_ALAW = 7;
  private static final int AUDIO_FORMAT_MLAW = 8;
  private static final int AUDIO_FORMAT_AAC = 10;

  private static final int AAC_PACKET_TYPE_SEQUENCE_HEADER = 0;
  private static final int AAC_PACKET_TYPE_AAC_RAW = 1;

  // State variables
  private boolean hasParsedAudioDataHeader;
  private boolean hasOutputFormat;
  private int audioFormat;

  public AudioTagPayloadReader(TrackOutput output) {
    super(output);
  }

  @Override
  public void seek() {
    // Do nothing.
  }

  @Override
  protected boolean parseHeader(ParsableByteArray data) throws UnsupportedFormatException {
    if (!hasParsedAudioDataHeader) {
      int header = data.readUnsignedByte();
      audioFormat = (header >> 4) & 0x0F;
      // TODO: Add support for MP3.
      if (audioFormat == AUDIO_FORMAT_ALAW || audioFormat == AUDIO_FORMAT_MLAW) {
        String type = audioFormat == AUDIO_FORMAT_ALAW ? MimeTypes.AUDIO_ALAW
            : MimeTypes.AUDIO_MLAW;
        int pcmEncoding = (header & 0x01) == 1 ? C.ENCODING_PCM_16BIT : C.ENCODING_PCM_8BIT;

        Format.Builder format = new Format.Builder();
        format.setId(null);
        format.setSampleMimeType(type);
        format.setCodecs(null);
        format.setAverageBitrate(Format.NO_VALUE);
        format.setMaxInputSize(Format.NO_VALUE);
        format.setChannelCount(1);
        format.setSampleRate(8000);
        format.setPcmEncoding(pcmEncoding);
        format.setInitializationData(null);
        format.setDrmInitData(null);
        format.setSelectionFlags(0);
        format.setLanguage(null);

        output.format(format.build());
        hasOutputFormat = true;
      } else if (audioFormat != AUDIO_FORMAT_AAC) {
        Throwable throwable = new Throwable();
        throw new UnsupportedFormatException("Audio format not supported: " + audioFormat, throwable, true, 0);
      }
      hasParsedAudioDataHeader = true;
    } else {
      // Skip header if it was parsed previously.
      data.skipBytes(1);
    }
    return true;
  }

  @Override
  protected void parsePayload(ParsableByteArray data, long timeUs) {
    int packetType = data.readUnsignedByte();
    if (packetType == AAC_PACKET_TYPE_SEQUENCE_HEADER && !hasOutputFormat) {
      // Parse the sequence header.
      byte[] audioSpecificConfig = new byte[data.bytesLeft()];
      data.readBytes(audioSpecificConfig, 0, audioSpecificConfig.length);
      Pair<Integer, Integer> audioParams = CodecSpecificDataUtil.parseAlacAudioSpecificConfig(
          audioSpecificConfig);

      Format.Builder format = new Format.Builder();
      format.setId(null);
      format.setSampleMimeType(MimeTypes.AUDIO_AAC);
      format.setCodecs(null);
      format.setAverageBitrate(Format.NO_VALUE);
      format.setMaxInputSize(Format.NO_VALUE);
      format.setChannelCount(audioParams.second);
      format.setSampleRate(audioParams.first);
      format.setInitializationData(Collections.singletonList(audioSpecificConfig));
      format.setDrmInitData(null);
      format.setSelectionFlags(0);
      format.setLanguage(null);

      output.format(format.build());
      hasOutputFormat = true;
    } else if (audioFormat != AUDIO_FORMAT_AAC || packetType == AAC_PACKET_TYPE_AAC_RAW) {
      int sampleSize = data.bytesLeft();
      output.sampleData(data, sampleSize);
      output.sampleMetadata(timeUs, C.BUFFER_FLAG_KEY_FRAME, sampleSize, 0, null);
    }
  }

}
