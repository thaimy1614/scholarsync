package com.datn.school_service.Dto.Respone.ClassSubjectResponse;
import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.security.auth.Subject;
import java.util.Set;
import java.util.HashSet;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassInClassSubjectResponse {
  private String ClassName;
  private String schoolYear;
  private GetUserNameResponse teacher;

}
