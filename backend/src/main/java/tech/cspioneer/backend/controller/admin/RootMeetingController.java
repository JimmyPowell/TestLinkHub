package tech.cspioneer.backend.controller.admin;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.MeetingParticipant;
import tech.cspioneer.backend.entity.MeetingVersion;
import tech.cspioneer.backend.entity.dto.request.MeetingCreateRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingPartReviewRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingReviewRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingUpdateRequest;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.MeetingPartService;
import tech.cspioneer.backend.service.MeetingService;

import java.util.List;

@RestController
@RequestMapping("/api/root/meeting")
public class RootMeetingController {


    //会议审核-/api/root/meeting/review管理员审核创建会议+
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/review")
    public ResponseEntity<ApiResponse<Void>> reviewMeeting(
            @RequestBody MeetingReviewRequest res,
            @AuthenticationPrincipal String useruuid){
        meetingService.reviewMeetingCreate(res, useruuid);
        return ResponseEntity.ok(new ApiResponse<>(200,"审核完成",null));
    }

    //审核会议浏览-/api/root/reviewlist/{page}{size}+
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/reviewlist")
    public ResponseEntity<ApiResponse<List<MeetingVersion>>> getMeetingReviewList(
            @RequestParam int page,
            @RequestParam int size,
            @AuthenticationPrincipal String useruuid
    ){
        List<MeetingVersion> reviewList = meetingService.getPendingReviewList(page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "查询成功", reviewList));
    }


    //会议详情-/api/root/meeting/meeting_version_uuid(这是审核的时候用的，所以前端应该是会议版本列表)+
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<MeetingVersion>> getMeetingDetails(
            @RequestParam String meeting_version_uuid,
            @AuthenticationPrincipal String useruuid
    ){
        MeetingVersion details = meetingService.getMeetingVersionDetails(meeting_version_uuid);
        return ResponseEntity.ok(ApiResponse.success(200, "获取会议版本详情成功", details));
    }




    @Autowired
    private MeetingService meetingService;
    @Autowired
    private MeetingPartService meetingPartService;

    // 创建会议（仅公司身份可访问）
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createMeeting(
            @RequestBody MeetingCreateRequest res,
            @AuthenticationPrincipal String useruuid) {

        meetingService.createMeetingWithVersion(res, useruuid);

        System.out.println("会议创建成功：" + useruuid);
        return ResponseEntity.ok(ApiResponse.success(200, "会议创建成功", null));
    }

    // 更新会议（仅公司身份可访问）
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateMeeting(
            @RequestBody MeetingUpdateRequest res,
            @AuthenticationPrincipal String useruuid) {
        meetingService.updateMeetingWithVersion(res, useruuid);

        System.out.println("会议更新成功：" + useruuid);
        return ResponseEntity.ok(ApiResponse.success(200, "会议更新成功", null));
    }

    // 删除会议（仅公司身份可访问）
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteMeeting(
            @RequestBody String meetingUuid,
            @AuthenticationPrincipal String useruuid) {

        meetingService.deleteMeeting(meetingUuid);

        System.out.println("会议删除成功：" + meetingUuid);
        return ResponseEntity.ok(ApiResponse.success(200, "会议删除成功", null));
    }





    //获取参会申请列表-/api/admin/meeting/part/list/{page}{size}
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<MeetingParticipant>>> getMeetingPartList(
            @AuthenticationPrincipal String useruuid,
            @RequestParam int page,
            @RequestParam int size
    ){
        List<MeetingParticipant> list = meetingPartService.getMeetingPartsByCreator(useruuid,page,size);

        //查找改用户名下的会议的申请
        return ResponseEntity.ok(ApiResponse.success(200, "返回列表成功", list));
    }


    //获取参会申请详细信息-/api/admin/meeting/part/{part_uuid}
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/part")
    public ResponseEntity<ApiResponse<MeetingParticipant>> getpartdetails(
            @RequestParam String partUuid,
            @AuthenticationPrincipal String useruuid
    ){
        //1.判断是不是会议创建人，先从part表中找出对应的参会会议，然后看会议的创建人，然后判断这俩是否一致
        MeetingParticipant part = meetingPartService.findMeetingPartByUuid(partUuid);
        Boolean is = meetingPartService.isCreator(useruuid,part);
        if(!is){
            return ResponseEntity.ok(ApiResponse.error(403, "您不是会议创建者，无权访问"));
        }

        //2.获取参会申请详细信息
        return ResponseEntity.ok(ApiResponse.success(200, "获取参会详细信息成功", part));

    }


    //审核参会-/api/admin/meeting/partreview/
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/partreview")
    public ResponseEntity<ApiResponse<Void>> reviewMeetingParticipant(
            @RequestBody MeetingPartReviewRequest req,
            @AuthenticationPrincipal String useruuid
    ) {
        //1.查找对应的会议，确定是不是会议创建者
        MeetingParticipant part = meetingPartService.findMeetingPartByUuid(req.getPartUuid());
        boolean isCreator = meetingPartService.isCreator(useruuid, part);
        if (!isCreator) {
            return ResponseEntity.ok(ApiResponse.error(403, "您不是会议创建者，无法审核此申请"));
        }
        //2.保存审核参会信息，
        meetingPartService.reviewpart(req);
        return ResponseEntity.ok(ApiResponse.success(200, "审核完成", null));
    }

}
