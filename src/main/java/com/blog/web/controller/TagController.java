package com.blog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.web.config.Result;
import com.blog.web.entity.Tag;
import com.blog.web.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 标签表 前端控制器
 *
 * @author 苍晖
 * @since 2024-07-01
 */
@RestController
@RequestMapping("/api/tag")
public class TagController {

    @Autowired
    private TagMapper tagMapper;

    /**
     * 通过POST请求添加标签。
     *
     * @param tag 待添加的标签对象，包含标签信息。
     * @return 如果添加成功，返回带有成功消息的结果对象；如果添加失败，返回带有错误消息的结果对象。
     */
    @PostMapping("/addtag")
    public Result<Tag> addTag(@RequestBody Tag tag) {
        // 对标签对象进行插入前的预处理，例如设置创建时间等
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());

        // 调用标签Mapper插入标签到数据库
        int rows = tagMapper.insert(tag);

        // 根据插入结果返回相应的操作结果
        if (rows > 0) {
            return Result.success("添加成功！");
        } else {
            return Result.error("添加失败！");
        }
    }

    /**
     * 通过@DeleteMapping注解指定该方法处理HTTP DELETE请求，请求的URL路径为/deletetag/{tagId}。
     * 其中{tagId}是一个占位符，代表标签的ID，通过@PathVariable注解将URL路径中的tagId绑定到方法参数tagId上。
     * 方法的目的是删除指定ID的标签，并返回删除结果。
     *
     * @param tagId 要删除的标签的ID，由路径变量提供。
     * @return 如果删除成功，返回一个包含成功消息和删除标签的Result对象；如果删除失败，返回一个包含错误消息的Result对象。
     */
    @DeleteMapping("/deletetag/{tagId}")
    public Result<Tag> removetag(@PathVariable("tagId") Integer tagId) {
        // 调用tagMapper的DeleteById方法，尝试删除ID为tagId的标签
        int rows = tagMapper.deleteById(tagId);
        // 检查删除操作影响的行数，如果大于0，则表示删除成功
        if (rows > 0) {
            return Result.success("删除成功！");
        } else {
            // 如果影响的行数为0，说明删除操作失败
            return Result.error("删除失败！");
        }
    }

    /**
     * 通过@PutMapping注解指定该方法处理HTTP PUT请求，路径为/updatetag。
     * 方法用于更新标签信息。
     *
     * @param tag 通过@RequestBody注解表明该参数是从请求体中获取的，代表需要更新的标签对象。
     * @return 返回一个Result<Tag>对象，其中包含更新操作的结果和更新后的标签对象。
     * 如果更新成功，Result对象的success方法将被调用，并返回"更新成功！"的消息；
     * 如果更新失败，Result对象的error方法将被调用，并返回"更新失败！"的消息。
     */
    @PutMapping("/updatetag")
    public Result<Tag> updatetag(@RequestBody Tag tag) {
        tag.setUpdatedAt(LocalDateTime.now());

        // 调用tagMapper的update方法更新标签信息，传入更新条件和待更新的标签对象
        int rows = tagMapper.updateById(tag);

        // 根据update方法的返回值判断更新操作是否成功
        if (rows > 0) {
            // 更新成功，返回成功的Result对象
            return Result.success("更新成功！");
        } else {
            // 更新失败，返回失败的Result对象
            return Result.error("更新失败！");
        }
    }

    /**
     * 根据标签ID获取标签信息。
     * <p>
     * 通过标签ID查询数据库中的标签信息。如果找到对应标签，则返回标签详情；
     * 如果未找到标签，则返回查询失败的错误信息。
     *
     * @param tagId 标签ID，可选参数。如果提供，将用于查询特定ID的标签。
     * @return 如果查询成功，返回包含标签信息的Result对象；如果查询失败，返回一个包含错误消息的Result对象。
     */
    @GetMapping("/gettag")
    public Result<Tag> gettag(@RequestParam(value = "tagId", required = false) Integer tagId) {
        // 使用LambdaQueryWrapper查询指定tagId的标签信息
        Tag tag1 = tagMapper.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getTagId, tagId));
        if (tag1 != null) {
            // 如果查询结果不为空，则返回查询成功的Result对象，包含标签信息
            return Result.success(tag1);
        } else {
            // 如果查询结果为空，则返回查询失败的Result对象，包含错误消息
            return Result.error("查询失败！");
        }
    }


    /**
     * 通过GET请求获取所有标签信息。
     *
     * @return Result<Tag> 结果对象，包含所有标签列表或错误信息。
     */
    @GetMapping("/getalltag")
    public Result<List<Tag>> getAllTag() {
        // 调用tagMapper的selectList方法，不传入任何条件，查询所有标签
        List<Tag> tags = tagMapper.selectList(null);

        // 判断查询结果是否存在
        if (tags != null) {
            // 如果查询结果存在，则返回成功的Result，包含查询到的标签信息列表
            return Result.success(tags);
        } else {
            // 如果查询结果不存在，则返回错误的Result，包含"查询失败！"信息
            return Result.error("查询失败！");
        }
    }

}
