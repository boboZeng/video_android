package com.video.live;

public enum IjkPlayerStatus {
    MEDIA_INFO_UNKNOWN(1, "Info undefine"),//未知資訊
    MEDIA_INFO_STARTED_AS_NEXT(2, "Info started as next"),//播放下一條
    MEDIA_INFO_VIDEO_RENDERING_START(3, "Info video rendering start"),//視訊開始整備中，準備渲染
    MEDIA_INFO_VIDEO_TRACK_LAGGING(700, "Info video track lagging"),//視訊日誌跟蹤
    MEDIA_INFO_BUFFERING_START(701, "Info buffering start"),//開始緩衝中 開始緩衝
    MEDIA_INFO_BUFFERING_END(702, "Info buffering end"),//緩衝結束
    MEDIA_INFO_NETWORK_BANDWIDTH(703, "Info network bandwidth"),//網路頻寬，網速方面
    MEDIA_INFO_BAD_INTERLEAVING(800, "Info bad interleaving"),
    MEDIA_INFO_NOT_SEEKABLE(801, "Info not seekable"),//不可設定播放位置，直播方面
    MEDIA_INFO_METADATA_UPDATE(802, "Info metadata update"),
    MEDIA_INFO_TIMED_TEXT_ERROR(900, "Info timed text error"),
    MEDIA_INFO_UNSUPPORTED_SUBTITLE(901, "Info subtitle unsupported"),//不支援字幕
    MEDIA_INFO_SUBTITLE_TIMED_OUT(902, "Info subtitle timed out"),//字幕超時
    MEDIA_INFO_VIDEO_INTERRUPT(-10000, "Info data interrupt"),//資料連線中斷，一般是視訊源有問題或者資料格式不支援，比如音訊不是AAC之類的
    MEDIA_INFO_VIDEO_ROTATION_CHANGED(10001, "Info video rendering"),//視訊方向改變，視訊選擇資訊
    MEDIA_INFO_AUDIO_RENDERING_START(10002, "Info audio rendering"),//音訊開始整備中
    MEDIA_ERROR_SERVER_DIED(100, "Error server died"),//服務掛掉，視訊中斷，一般是視訊源異常或者不支援的視訊型別。
    MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK(200, "Error not valid"),//資料錯誤沒有有效的回收
    MEDIA_ERROR_IO(-1004, "Error IO"),//IO 錯誤
    MEDIA_ERROR_MALFORMED(-1007, "Error malformed"),
    MEDIA_ERROR_UNSUPPORTED(-1010, "Error unsupported"),//資料不支援
    MEDIA_ERROR_TIMED_OUT(-110, "Error timed out"),//資料超時
    ;

    private int mErrorCode = -1;
    private String mErrorMessage = "";

    IjkPlayerStatus(int errorCode, String errorMessage) {
        this.mErrorCode = errorCode;
        this.mErrorMessage = errorMessage;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public void setErrorCode(int mErrorCode) {
        this.mErrorCode = mErrorCode;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public void setErrorMessage(String mErrorMessage) {
        this.mErrorMessage = mErrorMessage;
    }

    public static String getErrorMessageByErrorCode(int code) {
        String result = "";
        for (IjkPlayerStatus item : IjkPlayerStatus.values()) {
            if (item.getErrorCode() == code) {
                result = item.getErrorMessage();
                break;
            }else {
                result = String.valueOf(code);
            }
        }
        return result;
    }
}
