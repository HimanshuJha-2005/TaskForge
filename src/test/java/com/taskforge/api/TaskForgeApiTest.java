package com.taskforge.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskForgeApiTest {

  @Autowired private MockMvc mvc;

  @Test
  void healthIsPublic() throws Exception {
    mvc.perform(get("/api/health")).andExpect(status().isOk());
  }

  @Test
  void developerCannotCreateProject() throws Exception {
    String dev = register("dev1@example.com", "DEVELOPER");
    mvc.perform(
            post("/api/projects")
                .header("Authorization", "Bearer " + dev)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Shop","key":"SHOP","description":"store"}\
                    """))
        .andExpect(status().isForbidden());
  }

  @Test
  void fullIssueLifecycle() throws Exception {
    String pm = register("pm1@example.com", "PROJECT_MANAGER");
    String dev = register("dev2@example.com", "DEVELOPER");
    long projectId = createProject(pm, "Shop", "SHOP");
    addMember(pm, projectId, "dev2@example.com");

    long issueId = createIssue(dev, projectId, "Checkout bug", "HIGH");

    // OPEN -> RESOLVED directly is illegal.
    mvc.perform(
            patch("/api/issues/" + issueId + "/status")
                .header("Authorization", "Bearer " + dev)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status":"RESOLVED"}\
                    """))
        .andExpect(status().isUnprocessableEntity());

    for (String s : new String[] {"IN_PROGRESS", "IN_REVIEW", "RESOLVED", "CLOSED"}) {
      mvc.perform(
              patch("/api/issues/" + issueId + "/status")
                  .header("Authorization", "Bearer " + dev)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("""
                      {"status":"%s"}\
                      """.formatted(s)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value(s));
    }

    // Comments work for members.
    mvc.perform(
            post("/api/issues/" + issueId + "/comments")
                .header("Authorization", "Bearer " + dev)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"body":"Fixed in PR #42"}\
                    """))
        .andExpect(status().isCreated());
    mvc.perform(
            get("/api/issues/" + issueId + "/comments")
                .header("Authorization", "Bearer " + dev))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));

    // Dashboard aggregates the project.
    mvc.perform(
            get("/api/projects/" + projectId + "/dashboard")
                .header("Authorization", "Bearer " + pm))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalIssues").value(1))
        .andExpect(jsonPath("$.byStatus.CLOSED").value(1));
  }

  @Test
  void nonMemberIsDenied() throws Exception {
    String pm = register("pm2@example.com", "PROJECT_MANAGER");
    String outsider = register("outsider@example.com", "DEVELOPER");
    long projectId = createProject(pm, "Bank", "BANK");

    mvc.perform(get("/api/projects/" + projectId).header("Authorization", "Bearer " + outsider))
        .andExpect(status().isForbidden());
    mvc.perform(
            get("/api/issues?projectId=" + projectId).header("Authorization", "Bearer " + outsider))
        .andExpect(status().isForbidden());
  }

  @Test
  void duplicateProjectKeyReturns409() throws Exception {
    String pm = register("pm3@example.com", "PROJECT_MANAGER");
    createProject(pm, "One", "DUP");
    mvc.perform(
            post("/api/projects")
                .header("Authorization", "Bearer " + pm)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Two","key":"DUP","description":"x"}\
                    """))
        .andExpect(status().isConflict());
  }

  @Test
  void searchAndFilterIssues() throws Exception {
    String pm = register("pm4@example.com", "PROJECT_MANAGER");
    long projectId = createProject(pm, "App", "APP");
    createIssue(pm, projectId, "Login page crash", "CRITICAL");
    createIssue(pm, projectId, "Footer typo", "LOW");

    mvc.perform(
            get("/api/issues?projectId=" + projectId + "&query=login")
                .header("Authorization", "Bearer " + pm))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1));
    mvc.perform(
            get("/api/issues?projectId=" + projectId + "&priority=LOW")
                .header("Authorization", "Bearer " + pm))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  private String register(String email, String role) throws Exception {
    MvcResult result =
        mvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"email":"%s","password":"password123","role":"%s"}\
                        """
                        .formatted(email, role)))
            .andExpect(status().isCreated())
            .andReturn();
    return result.getResponse().getContentAsString().split("\"token\":\"")[1].split("\"")[0];
  }

  private long createProject(String token, String name, String key) throws Exception {
    MvcResult result =
        mvc.perform(
                post("/api/projects")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"name":"%s","key":"%s","description":"d"}\
                        """
                        .formatted(name, key)))
            .andExpect(status().isCreated())
            .andReturn();
    return Long.parseLong(result.getResponse().getContentAsString().split("\"id\":")[1].split(",")[0]);
  }

  private void addMember(String pmToken, long projectId, String email) throws Exception {
    mvc.perform(
            post("/api/projects/" + projectId + "/members")
                .header("Authorization", "Bearer " + pmToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"%s"}\
                    """.formatted(email)))
        .andExpect(status().isOk());
  }

  private long createIssue(String token, long projectId, String title, String priority)
      throws Exception {
    MvcResult result =
        mvc.perform(
                post("/api/issues")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"projectId":%d,"title":"%s","priority":"%s"}\
                        """
                        .formatted(projectId, title, priority)))
            .andExpect(status().isCreated())
            .andReturn();
    return Long.parseLong(result.getResponse().getContentAsString().split("\"id\":")[1].split(",")[0]);
  }
}
