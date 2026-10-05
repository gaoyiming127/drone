package com.drone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 维修记录新增/修改请求参数。
 *
 * <p>维修状态与维修结果不在本对象中传入：新增时状态固定为 PENDING，完成维修时通过
 * 单独的完成接口提交维修结果。</p>
 */
@Data
public class MaintenanceRecordDto {

    /**
     * 维修编号。
     *
     * <p>【待人工确认】维修编号由系统自动生成，业务层未读取该字段，请求中传入也不会生效；
     * 因可能属于对外接口契约的一部分，暂予保留，请人工确认后再决定是否删除。</p>
     */
    private String maintenanceCode;

    /** 设备类型：DRONE-无人机，BATTERY-电池，必填 */
    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    /** 设备ID，必填，须与设备类型对应的设备存在 */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /** 故障描述 */
    private String faultDescription;

    /** 维修内容 */
    private String maintenanceContent;

    /** 维修日期，为空时不修改 */
    private LocalDate maintenanceDate;

    /** 维修费用，为空时按 0 处理 */
    private BigDecimal maintenanceCost;

    /** 备注 */
    private String remark;
}
