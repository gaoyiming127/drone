package com.drone.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 分页响应结果封装，列表类接口的返回体。
 *
 * <p>与前端约定的字段名为 data（当前页数据）、total（总条数）、pageSize、currentPage、totalPages。</p>
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> {

    /** 业务状态码，固定为 200 */
    private int code;

    /** 提示信息，固定为 success */
    private String message;

    /** 当前页数据 */
    private List<T> data;

    /** 满足条件的总条数 */
    private long total;

    /** 每页条数 */
    private long pageSize;

    /** 当前页码，从 1 开始 */
    private long currentPage;

    /** 总页数 */
    private long totalPages;

    /**
     * 由 MyBatis-Plus 的分页对象构造分页响应。
     *
     * @param page 分页查询结果
     * @param <T>  列表元素类型
     * @return 分页响应
     */
    public static <T> PageResult<T> success(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPageSize(page.getSize());
        result.setCurrentPage(page.getCurrent());
        result.setTotalPages(page.getPages());
        return result;
    }

    /**
     * 由列表与分页参数构造分页响应，总页数按总条数与每页条数向上取整。
     *
     * <p>【待人工确认】当前项目内未发现调用（各列表接口均使用 IPage 版本），
     * 因属于对外响应工具方法，暂予保留，请人工确认后再决定是否删除。</p>
     *
     * @param list        当前页数据
     * @param total       总条数
     * @param pageSize    每页条数
     * @param currentPage 当前页码
     * @param <T>         列表元素类型
     * @return 分页响应
     */
    public static <T> PageResult<T> success(List<T> list, long total, long pageSize, long currentPage) {
        PageResult<T> result = new PageResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(list);
        result.setTotal(total);
        result.setPageSize(pageSize);
        result.setCurrentPage(currentPage);
        result.setTotalPages((long) Math.ceil((double) total / pageSize));
        return result;
    }
}
