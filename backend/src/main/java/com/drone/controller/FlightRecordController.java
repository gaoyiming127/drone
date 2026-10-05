package com.drone.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drone.dto.FlightRecordDto;
import com.drone.entity.User;
import com.drone.exception.BusinessException;
import com.drone.mapper.UserMapper;
import com.drone.service.FlightRecordService;
import com.drone.util.PageResult;
import com.drone.util.Result;
import com.drone.vo.FlightRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 飞行记录接口。
 *
 * <p>登记与查询对全部登录用户开放（普通用户仅限本人记录），删除同样受角色与归属限制。</p>
 */
@RestController
@RequestMapping("/api/flight-records")
@RequiredArgsConstructor
public class FlightRecordController {

    private final FlightRecordService flightRecordService;
    private final UserMapper userMapper;

    /**
     * 分页查询飞行记录。
     *
     * @param pageNum        页码，默认 1
     * @param pageSize       每页条数，默认 10
     * @param droneCode      无人机编号，模糊匹配，可为空
     * @param userName       使用人员（登录名或姓名），模糊匹配，可为空
     * @param flightDate     飞行日期，支持年、年-月、年-月-日，可为空
     * @param authentication 当前登录身份，由安全框架注入
     * @return 分页结果，普通用户仅包含本人登记的记录
     */
    @GetMapping
    public PageResult<FlightRecordVO> page(@RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize,
                                           @RequestParam(required = false) String droneCode,
                                           @RequestParam(required = false) String userName,
                                           @RequestParam(required = false) String flightDate,
                                           Authentication authentication) {
        User current = currentUser(authentication);
        // 管理员可查看全部记录，普通用户只能查看本人登记的记录
        Long onlyUserId = isAdmin(current) ? null : current.getId();
        IPage<FlightRecordVO> page = flightRecordService.pageFlightRecords(pageNum, pageSize, droneCode, userName,
                flightDate, onlyUserId);
        return PageResult.success(page);
    }

    /**
     * 查询飞行记录详情。
     *
     * @param id             飞行记录ID
     * @param authentication 当前登录身份，由安全框架注入
     * @return 飞行记录详情（含电池使用明细），普通用户只能查看本人登记的记录
     */
    @GetMapping("/{id}")
    public Result<FlightRecordVO> getById(@PathVariable Long id, Authentication authentication) {
        User current = currentUser(authentication);
        return Result.success(flightRecordService.getFlightRecordById(id, current.getId(), isAdmin(current)));
    }

    /**
     * 登记飞行记录。
     *
     * @param flightRecordDto 飞行记录信息（含电池使用明细）
     * @param authentication  当前登录身份，由安全框架注入
     * @return 新增后的飞行记录详情
     */
    @PostMapping
    public Result<FlightRecordVO> create(@Valid @RequestBody FlightRecordDto flightRecordDto,
                                         Authentication authentication) {
        User current = currentUser(authentication);
        return Result.success(flightRecordService.createFlightRecord(flightRecordDto, current.getId(), isAdmin(current)));
    }

    /**
     * 删除飞行记录。
     *
     * @param id             飞行记录ID
     * @param authentication 当前登录身份，由安全框架注入
     * @return 操作结果，普通用户只能删除本人登记的记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication authentication) {
        User current = currentUser(authentication);
        flightRecordService.deleteFlightRecord(id, current.getId(), isAdmin(current));
        return Result.success();
    }

    /**
     * 取当前登录用户。
     *
     * <p>以数据库中的账号为准判断身份与角色，避免使用令牌中的过期信息。</p>
     *
     * @param authentication 当前登录身份
     * @return 数据库中的当前用户
     * @throws BusinessException 未登录或账号已被删除时抛出
     */
    private User currentUser(Authentication authentication) {
        if (authentication == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, authentication.getName()));
        if (user == null) {
            throw new BusinessException(401, "账号不存在或已被删除");
        }
        return user;
    }

    /**
     * 判断是否为管理员。
     *
     * @param user 当前用户
     * @return 角色为 ADMIN 时返回 true
     */
    private boolean isAdmin(User user) {
        return "ADMIN".equals(user.getRole());
    }
}
