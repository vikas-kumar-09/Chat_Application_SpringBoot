package in.vikas.chatapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import in.vikas.chatapp.model.ChatMessage;
import in.vikas.chatapp.repository.ChatMessageRepository;

/**
 * Handles the WebSocket disconnect event.
 * Sends a leave message to the "/topic/public" destination when a user disconnects.
 *
 * @param event The SessionDisconnectEvent representing the WebSocket disconnect event.
 */
@Component
public class WebSocketEventListener {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            logger.info("User disconnected: {}", username);
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);

            // Save leave message to the database
            chatMessageRepository.save(chatMessage);

            // Uncomment the following lines if you want to send a message to "/topic/public" upon disconnect
            // messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}
