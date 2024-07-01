package com.blog.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 评论表
 * </p>
 *
 * @author 苍晖
 * @since 2024-07-01
 */
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论id，主键自增
     */
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Integer commentId;

    /**
     * 文章id
     */
    private Integer articleId;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 评论ip
     */
    private String commentIp;

    /**
     * 评论内容
     */
    private String commentContent;

    /**
     * 评论审核
     */
    private Integer commentRole;

    /**
     * 评论父id，实现多级评论
     */
    private Integer parentId;

    /**
     * 评论时间
     */
    private LocalDateTime createdAt;

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCommentIp() {
        return commentIp;
    }

    public void setCommentIp(String commentIp) {
        this.commentIp = commentIp;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public Integer getCommentRole() {
        return commentRole;
    }

    public void setCommentRole(Integer commentRole) {
        this.commentRole = commentRole;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Comment{" +
            "commentId = " + commentId +
            ", articleId = " + articleId +
            ", userId = " + userId +
            ", commentIp = " + commentIp +
            ", commentContent = " + commentContent +
            ", commentRole = " + commentRole +
            ", parentId = " + parentId +
            ", createdAt = " + createdAt +
        "}";
    }
}
