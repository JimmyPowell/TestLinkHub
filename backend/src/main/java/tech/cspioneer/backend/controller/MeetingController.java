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
import tech.cspioneer.backend.entity.dto.response.MeetingPartResponse;
import tech.cspioneer.backend.entity.dto.response.RootReviewResponse;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.MeetingPartService;
import tech.cspioneer.backend.service.MeetingService;

import java.time.LocalDateTime;
import java.util.List;


//这里是普通用户使用的接口（！！为了方便调试，这部分接口也开放给超级管理员，后面要改回来！！）
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
    public ResponseEntity<ApiResponse<MeetingPartResponse>> getMeetingPartDetail(
            @RequestParam String part_uuid,
            @AuthenticationPrincipal String useruuid
    ) {
        //根据申请uuid找参会详情
        MeetingPartResponse part = meetingPartService.findMeetingPartByUser(part_uuid);
        return ResponseEntity.ok(new ApiResponse<>(200,"获取参会详细信息成功",part));
    }

    //用户获取自己的参会申请列表-/api/user/meeting/part/list/{page}{size}+
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @GetMapping("/part/list")
    public ResponseEntity<ApiResponse<List<MeetingPartResponse>>> getUserPartList(
            @RequestParam int page,
            @RequestParam int size,
            @AuthenticationPrincipal String useruuid) {
        List<MeetingPartResponse> parts = meetingPartService.getMeetingPartsByUser(useruuid, page, size);
        return ResponseEntity.ok(new ApiResponse<>(200,"获取参会申请列表成功",parts));
    }


    //会议浏览-/api/user/meeting/list/{page}{size}+
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<RootReviewResponse>>> browseMeetings(
            @RequestParam int page,
            @RequestParam int size,
            @AuthenticationPrincipal String useruuid) {
        List<RootReviewResponse> meetings = meetingService.getPublishedMeetings(page, size);
        return ResponseEntity.ok(new ApiResponse<>(200,"浏览会议列表获取成功",meetings));
    }



    //会议详情-/api/user/meeting/{meeting_uuid}+
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<RootReviewResponse>> getMeetingDetails(
            @RequestParam String meeting_uuid,
            @AuthenticationPrincipal String useruuid
    ){
        RootReviewResponse details = meetingService.getMeetingDetails(meeting_uuid);
        return ResponseEntity.ok(ApiResponse.success(200, "获取会议详情成功", details));
    }


    //参加会议-/api/user/meeting/participant+
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @PostMapping("/participant")
    public ResponseEntity<ApiResponse<Void>> joinMeeting(
            @RequestBody MeetingParticipantRequest request,
            @AuthenticationPrincipal String useruuid) {
        try {
            meetingPartService.joinMeeting(request, useruuid);
            return ResponseEntity.ok(new ApiResponse<>(200,"参会申请发送成功",null));
        } catch (IllegalArgumentException e) {
            // 判断是否是重复报名异常
            if ("用户已报名该会议".equals(e.getMessage())) {
                return ResponseEntity.status(409)
                        .body(new ApiResponse<>(409, e.getMessage(), null));
            }
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, e.getMessage(), null));
        }
    }
    // 根据会议版本名称模糊分页查询接口
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @GetMapping("/versions/search/by-name")
    public ResponseEntity<ApiResponse<List<RootReviewResponse>>> searchMeetingVersionsByName(
            @RequestParam String name,
            @RequestParam int page,
            @RequestParam int size,
            @AuthenticationPrincipal String useruuid) {

        List<RootReviewResponse> results = meetingService.searchMeetingVersionsByName(name, page, size);
        return ResponseEntity.ok(new ApiResponse<>(200, "根据版本名称模糊查询会议版本成功", results));
    }

    // 根据会议版本 UUID 模糊分页查询接口
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @GetMapping("/versions/search/by-uuid")
    public ResponseEntity<ApiResponse<List<RootReviewResponse>>> searchMeetingVersionsByUuid(
            @RequestParam String uuid,
            @RequestParam int page,
            @RequestParam int size,
            @AuthenticationPrincipal String useruuid) {

        List<RootReviewResponse> results = meetingService.searchMeetingVersionsByUuid(uuid, page, size);
        return ResponseEntity.ok(new ApiResponse<>(200, "根据版本 UUID 模糊查询会议版本成功", results));
    }


    // 根据会议版本开始时间范围分页查询接口
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @GetMapping("/versions/search/by-start-time")
    public ResponseEntity<ApiResponse<List<RootReviewResponse>>> searchMeetingVersionsByStartTime(
            @RequestParam String start,    // 格式如 "2025-06-01T00:00:00"
            @RequestParam String end,      // 格式如 "2025-06-30T23:59:59"
            @RequestParam int page,
            @RequestParam int size,
            @AuthenticationPrincipal String useruuid) {

        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);

        List<RootReviewResponse> results =
                meetingService.searchMeetingVersionsByStartTimeRange(startTime, endTime, page, size);
        return ResponseEntity.ok(new ApiResponse<>(200, "根据开始时间范围查询会议版本成功", results));
    }


    // 取消参会申请 - /api/user/meeting/participant/cancel
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @DeleteMapping("/participant/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelMeetingParticipation(
            @RequestParam String part_uuid,
            @AuthenticationPrincipal String useruuid) {
        try {
            meetingPartService.cancelParticipation(part_uuid, useruuid);
            return ResponseEntity.ok(new ApiResponse<>(200, "取消参会成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(500, "服务器错误，取消失败", null));
        }
    }

}
