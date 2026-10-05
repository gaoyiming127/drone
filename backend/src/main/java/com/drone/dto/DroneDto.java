package com.drone.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * 无人机新增/修改请求参数。
 *
 * <p>累计飞行次数与累计飞行分钟不在此传入，由登记飞行记录时自动累加。</p>
 */
@Data
public class DroneDto {

    /** 设备编号，必填且全局唯一 */
    @NotBlank(message = "设备编号不能为空")
    private String droneCode;

    /** 设备名称，必填 */
    @NotBlank(message = "设备名称不能为空")
    private String droneName;

    /** 品牌 */
    private String brand;

    /** 型号 */
    private String model;

    /** 序列号 */
    private String serialNumber;

    /** 购买日期 */
    private LocalDate purchaseDate;

    /** 设备状态，新增时为空按 IDLE 处理；修改时留空表示不修改状态 */
    private String status;

    /** 备注 */
    private String remark;
}
