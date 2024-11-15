package com.example.testmediacodec.render

import android.content.Context

/**
 * @author: w.k
 * @date: 2024/11/15
 * @description:
 */
class ResourceReader {

    companion object{
        fun readTextFromRawFile(context: Context, rawFileId: Int): String {
            val stringBuffer = StringBuffer()
            var lineString: String?
            context.resources.openRawResource(rawFileId).bufferedReader().use { bufferedReader ->
                while (bufferedReader.readLine().also { lineString = it } != null) {
                    stringBuffer.append(lineString).append("\n")
                }
            }
            return stringBuffer.toString()
        }
    }


}