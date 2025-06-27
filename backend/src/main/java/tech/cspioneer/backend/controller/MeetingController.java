package tech.cspioneer.backend.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.MeetingParticipant;
import tech.cspioneer.backend.entity.MeetingVersion;
import tech.cspioneer.backend.entity.dto.request.MeetingParticipantRequest;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.MeetingPartService;
import tech.cspioneer.backend.service.MeetingService;

import java.util.List;

@RestController
@RequestMapping("/api/user/meeting")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;
    @Autowired
    private MeetingPartService meetingPartService;


    //获取参会申请详细信息-/api/user/meeting/part/{part_uuid}+
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @GetMapping("/part")
    public ResponseEntity<ApiResponse<MeetingParticipant>> getMeetingPartDetail(
            @RequestParam String part_uuid,
            @AuthenticationPrincipal String useruuid
    ) {
        //根据申请uuid找参会详情
        MeetingParticipant part = meetingPartService.findMeetingPartByUuid(part_uuid);
        return ResponseEntity.ok(new ApiResponse<>(200,"获取参会详细信息成功",part));
    }

    //用户获取自己的参会申请列表-/api/user/meeting/part/list/{page}{size}+
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @GetMapping("/part/list/")
    public ResponseEntity<ApiResponse<List<MeetingParticipant>>> getUserPartList(
            @RequestParam int page,
            @RequestParam int size,
            @AuthenticationPrincipal String useruuid) {
        List<MeetingParticipant> parts = meetingPartService.getMeetingPartsByUser(useruuid, page, size);
        return ResponseEntity.ok(new ApiResponse<>(200,"获取参会申请列表成功",parts));
    }


    //会议浏览-/api/user/meeting/list/{page}{size}+
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @GetMapping("/list/")
    public ResponseEntity<ApiResponse<List<MeetingVersion>>> browseMeetings(
            @RequestParam int page,
            @RequestParam int size,
            @AuthenticationPrincipal String useruuid) {
        List<MeetingVersion> meetings = meetingService.getPublishedMeetings(page, size);
        return ResponseEntity.ok(new ApiResponse<>(200,"浏览会议列表获取成功",meetings));
    }



    //会议详情-/api/user/meeting/{meeting_uuid}+
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<MeetingVersion>> getMeetingDetails(
            @RequestParam String meeting_uuid,
            @AuthenticationPrincipal String useruuid
    ){
        MeetingVersion details = meetingService.getMeetingDetails(meeting_uuid);
        return ResponseEntity.ok(ApiResponse.success(200, "获取会议详情成功", details));
    }


    //参加会议-/api/user/meeting/participant+
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @GetMapping("/participant")
    public ResponseEntity<ApiResponse<Void>> joinMeeting(
            @RequestBody MeetingParticipantRequest request,
            @AuthenticationPrincipal String useruuid) {
        meetingPartService.joinMeeting(request, useruuid);
        return ResponseEntity.ok(new ApiResponse<>(200,"参会申请发送成功",null));
    }

}
