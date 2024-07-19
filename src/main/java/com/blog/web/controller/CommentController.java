package com.blog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.web.config.Result;
import com.blog.web.entity.Comment;
import com.blog.web.mapper.CommentMapper;
import com.blog.web.service.ICommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论表 前端控制器
 *
 * @author 苍晖
 * @since 2024-07-01
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ICommentService iCommentService;

    /**
     * 通过POST请求添加评论。
     *
     * @param comment 待添加的评论对象，包含评论内容等信息。
     * @return 如果评论添加成功，返回成功的Result对象，包含评论成功的消息；
     * 如果添加失败，返回错误的Result对象，包含评论失败的消息。
     */
    @PostMapping("/addcomment")
    public Result<Comment> addComment(@RequestBody Comment comment, HttpServletRequest request) {
        comment.setCreatedAt(LocalDateTime.now());
        String clientIp = getClientIp(request);
        comment.setCommentIp(clientIp);
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
        // 根据评论ID删除评论，返回影响的行数
        int rows = commentMapper.deleteById(commentId);
        // 判断删除操作的影响行数，如果大于0，则删除成功
        if (rows > 0) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 根据文章ID获取评论列表。
     * <p>
     * 本方法通过查询数据库中与指定文章ID相关的评论，然后构建一个评论树状结构返回。
     * 如果没有指定文章ID，则返回所有评论。评论数据以List<Map<String, Object>>的形式返回，
     * 其中Map代表每个评论及其相关信息。
     *
     * @param articleId 文章ID，可选参数。如果指定了文章ID，则只返回该文章的评论。
     * @return 返回一个包含评论树的Result对象。如果未找到任何评论，则Result对象的data字段为空。
     */
    @GetMapping("/getcomment")
    public Result<List<Map<String, Object>>> getComment(
            @RequestParam(value = "articleId", required = false) Integer articleId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        // 如果提供了文章ID，则根据文章ID过滤评论
        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }
        // 根据查询条件获取评论列表
        List<Comment> commentList = commentMapper.selectList(wrapper);

        // 构建评论树状结构
        // 判断查询结果是否存在
        if (commentList != null) {
            List<Map<String, Object>> commentTree = buildCommentTree(commentList);
            // 返回带有评论树的Result对象
            return Result.success(commentTree);
        } else {
            // 如果未找到评论，返回一个空的Result对象
            // 如果未找到评论，返回空
            return Result.success();
        }
    }

    /**
     * 获取所有评论信息的接口。
     *
     * 通过页面编号和页面大小来分页获取评论数据。
     * 返回包含评论总数和评论列表的结果对象。
     *
     * @param pageNum 当前页码，用于分页查询
     * @param pageSize 每页的评论数量，用于分页查询
     * @return 包含评论总数和评论列表的结果对象
     */
    @GetMapping("/getallcomment")
    public Result<Map<String, Object>> getAllComment(@RequestParam(value = "pageNum") Integer pageNum, @RequestParam(value = "pageSize") Integer pageSize) {
        // 创建分页对象，用于后续的分页查询
        Page<Comment> page = new Page<>(pageNum, pageSize);
        // 调用评论服务的分页查询方法
        iCommentService.page(page);
        // 创建一个Map对象，用于存放查询结果
        Map<String, Object> map = new HashMap<>();
        // 将评论总数放入Map中
        map.put("total", page.getTotal());
        // 将评论列表放入Map中
        map.put("rows", page.getRecords());
        // 返回包含查询结果的success结果对象
        return Result.success(map);
    }

    /**
     * 通过PUT请求更新评论信息。
     *
     * @param comment 包含更新后评论信息的实体对象。
     * @return 如果更新成功，返回包含成功消息的结果对象；如果更新失败，返回包含错误消息的结果对象。
     */
    @PutMapping("/updatecomment")
    public Result<Comment> updateComment(@RequestBody Comment comment) {
        // 调用commentMapper的updateById方法更新评论
        int rows = commentMapper.updateById(comment);

        // 判断更新操作是否成功，如果成功，则返回更新成功的结果，否则返回更新失败的错误信息
        if (rows > 0) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 构建评论树。
     * <p>
     * 通过将评论分组到它们的父评论中，构建一个评论树结构。
     * 这个方法专注于处理评论的数据结构转换，不涉及具体的业务逻辑。
     *
     * @param comments 原始的评论列表，这些评论可能是树结构中的任意节点。
     * @return 返回一个列表，包含所有根评论的树结构表示。
     */
    // 构建评论树方法
    private List<Map<String, Object>> buildCommentTree(List<Comment> comments) {
        // 根据父评论ID将评论分组，以便后续构建评论树。
        // 使用一个map来存储每个parentId对应的子评论列表
        Map<Integer, List<Comment>> commentMap = comments.stream().collect(Collectors.groupingBy(Comment::getParentId));

        // 获取所有父评论ID为0的评论（即根评论），并将其转换为树结构表示。
        // 返回构建好的根评论列表。
        return commentMap.getOrDefault(0, new ArrayList<>())
                .stream()
                .map(comment -> convertToMap(comment, commentMap))
                .collect(Collectors.toList());
    }

    /**
     * 将评论对象及其子评论转换为Map结构，便于后续处理或传输。
     *
     * @param comment    当前评论对象，包含评论内容和评论ID。
     * @param commentMap 一个映射，其中键是评论ID，值是与该ID相关的评论列表。这个映射用于查找当前评论的子评论。
     * @return 返回一个Map，包含当前评论和它的子评论（如果存在）。
     */
    private Map<String, Object> convertToMap(Comment comment, Map<Integer, List<Comment>> commentMap) {
        // 初始化一个Map来存储当前评论和它的子评论
        Map<String, Object> map = new HashMap<>();
        // 将当前评论对象放入map中
        map.put("comment", comment);

        // 通过评论ID从commentMap中获取当前评论的子评论列表
        List<Comment> children = commentMap.get(comment.getCommentId());
        // 如果子评论存在且不为空，则将它们转换为Map结构并添加到当前Map中
        if (children != null && !children.isEmpty()) {
            // 使用stream将子评论列表转换为Map列表
            List<Map<String, Object>> childrenMaps = children.stream()
                    .map(child -> convertToMap(child, commentMap)) // 对每个子评论递归调用本方法
                    .collect(Collectors.toList());
            // 将子评论的Map列表放入当前Map中
            map.put("children", childrenMaps);
        }
        return map;
    }

    /**
     * 获取客户端IP地址的方法。
     *
     * @param request HttpServletRequest对象，用于获取客户端IP地址。
     * @return 客户端IP地址。
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于多次代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.length() > 15 && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }

}
