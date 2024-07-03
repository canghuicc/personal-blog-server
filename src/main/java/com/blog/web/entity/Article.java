package com.blog.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 文章表
 * </p>
 *
 * @author 苍晖
 * @since 2024-07-01
 */
@Data
public class Article implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文章id，主键自增
     */
    @TableId(value = "article_id", type = IdType.AUTO)
    private Integer articleId;

    /**
     * 文章标题
     */
    private String articleTitle;

    /**
     * 文章内容
     */
    private String articleContent;

    /**
     * 作者id
     */
    private Integer userId;

    /**
     * 分类id
     */
    private Integer categoryId;

    /**
     * 标签id
     */
    private Integer tagId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

}
