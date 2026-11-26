package learning.boot.day40;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class Day40FlowTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @Test void createReadUpdateAndRejectInvalidUpdate() throws Exception {
        MvcResult created = mvc.perform(post("/api/day40/teachers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"张老师\",\"subject\":\"Java\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.internalRemark").doesNotExist())
                .andReturn();
        JsonNode json = mapper.readTree(created.getResponse().getContentAsByteArray());
        String path = "/api/day40/teachers/" + json.get("id").asLong();
        assertEquals(path, created.getResponse().getHeader("Location"));
        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("张老师"));
        mvc.perform(put(path).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"李老师\",\"subject\":\"数学\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.subject").value("数学"));
        mvc.perform(put(path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("李老师"));
    }
    @Test void missingTeacherIsNotCreatedByUpdate() throws Exception {
        mvc.perform(put("/api/day40/teachers/-1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"张老师\",\"subject\":\"Java\"}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("TEACHER_NOT_FOUND"));
        mvc.perform(get("/api/day40/teachers/-1")).andExpect(status().isNotFound());
    }
}
