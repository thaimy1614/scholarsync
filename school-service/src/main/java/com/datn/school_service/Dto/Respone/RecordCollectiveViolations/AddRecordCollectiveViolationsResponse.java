package com.datn.school_service.Dto.Respone.RecordCollectiveViolations;

import com.datn.school_service.Dto.Respone.RecordPersonalViolations.AddRecordPersonalViolationsResponse;
import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import com.datn.school_service.Dto.Respone.ViolationType.ViolationTypeResponse;
import com.datn.school_service.Models.ViolationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddRecordCollectiveViolationsResponse {
  private Long recordCollectiveViolationsId;
  private LocalDateTime createdAt;
  private String dayOfWeek;  //
  private GetUserNameResponse redFlagInfo;
  private GetUserNameResponse principalInfo;
  private int absentCount;
  private List<ViolationTypeResponse> violationTypes;
  private Long classId;
  private String className;
  private double violationPoint;
  private List<AddRecordPersonalViolationsResponse> addRecordPersonalViolations;
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
    this.dayOfWeek = convertToDayOfWeek(createdAt);
  }

  private String convertToDayOfWeek(LocalDateTime dateTime) {
    return switch (dateTime.getDayOfWeek()) {
      case MONDAY -> "Monday";
      case TUESDAY -> "Tuesday";
      case WEDNESDAY -> "Wednesday";
      case THURSDAY -> "Thursday";
      case FRIDAY -> "Friday";
      case SATURDAY -> "Saturday";
      case SUNDAY -> "Sunday";
    };
  }
}
