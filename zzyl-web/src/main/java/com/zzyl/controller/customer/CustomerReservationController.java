package com.zzyl.controller.customer;

import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.controller.BaseController;
import com.zzyl.dto.ReservationDto;
import com.zzyl.service.ReservationService;
import com.zzyl.utils.ObjectUtil;
import com.zzyl.utils.UserThreadLocal;
import com.zzyl.vo.ReservationVo;
import com.zzyl.vo.TimeCountVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/customer/reservation")
@Api(tags = "客户预约管理")
public class CustomerReservationController extends BaseController {

    @Resource
    private ReservationService reservationService;

    @GetMapping("/countByTime")
    @ApiOperation("查询每个时间段剩余预约次数")
    public ResponseResult<List<TimeCountVo>> countReservationsForEachTimeWithinTimeRange(@RequestParam(required = false) Long time) {
        Instant instant = Instant.ofEpochMilli(time);
        LocalDateTime ldt = LocalDateTime.ofInstant(instant,ZoneId.systemDefault());
        List<TimeCountVo> responseResult;
        try {
            responseResult = reservationService.countReservationsForEachTimeWithinTimeRange(ldt);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("查询失败");
        }
        return ResponseResult.success(responseResult);
    }

    @GetMapping("/cancelled-count")
    @ApiOperation("查询取消预约数量")
    public ResponseResult<Integer> getCancelledReservationCount() {
        Long userId = UserThreadLocal.getUserId();
        int num = reservationService.getCancelledReservationCount(userId);
        return success(num);
    }

    @PostMapping
    @ApiOperation("新增预约")
    public ResponseResult<Void> add(@RequestBody ReservationDto dto) {
        try {
            reservationService.add(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("预约失败");
        }
        return ResponseResult.success();
    }


    @GetMapping
    @ApiOperation("查询所有预约")
    public ResponseResult<List<ReservationVo>> findAll(@RequestParam(required = false) String mobile, @RequestParam(required = false) Long time) {
        //TODO 待实现
        return null;
    }
    /*
     *分页查询增加预约人姓名，手机号，状态，类型的查询条件
     */
    @GetMapping("/page")
    @ApiOperation("分页查询预约")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "name", value = "预约人姓名", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "phone", value = "手机号", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "状态", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "类型", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, dataType = "long", paramType = "query")
    })
    public ResponseResult<PageResponse<ReservationVo>> findByPage(@RequestParam(defaultValue = "1") int pageNum,
                                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                                  @RequestParam(required = false) String name,
                                                                  @RequestParam(required = false) String phone,
                                                                  @RequestParam(required = false) Integer status,
                                                                  @RequestParam(required = false) Integer type,
                                                                  @RequestParam(required = false) Long startTime,
                                                                  @RequestParam(required = false) Long endTime) {
        //分页查询预约
        LocalDateTime startldt = null;
        LocalDateTime endldt = null;
        if (!ObjectUtil.isEmpty(startTime) && !ObjectUtil.isEmpty(endTime)) {
            // 将时间戳转换为Instant对象
            Instant startInstant = Instant.ofEpochMilli(startTime);
            Instant endInstant = Instant.ofEpochMilli(endTime);
            // 将Instant对象转换为本地时间
            startldt = LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault());
            endldt = LocalDateTime.ofInstant(endInstant, ZoneId.systemDefault());
            System.out.println(startldt+"-------------------"+endldt);
        }
        PageResponse<ReservationVo> res = null;
        try {
            res = reservationService.findByPage(pageNum,pageSize,name,phone,status,type,startldt,endldt);
        } catch (Exception e) {
            return ResponseResult.error("查询失败");
        }
        return ResponseResult.success(res);
    }


    @PutMapping("/{id}/cancel")
    @ApiOperation("取消预约")
    public ResponseResult<Void> cancel(@PathVariable Long id) {
        try {
            reservationService.cancelReservation(id);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("取消失败");
        }
        return ResponseResult.success();
    }

}
