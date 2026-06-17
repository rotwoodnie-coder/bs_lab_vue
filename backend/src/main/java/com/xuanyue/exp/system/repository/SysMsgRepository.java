package com.xuanyue.exp.system.repository;

import com.xuanyue.exp.system.entity.SysMsg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysMsgRepository extends JpaRepository<SysMsg, String> {
    List<SysMsg> findByReceiverUserIdOrderBySendTimeDesc(String receiverUserId);
    List<SysMsg> findByReceiverUserIdAndMsgTypeIdOrderBySendTimeDesc(String receiverUserId, String msgTypeId);
    List<SysMsg> findByReceiverUserIdAndLinkIdOrderBySendTimeDesc(String receiverUserId, String linkId);
}
