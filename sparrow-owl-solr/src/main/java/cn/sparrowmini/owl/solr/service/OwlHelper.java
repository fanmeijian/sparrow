package cn.sparrowmini.owl.solr.service;

import org.apache.jena.base.Sys;
import org.apache.jena.ontapi.model.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.SKOS;
import cn.sparrowmini.owl.solr.model.SkosRestriction;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import cn.sparrowmini.owl.solr.model.Restriction;

public class OwlHelper {
    public static Collection<String> getRangesOfProperty(OntProperty property) {
        Set<String> ranges = new HashSet<>();
        if(property.canAs(OntObjectProperty.class)) {
            OntObjectProperty ontObjectProperty = property.as(OntObjectProperty.class);
            ontObjectProperty.ranges().forEach(range -> {
                if (range instanceof OntClass.CollectionOf<?> dataRange) {
                    ranges.addAll(dataRange.components().map(OntObject::getURI).toList());
                } else {
                    ranges.add(range.getURI());
                }
            });
        }else if(property instanceof OntDataProperty ontDataProperty){
            ranges.add(ontDataProperty.getURI());
        }

        return ranges;
    }

    public static Restriction getRestrictionOfProperty(OntClass.UnaryRestriction<?> unaryRestriction) {
        OntRelationalProperty property = unaryRestriction.getProperty();

        Restriction r = null;
        if (unaryRestriction instanceof OntClass.ValueRestriction<?, ?> restriction) {
            if (property.getNameSpace().equals(SKOS.getURI()) || property.superProperties()
                    .anyMatch(superProperty -> superProperty.getNameSpace().equals(SKOS.getURI()))) {
                //处理SKOS的属性
                r = new SkosRestriction();
                r.setIsRequired(true);
                r.setOnClass(restriction.subClass().get().getURI());
                r.setOnProperty(property.getURI());
                RDFNode valueOfRestriction = restriction.getValue();

                if (valueOfRestriction instanceof OntClass.ValueRestriction<?, ?> valueRestriction) {
                    //处理嵌套restriction
                    OntRelationalProperty valueProperty = valueRestriction.getProperty();
                    if(valueProperty instanceof OntObjectProperty.Inverse inverseProp){
                        r.setValueProperty(inverseProp.getDirect().getURI());
                    }else{
                        r.setValueProperty(valueProperty.getURI());
                    }
                    r.setValue(getRestrictionValue(valueRestriction.getValue()));
                } else {
                    //属性值是类或个体
                    r.setValue(getRestrictionValue(valueOfRestriction));
                }

            } else {
                //处理非SKOS的属性
                r = new Restriction();
                r.setOnClass(restriction.subClass().get().getURI());
                r.setOnProperty(property.getURI());
                r.setIsRequired(true);

            }
        }

        if (unaryRestriction instanceof OntClass.ComponentRestriction<?, ?> restriction) {
            if (restriction instanceof OntClass.CardinalityRestriction<?, ?> cardinalityRestriction) {
                ;
                int limit = cardinalityRestriction.getCardinality();
                System.out.println("limit: " + cardinalityRestriction.getProperty().getURI() + limit);
                //处理非SKOS的属性
                r = new Restriction();
                r.setOnClass(restriction.subClass().get().getURI());
                r.setOnProperty(property.getURI());
                r.setIsRequired(limit != 0);

            }
        }


        return r;

    }

    private static Collection<String> getRestrictionValue(RDFNode valueOfRestriction) {
        if (valueOfRestriction instanceof OntClass.LogicalExpression logicalExpression) {
            if (logicalExpression instanceof OntClass.CollectionOf<?> collectionOf) {
                return collectionOf.components().map(Resource::getURI).toList();
            } else {
                return Set.of(logicalExpression.getURI());
            }
        } else {
            return Set.of(valueOfRestriction.asResource().getURI());
        }
    }

}
