package tech.cspioneer.backend.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.dto.request.MeetingCreateRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingUpdateRequest;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.MeetingService;

@RestController
@RequestMapping("/api/admin/meeting")
public class AdminMeetingController {

    private final MeetingService meetingService;

    public AdminMeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    // 创建会议（仅公司身份可访问）
    @PreAuthorize("hasAuthority('COMPANY')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createMeeting(
            @RequestBody MeetingCreateRequest res,
            @AuthenticationPrincipal UserDetails userDetails) {

        String useruuid = userDetails.getUsername();
        meetingService.createMeetingWithVersion(res, useruuid);

        System.out.println("会议创建成功：" + useruuid);
        return ResponseEntity.ok(ApiResponse.success(200, "会议创建成功", null));
    }

    // 更新会议（仅公司身份可访问）
    @PreAuthorize("hasAuthority('COMPANY')")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateMeeting(
            @RequestBody MeetingUpdateRequest res,
            @AuthenticationPrincipal UserDetails userDetails) {

        String useruuid = userDetails.getUsername();
        meetingService.updateMeetingWithVersion(res, useruuid);

        System.out.println("会议更新成功：" + useruuid);
        return ResponseEntity.ok(ApiResponse.success(200, "会议更新成功", null));
    }

    // 删除会议（仅公司身份可访问）
    @PreAuthorize("hasAuthority('COMPANY')")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteMeeting(
            @RequestBody String meetingUuid,
            @AuthenticationPrincipal UserDetails userDetails) {

        meetingService.deleteMeeting(meetingUuid);

        System.out.println("会议删除成功：" + meetingUuid);
        return ResponseEntity.ok(ApiResponse.success(200, "会议删除成功", null));
    }

    //获取参会申请列表-/api/admin/meeting/part/list/{page}{size}


    //获取参会申请详细信息-/api/admin/meeting/part/{part_uuid}
    @PreAuthorize("hasAnyAuthority('COMPANY')")
    @GetMapping("/part")
    public ResponseEntity<ApiResponse<Void>> getpartdetails(
            @RequestParam String partUuid,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        return ResponseEntity.ok(ApiResponse.success(200, "会议删除成功", null));

    }

}
