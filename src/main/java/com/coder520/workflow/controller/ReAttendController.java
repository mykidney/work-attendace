package com.coder520.workflow.controller;

import com.coder520.user.entity.User;
import com.coder520.workflow.entity.ReAttend;
import com.coder520.workflow.service.ReAttendService;
import org.activiti.engine.task.Task;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("reAttend")
public class ReAttendController {
    @Autowired
    private ReAttendService reAttendService;
//    @RequiresPermissions("reAttend:list")
    @RequestMapping("/info")
    public String reAttend(Model model,HttpSession session){
        User user= (User) session.getAttribute("userinfo");
        List<ReAttend>reAttendList=reAttendService.listReAttend(user.getUsername());
        model.addAttribute("reAttendList",reAttendList);
        return "reAttend";
    }
    @RequestMapping("/start")
    public void startReAttendFlow(@RequestBody ReAttend reAttend, HttpSession session){
//        User user= (User) session.getAttribute("userinfo");
//        reAttend.setReAttendStarter(user.getRealName());
        reAttend.setReAttendStarter("laowang");
        reAttendService.startReAttendFlow(reAttend);
    }
    @RequiresPermissions("reAttend:list")
    @RequestMapping("/list")
//    @ResponseBody
    public String listReAttendFlow(Model model,HttpSession session){
        User user=(User)session.getAttribute("userinfo");
//        String userName="laowang666";
        List<ReAttend>tasks=reAttendService.listTasks(user.getUsername());
        model.addAttribute("tasks",tasks);
        return "reAttend";
    }
    @RequestMapping("/approve")
    public void approveReAttendFlow(@RequestBody ReAttend reAttend){
        reAttendService.approve(reAttend);
    }
}
