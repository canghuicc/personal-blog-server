package com.blog.web.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.web.config.Result;
import com.blog.web.entity.Article;
import com.blog.web.mapper.ArticleMapper;
import com.blog.web.service.IArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 文章表 前端控制器
 *
 * @author 苍晖
 * @since 2024-07-01
 */
@RestController
@RequestMapping("/api/article")
public class ArticleController {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private IArticleService iArticleService;

    /**
     * 保存文章信息
     * <p>
     * 该方法通过POST请求接收一个Article对象，然后将其插入到数据库中。
     * 如果插入成功，返回操作成功的结果；如果插入失败，返回操作失败的结果。
     *
     * @param article 待保存的文章对象，包含文章的全部信息。
     * @return 如果插入成功，返回一个包含成功信息的结果对象；如果插入失败，返回一个包含错误信息的结果对象。
     */
    @PostMapping("/addarticle")
    public Result<Article> addArticle(@RequestBody Article article) {
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());

        // 将处理后的文章插入到数据库中，并返回插入操作影响的行数
        int rows = articleMapper.insert(article);

        // 根据插入操作的影响行数，判断插入操作是否成功，并返回相应的结果
        if (rows > 0) {
            return Result.success("发布成功！");
        } else {
            return Result.error("发布失败！");
        }
    }


    /**
     * 通过DELETE请求删除指定文章。
     *
     * @param articleId 要删除评论的文章ID，通过路径参数获取。
     * @return 如果删除成功，返回一个成功的Result对象；否则返回一个错误的Result对象。
     * Result对象封装了操作的结果状态和可能的错误信息。
     */
    @DeleteMapping("/deletearticle/{articleId}")
    public Result<Article> removeArticle(@PathVariable("articleId") Integer articleId) {
        // 调用articleMapper的DeleteById方法，根据文章ID删除。
        int rows = articleMapper.deleteById(articleId);
        // 检查删除操作影响的行数，如果大于0，则表示删除成功。
        if (rows > 0) {
            return Result.success("删除成功！");
        } else {
            return Result.error("删除失败!");
        }
    }

    /**
     * 根据页码和每页大小获取所有文章列表。
     *
     * @param pageNum 当前页码
     * @param pageSize 每页显示的文章数量
     * @return 包含文章总数和文章列表的结果对象
     */
    @GetMapping("/getallarticle")
    public Result<Map<String, Object>> getAllArticle(@RequestParam(value = "pageNum") Integer pageNum, @RequestParam(value = "pageSize") Integer pageSize) {
        // 创建分页对象，用于后续的分页查询
        Page<Article> page = new Page<>(pageNum, pageSize);
        // 调用文章服务的分页查询方法
        iArticleService.page(page);
        // 创建一个Map用于存放查询结果的总条数和文章列表
        Map<String, Object> map = new HashMap<>();
        // 将查询结果的总条数放入Map中
        map.put("total", page.getTotal());
        // 将查询到的文章列表放入Map中
        map.put("rows", page.getRecords());
        // 返回包含文章总数和文章列表的结果对象
        return Result.success(map);
    }

    /**
     * 根据文章ID获取文章详情。
     * <p>
     * 本方法通过GET请求方式，从数据库中查询指定文章ID的文章详情，并将查询结果封装为Result对象返回。
     * 如果文章ID未提供，则不进行查询操作。该方法旨在为前端提供获取文章详细信息的接口。
     *
     * @param articleId 文章的唯一标识ID，可选参数。
     * @return 包含查询结果的Result对象，如果查询成功，Result的状态码为200，数据部分为查询到的文章对象；
     * 如果查询失败或未找到对应文章，则Result的状态码和数据部分根据实际情况进行设置。
     */
    @GetMapping("/getarticle")
    public Result<Article> getArticle(@RequestParam(value = "articleId", required = false) Integer articleId) {
        // 根据文章ID查询文章详情
        Article article = articleMapper.selectById(articleId);
        // 返回查询结果，如果未查询到文章，则Result对象的数据部分为null
        if (article != null) {
            return Result.success(article);
        } else {
            return Result.error("查询失败！");
        }
    }

    /**
     * 通过PUT方法更新文章信息。
     *
     * @param article 包含待更新文章详细信息的实体类。
     * @return 如果更新成功，返回带有成功消息的结果对象；如果更新失败，返回带有错误消息的结果对象。
     */
    @PutMapping("/updatearticle")
    public Result<Article> updateArticle(@RequestBody Article article) {
        article.setUpdatedAt(LocalDateTime.now());

        // 如果文章标题不为空，更新文章标题
        if (StringUtils.isNotEmpty(article.getArticleTitle())) {
            article.setArticleTitle(article.getArticleTitle());
        }
        // 如果文章内容不为空，更新文章内容
        if (StringUtils.isNotEmpty(article.getArticleContent())) {
            article.setArticleContent(article.getArticleContent());
        }
        // 如果文章分类ID不为空，更新文章分类ID
        if (article.getCategoryId() != null) {
            article.setCategoryId(article.getCategoryId());
        }
        // 如果文章标签ID不为空，更新文章标签ID
        if (article.getTagId() != null) {
            article.setTagId(article.getTagId());
        }
        if(article.getBackgroundPath() != null){
            article.setBackgroundPath(article.getBackgroundPath());
        }

        // 更新数据库中的文章信息
        int rows = articleMapper.updateById(article);

        // 根据更新影响的行数判断更新操作是否成功
        if (rows > 0) {
            return Result.success("更新成功！");
        } else {
            return Result.error("更新失败！");
        }
    }


}
