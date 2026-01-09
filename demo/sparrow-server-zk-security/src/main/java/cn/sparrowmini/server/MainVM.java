package cn.sparrowmini.server;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.ListModelList;

import java.util.Objects;

@Component("mainVM")
@Scope("prototype")
@Getter
@VariableResolver(DelegatingVariableResolver.class)
public class MainVM {

    @Setter
    private String searchText;


    // 使用 ListModelList 会自动处理 UI 刷新
    private final ListModelList<MyTab> tabList = new ListModelList<>();

    @Setter
    private int selectedIndex=0;

    public boolean isSelected(int index){
        return selectedIndex==index;
    }

    @Command
    @NotifyChange({"tabList", "selectedIndex"})
    public void closeTab(int index){
        tabList.remove(index);
        this.selectedIndex=index>0? index-1:0;
    }

    @Init
    public void init() {
        tabList.add(new MyTab("首页", "~./zul/home.zul", false));
//        selectedIndex = 0;
    }

    @GlobalCommand("navigate")
    @NotifyChange({"tabList", "selectedIndex"})
    public void onNavigate(String url, String name) {
        for (int i = 0; i < tabList.size(); i++) {
            if (tabList.get(i).getTitle().equals(name)) {
                selectedIndex = i;
                return;
            }
        }
        MyTab newTab=new MyTab(name, "~./zul" + url + ".zul", true);
        tabList.add(newTab);
        selectedIndex = tabList.size() - 1;
    }

    @Command
    public void logout(){
        Executions.sendRedirect("/logout");
    }



    @Getter
    @Setter
    public static class MyTab {
        private String title;
        private String url;
        private boolean closeable;

        public MyTab(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public MyTab(String title, String url, boolean closeable) {
            this.title = title;
            this.url = url;
            this.closeable = closeable;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyTab myTab = (MyTab) o;
            return Objects.equals(title, myTab.title);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(title);
        }
    }
}
