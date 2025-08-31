//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import cn.sparrowmini.rules.huluwa.AddingRule;
//import cn.sparrowmini.rules.huluwa.DiscountRule;
//import cn.sparrowmini.rules.model.RuleTemplate;
//
//import java.math.BigDecimal;
//
//public class TestJava {
//
//    public static void main(String [] args) throws JsonProcessingException {
//        DiscountRule basePolicyRule = new DiscountRule(0,1500,BigDecimal.ONE,BigDecimal.valueOf(0.5));
//
//        AddingRule addingRule = new AddingRule(0,1500,BigDecimal.valueOf(0.5));
//
//        RuleTemplate ruleTemplate = new RuleTemplate();
//
//String ss ="rule \"加提$$endCount\"\n" +
//        "    when\n" +
//        "        $fee: NetSaleRebate(saleCount>$$startCount && saleCount<=$$endCount)\n" +
//        "    then\n" +
//        "        $fee.setRebatePrice(BigDecimal.valueOf($$price));\n" +
//        "        $fee.setRebateAmount(\n" +
//        "                $fee.getRebateAmount().add(\n" +
//        "                        $fee.getRebatePrice().multiply(\n" +
//        "                                BigDecimal.valueOf($fee.getSaleCount()-$$startCount)\n" +
//        "        )));\n" +
//        "        $fee.setSaleCount($$startCount);\n" +
//        "        update($fee);\n" +
//        "    end";
//        ruleTemplate.setTemplate(ss);
//        System.out.println(addingRule.toDrl());
//        System.out.println(new ObjectMapper().writeValueAsString(ruleTemplate));
//    }
//}
