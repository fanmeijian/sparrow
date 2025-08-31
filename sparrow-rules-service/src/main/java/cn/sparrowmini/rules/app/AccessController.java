package cn.sparrowmini.rules.app;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/access")
@RequiredArgsConstructor
public class AccessController {

    private final AccessControlService accessService;

    @GetMapping("/check")
    public List<AccessDecision> checkAccess() {
        MemberGroup group = new MemberGroup("PREMIUM");
        Member member = new Member("user123", "GOLD", List.of(group));
        Article article = new Article("art001", "internal", 10);

        return accessService.evaluateAccess(member, article);
    }
}