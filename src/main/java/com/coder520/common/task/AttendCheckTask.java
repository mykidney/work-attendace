package com.coder520.common.task;

import com.coder520.attend.service.AttendService;
import org.springframework.beans.factory.annotation.Autowired;

public class AttendCheckTask {
    @Autowired
    private AttendService attendService;
    public void checkAttend(){
        //首先获取 今天没打卡的人 给他插入打卡记录 并设置为异常 缺勤480分钟

        //如果有打卡记录 检查早晚打卡 看看考勤是不是正常
        attendService.checkAttend();

    }
}
