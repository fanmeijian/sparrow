package cn.sparrowmini.common.repository;

import cn.sparrowmini.common.model.BaseState;
import cn.sparrowmini.common.model.CommonStateEnum;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@NoRepositoryBean
public interface BaseStateRepository<S extends BaseState, ID> extends BaseOpLogRepository<S, ID>{

    default void updateState(Set<ID> idSet, String state){
        List<S> references = new ArrayList<>();
        idSet.forEach(id->{
            if(existsById(id)){
                S referenceById = getReferenceById(id);
                referenceById.setStat(state);
                references.add(referenceById);
            }
        });
        saveAll(references);
    }

    default void updateEntityState(Set<ID> idSet, CommonStateEnum entitySate){
        List<S> references = new ArrayList<>();
        idSet.forEach(id->{
            if(existsById(id)){
                S referenceById = getReferenceById(id);
                referenceById.setEntityStat(entitySate);
                references.add(referenceById);
            }
        });
        saveAll(references);
    }

    default void enable(Set<ID> idSet, boolean enabled){
        List<S> references = new ArrayList<>();
        idSet.forEach(id->{
            if(existsById(id)){
                S referenceById = getReferenceById(id);
                referenceById.setEnabled(enabled);
                references.add(referenceById);
            }
        });
        saveAll(references);
    }

    default void hide(Set<ID> idSet, boolean hide){
        List<S> references = new ArrayList<>();
        idSet.forEach(id->{
            if(existsById(id)){
                S referenceById = getReferenceById(id);
                referenceById.setEnabled(hide);
                references.add(referenceById);
            }
        });
        saveAll(references);
    }
}
