package com.example.testmediacodec.dvr;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by QJMOTOR on 2024/11/11.
 */
public class TUtil {

    public void reencodeSurfaceTexture(SurfaceTexture surfaceTexture, String outputPath)  {
        try {
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            MediaCodec mediaCodec = MediaCodec.createEncoderByType("video/avc");

            // 配置编解码器的输入和输出格式
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", 640, 480);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1250000);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);

            Surface surface = new Surface(surfaceTexture);
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.setInputSurface(surface);

            mediaCodec.start();

            // 创建输出文件
            File outputFile = new File(outputPath);
            FileOutputStream fos = new FileOutputStream(outputFile);
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);

            MediaMuxer mediaMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int trackIndex = mediaMuxer.addTrack(mediaFormat);
            mediaMuxer.start();
            while (true) {
                int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // 输出格式发生变化，可以获取并使用新格式
                    MediaFormat newFormat = mediaCodec.getOutputFormat();
                    mediaMuxer.addTrack(newFormat);
                } else if (outputBufferId == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // 输出缓冲区发生变化
                } else if (outputBufferId < 0) {
                    // 无可用输出
                    if (outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // 无可用输出，稍后再试
                    } else if (outputBufferId == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        // 输出缓冲区发生变化
                    }
                } else {
                    // 有编码数据输出
                    ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferId);
                    int chunkSize = bufferInfo.size;
                    buffer.clear();
                    buffer.put(outputBuffer.array(), bufferInfo.offset, chunkSize);
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        fos.getChannel().write(buffer);
                    }
                    mediaCodec.releaseOutputBuffer(outputBufferId, false);
                }
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }
            mediaMuxer.stop();
            mediaMuxer.release();
        }catch (IOException e){
            Log.d("TAG24", "??????"+ e.getMessage());
        }
    }
}
