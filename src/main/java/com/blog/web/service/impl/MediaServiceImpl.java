package com.blog.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.web.entity.Media;
import com.blog.web.mapper.MediaMapper;
import com.blog.web.service.IMediaService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 媒体文件表 服务实现类
 * </p>
 *
 * @author canghui
 * @since 2024-05-05
 */
@Service
public class MediaServiceImpl extends ServiceImpl<MediaMapper, Media> implements IMediaService {

}
