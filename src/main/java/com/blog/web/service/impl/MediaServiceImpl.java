package com.blog.web.service.impl;

import com.blog.web.entity.Media;
import com.blog.web.mapper.MediaMapper;
import com.blog.web.service.IMediaService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 媒体文件表 服务实现类
 * </p>
 *
 * @author 苍晖
 * @since 2024-07-01
 */
@Service
public class MediaServiceImpl extends ServiceImpl<MediaMapper, Media> implements IMediaService {

}
