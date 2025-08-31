//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import cn.sparrowmini.rules.huluwa.NetSaleSummary;
//import cn.sparrowmini.rules.huluwa.RebateService;
//import cn.sparrowmini.rules.huluwa.RebateServiceImpl;
//
//import java.math.BigDecimal;
//
//@SpringBootTest(classes = {RebateServiceImpl.class})
//public class RebateTest {
//    @Autowired
//    private RebateService rebateService;
//
//    @Test
//    public void test(){
//        NetSaleSummary netSaleSummary = new NetSaleSummary("","",2500, BigDecimal.valueOf(70000),null, null);
//        System.out.println("-------: " + this.rebateService.calc(netSaleSummary,null));
////        NetSaleSummary fee= null;
////        fee.getRebateAmount().add(fee.getRebatePrice().multiply(BigDecimal.valueOf((fee.getSaleCount()-0)*(1/1))));
//    }
//}
