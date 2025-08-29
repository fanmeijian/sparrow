//package cn.sparrowmini.permission.model.relation;
//
//import cn.sparrowmini.common.model.BaseEntity;
//import cn.sparrowmini.common.model.TablePrefix;
//import cn.sparrowmini.pem.model.PermissionGroup;
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import lombok.*;
//import org.hibernate.envers.Audited;
//
//import java.io.Serializable;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@Entity(name = "pem_group_sysrole")
//@Table(name = TablePrefix.NAME + "pem_group_sysrole")
//public class GroupSysrole extends BaseEntity implements Serializable {
//
//    private static final long serialVersionUID = 1L;
//
//    @EqualsAndHashCode.Include
//    @EmbeddedId
//    @Audited
//    private GroupSysroleId id;
//
//    @JsonIgnore
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "groupId", insertable = false, updatable = false)
//    private PermissionGroup group;
//
////	@JsonIgnore
////	@ManyToOne(fetch = FetchType.EAGER)
////	@JoinColumn(name = "sysrole_id", insertable = false, updatable = false)
////	private Sysrole sysrole;
//
//    public GroupSysrole(GroupSysroleId f) {
//        this.id = f;
//    }
//
//    public GroupSysrole(String groupId, String sysroleId) {
//        this.id = new GroupSysroleId(groupId, sysroleId);
//    }
//
//
//    @Data
//    @NoArgsConstructor
//    @Embeddable
//    public static class GroupSysroleId implements Serializable {
//        /**
//         *
//         */
//        private static final long serialVersionUID = 1L;
//        private String groupId;
//        private String sysroleId;
//
//        public GroupSysroleId(String groupId, String sysroleId) {
//            this.groupId = groupId;
//            this.sysroleId = sysroleId;
//        }
//
//    }
//
//}
