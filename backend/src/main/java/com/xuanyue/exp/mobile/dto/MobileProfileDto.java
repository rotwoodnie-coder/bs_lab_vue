package com.xuanyue.exp.mobile.dto;

import java.util.Date;

/**
 * 移动端个人中心用户信息 DTO（不包含敏感字段）
 */
public class MobileProfileDto {

    private String userId;
    private String userName;
    private String loginName;
    private String userNickName;
    private String userPhone;
    private String userEmail;
    private String userLogo;
    private String rootOrgId;
    private String rootOrgName;
    private String userOrgName;
    private String userRoleId;
    private String perResume;
    private Integer perScore;
    private String prefTitleId;
    private String prefTitleName;
    private Date createTime;
    private Date lastLoginTime;
    /** 学生作品数（已提交：待审核/通过/未通过，不含草稿） */
    private Integer worksCount;
    /** 学生作品累计获赞数 */
    private Integer totalLikes;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }

    public String getUserNickName() { return userNickName; }
    public void setUserNickName(String userNickName) { this.userNickName = userNickName; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserLogo() { return userLogo; }
    public void setUserLogo(String userLogo) { this.userLogo = userLogo; }

    public String getRootOrgId() { return rootOrgId; }
    public void setRootOrgId(String rootOrgId) { this.rootOrgId = rootOrgId; }

    public String getRootOrgName() { return rootOrgName; }
    public void setRootOrgName(String rootOrgName) { this.rootOrgName = rootOrgName; }

    public String getUserOrgName() { return userOrgName; }
    public void setUserOrgName(String userOrgName) { this.userOrgName = userOrgName; }

    public String getUserRoleId() { return userRoleId; }
    public void setUserRoleId(String userRoleId) { this.userRoleId = userRoleId; }

    public String getPerResume() { return perResume; }
    public void setPerResume(String perResume) { this.perResume = perResume; }

    public Integer getPerScore() { return perScore; }
    public void setPerScore(Integer perScore) { this.perScore = perScore; }

    public String getPrefTitleId() { return prefTitleId; }
    public void setPrefTitleId(String prefTitleId) { this.prefTitleId = prefTitleId; }

    public String getPrefTitleName() { return prefTitleName; }
    public void setPrefTitleName(String prefTitleName) { this.prefTitleName = prefTitleName; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(Date lastLoginTime) { this.lastLoginTime = lastLoginTime; }

    public Integer getWorksCount() { return worksCount; }
    public void setWorksCount(Integer worksCount) { this.worksCount = worksCount; }

    public Integer getTotalLikes() { return totalLikes; }
    public void setTotalLikes(Integer totalLikes) { this.totalLikes = totalLikes; }
}
