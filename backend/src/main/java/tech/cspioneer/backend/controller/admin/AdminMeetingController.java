package tech.cspioneer.backend.controller.admin;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.cspioneer.backend.entity.Meeting;
import tech.cspioneer.backend.entity.dto.request.MeetingCreateRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingUpdateRequest;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.MeetingService;

@RestController
@RequestMapping("/api/admin/meeting")
public class AdminMeetingController {
    private  MeetingService meetingService;


    //创建会议
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createMeeting(
            @RequestBody MeetingCreateRequest res,
            @AuthenticationPrincipal UserDetails userDetails) {
        //1.获取当前用户的uuid
        String useruuid = userDetails.getUsername(); //这里我直接传uuid给service处理了
        //2.创建会议

        meetingService.createMeetingWithVersion(res,useruuid);
        System.out.println("会议创建成功"+useruuid);

        return ResponseEntity.ok(ApiResponse.success(200, "会议创建成功", null));

    }

    //更新会议
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateMeeting(
            @RequestBody MeetingUpdateRequest res,
            @AuthenticationPrincipal UserDetails userDetails) {
        //1.获取当前用户的uuid，这里的当前用户应该是编辑者
        String useruuid = userDetails.getUsername(); //这里我直接传uuid给service处理了
        //2.编辑会议

        meetingService.updateMeetingWithVersion(res,useruuid);
        System.out.println("会议更新成功"+useruuid);

        return ResponseEntity.ok(ApiResponse.success(200, "会议更新成功", null));

    }


//    //更新会议
//    @PostMapping("/update")
//    public ResponseEntity<ApiResponse<Void>> updateMeeting(
//            @RequestBody MeetingUpdateRequest res,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        //1.获取当前用户的uuid，这里的当前用户应该是编辑者
//        String useruuid = userDetails.getUsername(); //这里我直接传uuid给service处理了
//        //2.编辑会议
//
//        meetingService.updateMeetingWithVersion(res,useruuid);
//        System.out.println("会议更新成功"+useruuid);
//
//        return ResponseEntity.ok(ApiResponse.success(200, "会议更新成功", null));
//
//    }



}
