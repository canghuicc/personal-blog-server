package com.blog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.blog.web.config.Result;
import com.blog.web.config.TimestampHandler;
import com.blog.web.entity.Comment;
import com.blog.web.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论表 前端控制器
 *
 * @author 苍晖
 * @since 2024-07-01
 */
@Controller
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private TimestampHandler timestampHandler;

    @Autowired
    private CommentMapper commentMapper;

    /**
     * 通过POST请求添加评论。
     *
     * @param comment 待添加的评论对象，包含评论内容等信息。
     * @return 如果评论添加成功，返回成功的Result对象，包含评论成功的消息；
     * 如果添加失败，返回错误的Result对象，包含评论失败的消息。
     */
    @PostMapping("/addcomment")
    public Result<Comment> addComment(@RequestBody Comment comment) {
        // 在插入评论前，对评论对象进行时间戳处理
        // 在插入评论前处理时间戳
        timestampHandler.preprocessForInsert(comment);
        // 将评论对象插入数据库，返回影响的行数
        int rows = commentMapper.insert(comment);
        // 判断插入操作是否成功，成功则返回成功结果，失败则返回错误结果
        if (rows > 0) {
            return Result.success("评论成功");
        } else {
            return Result.error("评论失败");
        }
    }

    /**
     * 通过DELETE请求删除指定ID的评论。
     *
     * @param commentId 评论的唯一标识ID。
     * @return 删除操作的结果，成功或失败的信息。
     */
    @DeleteMapping("/deletecomment/{commentId}")
    public Result<Comment> removeComment(@PathVariable("commentId") Integer commentId) {
        // 调用评论Mapper接口，根据ID删除评论
        // 根据评论ID删除评论，返回影响的行数
        int rows = commentMapper.deleteById(commentId);
        // 判断删除操作的影响行数，如果大于0，则删除成功
        // 判断删除操作是否成功，成功则返回成功结果，失败则返回错误结果
        if (rows > 0) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 更新评论信息的方法。
     *
     * @param comment 待更新的评论对象，包含评论的全部信息。
     * @return 如果更新成功，返回带有成功消息的结果对象；如果更新失败，返回带有错误消息的结果对象。
     */
    @PutMapping("/updatecomment")
    public Result<Comment> updateComment(@RequestBody Comment comment) {
        // 对评论对象进行更新前的预处理
        timestampHandler.preprocessForUpdate(comment);

        // 如果评论内容不为空，则更新评论内容
        if (StringUtils.isNotBlank(comment.getCommentContent())){
            comment.setCommentContent(comment.getCommentContent());
        }

        // 如果评论角色不为空，则更新评论角色
        if (comment.getCommentRole()!=null){
            comment.setCommentRole(comment.getCommentRole());
        }

        // 构建查询条件，指定更新的评论为目标评论
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getCommentId,comment.getCommentId());

        // 执行评论更新操作
        int rows=commentMapper.update(comment,wrapper);

        // 根据更新操作的影响行数，判断更新是否成功，并返回相应的结果
        if (rows>0){
            return Result.success("更新成功");
        }else {
            return Result.error("更新失败");
        }
    }

    /**
     * 根据评论ID、文章ID、父评论ID和用户ID获取评论信息。
     * 支持通过多个条件组合查询，返回符合条件的评论。如果指定了评论ID，则返回第一个匹配的评论；
     * 否则，返回所有匹配的评论列表。
     *
     * @param commentId 评论ID，可选参数。
     * @param articleId 文章ID，可选参数。
     * @param parentId 父评论ID，可选参数。
     * @param userId 用户ID，可选参数。
     * @return 如果找到评论，则返回评论对象或评论列表；如果未找到评论，则返回错误信息。
     */
    @GetMapping("/getcomment")
    public Result<Comment> getComment(
            @RequestParam(value = "commentId", required = false) Integer commentId,
            @RequestParam(value = "articleId", required = false) Integer articleId,
            @RequestParam(value = "parentId", required = false) Integer parentId,
            @RequestParam(value = "userId", required = false) Integer userId){
        // 创建查询条件包装对象
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        // 根据传入的ID添加相应的查询条件
        if (commentId!=null){
            wrapper.eq(Comment::getCommentId,commentId);
        }
        if (articleId!=null){
            wrapper.eq(Comment::getArticleId,articleId);
        }
        if (parentId!=null){
            wrapper.eq(Comment::getParentId,parentId);
        }
        if (userId!=null){
            wrapper.eq(Comment::getUserId,userId);
        }
        // 根据查询条件获取评论列表
        List<Comment> commentList = commentMapper.selectList(wrapper);

        // 判断查询结果是否存在
        if (commentList!=null&&!commentList.isEmpty()){
            // 如果指定了评论ID，返回第一个评论；否则，返回整个评论列表
            if (commentId!=null){
                return Result.success(commentList.get(0));
            }else {
                return Result.success((Comment) commentList);
            }
        }else {
            // 如果未找到评论，返回错误信息
            return Result.error("未找到评论");
        }
    }

    /**
     * 通过GET请求获取所有评论
     *
     * @return Result<Comment> - 返回包含所有评论的Result对象，如果查询失败，则返回错误信息
     */
    @GetMapping("/getallcomment")
    public Result<Comment> getAllComment() {
        // 调用commentMapper的selectList方法查询所有评论
        List<Comment> commentList = commentMapper.selectList(null);

        // 判断查询结果是否为空，如果不为空，则返回查询成功的结果，包含评论列表
        if (commentList != null){
            return Result.success((Comment) commentList);
        } else {
            // 如果查询结果为空，则返回查询失败的错误信息
            return Result.error("查询失败");
        }
    }
}
