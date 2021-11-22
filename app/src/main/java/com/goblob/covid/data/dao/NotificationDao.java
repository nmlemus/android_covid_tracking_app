package com.goblob.covid.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import com.goblob.covid.data.dao.model.Notification;

import java.util.List;

@Dao
public interface NotificationDao {
    @Query("SELECT * FROM notification where (fromProfileId= :fromProfileId and toProfileId= :toProfileId) or (fromProfileId= :toProfileId and toProfileId= :fromProfileId) order by local_create desc limit :limit")
    List<Notification> getNotifications(String fromProfileId, String toProfileId, int limit);

    @Query("SELECT * FROM notification order by local_create desc limit :limit")
    List<Notification> getLastNotifications(int limit);

    @Query("SELECT * FROM notification where (fromProfileId= :fromProfileId and toProfileId= :toProfileId) or (fromProfileId= :toProfileId and toProfileId= :fromProfileId) order by local_create desc limit :limit")
    Notification getLastMessage(String fromProfileId, String toProfileId, int limit);

    @Query("SELECT * FROM notification where (messageType = 'IMAGE_MESSAGE' and (fromProfileId= :fromProfileId and toProfileId= :toProfileId) or (fromProfileId= :toProfileId and toProfileId= :fromProfileId)) order by local_create")
    List<Notification> getImages(String fromProfileId, String toProfileId);

    @Query("DELETE FROM notification where (fromProfileId= :fromProfileId and toProfileId= :toProfileId) or (fromProfileId= :toProfileId and toProfileId= :fromProfileId)")
    void deleteMessage(String fromProfileId, String toProfileId);

    @Query("DELETE FROM notification where (uid= :uid)")
    void deleteMessageByUID(String uid);

    @Query("SELECT COUNT(*) from notification WHERE messageId = :messageId")
    int count(String messageId);

    @Query("SELECT COUNT(*) from notification where (fromProfileId= :fromProfileId and toProfileId= :toProfileId) or (fromProfileId= :toProfileId and toProfileId= :fromProfileId)")
    int countAll(String fromProfileId, String toProfileId);

    @Query("SELECT COUNT(*) from notification where (status= 'UNREAD')")
    int countUnreadNotifications();

    @Insert
    long insert(Notification notification);

    @Update
    void update(Notification notification);

    @Query("UPDATE notification SET received_at=:received_at WHERE uid = :uid")
    void update(long uid, String received_at);

    @Query("UPDATE notification SET status= 'READ' WHERE fromProfileId= :toProfileId")
    void updateStatus(String toProfileId);

    @Insert
    long[] insert(Notification... notifications);

    @Query("SELECT * FROM notification WHERE uid = :uid")
    Notification getMessage(long uid);

    @Query("SELECT * FROM notification WHERE downloadId = :downloadId")
    Notification getMessageByDownloadID(long downloadId);

}
