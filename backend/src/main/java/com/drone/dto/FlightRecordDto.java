package com.drone.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 飞行记录登记请求参数。
 *
 * <p>记录编号由系统生成；同时需要提交本次飞行所使用的电池明细。</p>
 */
@Data
public class FlightRecordDto {

    /** 使用的无人机ID，必填；已停用或维修中的无人机不允许登记 */
    @NotNull(message = "无人机ID不能为空")
    private Long droneId;

    /** 使用人员ID，必填；普通用户只能登记本人的记录 */
    @NotNull(message = "使用人员ID不能为空")
    private Long userId;

    /** 飞行地点 */
    private String flightLocation;

    /** 飞行日期，必填 */
    @NotNull(message = "飞行日期不能为空")
    private LocalDate flightDate;

    /** 飞行时长（分钟），必填，同时累加到无人机的累计飞行分钟 */
    @NotNull(message = "飞行分钟数不能为空")
    private Integer flightMinutes;

    /** 起飞次数，必填 */
    @NotNull(message = "起飞次数不能为空")
    private Integer takeoffCount;

    /** 是否发生异常：0-否，1-是，必填 */
    @NotNull(message = "请选择是否发生异常")
    private Integer hasException;

    /** 异常描述，发生异常时填写 */
    private String exceptionDescription;

    /** 备注 */
    private String remark;

    /** 电池使用明细，必填且至少一块电池 */
    @Valid
    @NotNull(message = "电池使用信息不能为空")
    private List<FlightRecordBatteryDto> batteries;
}
