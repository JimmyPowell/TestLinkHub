package tech.cspioneer.backend.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.MeetingParticipant;
import tech.cspioneer.backend.entity.MeetingVersion;
import tech.cspioneer.backend.entity.dto.request.MeetingCreateRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingPartReviewRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.MeetingApplicationResponse;
import tech.cspioneer.backend.entity.dto.response.MeetingVersionWithMeetingUuidResponse;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.MeetingPartService;
import tech.cspioneer.backend.service.MeetingService;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;


//这是供公司管理员使用的接口合集
@RestController
@RequestMapping("/api/admin/meeting")
public class AdminMeetingController {
    @Autowired
    private MeetingService meetingService;
    @Autowired
    private MeetingPartService meetingPartService;


    // 创建会议，管理员创建的会议需要申请
    @PreAuthorize("hasAuthority('COMPANY')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createMeeting(
            @RequestBody MeetingCreateRequest res,
            @AuthenticationPrincipal String useruuid) {

        try {
            meetingService.createMeetingWithVersion(res, useruuid);
            return ResponseEntity.ok(ApiResponse.success(200, "会议创建成功", null));
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细异常堆栈
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "请求体格式错误：" + e.getMessage()));
        }
    }

    // 更新会议（仅公司身份可访问）
    @PreAuthorize("hasAuthority('COMPANY')")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateMeeting(
            @RequestBody MeetingUpdateRequest res,
            @AuthenticationPrincipal String useruuid) {
        meetingService.updateMeetingWithVersion(res, useruuid);

        System.out.println("会议更新成功：" + useruuid);
        return ResponseEntity.ok(ApiResponse.success(200, "会议更新成功", null));
    }

    // 删除会议（仅公司身份可访问）
    @PreAuthorize("hasAuthority('COMPANY')")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteMeeting(
            @RequestParam String meeting_uuid,
            @AuthenticationPrincipal String useruuid) {

        meetingService.deleteMeeting(meeting_uuid);

        System.out.println("会议删除成功：" + meeting_uuid);
        return ResponseEntity.ok(ApiResponse.success(200, "会议删除成功", null));
    }





    //获取参会申请列表-/api/admin/meeting/part/list/{page}{size}
    @PreAuthorize("hasAnyAuthority('COMPANY')")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<MeetingApplicationResponse>>> getMeetingPartList(
            @AuthenticationPrincipal String useruuid,
            @RequestParam(required = false) String status,
            @RequestParam int page,
            @RequestParam int size
    ){
        //获取参会申请列表
        List<MeetingApplicationResponse> list = meetingPartService.getMeetingPartsByCreator(useruuid, status, page, size);

        //查找改用户名下的会议的申请
        return ResponseEntity.ok(ApiResponse.success(200, "返回列表成功", list));
    }


    //获取参会申请详细信息-/api/admin/meeting/part/{part_uuid}
    @PreAuthorize("hasAnyAuthority('COMPANY')")
    @GetMapping("/part")
    public ResponseEntity<ApiResponse<MeetingParticipant>> getpartdetails(
            @RequestParam String part_uuid,
            @AuthenticationPrincipal String useruuid
    ){
        //1.判断是不是会议创建人，先从part表中找出对应的参会会议，然后看会议的创建人，然后判断这俩是否一致
        MeetingParticipant part = meetingPartService.findMeetingPartByUuid(part_uuid);
        Boolean is = meetingPartService.isCreator(useruuid,part);
        if(!is){
            return ResponseEntity.ok(ApiResponse.error(403, "您不是会议创建者，无权访问"));
        }

        //2.获取参会申请详细信息
        return ResponseEntity.ok(ApiResponse.success(200, "获取参会详细信息成功", part));

    }


    //审核参会-/api/admin/meeting/partreview/
    @PreAuthorize("hasAnyAuthority('COMPANY')")
    @PostMapping("/partreview")
    public ResponseEntity<ApiResponse<Void>> reviewMeetingParticipant(
            @RequestBody MeetingPartReviewRequest req,
            @AuthenticationPrincipal String useruuid
    ) {
        // 1. 查找对应的参会申请
        MeetingParticipant part = meetingPartService.findMeetingPartByUuid(req.getPartUuid());
        if (part == null) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "找不到该参会申请"));
        }

        // 2. 确定是不是会议创建者
        boolean isCreator = meetingPartService.isCreator(useruuid, part);
        if (!isCreator) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "您不是会议创建者，无法审核此申请"));
        }
        // 3. 保存审核参会信息
        meetingPartService.reviewpart(req);
        return ResponseEntity.ok(ApiResponse.success(200, "审核完成", null));
    }


    //获取会议创建/修改列表
    @PreAuthorize("hasAnyAuthority('COMPANY')")
    @GetMapping("/version/application/list")
    public ResponseEntity<ApiResponse<List<MeetingVersionWithMeetingUuidResponse>>> getCreatedMeetingVersions(
            @AuthenticationPrincipal String useruuid,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<MeetingVersionWithMeetingUuidResponse> versions = meetingService.getMeetingVersionsByCreator(useruuid, name, startTime, endTime, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "获取创建的会议版本成功", versions));
    }

    @PreAuthorize("hasAnyAuthority('COMPANY')")
    @GetMapping("/version/detail")
    public ResponseEntity<ApiResponse<MeetingVersion>> getMeetingVersionDetail(
            @RequestParam String uuid,
            @AuthenticationPrincipal String useruuid) {

        MeetingVersion version = meetingService.getMeetingVersionDetail(uuid, useruuid);
        if (version == null) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "无权限查看该会议版本"));
        }

        return ResponseEntity.ok(ApiResponse.success(200, "获取会议版本详情成功", version));
    }

}
