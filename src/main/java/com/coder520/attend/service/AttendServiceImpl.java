package com.coder520.attend.service;

import com.coder520.attend.dao.AttendMapper;
import com.coder520.attend.entity.Attend;
import com.coder520.attend.vo.QueryCondition;
import com.coder520.common.page.PageQueryBean;
import com.coder520.common.utils.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("attendServiceImpl")
public class AttendServiceImpl implements AttendService {

    private static final int NOON_HOUR = 12;
    private static final int NOON_MINUTE = 00;
    private static final Integer ABSENCE_DAY = 480;
    private static final Byte ATTEND_STATUS_ABNORMAL = 2;
    private static final Byte ATTEND_STATUS_NORMAL = 1;
    private static final int MORNING_HOUR = 9;
    private static final int MORNING_MINUTE = 30;
    private static final int EVENING_HOUR = 18;
    private static final int EVENING_MINUTE = 30;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Log log = LogFactory.getLog(AttendServiceImpl.class);
    @Autowired
    private AttendMapper attendMapper;

    @Override
    public void signAttend(Attend attend) throws Exception {
        try {
            Date today = new Date();
            attend.setAttendDate(today);
            attend.setAttendWeek((byte) DateUtils.getTodayWeek());
//            查询当天 此人有没有打卡记录
            Attend todayRecord = attendMapper.selectTodaySignRecord(attend.getUserId());
            Date noon = DateUtils.getDate(NOON_HOUR, NOON_MINUTE);
            Date morningAttend=DateUtils.getDate(MORNING_HOUR,MORNING_MINUTE);
            if (todayRecord == null) {
//                打卡记录不存在
                if (today.compareTo(noon) <= 0) {
                    //打卡时间 早于十二点 上午打卡
                    attend.setAttendMoring(today);
                    if (today.compareTo(morningAttend) > 0) {
                        //大于九点半迟到了
                        attend.setAttendStatus(ATTEND_STATUS_ABNORMAL);
                        attend.setAbsence(DateUtils.getMunite(morningAttend, today));
                    }
                } else {
//                    晚上打卡
                    attend.setAttendEvening(today);
                }
                attendMapper.insertSelective(attend);
            } else {
                if (today.compareTo(noon) <= 0) {
                    return;
                } else {
//                    晚上打卡
                    todayRecord.setAttendEvening(today);
                    //判断打卡时间是不是18.30以后 是不是早退
                    Date eveningAttend=DateUtils.getDate(EVENING_HOUR,EVENING_MINUTE);
                    if (today.compareTo(eveningAttend)<0){
                        //早于下午六点半 早退
                        todayRecord.setAttendStatus(ATTEND_STATUS_ABNORMAL);
                        todayRecord.setAbsence(DateUtils.getMunite(today,eveningAttend));
                    }else {
                        todayRecord.setAttendStatus(ATTEND_STATUS_NORMAL);
                        todayRecord.setAbsence(0);
                    }
                    attendMapper.updateByPrimaryKeySelective(todayRecord);
                }

            }


//            中午十二点之前打卡 都算早晨打卡 十二点以后都算下午打卡 下午打卡 检查与上午打卡时间差 不足八小时都算异常 缺席多长时间 要存进去
//            十二点以后打卡都算下午打卡
//            下午打卡 检查与上午打卡时间差 18点之前 算异常
//            不足八小时都算异常 并且 缺席多长时间要存进去

        } catch (Exception e) {
            log.error("用户签到异常", e);
            throw e;
        }
    }

    @Override
    public PageQueryBean listAttend(QueryCondition condition) {
//        根据条件查询count记录数目
//        如果有记录 才去查询分页数据 没有相关记录数目 没必要去查分页数据
        int count = attendMapper.countByCondition(condition);
        PageQueryBean pageResult = new PageQueryBean();
        if (count > 0) {
            pageResult.setPageSize(condition.getPageSize());
            pageResult.setTotalRows(count);
            pageResult.setCurrentPage(condition.getCurrentPage());

            List<Attend> attendList = attendMapper.selectAttendPage(condition);
            pageResult.setItems(attendList);
        }
        return pageResult;
    }

    @Override
    @Transactional
    public void checkAttend() {
        //查询缺勤用户id 插入打卡记录 并设置为异常 缺勤480分钟
        List<Long> userIdList = attendMapper.selectTodayAbsence();
        if (CollectionUtils.isNotEmpty(userIdList)) {
            List<Attend> attendList = new ArrayList();
            for (Long userId : userIdList) {
                Attend attend = new Attend();
                attend.setUserId(userId);
                attend.setAttendDate(new Date());
                attend.setAttendWeek((byte) DateUtils.getTodayWeek());
                attend.setAbsence(ABSENCE_DAY);
                attend.setAttendStatus(ATTEND_STATUS_ABNORMAL);
                attendList.add(attend);
            }
            attendMapper.batchInsert(attendList);
        }
        //检查晚打卡 将下班未打卡 记录设置为异常
        List<Attend> absenceList = attendMapper.selectTodayEveningAbsence();
        if (CollectionUtils.isNotEmpty(absenceList)) {
            for (Attend attend : absenceList) {
                attend.setAbsence(ABSENCE_DAY);
                attend.setAttendStatus(ATTEND_STATUS_ABNORMAL);
//                没有批量更新语句
                attendMapper.updateByPrimaryKeySelective(attend);
            }
        }
    }
}
