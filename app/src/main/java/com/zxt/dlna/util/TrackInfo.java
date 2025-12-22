package com.zxt.dlna.util;

public class TrackInfo {
    private int duration;
    private boolean isMQA;
    private int audioType;
    private int mQAUiState;
    private int mQAOutputSampleRate;
    private int channels;
    private int samplerate;
    private int bitrate;
    private int bitsPerSample;
    private String codec;
    private String mQAReplayGain;
    private String replayGainAlbum;

    public int getDuration() {
        return duration;
    }

    public boolean getisMQA() {
        return isMQA;
    }

    public int getAudioType() {
        return audioType;
    }

    public int getMQAUiState() {
        return mQAUiState;
    }

    public int getMQAOutputSampleRate() {
        return mQAOutputSampleRate;
    }

    public int getChannels() {
        return channels;
    }

    public int getSamplerate() {
        return samplerate;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getBitsPerSample() {
        return bitsPerSample;
    }

    public String getCodec() {
        return codec;
    }

    public String getMQAReplayGain() {
        return mQAReplayGain;
    }

    public String getReplayGainAlbum() {
        return replayGainAlbum;
    }
}