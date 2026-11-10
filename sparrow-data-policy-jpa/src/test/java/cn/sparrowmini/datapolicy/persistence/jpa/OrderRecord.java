package cn.sparrowmini.datapolicy.persistence.jpa;

import jakarta.persistence.*;

@Entity
@Table(name="test_order")
public class OrderRecord {
    @Id public Long id;
    public String region;
    public Integer cost;
    public String note;
    @Version public Long version;
    public OrderRecord() { }
    OrderRecord(long id, String region, int cost) {
        this.id=id; this.region=region; this.cost=cost;
    }
}
