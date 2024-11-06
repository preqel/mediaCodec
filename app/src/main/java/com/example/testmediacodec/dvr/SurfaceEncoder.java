package com.example.testmediacodec.dvr;

import static android.util.Log.VERBOSE;
import static com.example.testmediacodec.util.LogUtil.TAG;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class SurfaceEncoder {

    static SurfaceEncoder mSufaceEncoder;
    private FileOutputStream mOuputFile;
    private MediaCodec mEncoder;
    private long mVideoStartStampUs;

    boolean VERBOSE = true;

    private MediaMuxer mMuxer;
    private int mVideoTrackIndex;
    private MediaCodec.BufferInfo mBufferInfo;
    private boolean mMuxerStarted;
    private Object sMuxSync = new Object();

    public static SurfaceEncoder getInstance(){
        if(mSufaceEncoder== null){
            mSufaceEncoder = new SurfaceEncoder();
        }
        return mSufaceEncoder;
    }

    public boolean isRecording(){
//todo
        return false;
    }


    public void drainAllEncoderMuxer(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        if (VERBOSE) Log.d(TAG, "drainAllEncoderMuxer(" + endOfStream + ")");

        if (endOfStream) {
            if (VERBOSE) Log.d(TAG, "sending EOS to mEncoder");
            mEncoder.signalEndOfInputStream();
        }
        //recordAndMuxingAudio();
        try {
            if(mOuputFile==null)
                mOuputFile = new FileOutputStream(Environment.getExternalStorageDirectory()+"/test.h264");


            ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
            while (true) {
                int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                Log.v(TAG, "bufferInfo f:" + mBufferInfo.flags + "\tpts:" + mBufferInfo.presentationTimeUs);
                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    Log.v(TAG,"INFO_TRY_AGAIN_LATER");
                    // no output available yet
                    if (!endOfStream) {
                        break;      // out of while
                    } else {
                        if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                    }
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not expected for an mEncoder
                    encoderOutputBuffers = mEncoder.getOutputBuffers();
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    Log.i(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
                    // should happen before receiving buffers, and should only happen once
                    if (mMuxerStarted) {
                        throw new RuntimeException("format changed twice");
                    }
                    MediaFormat newFormat = mEncoder.getOutputFormat();
                    Log.d(TAG, "mEncoder output format changed: " + newFormat);
                    if(mMuxer!=null){
                        mVideoTrackIndex = mMuxer.addTrack(newFormat);
                        //tryStartMuxer();  这里本来是没注掉的
                        mMuxer.start();
                    }
                    if(mOuputFile!=null){
                        ByteBuffer sps = newFormat.getByteBuffer("csd-0");
                        ByteBuffer pps = newFormat.getByteBuffer("csd-1");
                        mOuputFile.write(sps.array());
                        mOuputFile.write(pps.array());
                    }
                } else if (encoderStatus < 0) {
                    Log.w(TAG, "unexpected result from mEncoder.dequeueOutputBuffer: " +
                            encoderStatus);
                } else {
                    ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                    if (encodedData == null) {
                        throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                                " was null");
                    }

                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        // @wei The codec config data was pulled out and fed to the muxer when we got
                        // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                        if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                        mBufferInfo.size = 0;   //or continue
                    }

                    if (mBufferInfo.size != 0 /*&& mBufferInfo.presentationTimeUs!=0*/) {   //pts is for test todo  the first frame
                        // adjust the ByteBuffer values to match BufferInfo (not needed?)
                        encodedData.position(mBufferInfo.offset);
                        encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                        if(mVideoStartStampUs==0){
                            mBufferInfo.presentationTimeUs = 0;
                            mVideoStartStampUs = System.currentTimeMillis() * 1000;
                        }else
                            mBufferInfo.presentationTimeUs = System.currentTimeMillis() * 1000 - mVideoStartStampUs;
                        //mBufferInfo.presentationTimeUs - mVideoStartStampUs;
                        if(mMuxer!=null && mMuxerStarted)
                            mMuxer.writeSampleData(mVideoTrackIndex, encodedData, mBufferInfo);
                        if(mOuputFile !=null){
                            byte[] outData = new byte[mBufferInfo.size];                                //copy protected buffer.
                            encodedData.get(outData);
                            mOuputFile.write(outData);
                        }
                        if (VERBOSE)
                            Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer, ts=" +mBufferInfo.presentationTimeUs);
                    }

                    mEncoder.releaseOutputBuffer(encoderStatus, false);

                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (!endOfStream) {
                            Log.w(TAG, "reached end of stream unexpectedly");
                        } else {
                            if (VERBOSE) Log.d(TAG, "end of stream reached");
                        }
                        break;      // out of while
                    }
                }//END OF WHILE true
            }
        } catch (IOException e) {
            e.printStackTrace();
        }                                                                //不给音频单独线程.
        if(mMuxerStarted){
            synchronized (sMuxSync){
                sMuxSync.notifyAll();
            }
        }
/*        if(stopCounterTest++ >=400){
            stopCounterTest = 0;
            mMuxer.stop();
            mMuxer.release();
        }*/
    }
}
