package com.blog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.web.config.Result;
import com.blog.web.entity.Media;
import com.blog.web.mapper.MediaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 媒体文件表 前端控制器
 *
 * @author 苍晖
 * @since 2024-07-01
 */
@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    @Autowired
    private MediaMapper mediaMapper;

    /**
     * 通过POST请求添加媒体文件。
     *
     * @param file 上传的媒体文件。
     * @param mediaName 媒体文件的名称。
     * @return 添加结果，包括成功与否和添加的媒体信息。
     */
    @PostMapping("/addmedia")
    public Result<Media> addMedia(@RequestParam("file") MultipartFile file, @RequestParam("mediaName") String mediaName) {
        try {
            // 读取上传文件的内容字节。
            // 获取文件的字节数组
            byte[] bytes = file.getBytes();

            // 构建文件在服务器上的存储路径。
            // 构建文件路径
            Path path = Paths.get(uploadPath + File.separator + file.getOriginalFilename());

            // 将文件内容写入到服务器的指定路径。
            // 将文件保存到指定路径
            Files.write(path, bytes);

            // 创建Media对象，用于存储媒体信息。
            // 构建媒体对象并设置属性
            Media media = new Media();
            // 设置媒体文件的名称。
            media.setMediaName(mediaName);
            // 设置媒体文件的存储路径。
            media.setMediaPath(path.toString());

            // 调用MediaMapper的insert方法，将媒体信息插入数据库。
            // 调用MediaMapper的insert方法插入媒体信息
            int rows = mediaMapper.insert(media);

            // 根据插入操作的影响行数判断添加是否成功。
            // 判断插入操作影响的行数，如果大于0则表示插入成功
            if (rows > 0) {
                // 添加成功，返回成功结果和添加的媒体信息。
                return Result.success("添加成功", media);
            } else {
                // 添加失败，返回错误结果。
                return Result.error("添加失败");
            }
        } catch (IOException e) {
            // 文件上传过程中发生IO异常，返回错误结果。
            return Result.error("文件上传失败");
        }
    }


    /**
     * 通过媒体ID删除媒体信息。
     *
     * @param mediaId 媒体的唯一标识ID。
     * @return 删除操作的结果，成功或失败。
     */
    @DeleteMapping("/deletemedia/{mediaId}")
    public Result<Media> removeMedia(@PathVariable("mediaId") Integer mediaId) {
        // 调用媒体Mapper删除指定ID的媒体记录
        int rows = mediaMapper.deleteById(mediaId);

        // 根据删除操作影响的行数判断删除是否成功
        // 根据删除操作影响的行数判断删除是否成功，返回相应的Result对象
        if (rows > 0) {
            return Result.success();
        } else {
            return Result.error();
        }
    }

    /**
     * 通过接口获取所有媒体信息。
     * <p>
     * 本接口不接受任何参数，返回所有媒体信息的列表。
     * 使用MediaMapper的selectList方法查询所有媒体信息。
     * </p>
     *
     * @return Result<Media> 包含所有媒体信息的结果对象。结果对象中包含了操作的成功状态及媒体信息数据。
     */
    @GetMapping("/getallmedia")
    public Result<List<Media>> getAllMedia() {
        // 查询所有媒体信息
        List<Media> mediaList = mediaMapper.selectList(null);
        if (mediaList != null) {
            return Result.success(mediaList);
        } else {
            return Result.error();
        }
    }

    /**
     * 根据媒体路径获取媒体信息。
     * <p>
     * 本方法通过GET请求访问，路径为/getmedia，可选地携带mediaPath参数。
     * 如果提供了mediaPath参数，则尝试根据该路径查询对应的媒体信息；如果未提供，则查询所有媒体信息。
     * </p>
     *
     * @param mediaPath 媒体文件的路径，可选参数。如果提供，将用于精确查询媒体信息。
     * @return 返回一个Result对象，其中包含查询到的媒体信息。如果未找到匹配的媒体信息，则Result对象中的数据部分可能为空。
     */
    @GetMapping("/getmedia")
    public Result<Media> getMedia(
            @RequestParam(value = "mediaPath", required = false) String mediaPath
    ) {
        // 构建查询条件，针对mediaPath字段进行等值查询
        // 构建查询条件，根据mediaPath查询唯一的媒体信息
        LambdaQueryWrapper<Media> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Media::getMediaPath, mediaPath);

        // 根据查询条件尝试获取唯一的媒体信息
        // 根据查询条件从数据库中查询媒体信息
        Media media1 = mediaMapper.selectOne(wrapper);

        // 返回查询结果，包含可能存在的媒体信息
        // 返回查询结果，如果找到，则返回媒体信息；否则，返回空或错误信息
        if (media1 != null) {
            return Result.success(media1);
        } else {
            return Result.error("未找到图片");
        }
    }


}
