package learning.boot.day39;

import learning.boot.day35.TeacherResponse;
import learning.boot.day37.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(Day37ExceptionController.class)
class Day39WebTest {
    @Autowired private MockMvc mvc;
    // 替换业务实现，仅验证 MVC 路由、绑定、校验和异常转换。
    @MockBean private Day37TeacherService service;

    @Test void returnsTeacherJson() throws Exception {
        when(service.find(10001L)).thenReturn(new TeacherResponse(10001L, "张老师", "Java"));
        mvc.perform(get("/api/day37/teachers/10001"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("张老师"))
                .andExpect(jsonPath("$.internalRemark").doesNotExist());
        verify(service).find(10001L);
    }
    @Test void convertsBusinessFailure() throws Exception {
        when(service.find(99999L)).thenThrow(new TeacherNotFoundException());
        mvc.perform(get("/api/day37/teachers/99999"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("TEACHER_NOT_FOUND"));
    }
    @Test void rejectsInvalidField() throws Exception {
        mvc.perform(post("/api/day37/teachers/preview").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"subject\":\"Java\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("name")));
    }
    @Test void rejectsInvalidPathBeforeService() throws Exception {
        mvc.perform(get("/api/day37/teachers/abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
        verifyNoInteractions(service);
    }
    @Test void rejectsBrokenJson() throws Exception {
        mvc.perform(post("/api/day37/teachers/preview").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_BODY"));
    }
    @Test void hidesUnexpectedDetails() throws Exception {
        when(service.simulateFailure()).thenThrow(new IllegalStateException("PRIVATE_TEST_DETAIL"));
        mvc.perform(get("/api/day37/teachers/simulate-error"))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.requestId", not(emptyOrNullString())))
                .andExpect(content().string(not(containsString("PRIVATE_TEST_DETAIL"))));
    }
}
