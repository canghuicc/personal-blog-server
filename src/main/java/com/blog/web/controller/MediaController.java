package com.blog.web.controller;

import com.blog.web.config.Result;
import com.blog.web.entity.Media;
import com.blog.web.mapper.MediaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
     * @param file      上传的媒体文件。
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
        Media media=mediaMapper.selectById(mediaId);
        if (media != null) {
            Path path = Paths.get(media.getMediaPath());
            try {
                Files.deleteIfExists(path);
                int rows = mediaMapper.deleteById(mediaId);
                if (rows > 0) {
                    return Result.success("删除成功");
                } else {
                    return Result.error("删除失败");
                }
            } catch (IOException e) {
                return Result.error("删除失败");
            }
        } else {
            return Result.error("未找到媒体文件");
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
    public Result<List<byte[]>> getAllMedia() {
        // 查询所有媒体信息
        List<Media> mediaList = mediaMapper.selectList(null);
        if (mediaList != null && !mediaList.isEmpty()) {
            try {
                List<byte[]> allImages = new ArrayList<>();
                for (Media media : mediaList) {
                    Path path = Paths.get(media.getMediaPath());
                    byte[] fileContent = Files.readAllBytes(path);
                    allImages.add(fileContent);
                }
                return Result.success(allImages);
            } catch (IOException e) {
                return Result.error("读取文件失败");
            }
        } else {
            return Result.error("未找到媒体文件");
        }
    }

    /**
     * 通过GET请求获取媒体文件。
     *
     * 此方法处理来自客户端的GET请求，以获取指定路径的媒体文件（如图片）。
     * 它首先尝试根据提供的媒体路径读取文件内容，然后将内容作为响应返回给客户端。
     * 如果文件不存在或无法访问，方法将返回一个404状态码的响应，表示文件未找到。
     *
     * @param mediaPath 请求中携带的媒体文件路径参数，用于指定要获取的媒体文件的位置。
     * @return ResponseEntity 包含媒体文件内容的响应体，如果文件不存在，则返回一个空的响应体和404状态码。
     */
    @GetMapping("/getmedia")
    public ResponseEntity<byte[]> getMedia(@RequestParam(value = "mediaPath") String mediaPath) {
        try {
            // 根据提供的媒体路径获取文件的完整路径。
            Path path = Paths.get(mediaPath);
            // 读取文件的所有字节内容。
            byte[] fileContent = Files.readAllBytes(path);

            // 设置响应头信息，指定响应的内容类型为JPEG图片。
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);

            // 返回文件内容和相应的头信息。
            return ResponseEntity.ok().headers(headers).body(fileContent);
        } catch (IOException e) {
            // 如果发生IO异常，说明文件不存在或无法访问，返回404状态码。
            return ResponseEntity.notFound().build();
        }
    }

}
