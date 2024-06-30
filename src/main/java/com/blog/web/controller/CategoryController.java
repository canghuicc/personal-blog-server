package com.blog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.web.config.Result;
import com.blog.web.config.TimestampHandler;
import com.blog.web.entity.Category;
import com.blog.web.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * <p>
 * 分类表 前端控制器
 * </p>
 *
 * @author 苍晖
 * @since 2024-07-01
 */
@Controller
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private TimestampHandler timestampHandler;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 通过POST请求添加新的分类。
     *
     * @param category 通过请求体传入的待添加的分类对象。
     * @return 如果添加成功，返回带有成功消息的结果对象；如果添加失败，返回带有错误消息的结果对象。
     */
    @PostMapping("/addcategory")
    public Result<Category> addCategory(@RequestBody Category category) {
        // 在插入数据库之前，对分类对象进行时间戳处理
        // 在插入前对分类信息进行时间戳处理
        timestampHandler.preprocessForInsert(category);

        // 调用Mapper接口插入分类信息到数据库
        // 调用服务层方法，保存分类信息
        int rows = categoryMapper.insert(category);

        // 根据插入操作影响的行数判断添加操作是否成功
        if (rows > 0) {
            return Result.success("添加成功");
        } else {
            return Result.error("添加失败");
        }
    }

    /**
     * 通过DELETE请求删除指定ID的分类。
     *
     * @param categoryId 分类的唯一标识ID，通过路径参数传递。
     * @return 如果删除成功，返回一个成功的Result对象，包含删除成功的消息；
     * 如果删除失败，返回一个错误的Result对象，包含删除失败的消息。
     */
    @DeleteMapping("/deletecategory/{categoryId}")
    public Result<Category> removeCategory(@PathVariable("categoryId") Integer categoryId) {
        // 调用categoryMapper的.deleteById方法，尝试删除指定ID的分类
        int rows = categoryMapper.deleteById(categoryId);
        // 判断删除操作影响的行数，如果大于0，则表示删除成功
        if (rows > 0) {
            return Result.success("删除成功");
        } else {
            // 如果影响的行数为0，说明删除操作失败
            return Result.error("删除失败");
        }
    }

    /**
     * 通过@PutMapping注解指定该方法处理PUT请求，路径为/updatecategory。
     * 方法用于更新分类信息。
     *
     * @param category 通过@RequestBody注解将请求体中的数据绑定到Category对象，用于更新操作。
     * @return 返回Result对象，其中包含更新操作的结果。如果更新成功，返回成功消息；如果更新失败，返回错误消息。
     */
    @PutMapping("/updatecategory")
    public Result<Category> updateCategory(@RequestBody Category category) {
        // 创建LambdaQueryWrapper用于构建查询条件
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        // 设置查询条件：分类ID和分类名称必须与传入的category对象相同
        wrapper.eq(Category::getCategoryId, category.getCategoryId());
        wrapper.eq(Category::getCategoryName, category.getCategoryName());

        // 调用categoryMapper的update方法，使用wrapper作为更新条件，更新category对象中的数据
        int rows = categoryMapper.update(category, wrapper);

        // 根据update方法的返回值判断更新操作是否成功
        if (rows > 0) {
            // 更新成功，返回成功结果
            return Result.success("更新成功");
        } else {
            // 更新失败，返回错误结果
            return Result.error("更新失败");
        }
    }

    /**
     * 根据分类ID获取分类信息。
     *
     * @param categoryId 分类ID，可选参数。
     * @return 如果分类存在，则返回分类信息；如果分类不存在，则返回错误信息。
     */
    @GetMapping("/getCategory")
    public Result<Category> getcategory(
            @RequestParam(value = "categoryId", required = false) Integer categoryId
    ) {
        // 根据categoryId查询分类信息
        Category category = categoryMapper.selectById(categoryId);
        // 如果分类信息存在
        if (category != null) {
            // 返回成功的查询结果
            return Result.success(category);
        } else {
            // 返回查询失败的错误信息
            return Result.error("查询失败");
        }
    }

    /**
     * 通过GET请求获取所有分类信息。
     *
     * @return Result<Category> - 包含所有分类的列表及操作状态的信息。
     */
    @GetMapping("/getallcategory")
    public Result<Category> getAllCategory() {
        // 调用categoryMapper的selectList方法，不传入任何条件，获取所有分类信息
        List<Category> categoryList = categoryMapper.selectList(null);
        // 将获取到的分类列表封装在Result对象中，以成功状态返回
        return Result.success((Category) categoryList);
    }
}
