package com.harsh.propertymanagementsystem.chat.repository;

import com.harsh.propertymanagementsystem.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m " +
           "WHERE (m.sender.id = :user1 AND m.receiver.id = :user2) " +
           "   OR (m.sender.id = :user2 AND m.receiver.id = :user1) " +
           "ORDER BY m.timestamp ASC")
    List<ChatMessage> findConversationBetweenUsers(
            @Param("user1") Long user1,
            @Param("user2") Long user2
    );

    List<ChatMessage> findByBookingIdOrderByTimestampAsc(Long bookingId);

    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.sender.id = :userId OR m.receiver.id = :userId " +
           "ORDER BY m.timestamp DESC")
    List<ChatMessage> findAllMessagesForUser(@Param("userId") Long userId);

    long countBySenderIdAndReceiverIdAndIsReadFalse(Long senderId, Long receiverId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true " +
           "WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.isRead = false")
    void markMessagesAsRead(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId
    );
}
