package cloneproject.Instagram.dto.chat;

import cloneproject.Instagram.dto.member.MenuMemberDTO;
import cloneproject.Instagram.entity.chat.MessageImage;
import cloneproject.Instagram.entity.chat.MessagePost;
import cloneproject.Instagram.entity.chat.MessageText;
import cloneproject.Instagram.vo.Image;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {

    private Long roomId;
    private Long messageId;
    private Long senderId;
    private Image senderImage;
    private Object content;
    private String messageType;
    private LocalDateTime messageDate;

    public MessageDTO(MessagePost message) {
        this.roomId = message.getRoom().getId();
        this.messageId = message.getId();
        this.senderId = message.getMember().getId();
        this.senderImage = message.getMember().getImage();
        this.messageDate = message.getCreatedDate();
        this.messageType = message.getDtype();
        this.content = MessagePostDTO.builder()
                .status(message.getPost() == null ? "DELETED" : "UPLOADED")
                .postId(message.getPost().getId())
                .postImage(message.getPost().getPostImages().get(0).getImage())
                .postImageCount(message.getPost().getPostImages().size())
                .uploader(new MenuMemberDTO(message.getMember()))
                .build();
    }

    public MessageDTO(MessageText message) {
        this.roomId = message.getRoom().getId();
        this.messageId = message.getId();
        this.senderId = message.getMember().getId();
        this.senderImage = message.getMember().getImage();
        this.messageDate = message.getCreatedDate();
        this.messageType = message.getDtype();
        this.content = message.getContent();
    }

    public MessageDTO(MessageImage message) {
        this.roomId = message.getRoom().getId();
        this.messageId = message.getId();
        this.senderId = message.getMember().getId();
        this.senderImage = message.getMember().getImage();
        this.messageDate = message.getCreatedDate();
        this.messageType = message.getDtype();
        this.content = message.getImage();
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessagePostDTO {

        private String status;
        private Long postId;
        private Image postImage;
        private Integer postImageCount;
        private MenuMemberDTO uploader;
    }
}
