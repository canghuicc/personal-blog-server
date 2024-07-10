package com.blog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.blog.web.config.Result;
import com.blog.web.config.security.JwtUtilService;
import com.blog.web.config.security.MyAuthenticationProvider;
import com.blog.web.entity.User;
import com.blog.web.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户表 前端控制器
 *
 * @author 苍晖
 * @since 2024-07-01
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MyAuthenticationProvider myAuthenticationProvider;

    @Autowired
    private JwtUtilService jwtUtilService;

    /**
     * 通过POST请求添加用户信息到数据库。
     *
     * @param user 包含待添加用户信息的请求体。
     * @return 如果用户添加成功，返回一个成功的Result对象；如果添加失败，返回一个错误的Result对象。
     * Result对象中不包含具体添加的用户信息。
     */
    @PostMapping("/adduser")
    public Result<User> saveUser(@RequestBody User user) {
        User user1 = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (user1 != null) {
            // 如果存在相同的用户名，则返回错误信息
            return Result.error("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 调用UserMapper的insert方法插入用户数据
        int rows = userMapper.insert(user);

        // 根据插入数据的行数判断操作是否成功
        if (rows > 0) {
            return Result.success("增加用户成功");
        } else {
            return Result.error("增加用户失败");
        }
    }

    /**
     * 通过DELETE请求删除指定ID的用户。
     *
     * @param userId 要删除的用户的ID，由路径参数提供。
     * @return 返回一个Result对象，其中包含删除操作的结果消息。
     * 如果删除成功（影响的行数大于0），消息为"删除成功"；
     * 如果删除失败（影响的行数为0），消息为"删除失败"。
     * 注意，此方法不返回删除的用户对象，因为操作的焦点是删除而非获取用户信息。
     */
    @DeleteMapping("/deleteuser/{userId}")
    public Result<User> removeUser(@PathVariable("userId") Integer userId) {
        int rows = userMapper.deleteById(userId);
        if (rows > 0) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 通过PUT方法更新用户信息。
     * 接收一个User对象作为请求体，其中包含了待更新的用户信息。
     * 如果请求体中的密码、电子邮件、头像路径或用户昵称非空，则更新相应的字段。
     * 用户角色如果非空，也会被更新。
     * 使用LambdaQueryWrapper构建查询条件，根据用户ID进行更新。
     * 返回更新后的用户信息。
     *
     * @param user 包含待更新用户信息的请求体。
     * @return 更新成功的用户信息。
     */
    @PutMapping("/updateuser")
    public Result<User> updateUser(@RequestBody User user) {
        // 如果密码非空，更新密码
        if (StringUtils.isNotBlank(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        // 如果电子邮件非空，更新电子邮件
        if (StringUtils.isNotBlank(user.getEmail())) {
            user.setEmail(user.getEmail());
        }
        // 如果头像路径非空，更新头像路径
        if (StringUtils.isNotBlank(user.getAvatarPath())) {
            user.setAvatarPath(user.getAvatarPath());
        }
        // 如果用户昵称非空，更新用户昵称
        if (StringUtils.isNotBlank(user.getUserNickname())) {
            user.setUserNickname(user.getUserNickname());
        }
        // 如果用户角色非空，更新用户角色
        if (user.getUserRole() != null) {
            user.setUserRole(user.getUserRole());
        }
        user.setUpdatedAt(LocalDateTime.now());

        // 更新用户信息
        // 根据非空属性更新记录
        int rows = userMapper.updateById(user);

        if (rows > 0) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 根据用户名或电子邮件获取用户信息。
     * <p>
     * 该接口支持通过用户名或电子邮件查询用户。如果同时提供了用户名和电子邮件，则返回匹配这两个条件的用户；
     * 如果只提供了用户名或电子邮件中的一个，则返回匹配该条件的用户。如果查询条件都不满足，则返回错误信息。
     *
     * @param username 用户名，可选参数。
     * @param email    电子邮件，可选参数。
     * @return 如果找到用户，则返回用户的详细信息；如果未找到用户，则返回错误信息。
     */
    @GetMapping("/getuser")
    public Result<User> getUser(@RequestParam(value = "username", required = false) String username, @RequestParam(value = "email", required = false) String email) {
        // 初始化查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 根据提供的用户名和电子邮件构建查询条件
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(email)) {
            wrapper.eq(User::getUsername, username).eq(User::getEmail, email);
        } else if (StringUtils.isNotBlank(username)) {
            wrapper.eq(User::getUsername, username);
        } else if (StringUtils.isNotBlank(email)) {
            wrapper.eq(User::getEmail, email);
        }

        // 根据构建的查询条件尝试获取用户信息
        User user1 = userMapper.selectOne(wrapper);
        // 根据查询结果返回相应的响应
        if (user1 != null) {
            return Result.success(user1);
        } else {
            return Result.error("未找到用户");
        }
    }

    /**
     * 获取所有用户的信息。
     * <p>
     * 本接口不需要接收任何参数，调用成功后，返回所有用户的信息。
     *
     * @return Result<User> 返回一个结果对象，其中包含所有用户的信息。如果操作失败，结果对象中会包含相应的错误信息。
     */
    @GetMapping("/getalluser")
    public Result<List<User>> getAllUser() {
        List<User> users = userMapper.selectList(null);
        if (users != null) {
            return Result.success(users);
        } else {
            return Result.error("获取失败");
        }
    }

    /**
     * 用户登录接口。
     * 通过接收用户提交的登录信息，验证其合法性，并生成令牌（Token）以供用户后续请求使用。
     *
     * @param user 包含用户登录信息的实体类，用户名和密码。
     * @return 如果登录成功，返回包含生成的Token的Map；如果登录失败，返回错误信息。
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody User user) {
        // 创建认证令牌，包含用户名和密码。
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

        // 使用自定义的认证提供者进行用户认证。
        Authentication authenticate = myAuthenticationProvider.authenticate(authenticationToken);

        // 验证认证结果，如果认证失败（即authenticate为null），返回错误信息。
        if (authenticate == null) {
            return Result.error("用户名或密码错误");
        }

        // 获取认证成功的用户详情。
        UserDetails loginUser = (UserDetails) authenticate.getPrincipal();
        // 从用户详情中提取用户名。
        String username = loginUser.getUsername();
        // 生成基于用户名的JWT Token。
        String token = jwtUtilService.createToken(username);

        // 创建包含Token的Map，准备返回给客户端。
        HashMap<String, String> map = new HashMap<>();
        map.put("token", token);

        // 返回成功登录的结果，包含生成的Token。
        return Result.success(map);
    }

    /**
     * 管理员登录接口。
     * 通过接收用户名和密码，验证用户身份，并返回认证令牌。
     *
     * @param user 包含用户名和密码的用户对象。
     * @return 如果登录成功，返回包含认证令牌的结果；否则返回错误信息。
     */
    @PostMapping("/admin/login")
    public Result<Map<String, String>> AdminLogin(@RequestBody User user) {
        // 根据用户名查询数据库中的用户记录
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        User user1 = userMapper.selectOne(wrapper);

        HashMap<String, String> map = new HashMap<>();
        // 验证用户是否存在，密码是否正确，并且用户角色是否为管理员
        if (user1 != null && passwordEncoder.matches(user.getPassword(), user1.getPassword()) && user1.getUserRole() == 1) {
            // 使用自定义的认证提供者进行用户认证。
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

            Authentication authenticate = myAuthenticationProvider.authenticate(authenticationToken);

            // 验证认证结果，如果认证失败（即authenticate为null），返回错误信息。
            if (authenticate == null) {
                return Result.error("用户名或密码错误");
            }

            // 获取认证成功的用户详情。
            UserDetails loginUser = (UserDetails) authenticate.getPrincipal();
            // 从用户详情中提取用户名。
            String username = loginUser.getUsername();
            // 生成基于用户名的JWT Token。
            String token = jwtUtilService.createToken(username);
            map.put("token", token);
        } else {
            // 如果凭据无效，返回错误消息
            return Result.error("用户名或密码错误");
        }
        // 返回成功登录的结果，包含生成的Token。
        return Result.success(map);
    }

    /**
     * 处理用户登出请求。
     * 通过验证Token的有效性来确认用户身份，并从Redis中删除对应的Token，实现用户登出功能。
     *
     * @param request 请求对象，用于获取请求头中的Authorization信息。
     * @return 登出结果。如果Token有效且被成功删除，则返回注销成功的信息；否则返回Token失效的错误信息。
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        // 获取请求头中的Authorization信息，用于验证Token。
        String authHeader = request.getHeader("Authorization");

        // 检查Authorization信息是否存在且以Bearer开头。
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 从Authorization信息中提取Token。
            String token = authHeader.substring(7);

            // 验证Token是否有效
            if (jwtUtilService.validateToken(token)) {
                // 从Token中提取用户名
                String username = jwtUtilService.extractUsernameFromToken(token);

                // 删除Redis中的Token
                redisTemplate.delete("token:" + username);

                // 返回注销成功的信息。
                return Result.success("注销成功");
            }
        }

        // 如果Token无效或不存在，则返回Token失效的错误信息。
        return Result.error("Token已失效");
    }

    /**
     * 用户注册接口。
     * 通过接收@RequestBody注解的User对象，注册新用户。首先检查用户名是否已存在，并验证密码是否匹配，
     * 如果存在相同用户名和密码，则返回错误信息。如果不存在，则对用户信息进行处理（如加密密码）后插入数据库，
     * 并返回注册成功信息。
     *
     * @param user 用户信息对象，包含用户名、密码、邮箱和昵称等信息。
     * @return 如果注册成功，返回包含成功消息的结果对象；如果用户名已存在，返回包含错误消息的结果对象。
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        // 检查用户名是否已存在
        // 查询数据库中是否存在相同的用户名
        User user1 = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (user1 != null) {
            // 如果存在相同的用户名，则返回错误信息
            return Result.error("用户名已存在");
        }

        // 设置新用户信息，并加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 获取邮箱和昵称字段存入queryWrapper
        user.setEmail(user.getEmail());
        user.setUserNickname(user.getUserNickname());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // 对用户信息进行插入前的处理，如设置创建时间等
//        timestampHandler.preprocessForInsert(user);
        // 插入用户信息到数据库
        userMapper.insert(user);
        // 注册成功，返回成功信息
        return Result.success("注册成功！");
    }

}

