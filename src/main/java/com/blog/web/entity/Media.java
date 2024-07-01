package com.blog.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

/**
 * <p>
 * 媒体文件表
 * </p>
 *
 * @author 苍晖
 * @since 2024-07-01
 */
public class Media implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图片id，主键自增
     */
    @TableId(value = "media_id", type = IdType.AUTO)
    private Integer mediaId;

    /**
     * 图片名
     */
    private String mediaName;

    /**
     * 图片路径
     */
    private String mediaPath;

    public Integer getMediaId() {
        return mediaId;
    }

    public void setMediaId(Integer mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    @Override
    public String toString() {
        return "Media{" +
            "mediaId = " + mediaId +
            ", mediaName = " + mediaName +
            ", mediaPath = " + mediaPath +
        "}";
    }
}
