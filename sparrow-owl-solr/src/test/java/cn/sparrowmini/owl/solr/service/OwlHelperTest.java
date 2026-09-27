package cn.sparrowmini.owl.solr.service;

import cn.sparrowmini.owl.solr.model.Restriction;
import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntModel;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OwlHelperTest {
    private static final String NS = "http://www.cn-plc.com/ontology/cms#";

    @Test
    void getRestrictionOfPropertyIgnoresAnonymousSubclassesWithoutNamespace() {
        String ontology = """
                @prefix cms: <http://www.cn-plc.com/ontology/cms#> .
                @prefix owl: <http://www.w3.org/2002/07/owl#> .
                @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
                @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

                cms:hasCommunicationType a owl:ObjectProperty .
                cms:CommunicationType a owl:Class .
                cms:PowerStandard a owl:Class ;
                    rdfs:subClassOf [
                        a owl:Restriction ;
                        owl:onProperty cms:hasCommunicationType ;
                        owl:minQualifiedCardinality "1"^^xsd:nonNegativeInteger ;
                        owl:onClass cms:CommunicationType
                    ] .
                """;

        OntModel model = OntModelFactory.createModel(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF);
        model.read(new StringReader(ontology), null, "TTL");

        OntClass.UnaryRestriction<?> unaryRestriction = model.ontObjects(OntClass.UnaryRestriction.class)
                .filter(r -> NS.concat("hasCommunicationType").equals(r.getProperty().getURI()))
                .findFirst()
                .orElseThrow();

        Restriction restriction = assertDoesNotThrow(
                () -> OwlHelper.getRestrictionOfProperty(unaryRestriction));

        assertEquals(NS + "PowerStandard", restriction.getOnClass());
        assertTrue(restriction.getOnAllClass().contains(NS + "PowerStandard"));
    }
}
