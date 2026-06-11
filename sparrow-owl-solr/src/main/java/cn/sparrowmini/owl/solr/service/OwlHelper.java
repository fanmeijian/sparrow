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
import java.util.stream.Collectors;

import cn.sparrowmini.owl.solr.model.Restriction;

public class OwlHelper {
    public static Set<String> nss = Set.of("http://www.cn-plc.com/ontology/cms#", "http://www.w3.org/2004/02/skos/core#","http://www.w3.org/2001/XMLSchema#");

    public static Collection<String> getRangesOfProperty(OntProperty property) {
        Set<String> ranges = new HashSet<>();
        if (property.getURI().equals("http://www.cn-plc.com/ontology/cms#hasInternationalParty")) {
            property.ranges().forEach(a->System.out.println(a.getURI()));
        }
        OntProperty ontProperty = property;
        if (property.canAs(OntObjectProperty.class)) {
            ontProperty=property.as(OntObjectProperty.class);
        }else{
            ontProperty=property.as(OntDataProperty.class);
        }

        ontProperty.ranges()
                .filter(r -> r.isAnon() || (!r.isAnon() && nss.contains(r.getNameSpace())))
                .forEach(range -> {
                    if (range instanceof OntClass.CollectionOf<?> dataRange) {
                        ranges.addAll(dataRange.components().map(OntObject::getURI).toList());
                    } else {
                        ranges.add(range.getURI());
                    }
                });

//        if (property.canAs(OntObjectProperty.class)) {
//            OntObjectProperty ontObjectProperty = property.as(OntObjectProperty.class);
//
//            ontObjectProperty.ranges()
//                    .filter(r -> r.isAnon() || (!r.isAnon() && nss.contains(r.getNameSpace())))
//                    .forEach(range -> {
//                        if (range instanceof OntClass.CollectionOf<?> dataRange) {
//                            ranges.addAll(dataRange.components().map(OntObject::getURI).toList());
//                        } else {
//                            ranges.add(range.getURI());
//                        }
//                    });
//        } else {
//
//            ranges.add(ontDataProperty.getURI());
//        }

        return ranges;
    }

    public static Restriction getRestrictionOfProperty(OntClass.UnaryRestriction<?> unaryRestriction) {
        OntRelationalProperty property = unaryRestriction.getProperty();

        Restriction r = null;
        if (property.getURI().equals("http://www.cn-plc.com/ontology/cms#hasInternationalParty")) {
            System.out.println("");
        }
        if (unaryRestriction instanceof OntClass.ComponentRestriction<?, ?> restriction) {
            r = SkosRestriction.builder().build();
            r.setIsRequired(true);
            r.setOnProperty(property.getURI());

            OntClass definedOnClass = restriction.subClass().filter(s -> s.isLocal()).get();
            r.setOnClass(definedOnClass.getURI());

            //处理嵌套restriction
            RDFNode valueOfRestriction = restriction.getValue();

            if (valueOfRestriction instanceof OntClass.ValueRestriction<?, ?> nestedValueRestriction) {

                OntRelationalProperty valueProperty = nestedValueRestriction.getProperty();
                if (valueProperty instanceof OntObjectProperty.Inverse inverseProp) {
                    r.setValueProperty(inverseProp.getDirect().getURI());
                } else {
                    r.setValueProperty(valueProperty.getURI());
                }
                r.setValue(getRestrictionValue(nestedValueRestriction.getValue()));
            } else {
                //属性值是类或个体
                r.setValue(getRestrictionValue(valueOfRestriction));
            }

            if (restriction instanceof OntClass.CardinalityRestriction<?, ?> cardinalityRestriction) {
                int limit = cardinalityRestriction.getCardinality();
                System.out.println("limit: " + cardinalityRestriction.getProperty().getURI() + limit);
                //处理非SKOS的属性
                if (r == null) {
                    r = Restriction.builder().build();
                    r.setOnProperty(property.getURI());
                    r.setOnClass(cardinalityRestriction.subClass().get().getURI());
                    r.setId(SolrIdGenerator.generateRestrictionId(r.getOnClass(),r.getOnProperty()));
                }

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
