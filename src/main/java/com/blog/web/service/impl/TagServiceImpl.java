package com.blog.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.web.entity.Tag;
import com.blog.web.mapper.TagMapper;
import com.blog.web.service.ITagService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 标签表 服务实现类
 * </p>
 *
 * @author 苍晖
 * @since 2024-07-01
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {

}
