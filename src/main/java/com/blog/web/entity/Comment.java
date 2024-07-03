package com.blog.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

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
@Data
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

}
