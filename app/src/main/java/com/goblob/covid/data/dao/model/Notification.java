package com.goblob.covid.data.dao.model;

import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.goblob.covid.utils.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Entity(tableName = "notification")
public class Notification {
    @PrimaryKey(autoGenerate = true)
    private long uid;
    @ColumnInfo(name = "fromProfileId")
    private String fromProfileId;
    @ColumnInfo(name = "toProfileId")
    private String toProfileId;
    @ColumnInfo(name = "messageType")
    private String messageType;
    @ColumnInfo(name = "messageText")
    private String messageText;
    @ColumnInfo(name = "toUserID")
    private String toUserID;
    @ColumnInfo(name = "created_at")
    private String created_at;
    @ColumnInfo(name = "received_at")
    private String received_at;
    @ColumnInfo(name = "sent_at")
    private String sent_at;
    @ColumnInfo(name = "messageId")
    private String messageId;
    @ColumnInfo(name = "fromUserID")
    private String fromUserID;
    @ColumnInfo(name = "status")
    private String status;
    @ColumnInfo(name = "local_create")
    private String local_create;
    @ColumnInfo(name = "end_time")
    private String endTime;
    @ColumnInfo(name = "start_time")
    private String startTime;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "remote_file")
    private String remoteFile;
    @ColumnInfo(name = "local_file")
    private String localFile;
    @ColumnInfo(name = "duration")
    private String duration;
    @Ignore
    private Date createdAt;

    @ColumnInfo(name="downloadId")
    private long downloadId;


    public Notification(String text) {
        this.messageText = text;
        this.createdAt = createdAt;
        messageId = UUID.randomUUID().toString();
        local_create = TimeUtil.getUTCTime();;
    }

    public Notification() {
        messageId = UUID.randomUUID().toString();
        local_create = TimeUtil.getUTCTime();
        createdAt = new Date();
    }

    public Notification(Parcel in) {
        // String[] data = new String[13];
        // in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.uid = in.readLong();
        this.fromProfileId = in.readString();
        this.toProfileId = in.readString();
        this.messageType = in.readString();
        this.messageText = in.readString();
        this.toUserID = in.readString();
        this.created_at = in.readString();
        this.received_at = in.readString();
        this.sent_at = in.readString();
        this.messageId = in.readString();
        this.fromUserID = in.readString();
        this.status = in.readString();
        this.local_create = in.readString();
        this.downloadId = in.readLong();
        // this.user = in.readParcelable(User.class.getClassLoader());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getLocalFile() {
        return localFile;
    }

    public void setLocalFile(String localFile) {
        this.localFile = localFile;
    }

    public String getRemoteFile() {
        return remoteFile;
    }

    public void setRemoteFile(String remoteFile) {
        this.remoteFile = remoteFile;
    }

    public String getLocal_create() {
        return local_create;
    }

    public void setLocal_create(String local_create) {
        this.local_create = local_create;
    }


    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setFromProfileId(String fromProfileId) {
        this.fromProfileId = fromProfileId;
    }

    public String getFromProfileId() {
        return fromProfileId;
    }

    public void setToProfileId(String toProfileId) {
        this.toProfileId = toProfileId;
    }

    public String getToProfileId() {
        return toProfileId;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageText() {
        return messageText;
    }

    @Override
    public String toString() {
        return uid + "-----" + messageId + " = " + messageText + "---" + created_at + "---" + sent_at + "---" + received_at + "-----type: " + messageType + "\n";
    }

    public void setToUserID(String toUserID) {
        this.toUserID = toUserID;
    }

    public String getToUserID() {
        return toProfileId;
    }

    public void setReceived_at(String received_at) {
        this.received_at = received_at;
    }

    public String getReceived_at() {
        return received_at;
    }

    public void setSent_at(String sent_at) {
        this.sent_at = sent_at;
    }

    public String getSent_at() {
        return sent_at;
    }

    public int describeContents() {
        return 0;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setFromUserID(String fromUserID) {
        this.fromUserID = fromUserID;
    }

    public String getFromUserID() {
        return fromUserID;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getId() {
        return String.valueOf(uid);
    }

    public String getText() {
        return getMessageText();
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public static class Image {

        private String url;

        public Image(String url) {
            this.url = url;
        }
    }
}
