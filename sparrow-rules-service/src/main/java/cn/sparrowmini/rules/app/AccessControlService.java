package cn.sparrowmini.rules.app;

import cn.sparrowmini.rules.util.KieUtil;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccessControlService {


    @Transactional
    public List<AccessDecision> evaluateAccess(Member member, Article article) {
        KieSession session = KieUtil.getSessionByCode("member_download");//.getKieSession_("access-rules.drl");;

        // 事实插入
        session.insert(member);
        for (MemberGroup group : member.getGroups()) {
            session.insert(group); // 组也要单独插入
        }
        session.insert(article);

        // 收集结果
        List<AccessDecision> result = new ArrayList<>();
        session.setGlobal("accessResults", result);

        session.fireAllRules();

        // Collect decisions from working memory
        Collection<?> decisions = session.getObjects(o -> o instanceof AccessDecision);
        return decisions.stream()
                .map(o -> (AccessDecision) o)
                .collect(Collectors.toList());
    }
}