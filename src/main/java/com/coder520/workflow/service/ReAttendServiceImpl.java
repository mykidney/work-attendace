package com.coder520.workflow.service;

        import com.coder520.common.page.PageQueryBean;
        import com.coder520.workflow.dao.ReAttendMapper;
        import com.coder520.workflow.entity.ReAttend;
        import org.activiti.engine.HistoryService;
        import org.activiti.engine.RuntimeService;
        import org.activiti.engine.TaskService;
        import org.activiti.engine.runtime.ProcessInstance;
        import org.activiti.engine.task.Task;
        import org.apache.commons.collections.CollectionUtils;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.stereotype.Service;
        import org.springframework.transaction.annotation.Transactional;

        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

@Service("reAttendServiceImpl")
public class ReAttendServiceImpl implements ReAttendService {

    private static final java.lang.String RE_ATTEND_FLOW_ID="re_attend";
    //    任务关联补签数据键
    private static final String RE_ATTEND_SIGN = "re_attend";
    //    流程下一步处理人
    private static final String NEXT_HANDLER ="next_handler" ;
    private static final Byte RE_ATTEND_STATUS_ONGOING = 1;
    private static final Byte RE_ATTEND_sTATUS_PASS = 2;
    private static final Byte RE_ATTEND_REFUSE = 3;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ReAttendMapper reAttendMapper;

//    开启工作流
    @Override
    @Transactional
    public void startReAttendFlow(ReAttend reAttend) {
        //从公司组织架构中 查询到此人上级领导用户名
        reAttend.setCurrentHandler("laowang666");
        reAttend.setStatus(RE_ATTEND_STATUS_ONGOING);
        //插入数据库补签表
        reAttendMapper.insertSelective(reAttend);
        Map<String,Object>map=new HashMap();
        map.put(RE_ATTEND_SIGN,reAttend);
        map.put(NEXT_HANDLER,reAttend.getCurrentHandler());
//        启动补签流程实例
        ProcessInstance instance=runtimeService.startProcessInstanceByKey(RE_ATTEND_FLOW_ID,map);
//        System.out.println(instance.getId()+"-----"+instance.getActivityId());
//        用户提交补签任务
        Task task=taskService.createTaskQuery().processInstanceId(instance.getId()).singleResult();
//        System.out.println(task.getId()+"--"+task.getName());
//        Map<String,Object>map=new HashMap<String, Object>();
//        map.put("attend_morning","09:00");
//        map.put("attend_evening","18:30");
        taskService.complete(task.getId(),map);
    }

//    展示代办事项
    @Override
    public List<ReAttend> listTasks(String userName) {
        List<ReAttend>reAttendList=new ArrayList<ReAttend>();
        List<Task>taskList=taskService.createTaskQuery().processVariableValueEquals(userName).list();
//        转换成页面实体
        if (CollectionUtils.isNotEmpty(taskList)){
            for (Task task:taskList
                    ) {
                Map<String,Object>varible=taskService.getVariables(task.getId());
                ReAttend reAttend= (ReAttend) varible.get(RE_ATTEND_SIGN);
                reAttend.setTaskId(task.getId());
                reAttendList.add(reAttend);
            }
        }
        return reAttendList;
//        Map<String,Object>param=taskService.getVariables(taskList.get(0).getId());
//        return taskList;
    }
//处理工作流
    @Override
    @Transactional
    public void approve(ReAttend reAttend) {
        Task task=taskService.createTaskQuery().taskId(reAttend.getTaskId()).singleResult();
        if(RE_ATTEND_sTATUS_PASS.toString().equals(reAttend.getApproveFlag())){
            //审批通过 修改补签数据状态
            reAttend.setStatus(RE_ATTEND_sTATUS_PASS);
            reAttendMapper.updateByPrimaryKeySelective(reAttend);
        }else if (RE_ATTEND_REFUSE.toString().equals(reAttend.getApproveFlag())){
            reAttend.setStatus(RE_ATTEND_REFUSE);
            reAttendMapper.updateByPrimaryKeySelective(reAttend);
        }
        taskService.complete(reAttend.getTaskId());
    }

    @Override
    public List<ReAttend> listReAttend(String username) {
        List<ReAttend>reAttendList=new ArrayList<ReAttend>();
        reAttendList=reAttendMapper.selectByUserName(username);
        return reAttendList;
    }
}
