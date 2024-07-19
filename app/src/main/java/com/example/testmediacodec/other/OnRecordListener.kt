package com.yjx.baselib.tools.camera2

/**
 * @author: QiuYunLiang
 * @date: 2022/11/10
 * @description:
 */

interface OnRecordListener {
    /**
     * 录制完成
     */
    fun onRecordFinish(path: String)

    /**
     * 录制失败
     */
    fun onRecordError(error: String?)

    /**
     * 录制开始
     */
    fun onRecordStart()
}