package tech.cspioneer.backend.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class MeetingController {

    //获取参会申请详细信息-/api/user/meeting/part/{part_uuid}

    //用户获取自己的参会申请列表-/api/user/meeting/part/list/{page}{size}

    //会议浏览-/api/user/meeting/list/{page}{size}

    //会议详情-/api/user/meeting/meeting_uuid/{meeting_uuid}

    //参加会议-/api/user/meeting/participant
}
